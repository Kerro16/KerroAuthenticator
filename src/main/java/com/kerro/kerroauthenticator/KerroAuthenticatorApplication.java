package com.kerro.kerroauthenticator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KerroAuthenticatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(KerroAuthenticatorApplication.class, args);

        final Logger log =  LoggerFactory.getLogger(KerroAuthenticatorApplication.class);

        log.info("**********KerroAuthenticatorApplication starting**********");
        log.info("KerroAuthenticatorApplication started successfully");

    }

}
