package com.shardingSphere.demo.config;

import cn.muzisheng.pear.config.CacheConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfiguration {
    @Bean
    public CacheConfig cacheConfig(){
        return new CacheConfig.Builder().capacity(1000).cacheName("cache").build();
    }
}
