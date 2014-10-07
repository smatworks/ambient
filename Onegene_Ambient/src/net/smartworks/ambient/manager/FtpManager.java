/* 
 * $Id$
 * created by    : yukm
 * creation-date : 2014. 9. 17.
 * =========================================================
 * Copyright (c) 2014 ManinSoft, Inc. All rights reserved.
 */

package net.smartworks.ambient.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import net.smartworks.ambient.util.Constant;
import net.smartworks.ambient.util.PropertiesLoader;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;

public class FtpManager {

	static Logger logger = Logger.getLogger(FtpManager.class);
	public boolean sendXmlFilesToFtp(List<File> files) throws Exception {

		Properties prop = PropertiesLoader.loadProp(Constant.PROPERTIES_PATH);
		String serverAddress = prop.getProperty("ftp.address");
		int serverPort = Integer.parseInt(prop.getProperty("ftp.port"));
		String userId = prop.getProperty("ftp.id");
		String userPassword = prop.getProperty("ftp.password");
		
		FTPClient ftpClient = new FTPClient();
        try {
 
            ftpClient.connect(serverAddress, serverPort);
            ftpClient.login(userId, userPassword);
           
           // ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
 
            for (int i = 0; i < files.size(); i++) {
            	File file = files.get(i);
            	 // APPROACH #1: uploads first file using an InputStream
                InputStream inputStream = new FileInputStream(file);
                logger.info("@@ Ambient Job - Start uploading xml file : " + file.getAbsolutePath());
                boolean done = ftpClient.storeFile(file.getName(), inputStream);
                inputStream.close();
                if (done) {
                    logger.info("@@ Ambient Job - The xml file is uploaded successfully.");
                }
			}

        } catch (IOException ex) {
            logger.error(ex, ex);
            ex.printStackTrace();
            throw ex;
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
		return true;
	}
	public boolean sendXmlFileToFtp(String filePath) throws Exception {

		Properties prop = PropertiesLoader.loadProp(Constant.PROPERTIES_PATH);
		String serverAddress = prop.getProperty("ftp.address");
		int serverPort = Integer.parseInt(prop.getProperty("ftp.port"));
		String userId = prop.getProperty("ftp.id");
		String userPassword = prop.getProperty("ftp.password");
		
		FTPClient ftpClient = new FTPClient();
        try {
 
            ftpClient.connect(serverAddress, serverPort);
            ftpClient.login(userId, userPassword);
           
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
 
            // APPROACH #1: uploads first file using an InputStream
            File xmlFile = new File(filePath);
            InputStream inputStream = new FileInputStream(xmlFile);
 
            logger.debug("@@ Ambient Job - Start uploading xml file : " + filePath);
            
            boolean done = ftpClient.storeFile(xmlFile.getName(), inputStream);
            inputStream.close();
            if (done) {
                logger.debug("@@ Ambient Job - The xml file is uploaded successfully.");
            }

        } catch (IOException ex) {
            logger.error(ex, ex);
            ex.printStackTrace();
            throw ex;
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
		return true;
	}
}
