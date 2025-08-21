package com.lx.lighthausbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.lx.lighthausbackend.mapper")
// 将暴露代理设置为true
@EnableAspectJAutoProxy(exposeProxy = true)
public class LightHausBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LightHausBackendApplication.class, args);
    }
}