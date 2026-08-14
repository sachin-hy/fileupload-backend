package com.fileupload.fileproject.Config;


import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.io.File;
import java.io.InputStream;

@Configuration
public class ShardingConfig {
    @Bean
    public DataSource dataSource() throws Exception {
        byte[] yamlBytes;
        try (InputStream yamlStream = new ClassPathResource("sharding.yaml").getInputStream()) {
            yamlBytes = yamlStream.readAllBytes();
        }
        return YamlShardingSphereDataSourceFactory.createDataSource(yamlBytes);
    }
}