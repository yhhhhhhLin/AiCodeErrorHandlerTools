package linyh.shop.aicodeerrorhandlertools.dao.impl;

import linyh.shop.aicodeerrorhandlertools.dao.TestDao;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class DaoImpl implements TestDao {
    @Override
    public void testt() {
        throw new RuntimeException("测试错误");

    }

    @Override
    public void testt(String s) {
        System.out.println("这个不应该出现");
        throw new RuntimeException("测试错误2");
    }

    public void testtt2(){
        System.out.println("123");
    }

    public void test3(){
        System.out.println("234");
    }


}
