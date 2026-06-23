package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.Minio.MinioProperties;
import com.atguigu.lease.web.admin.service.FileService;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import io.minio.*;
import io.minio.errors.*;
import jakarta.xml.bind.annotation.XmlElementDecl;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PropertiesLoaderSupport;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {


    @Autowired
    private MinioProperties minioProperties;
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private PropertiesLoaderSupport propertiesLoaderSupport;

    @Override
    public String upload(MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        boolean bucket = minioClient.bucketExists(BucketExistsArgs.builder().bucket(
                minioProperties.getBucketName()
        ).build());

        String url = "";


        if (!bucket) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.
                    getBucketName()).build()
            );
        }

        minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(minioProperties.getBucketName()).config(

                createBucketPolicyConfig(minioProperties.getBucketName())).build());

        String filename = new SimpleDateFormat("yyyyMMdd").format(new Date()) + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        minioClient.putObject(PutObjectArgs.builder().
                bucket(minioProperties.getBucketName())
                .stream(file.getInputStream(), file.getSize(), -1)
                .object(filename)
                .contentType(file.getContentType())
                .build());
//                String url = minioProperties.getEndpoint()+"/" + minioProperties.getBucketName() + "/" + filename;
        url = String.join("/", minioProperties.getEndpoint(), minioProperties.getBucketName(), filename);



        return url;
    }

    private String createBucketPolicyConfig(String bucketName) {

        return """
                {
                  "Statement" : [ {
                    "Action" : "s3:GetObject",
                    "Effect" : "Allow",
                    "Principal" : "*",
                    "Resource" : "arn:aws:s3:::%s/*"
                  } ],
                  "Version" : "2012-10-17"
                }
                """.formatted(bucketName);
    }
}
