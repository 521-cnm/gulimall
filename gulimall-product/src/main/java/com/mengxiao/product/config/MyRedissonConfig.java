package com.mengxiao.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyRedissonConfig {
    /**
     * 所有对Redisson的使用都是通过RedissonClient对象
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() throws Exception {
        //创建配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.182.128:6379");
        //根据Config创建出RedisonClient实例
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
