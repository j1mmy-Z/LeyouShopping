package com.leyou.service.Impl;

import com.leyou.service.PageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestPageService {

    @Autowired
    private PageService pageService;

    @Test
    public void testCreatHtml(){
        pageService.createHtml(141L);
    }
}
