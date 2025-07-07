package com.shardingSphere.demo.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
@Data
@Configuration
@ConfigurationProperties("tencent.cos")
public class COSProperties {
    @NotEmpty
    private String secretId;
    @NotEmpty
    private String secretKey;
    @NotEmpty
    private String bucketName;
    @NotEmpty
    private String region;
}
