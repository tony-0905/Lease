package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.Minio.MinioProperties;
import com.atguigu.lease.web.app.service.FileService;
import io.minio.*;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class FileServiceimpl implements FileService {
    @Autowired
    private  MinioClient minioClient;
    @Autowired
    private MinioProperties minioProperties;


    public FileServiceimpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }


    @Override
    public String upload(MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        boolean bucket = minioClient.bucketExists(BucketExistsArgs.builder().bucket("app")
                .build());

        if(!bucket){
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket("app").build()
            );
        }

        minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket("app").config(
                createBucketPolicyConfig("app")
        ).build());

        String filename = new SimpleDateFormat("yyyyMMdd").format(new Date()) + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        minioClient.putObject(PutObjectArgs.builder().
                bucket("app")
                .stream(file.getInputStream(), file.getSize(), -1)
                .object(filename)
                .contentType(file.getContentType())
                .build());


        String url = String.join("/", minioProperties.getEndpoint(), "app", filename);




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
