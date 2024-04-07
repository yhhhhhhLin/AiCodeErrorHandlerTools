package linyh.shop.aicodeerrorhandlertools.aop;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Optional;

@Aspect
@Component
public class AopTest {

    String groupAndArticleName = "linyh.shop.aicodeerrorhandlertools";

    @AfterThrowing(value = "execution(* linyh.shop..*.*(..))", throwing = "e")
    public Exception exceptionGet(JoinPoint jp,Throwable e) throws FileNotFoundException {

        ArrayList<String> methodChainCode = new ArrayList<>();
        System.out.println("-------------------------------");

        String workPath = System.getProperty("user.dir");
        String basePath = "src"+File.separator+"main"+File.separator+"java";
        LinkedHashSet<String> methodChain = new LinkedHashSet<>();
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
//            System.out.println(stackTraceElement.getFileName());
            if(stackTraceElement.getClassName().startsWith(groupAndArticleName) && !stackTraceElement.getClassName().contains("$$")){
                System.out.println(stackTraceElement.getClassName()+"::"+stackTraceElement.getMethodName());
                methodChain.add(stackTraceElement.getClassName()+"::"+stackTraceElement.getMethodName());
            }
        }

//        获取类中的每个方法代码
        String classPath = null;
        for (String classAndMethod :methodChain){
            String[] classAndMethodArray = classAndMethod.split("::");
            if(classAndMethodArray.length < 2){
                continue;
            }
            String className = classAndMethodArray[0];
            String methodName = classAndMethodArray[1];
            String absPath = workPath+File.separator+basePath+File.separator+className.replace(".",File.separator)+".java";
            FileInputStream fis = new FileInputStream(absPath);
            ParseResult<CompilationUnit> parseResult = new JavaParser().parse(fis);
            Optional<CompilationUnit> result = parseResult.getResult();
            CompilationUnit code = result.orElse(null);


            for (Node childNode : code.getChildNodes()) {
//                先假设没有内部类
                if(childNode instanceof ClassOrInterfaceDeclaration){
                    for (Node node : childNode.getChildNodes()) {
                        if(node instanceof MethodDeclaration){
//                            要获取相同方法名字的
                            MethodDeclaration method = (MethodDeclaration) node;
//                    获取方法名
                            if(methodName.equals(method.getName().asString())){
                                String methodMsg = classAndMethod+"\n"+method.toString();
                                methodChainCode.add(methodMsg);
                            }
                        }

                    }
//                    获取类名
                }
            }



        }
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++");
        for (String s : methodChainCode) {
            System.out.println(s);
        }

//        获取每个方法的代码

        return null;

    }



}
