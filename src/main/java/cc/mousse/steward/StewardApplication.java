package cc.mousse.steward;

import cc.mousse.steward.server.Gateway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;

/**
 * @author PhineasZ
 */
@SpringBootApplication
public class StewardApplication implements CommandLineRunner {

  @Resource private Gateway gateway;

  public static void main(String[] args) {
    SpringApplication.run(StewardApplication.class, args);
  }

  @Override
  public void run(String... args) {
    gateway.start();
  }
}
