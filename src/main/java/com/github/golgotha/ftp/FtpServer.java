package com.github.golgotha.ftp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Valery Kantor
 */
@SpringBootApplication
public class FtpServer {
    public static void main(String[] args) {
        SpringApplication.run(FtpServer.class, args);
    }
}
