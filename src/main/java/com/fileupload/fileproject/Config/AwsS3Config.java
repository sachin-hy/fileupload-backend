package com.fileupload.fileproject.Config;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsS3Config {


//   @Value("${spring.minio.url}")
//   private String minioUrl;

   @Value("${aws.access-key}")
   private String accesskey;

   @Value("${aws.secret-key}")
   private String secretkey;

   @Value("${aws.s3.region}")
   private String region;

    @Bean
    public AmazonS3 awsCredentials() {
        // TRIM THE KEYS TO REMOVE HIDDEN SPACES
        String cleanAccessKey = accesskey.trim();
        String cleanSecretKey = secretkey.trim();

        System.out.println("Access Key Length: " + cleanAccessKey.length()); // Debugging
        System.out.println("Secret Key Length: " + cleanSecretKey.length()); // Debugging

        AWSCredentials credentials = new BasicAWSCredentials(cleanAccessKey, cleanSecretKey);

        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.AP_SOUTH_1) // Matches your CLI output
                .build();
    }

//    public AmazonS3 awsCredentials() {
//        //AWSCredentials credentials = new BasicAWSCredentials(accesskey, secretkey);
////        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
////                .withCredentials(new AWSStaticCredentialsProvider(credentials))
////                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(minioUrl, "us-east-1"))
////                .withPathStyleAccessEnabled(true)
////                .build();
////
////        return s3Client;
//        AWSCredentials credentials = new BasicAWSCredentials(accesskey, secretkey);
//        return AmazonS3ClientBuilder.standard()
//                .withCredentials(new AWSStaticCredentialsProvider(credentials))
//                // 1. CHANGE: Replace withEndpointConfiguration with withRegion
//                .withRegion(Regions.AP_SOUTH_1) // Use your actual bucket region (e.g., AP_SOUTH_1 for Mumbai)
//
//                // 2. CHANGE: Remove or set to false
//                // .withPathStyleAccessEnabled(true) -> AWS uses Virtual Hosted Style by default
//                .build();
//    }
}
