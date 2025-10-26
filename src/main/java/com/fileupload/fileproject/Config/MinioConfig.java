package com.fileupload.fileproject.Config;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {


   @Value("${spring.minio.url}")
   private String minioUrl;

   @Value("${spring.minio.access-key}")
   private String accesskey;

   @Value("${spring.minio.secret-key}")
   private String secretkey;


    @Bean
    public AmazonS3 awsCredentials() {
        AWSCredentials credentials = new BasicAWSCredentials(accesskey, secretkey);
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(minioUrl, "us-east-1"))
                .withPathStyleAccessEnabled(true)
                .build();

        return s3Client;
    }
}
