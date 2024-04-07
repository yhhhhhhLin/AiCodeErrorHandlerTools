package linyh.shop.aicodeerrorhandlertools.service.impl;

import linyh.shop.aicodeerrorhandlertools.dao.TestDao;
import linyh.shop.aicodeerrorhandlertools.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private TestDao testDao;

    @Override
    public void test() {
        testDao.testt("1");
    }
}
