package dev.hammonds.mybudget.home;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class HomeController {
  @RequestMapping("/")
  String home() {
    return "Yurrr";
  }
}
