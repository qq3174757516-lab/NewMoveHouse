package com.newmovehouse;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 搬家业务 Spring Boot 启动入口，扫描 MyBatis Mapper 包 {@code com.newmovehouse.mapper}。
 */
@SpringBootApplication
@MapperScan("com.newmovehouse.mapper")
public class MoveHouseApplication {

    /**
     * JVM 入口：启动内嵌 Web 容器与 Spring 上下文。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(MoveHouseApplication.class, args);
    }
}
