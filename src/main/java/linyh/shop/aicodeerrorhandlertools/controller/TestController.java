package linyh.shop.aicodeerrorhandlertools.controller;

import linyh.shop.aicodeerrorhandlertools.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Controller
public class TestController {


    @Autowired
    private TestService testService;


    @GetMapping("/test")
    public  void test(){
        testService.test();
    }
}
