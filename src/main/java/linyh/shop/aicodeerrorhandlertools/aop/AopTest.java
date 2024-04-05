package linyh.shop.aicodeerrorhandlertools.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AopTest {

    @AfterThrowing(value = "execution(* linyh.shop.aicodeerrorhandlertools.*.*(..))",throwing = "e")
    public Exception exceptionGet(JoinPoint jp,Throwable e){
        System.out.println("12345");
        System.out.println("-------------------------"+e.getStackTrace());
        return null;

    }

    @Around("execution(* linyh.shop.aicodeerrorhandlertools.*.*(..))")
    public void aroundTest(JoinPoint jp){
        System.out.println("jhiduojhoisefsd");
    }





}
