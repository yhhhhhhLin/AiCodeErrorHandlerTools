package linyh.shop.aicodeerrorhandlertools.aop;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

@Aspect
@Component
@Data
@ConfigurationProperties(prefix = "aicode.errorhandler")
public class ErrorHandlerAOP {

    boolean isEnable = true;

    String gptGateway = null;

    String gptKey = null;

    String language = "chinese";

    String model = "gpt-3.5-turbo";

    final String groupName = "linyh.shop";
    String groupAndArticleName = "linyh.shop.aicodeerrorhandlertools";
    final String executionValue = "execution(* " + groupName + "..*.*(..))";

    @AfterThrowing(value = executionValue, throwing = "e")
    public Throwable exceptionGet(JoinPoint jp, Throwable e) {
        if (!isEnable) {
            return e;
        }
        String workPath = System.getProperty("user.dir");
        String basePath = "src" + File.separator + "main" + File.separator + "java";

        LinkedHashSet<String> methodClassChain = getClassChain(e);

        ArrayList<String> methodCodeChain = null;
        try {
            methodCodeChain = getErrorMethodChain(methodClassChain, workPath, basePath);
//            TODO 发送给gpt进行处理
            sendMsgToGPT(methodCodeChain);

        } catch (FileNotFoundException ex) {
            System.out.println("出现错误了:" + ex);
        }

        return e;

    }

    /**
     * 发送消息到GPT
     *
     * @param methodCodeChain
     */
    private void sendMsgToGPT(ArrayList<String> methodCodeChain) {
//        将list转为json
        GPTBody gptBody = new GPTBody.Builder()
                .messages(methodCodeChain)
                .model(model)
                .stream(true)
                .build();

        WebClient httpClient = WebClient.create();
        Flux<String> resp = httpClient.post()
                .uri(gptGateway + "/v1/chat/completions")
                .header("Authorization", "Bearer " + gptKey)
                .header("Content-Type", "application/json")
                .bodyValue(gptBody)
                .retrieve()
                .bodyToFlux(String.class);
        resp.subscribe(System.out::println);


    }

    /**
     * 根据错误类链，获取每个类中有关的方法代码
     *
     * @param methodClassChain
     * @param workPath
     * @param basePath
     * @return
     * @throws FileNotFoundException
     */
    private ArrayList<String> getErrorMethodChain(LinkedHashSet<String> methodClassChain, String workPath, String basePath) throws FileNotFoundException {
        ArrayList<String> methodCodeChain = new ArrayList<>();
        for (String classAndMethod : methodClassChain) {
            String[] classAndMethodArray = classAndMethod.split("::");
//            如果里面没有指定类和方法，那么直接跳过
            if (classAndMethodArray.length < 2) {
                continue;
            }
            String className = classAndMethodArray[0];
            String methodName = classAndMethodArray[1];
            String absPath = workPath + File.separator + basePath + File.separator + className.replace(".", File.separator) + ".java";
            FileInputStream fis = new FileInputStream(absPath);
            ParseResult<CompilationUnit> parseResult = new JavaParser().parse(fis);
            Optional<CompilationUnit> result = parseResult.getResult();
            CompilationUnit code = result.orElse(null);

            for (Node childNode : code.getChildNodes()) {
//                TODO 先假设没有内部类
                if (childNode instanceof ClassOrInterfaceDeclaration) {
                    for (Node node : childNode.getChildNodes()) {
//                        如果是方法node的话，就判断方法名是否和异常信息中的方法名一致，如果一致，那么就获取方法代码
                        if (node instanceof MethodDeclaration) {
                            MethodDeclaration method = (MethodDeclaration) node;
                            if (methodName.equals(method.getName().asString())) {
                                String methodMsg = classAndMethod + "\n" + method;
                                methodCodeChain.add(methodMsg);
                            }
                        }
                    }
                }
            }


        }
        return methodCodeChain;
    }

    /**
     * 根据异常信息获取方法类链
     *
     * @param e
     * @return
     */
    private LinkedHashSet<String> getClassChain(Throwable e) {
        LinkedHashSet<String> methodClassChain = new LinkedHashSet<>();
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            if (stackTraceElement.getClassName().startsWith(groupAndArticleName) && !stackTraceElement.getClassName().contains("$$")) {
                System.out.println(stackTraceElement.getClassName() + "::" + stackTraceElement.getMethodName());
                methodClassChain.add(stackTraceElement.getClassName() + "::" + stackTraceElement.getMethodName());
            }
        }
        return methodClassChain;
    }


}

class GPTBody {

    private List messages;

    private double temperature;

    private boolean stream;

    private String model;


    private GPTBody(Builder builder) {
        this.messages = builder.messages;
        this.stream = builder.stream;
        this.model = builder.model;
        this.temperature = builder.temperature;
    }

    /**
     * 利用构建者模式快速选择创建对应body信息
     */
    public static class Builder {

        //        @Autowired
//        private GPTProperties properties;
        private double temperature;

        private boolean stream;

        private String model;

        private List messages;

        public Builder stream(boolean stream) {
            this.stream = stream;
            return this;
        }

        public Builder messages(List messages) {
            this.messages = messages;
            return this;
        }

        public Builder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public GPTBody build() {
            return new GPTBody(this);
        }
    }
}


@Data
@AllArgsConstructor
@NoArgsConstructor
class GPTMessage {

    private String role;

    private String content;
}
