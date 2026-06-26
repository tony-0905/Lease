package com.atguigu.lease.common.emailsender;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Component
@Slf4j
public class emailSend {
    @Autowired
    private EmailConstant emailConstant;

    public void sendEmail(String email, String code){
        log.info("准备向邮箱 {} 发送验证码", email);

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.126.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.ssl.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailConstant.getMyEmailCode(),
                        emailConstant.getAuthCode());
            }
        });

        try{
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailConstant.getMyEmailCode()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject(EmailConstant.EMAIL_SUBJECT);
            message.setText(String.format(EmailConstant.EMAIL_TEMPLATE, code));

            Transport.send(message);
            log.info("邮件发送成功: {}", email);

        } catch (MessagingException e) {
            log.error("邮件发送失败: {}", email, e);
            e.printStackTrace();
        }
    }
}
