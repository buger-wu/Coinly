package com.coinly;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.coinly.business.*.mapper")
@SpringBootApplication
public class BackendJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendJavaApplication.class, args);
    }

}
