package com.fileupload.fileproject.Config;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsS3Config {




   @Value("${aws.s3.access-key}")
   private String accesskey;

   @Value("${aws.s3.secret-key}")
   private String secretkey;

   @Value("${aws.s3.region}")
   private String region;

//    @Value("${aws.s3.endpoint}")
//    private String endpoint;


    @Bean
    public AmazonS3 awsCredentials() {

        String cleanAccessKey = accesskey.trim();
        String cleanSecretKey = secretkey.trim();


        AWSCredentials credentials = new BasicAWSCredentials(cleanAccessKey, cleanSecretKey);

        return AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

}
