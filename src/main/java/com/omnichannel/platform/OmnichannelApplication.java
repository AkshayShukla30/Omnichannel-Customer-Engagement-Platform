package com.omnichannel.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class OmnichannelApplication {

    public static void main(String[] args) {
        SpringApplication.run(OmnichannelApplication.class, args);
    }
}
