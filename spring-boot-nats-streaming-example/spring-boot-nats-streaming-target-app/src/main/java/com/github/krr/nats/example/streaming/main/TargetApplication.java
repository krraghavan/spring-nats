package com.github.krr.nats.example.streaming.main;


import com.github.krr.nats.controllers.TargetSubscriber;
import com.github.krr.nats.springboot.nats.config.NatsServerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(NatsServerConfiguration.class)
@ComponentScan(basePackageClasses = TargetSubscriber.class)
public class TargetApplication {

  public static void main(String[] args) {
    SpringApplication.run(TargetApplication.class);
  }

}
