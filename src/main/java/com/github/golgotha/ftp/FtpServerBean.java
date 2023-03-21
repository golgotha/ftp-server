package com.github.golgotha.ftp;

import lombok.extern.log4j.Log4j2;
import org.apache.ftpserver.FtpServer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Valery Kantor
 */
@Log4j2
public class FtpServerBean implements InitializingBean, DisposableBean {

    private final FtpServer server;

    public FtpServerBean(FtpServer server) {
        this.server = server;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.server.start();
    }

    @Override
    public void destroy() throws Exception {
        this.server.stop();
    }
}
