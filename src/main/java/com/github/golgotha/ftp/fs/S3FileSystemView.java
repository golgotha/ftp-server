package com.github.golgotha.ftp.fs;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;

/**
 * @author Valery Kantor
 */
public class S3FileSystemView implements FileSystemView {

    private S3FtpFile workingDir;
    private S3FtpFile homeDir;
    private FileObject currentWorkingFile;

    public S3FileSystemView(FileObject fileObject) {
        this.homeDir = new S3FtpFile(fileObject);
        this.workingDir = new S3FtpFile(fileObject);
    }

    @Override
    public FtpFile getHomeDirectory() throws FtpException {
        return homeDir;
    }

    @Override
    public FtpFile getWorkingDirectory() throws FtpException {
        return workingDir;
    }

    @Override
    public boolean changeWorkingDirectory(String dir) throws FtpException {
        try {
            FileObject changedDir = this.workingDir.getPhysicalFile().resolveFile(dir);
            if (changedDir.getType().hasChildren()) {
                String changeDirUrl = changedDir.getURL().toString();
                String homeDirUrl = homeDir.getPhysicalFile().getURL().toString();
                if (changeDirUrl.startsWith(homeDirUrl)) {
                    this.workingDir = new S3FtpFile(changedDir);
                    return true;
                } else {
                    throw new AuthenticationFailedException("Access is restricted to directory " + dir);
                }
            }
        } catch (FileSystemException e) {
            throw new FtpException("Could not change to directory " + dir, e);
        }

        return false;
    }

    @Override
    public FtpFile getFile(String file) throws FtpException {
        try {
            FileObject fileObject = this.workingDir.getPhysicalFile().resolveFile(file);
            currentWorkingFile = fileObject;

            String fileUrl = fileObject.getURL().toString();
            String homeDirUrl = homeDir.getPhysicalFile().getURL().toString();
            if (fileUrl.startsWith(homeDirUrl)) {
                return new S3FtpFile(fileObject);
            }
            throw new AuthenticationFailedException("Access is restricted to file " + file);
        } catch (FileSystemException e) {
            throw new FtpException("Could not get file " + file, e);
        }
    }

    @Override
    public boolean isRandomAccessible() throws FtpException {
        FileSystem fs = this.homeDir.getPhysicalFile().getFileSystem();
        boolean randomAccessible = fs.hasCapability(Capability.RANDOM_ACCESS_READ);
        if (randomAccessible && fs.hasCapability(Capability.WRITE_CONTENT)) {
            randomAccessible = fs.hasCapability(Capability.RANDOM_ACCESS_WRITE);
        }
        return randomAccessible;
    }

    @Override
    public void dispose() {
    }

    public FileObject getCurrentWorkingFile() {
        return currentWorkingFile;
    }
}
