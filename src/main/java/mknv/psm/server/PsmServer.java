package mknv.psm.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 *
 * @author mknv
 */
@SpringBootApplication
public class PsmServer extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(PsmServer.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PsmServer.class);
    }

}
