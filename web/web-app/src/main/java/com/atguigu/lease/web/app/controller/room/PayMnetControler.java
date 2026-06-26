package com.atguigu.lease.web.app.controller.room;


import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.web.app.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("app")
public class PayMnetControler {


    @Autowired
    private PayService payService;

    @GetMapping("payment/create")
    public Result<Map<String, Object>> payMnet(Long roomId, Long mount){
        if (roomId == null) {
            return Result.fail();
        }
        if (mount == null || mount <= 0) {
            return Result.fail();
        }
        Map<String, Object> result = payService.payWaterMark(roomId, mount);

        return Result.ok(result);
    }
}


