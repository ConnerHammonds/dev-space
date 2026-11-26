package dev.hammonds.mybudget;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MyBudget {
 
  public static void main(String[] args) {
    SpringApplication.run(MyBudget.class, args);
  }

}
