package com.atguigu.lease.common.com.atguigu.lease.common.redis.emailsender;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "email")
public class EmailProperties {

    private String host;

    private Integer port;

    private String username;

    private String password;

    private Boolean ssl;
}
