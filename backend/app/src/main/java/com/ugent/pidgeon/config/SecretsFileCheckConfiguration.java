package com.ugent.pidgeon.config;

import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * We check if the application-secrets.properties file exists. If it does not exist, we throw an exception.
 */
@Configuration
public class SecretsFileCheckConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(SecretsFileCheckConfiguration.class);

    @PostConstruct
    public void checkSecretsFile() {
        ClassPathResource secretsFile = new ClassPathResource("application-secrets.properties");
        boolean exists;
        try {
            exists = secretsFile.exists();
        } catch (Exception e) {
            logger.error("Error checking the application-secrets.properties file.", e);
            System.exit(1);
            return;
        }

        if (!exists) {
            logger.error("=========================================================");

            logger.error("The application-secrets.properties file does not exist.");
            logger.error("Please create the file and add the required properties.");
            logger.error("You can use the application-secrets.properties.template file as a template.");

            logger.error("=========================================================");
            System.exit(1);
        }
    }
}
