package com.newmovehouse;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.newmovehouse.mapper")
public class MoveHouseApplication {
    public static void main(String[] args) {
        SpringApplication.run(MoveHouseApplication.class, args);
    }
}

