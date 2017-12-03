package org.datenmuehle.persistence.ftp;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.ftp.FtpFileType;
import org.apache.commons.vfs2.provider.ftps.FtpsFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.ftps.FtpsMode;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.datenmuehle.persistence.Persistor;
import org.eclipse.jetty.util.log.Log;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.Buffer;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;

public class FtpPersistor implements Persistor {

    public static Properties getProperties() {
        return properties;
    }

    public static void setProperties(Properties properties) {
        FtpPersistor.properties = properties;
    }

    static Properties properties;

    static FtpPersistor INSTANCE;

    public static FtpPersistor get() {

        if (INSTANCE == null) {
            INSTANCE = new FtpPersistor();
        }

        return INSTANCE;
    }

    private FtpPersistor() {
    }

    public void write(BufferedImage image, String fileName) {
        try {
            StandardFileSystemManager fileSystemManager = new StandardFileSystemManager();
            fileSystemManager.init();

            // get properties
            String userId = properties.getProperty("user").trim();
            String password = properties.getProperty("password").trim();
            String serverAddress = properties.getProperty("server").trim();
            String remoteDirectory = properties.getProperty("remotedir").trim();
            String localDirectory = properties.getProperty("localdir").trim();

            //Setup our SFTP configuration
            FileSystemOptions opts = getFtpsFileOptions();

            StaticUserAuthenticator auth = new StaticUserAuthenticator(serverAddress, userId, password);
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);

            // Create remote file object
            FileObject remoteFile
                    = fileSystemManager.resolveFile(getftpsUri(serverAddress, remoteDirectory, fileName), opts);

            remoteFile.createFile();
            FileContent content = remoteFile.getContent();

            // write image to server
            writeImage(content.getOutputStream(), image);

            // close file
            content.close();

            System.out.println("File upload successful");
        } catch (IOException e) {
            // ToDo: write to log
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    protected void writeImage(OutputStream os, BufferedImage image) {
        ImageOutputStream ios = null;
        try {
            ios = ImageIO.createImageOutputStream(os);

            ImageWriter writer = null;
            Iterator iter = ImageIO.getImageWritersByFormatName("jpg");
            if (iter.hasNext()) {
                writer = (ImageWriter) iter.next();
            }

            writer.setOutput(ios);

            ImageWriteParam iwparam = new JPEGImageWriteParam(Locale.getDefault());
            iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwparam.setCompressionQuality(1);

            writer.write(null, new IIOImage(image, null, null), iwparam);
            ios.flush();
            writer.dispose();
            ios.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    protected FileSystemOptions getFtpsFileOptions() {
        FileSystemOptions opts = new FileSystemOptions();
        FtpsFileSystemConfigBuilder.getInstance().setFtpsMode(opts, FtpsMode.EXPLICIT);
        FtpsFileSystemConfigBuilder.getInstance().setFileType(opts, FtpFileType.BINARY);
        FtpsFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);
        return opts;
    }

    protected String getftpsUri(String serverAddress, String remoteDir, String fileName) {
        return "ftps://" + serverAddress + "/" + remoteDir + fileName;
    }

}