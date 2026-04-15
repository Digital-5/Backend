package com.digital5;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Collection;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BackendApplication.class);
        app.addListeners((ApplicationEnvironmentPreparedEvent event) -> {
            ConfigurableEnvironment env = event.getEnvironment();
            env.getPropertySources()
                    .stream()
                    .filter(ps -> ps instanceof MapPropertySource)
                    .map(ps -> ((MapPropertySource) ps).getSource().keySet())
                    .flatMap(Collection::stream)
                    .distinct()
                    .sorted()
                    .forEach(key -> System.out.println("key: " + key + "; property: " + env.getProperty(key)));
        });
        app.run(args);
    }

}
