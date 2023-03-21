package com.github.golgotha.ftp.fs;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.ftpserver.ftplet.FtpFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Valery Kantor
 */
public class S3FtpFile implements FtpFile {

    private final Logger log = LoggerFactory.getLogger(S3FtpFile.class);

    private final FileObject file;

    public S3FtpFile(FileObject file) {
        this.file = file;
    }

    @Override
    public String getAbsolutePath() {
        return file.getName().getPath();
    }

    @Override
    public String getName() {
        return file.getName().getBaseName();
    }

    @Override
    public boolean isHidden() {
        boolean result = false;
        try {
            result = this.file.isHidden();
        } catch (FileSystemException e) {
            log.error("Error determining if hidden on " + this.file.getName(), e);
        }
        return result;
    }

    @Override
    public boolean isDirectory() {
        boolean result = false;

        try {
            result = this.file.getType().equals(FileType.FOLDER) ||
                    this.file.getType().equals(FileType.FILE_OR_FOLDER);
        } catch (FileSystemException e) {
            log.error("Error determining directory type on " + this.file.getName(), e);
        }
        return result;
    }

    @Override
    public boolean isFile() {
        boolean result = false;

        try {
            result = this.file.getType().equals(FileType.FILE) ||
                    this.file.getType().equals(FileType.FILE_OR_FOLDER);
        } catch (FileSystemException e) {
            log.error("Error determining file type on " + this.file.getName(), e);
        }
        return result;
    }

    @Override
    public boolean doesExist() {
        boolean result = false;
        try {
            result = this.file.exists();
        } catch (FileSystemException e) {
            log.error("Error determining existence of file " + this.file.getName(), e);
        }
        return result;
    }

    @Override
    public boolean isReadable() {
        boolean result = false;
        try {
            result = file.isReadable();
        } catch (FileSystemException e) {
            log.error("Error to determine read permission on " + this.file.getName(), e);
        }

        return result;
    }

    @Override
    public boolean isWritable() {
        boolean result = false;
        try {
            result = this.file.isWriteable();
        } catch (FileSystemException e) {
            log.error("Error determining write permission on " + this.file.getName(), e);
        }
        return result;
    }

    @Override
    public boolean isRemovable() {
        boolean result = false;
        try {
            result = this.file.isWriteable();
        } catch (FileSystemException e) {
            log.error("Error determining delete permission on " + this.file.getName(), e);
        }
        return result;
    }

    @Override
    public String getOwnerName() {
        return "owner";
    }

    @Override
    public String getGroupName() {
        return "group";
    }

    @Override
    public int getLinkCount() {
        return 0;
    }

    @Override
    public long getLastModified() {
        long result = 0;

        if (this.file.getFileSystem().hasCapability(Capability.GET_LAST_MODIFIED)) {
            try {
                result = this.file.getContent().getLastModifiedTime();
            } catch (FileSystemException e) {
                log.error("Error getting last modified of " + this.file.getName(), e);
            }
        }
        return result;
    }

    @Override
    public boolean setLastModified(long time) {
        boolean result = false;
        if (this.file.getFileSystem().hasCapability(Capability.SET_LAST_MODIFIED_FILE)) {
            try {
                this.file.getContent().setLastModifiedTime(time);
                result = true;
            } catch (FileSystemException e) {
                log.error("Could not set last modified of " + this.file.getName() + " to " + time, e);
            }
        }
        return result;
    }

    @Override
    public long getSize() {
        long result = 0;
        try {
            if (file.isFile()) {
                result = file.getContent().getSize();
            }
        } catch (FileSystemException e) {
            log.error("Error getting size of " + this.file.getName(), e);
        }
        return result;
    }

    @Override
    public FileObject getPhysicalFile() {
        return this.file;
    }

    @Override
    public boolean mkdir() {
        boolean result = false;
        try {
            this.file.createFolder();
            result = true;
        } catch (FileSystemException e) {
            log.error("Error making dir " + this.file.getName(), e);
        }
        return result;
    }

    @Override
    public boolean delete() {
        boolean result = false;
        try {
            this.file.delete();
            result = true;
        } catch (FileSystemException e) {
            log.error("Error deleting dir " + this.file.getName(), e);
        }
        return result;
    }

    @Override
    public boolean move(FtpFile destination) {
        boolean result = false;
        if (destination instanceof S3FtpFile s3dest) {
            try {
                this.file.moveTo(s3dest.getPhysicalFile());
                result = true;
            } catch (FileSystemException e) {
                log.error("Error moving " + this.file.getName() + " to " + destination.getAbsolutePath(), e);
            }
        }
        return result;
    }

    @Override
    public List<? extends FtpFile> listFiles() {

        if (log.isDebugEnabled()) {
            log.debug("Requesting list of files for {}", file);
        }

        try {
            if (file.isFile()) {
                return Collections.emptyList();
            }

            return Arrays.stream(file.getChildren())
                    .map(S3FtpFile::new)
                    .sorted(Comparator.comparing(S3FtpFile::getName))
                    .toList();
        } catch (FileSystemException e) {
            log.error("Error during getting list files of " + this.file.getName(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public OutputStream createOutputStream(long offset) throws IOException {
        //Check permission
        if (!isWritable()) {
            throw new IOException("No write permission : " + file.getName());
        }

        return file.getContent().getOutputStream();
    }

    @Override
    public InputStream createInputStream(long offset) throws IOException {
        //Check permission
        if (!isReadable()) {
            throw new IOException("No read permission : " + file.getName());
        }

        return file.getContent().getInputStream();
    }
}
