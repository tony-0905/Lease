package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.ssm.PhoneCodeVo;
import com.atguigu.lease.common.ssm.sendmessege;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SmsServiceImplTest {

    @Test
    public void sendCode() {

        System.out.println(sendmessege.sendCode("13888888888", "1234"));
    }

    @Test
    public void testSendCode() {


    }

}