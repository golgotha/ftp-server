package com.github.golgotha.ftp.fs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;

/**
 * Amazon S3 file system factory implementation.
 * @author Valery Kantor
 */
public class S3FileSystemFactory implements FileSystemFactory {

    private final FileSystemManager fsManager;

    public S3FileSystemFactory() throws FileSystemException {
        this.fsManager = VFS.getManager();
    }

    @Override
    public FileSystemView createFileSystemView(User user) throws FtpException {
        var homeDirectory = user.getHomeDirectory();
        try(FileObject fileObject = fsManager.resolveFile(homeDirectory)) {
            if (!fileObject.exists()) {
                fileObject.createFolder();
            }
            return new S3FileSystemView(fileObject);
        } catch (FileSystemException e) {
            throw new FtpException(e);
        }
    }
}
