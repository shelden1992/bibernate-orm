package com.bobocode.bibernateorm;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BibernateOrmApplication {
    private static final Logger log = LogManager.getLogger(BibernateOrmApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BibernateOrmApplication.class, args);
    }

}
