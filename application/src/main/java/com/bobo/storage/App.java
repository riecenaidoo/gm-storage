package com.bobo.storage;

import com.bobo.storage.core.CoreContext;
import com.bobo.storage.web.WebContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The entrypoint to the application.
 *
 * <p>I prefer placing it in the root of our package structure, because I feel it makes most sense
 * there. Placing it there is also helpful as we can use it as the target for component scanning.
 *
 * @see SpringBootApplication#scanBasePackageClasses()
 * @see ComponentScan#basePackageClasses()
 */
@SpringBootApplication
@CoreContext
@WebContext
@EnableScheduling
public class App {

  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }
}
