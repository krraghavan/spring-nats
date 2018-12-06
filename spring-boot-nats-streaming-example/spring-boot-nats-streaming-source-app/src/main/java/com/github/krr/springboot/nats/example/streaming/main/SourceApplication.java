package com.github.krr.springboot.nats.example.streaming.main;


import com.github.krr.springboot.nats.config.NatsServerConfiguration;
import com.github.krr.springboot.nats.rest.controllers.SourceRestController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(NatsServerConfiguration.class)
@ComponentScan(basePackageClasses = SourceRestController.class)
public class SourceApplication {

  public static void main(String[] args) {
    SpringApplication.run(SourceApplication.class);
  }

}
