package dev.hammonds;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@SpringBootApplication
@ConfigurationPropertiesScan
public class MyBudget {
 
  @RequestMapping("/")
  String home() {
    return "Yurrr";
  }
 
  public static void main(String[] args) {
    SpringApplication.run(MyBudget.class, args);
  }

}
