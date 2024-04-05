package linyh.shop.aicodeerrorhandlertools.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {


    @GetMapping("/test")
    public  void test(){
        System.out.println("123");
    }
}
