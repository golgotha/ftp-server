package com.github.golgotha.ftp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * @author Valery Kantor
 */
@ConfigurationProperties(prefix = "ftp.server")
public class FtpServerProperties {

    private String host;

    private Integer port;

    private Type type;

    private String bucketUrl;

    private String localHomeDirectory;

    private AdminCredentials admin;

    /**
     * Maximum user logins.
     */
    private int maxLogins = 100;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getBucketUrl() {
        return bucketUrl;
    }

    public void setBucketUrl(String bucketUrl) {
        this.bucketUrl = bucketUrl;
    }

    public String getLocalHomeDirectory() {
        return localHomeDirectory;
    }

    public void setLocalHomeDirectory(String localHomeDirectory) {
        this.localHomeDirectory = localHomeDirectory;
    }

    public AdminCredentials getAdmin() {
        return admin;
    }

    public void setAdmin(AdminCredentials admin) {
        this.admin = admin;
    }

    public int getMaxLogins() {
        return maxLogins;
    }

    public void setMaxLogins(int maxLogins) {
        this.maxLogins = maxLogins;
    }

    public enum Type {
        /**
         * Use S3 as file system
         **/
        S3,

        /**
         * Local machine file system
         **/
        LOCAL
    }

    public static class AdminCredentials {
        private final String username;
        private final String password;

        @ConstructorBinding
        public AdminCredentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}
