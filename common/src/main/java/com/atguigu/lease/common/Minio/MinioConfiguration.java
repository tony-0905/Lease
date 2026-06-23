package com.atguigu.lease.common.Minio;


import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//@ConfigurationPropertiesScan("com.atguigu.lease.common.Minio")//注册MinioProperties类,方法二选一

@Configuration
@ConditionalOnProperty(name = "minio.endpoint")
@EnableConfigurationProperties(MinioProperties.class)//注册MinioProperties类
public class MinioConfiguration {

    @Autowired
    private MinioProperties minioProperties;

    @Bean
    public MinioClient client(){
        return  MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();

    }

}
