package com.rei.algo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.rei.algo.mapper")
@OpenAPIDefinition(info = @Info(title = "ReiAlgo API", version = "1.0", description = "API documentation for ReiAlgo"))
public class ReiAlgoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReiAlgoApplication.class, args);
	} 
 
}
