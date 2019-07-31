package com.rabbitmq.controller;

import com.rabbitmq.service.CallBackSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test  class
 *
 * @author : yuxiang
 * @date : 2019-07-31 16:39
 **/
@RestController
public class Test {
    @Autowired
    private CallBackSender callBackSender;

    @RequestMapping("/test1")
    public void send1(){
        callBackSender.send();
    }

    @RequestMapping("/test2")
    public void send2(){
        callBackSender.direct();
    }
}
