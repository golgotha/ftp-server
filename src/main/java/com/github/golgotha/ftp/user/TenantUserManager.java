package com.github.golgotha.ftp.user;

import com.github.golgotha.ftp.FtpServerProperties;
import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.text.MessageFormat.format;

/**
 * User manager implementation based on registered tenants.
 * Tenants are stored in database.
 *
 * @author Valery Kantor
 */
@Service
public class TenantUserManager implements UserManager {

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private HomeDirectoryResolver homeDirectoryResolver;

    @Autowired
    private FtpServerProperties serverProperties;


    @Override
    public User getUserByName(String username) throws FtpException {
        String homeDirectory;
        List<Authority> authorities = new ArrayList<>();
        authorities.add(new WritePermission());

        if (!isAdmin(username)) {
            Tenant tenant = tenantRepository.findByName(username)
                    .orElseThrow(() -> new FtpException(format("User {0} not found", username)));
            homeDirectory = resolveUserHomeDirectory(tenant.getHomeDirectory());

            authorities.add(new ConcurrentLoginPermission(0, 0));
        } else {
            homeDirectory = resolveAdminHomeDirectory();
            // allow only one admin connection at the same time
            authorities.add(new ConcurrentLoginPermission(1, 0));
        }

        BaseUser user = new BaseUser();
        user.setName(username);
        user.setHomeDirectory(homeDirectory);
        user.setEnabled(true);
        user.setAuthorities(authorities);
        return user;
    }

    @Override
    public String[] getAllUserNames() throws FtpException {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public void delete(String username) throws FtpException {
        throw new UnsupportedOperationException("Delete is unsupported operation");
    }

    @Override
    public void save(User user) throws FtpException {
        throw new UnsupportedOperationException("Save is unsupported operation");
    }

    @Override
    public boolean doesExist(String username) throws FtpException {
        return tenantRepository.existsByName(username);
    }

    @Override
    public User authenticate(Authentication authentication) throws AuthenticationFailedException {
        if (authentication instanceof AnonymousAuthentication) {
            throw new AuthenticationFailedException("Anonymous users doesn't supported");
        }

        UsernamePasswordAuthentication usernamePasswordAuthentication = (UsernamePasswordAuthentication) authentication;
        var username = usernamePasswordAuthentication.getUsername();
        var password = usernamePasswordAuthentication.getPassword();

        try {
            if (isAdmin(username)) {
                verifyAdminPassword(password);
            } else {
                var tenant = tenantRepository.findByName(username)
                        .orElseThrow(() -> new AuthenticationFailedException("Invalid user credentials"));

                if (!tenant.getPassword().equals(password)) {
                    throw new AuthenticationFailedException("Invalid user credentials");
                }
            }

            return getUserByName(username);
        } catch (FtpException e) {
            throw new AuthenticationFailedException(e);
        }
    }

    @Override
    public String getAdminName() throws FtpException {
        return serverProperties.getAdmin().getUsername();
    }

    @Override
    public boolean isAdmin(String username) throws FtpException {
        return serverProperties.getAdmin().getUsername().equals(username);
    }

    public String resolveAdminHomeDirectory() {
        return homeDirectoryResolver.resolve("");
    }

    private void verifyAdminPassword(String password) throws AuthenticationFailedException {
        if (!serverProperties.getAdmin().getPassword().equals(password)) {
            throw new AuthenticationFailedException("Invalid user credentials");
        }
    }

    private String resolveUserHomeDirectory(String homeDirectory) {
        return homeDirectoryResolver.resolve(homeDirectory);
    }
}
