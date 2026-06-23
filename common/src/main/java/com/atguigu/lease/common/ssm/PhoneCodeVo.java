package com.atguigu.lease.common.ssm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class PhoneCodeVo {
    private String phone  = "13888888888";
    private String code = "1234";
}
