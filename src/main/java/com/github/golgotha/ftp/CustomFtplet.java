package com.github.golgotha.ftp;

import com.github.golgotha.ftp.fs.S3FileSystemView;
import org.apache.ftpserver.ftplet.DefaultFtplet;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.FtpletResult;

import java.io.IOException;

/**
 * Ftplet responsible for closing using resource as soon as a file uploaded/downloaded.
 * The main goal of the class to prevent leak of disk space because temporary files removes after main process finish.
 * @author Valery Kantor
 */
public class CustomFtplet extends DefaultFtplet {

    @Override
    public FtpletResult onUploadEnd(FtpSession session, FtpRequest request) throws IOException {
        var workingFile = ((S3FileSystemView)session.getFileSystemView()).getCurrentWorkingFile();
        if (workingFile != null) {
            workingFile.close();
        }
        return FtpletResult.DEFAULT;
    }

    @Override
    public FtpletResult onDownloadEnd(FtpSession session, FtpRequest request) throws IOException {
        var workingFile = ((S3FileSystemView)session.getFileSystemView()).getCurrentWorkingFile();
        if (workingFile != null) {
            workingFile.close();
        }

        return FtpletResult.DEFAULT;
    }
}
