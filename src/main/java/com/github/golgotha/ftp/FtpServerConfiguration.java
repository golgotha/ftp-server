package com.github.golgotha.ftp;

import com.github.golgotha.ftp.fs.S3FileSystemFactory;
import com.github.golgotha.ftp.user.HomeDirectoryResolver;
import com.github.golgotha.ftp.user.TenantUserManager;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.config.spring.factorybeans.ConnectionConfigFactoryBean;
import org.apache.ftpserver.config.spring.factorybeans.DataConnectionConfigurationFactoryBean;
import org.apache.ftpserver.filesystem.nativefs.NativeFileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * @author Valery Kantor
 */
@Log4j2
@Configuration
@EnableConfigurationProperties(FtpServerProperties.class)
public class FtpServerConfiguration {

    @Bean
    public FtpServerBean ftpServerInstance(Listener listener,
                                           TenantUserManager userManager,
                                           FileSystemFactory fileSystemFactory,
                                           ConnectionConfigFactory connectionConfigFactory) {
        FtpServerFactory ftpServerFactory = new FtpServerFactory();
        ftpServerFactory.setConnectionConfig(connectionConfigFactory.createConnectionConfig());
        ftpServerFactory.setFtplets(Collections.singletonMap("custom", new CustomFtplet()));
        ftpServerFactory.setListeners(Collections.singletonMap("default", listener));
        ftpServerFactory.setUserManager(userManager);
        ftpServerFactory.setFileSystem(fileSystemFactory);
        return new FtpServerBean(ftpServerFactory.createServer());
    }

    @Bean
    public ConnectionConfigFactoryBean connectionConfigFactory(FtpServerProperties properties) {
        ConnectionConfigFactoryBean connectionConfigFactoryBean = new ConnectionConfigFactoryBean();
        connectionConfigFactoryBean.setMaxLogins(properties.getMaxLogins());
        return connectionConfigFactoryBean;
    }

    @Bean
    public DataConnectionConfigurationFactoryBean dataConnectionConfigurationFactory() {
        DataConnectionConfigurationFactoryBean connectionConfigurationFactoryBean = new DataConnectionConfigurationFactoryBean();
        connectionConfigurationFactoryBean.setPassivePorts("2122-2199");
        return connectionConfigurationFactoryBean;
    }

    @Bean
    public Listener listener(FtpServerProperties properties,
                             DataConnectionConfigurationFactory connectionConfigurationFactory) {
        ListenerFactory factory = new ListenerFactory();
        factory.setDataConnectionConfiguration(connectionConfigurationFactory.createDataConnectionConfiguration());
        factory.setPort(properties.getPort());
        return factory.createListener();
    }

    @Configuration
    @ConditionalOnProperty(value = "ftp.server.type", havingValue = "s3")
    public static class S3FileSystemFactoryConfiguration {

        @Bean
        public FileSystemFactory fileSystemFactory() throws FileSystemException {
            return new S3FileSystemFactory();
        }

        @Bean
        public HomeDirectoryResolver homeDirectoryResolver(FtpServerProperties properties) {
            return homeDirectory -> {
                if (homeDirectory == null || homeDirectory.isEmpty()) {
                    return properties.getBucketUrl() + "/";
                }

                return properties.getBucketUrl() + "/" + homeDirectory;
            };
        }
    }

    @Configuration
    @ConditionalOnProperty(value = "ftp.server.type", havingValue = "local")
    public static class NativeFileSystemFactoryConfiguration {

        @Bean
        public FileSystemFactory fileSystemFactory() {
            NativeFileSystemFactory fileSystemFactory = new NativeFileSystemFactory();
            fileSystemFactory.setCreateHome(true);
            return fileSystemFactory;
        }

        @Bean
        public HomeDirectoryResolver homeDirectoryResolver(FtpServerProperties properties) {
            return homeDirectory -> {
                String localHomeDirectory = properties.getLocalHomeDirectory();
                if (localHomeDirectory == null || localHomeDirectory.isEmpty()) {
                    return properties.getBucketUrl() + "/";
                }

                return properties.getBucketUrl() + "/" + localHomeDirectory;
            };
        }
    }
}
