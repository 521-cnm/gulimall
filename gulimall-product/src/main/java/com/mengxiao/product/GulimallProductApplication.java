package com.mengxiao.product;

import org.mybatis.spring.annotation.MapperScan;
import org.redisson.spring.session.config.EnableRedissonHttpSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 1.整合mybatis-plus
 * 1.导入依赖
 * 2.配置数据源
 * 3.配置mybatis-plus
 * 1.使用@MapperScan
 * 2.告诉mybatis-plus xml文件位置
 * 2.逻辑删除
 * 1.配置全局的逻辑删除规则
 * 2.配置逻辑删除的组件bean
 * 3.给bean加上逻辑删除注解@TableLogic
 * 3.JSR303
 * 1.给bean添加校验注解，并定义自己的message提示
 * 2.开启校验功能@Valid
 * 3.给校验的bean后紧跟一个bindingresult，就可以获取到校验结果
 * 4.分组校验
 * 1.@NotBlank(message,group) 给校验注解标注什么情况需要进行校验
 * 2.@Validated()
 * 3.默认没有指定分组的校验注解@NotBlank，在分组校验情况下不生效
 * 5.自定义校验
 * 1.编写一个自定义的校验注解
 * 2.编写一个自定义的校验器
 * 3.关联自定义的校验器的校验注解
 */
@EnableRedissonHttpSession
@EnableFeignClients(basePackages = "com.mengxiao.product.feign")
@EnableDiscoveryClient
@MapperScan("com.mengxiao.product.dao")
@SpringBootApplication
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
