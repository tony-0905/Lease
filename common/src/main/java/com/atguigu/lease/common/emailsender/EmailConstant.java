package com.atguigu.lease.common.emailsender;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailConstant {

    @Autowired
    private EmailProperties emailProperties;

    public String getMyEmailCode() {
        return emailProperties.getUsername();
    }

    public String getAuthCode() {
        return emailProperties.getPassword();
    }

    public static final String EMAIL_SUBJECT = "验证码";

    public static final String EMAIL_TEMPLATE = "您的验证码为：%s，有效期5分钟，请勿泄露给他人。";
}
