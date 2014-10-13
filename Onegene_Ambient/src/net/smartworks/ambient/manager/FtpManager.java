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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.Stack;

import net.smartworks.ambient.util.Constant;
import net.smartworks.ambient.util.FileUtil;
import net.smartworks.ambient.util.PropertiesLoader;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;

public class FtpManager {

	static Logger logger = Logger.getLogger(FtpManager.class);
	public boolean sendXmlFilesToFtpWithRetry(List<File> files) throws Exception {

		Properties prop = PropertiesLoader.loadProp(Constant.PROPERTIES_PATH);
		int retryCount = Integer.parseInt(prop.getProperty("ftp.retry.count"));
		String errorDir = prop.getProperty("dir.error");
		String serverAddress = prop.getProperty("ftp.address");
		int serverPort = Integer.parseInt(prop.getProperty("ftp.port"));
		String userId = prop.getProperty("ftp.id");
		String userPassword = prop.getProperty("ftp.password");
		
		Queue<File> fileQueue = new LinkedList<File>();
		for (int i = 0; i < files.size(); i++) {
			fileQueue.add(files.get(i));
		}
        logger.info("@@ Ambient Job - Push To Queue : " + fileQueue.size() + " files");

		Exception finalException = null;

		FTPClient ftpClient = new FTPClient();
		InputStream inputStream = null;
		
		for (int i = 1; i <= retryCount; i++) {
			if (fileQueue.isEmpty()) {
				logger.info("@@ Ambient Job - fileStack Is Empty! Try " + i + " !");
				break;
			}
	        Thread.sleep(5000);
	        try {
	        	ftpClient.connect(serverAddress, serverPort);
	            ftpClient.login(userId, userPassword);
	           
	           // ftpClient.enterLocalPassiveMode();
	            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
	 
	            while (!fileQueue.isEmpty()) {
	            	File file = fileQueue.peek();
	            	 // APPROACH #1: uploads first file using an InputStream
	                inputStream = new FileInputStream(file);
	                logger.info("start uploading xml file : " + file.getName());
	                boolean done = ftpClient.storeFile(file.getName(), inputStream);
	                inputStream.close();
	                if (done) {
	                    logger.info("file uploading successfully.");
	                    fileQueue.poll();
	                } else {
	                    logger.error("The xml file is uploaded Fail! : "  + file.getName());
	                	throw new Exception("The xml file is uploaded Fail!(ftpClient.storeFile) : " + file.getName());
	                }
				}
			} catch (Exception e) {
				finalException = e;
		        logger.info("@@ Ambient Job - Occurred Problem So Send Xml Files To FTP Try Again! " + i + " !");
				continue;
			} finally {
	            try {
	                if (ftpClient.isConnected()) {
	                    ftpClient.logout();
	                    ftpClient.disconnect();
	                }
	            	if (inputStream != null)
	                    inputStream.close();
	            } catch (IOException ex) {
	                ex.printStackTrace();
	            }
	        }
		}
		if (!fileQueue.isEmpty()) {
			//retry 횟수를 초과해서도 fileStack에 파일이 남아 있다면 오류를 발생한다
            logger.error("@@ Ambient Job - Fail to Send File : ");
            StringBuffer remainFiles = new StringBuffer();
            while (!fileQueue.isEmpty()) {
                logger.error("Fail : " + fileQueue.peek());
                remainFiles.append("Fail : ").append(fileQueue.poll()).append(FileUtil.RN);
			}
            //전송되지 못한 파일을 기록한다
    		Date now = new Date();
    		SimpleDateFormat targetFileSdf = new SimpleDateFormat("yyyyMMddHHmmss");
    		String targetFileName = errorDir + "FAIL_" + targetFileSdf.format(now) + ".txt";
            FileUtil.writeString(targetFileName, remainFiles.toString(), true);
			throw finalException;
		}
		return true;
	}
	public boolean sendXmlFilesToFtpFromStackWithRetry(List<File> files) throws Exception {

		Properties prop = PropertiesLoader.loadProp(Constant.PROPERTIES_PATH);
		int retryCount = Integer.parseInt(prop.getProperty("ftp.retry.count"));
		String errorDir = prop.getProperty("dir.error");
		String serverAddress = prop.getProperty("ftp.address");
		int serverPort = Integer.parseInt(prop.getProperty("ftp.port"));
		String userId = prop.getProperty("ftp.id");
		String userPassword = prop.getProperty("ftp.password");
		
		Stack<File> fileStack = new Stack<File>();
		for (int i = 0; i < files.size(); i++) {
			fileStack.push(files.get(i));
		}
        logger.info("@@ Ambient Job - Push To Stack : " + fileStack.size() + " files");

		Exception finalException = null;

		FTPClient ftpClient = new FTPClient();
		InputStream inputStream = null;
		
		for (int i = 1; i <= retryCount; i++) {
			if (fileStack.empty()) {
				logger.info("@@ Ambient Job - fileStack Is Empty! Try " + i + " !");
				break;
			}
	        logger.info("@@ Ambient Job - Occurred Problem So Send Xml Files To FTP Try Again! " + i + " !");
	        Thread.sleep(5000);
	        try {
	        	ftpClient.connect(serverAddress, serverPort);
	            ftpClient.login(userId, userPassword);
	           
	           // ftpClient.enterLocalPassiveMode();
	            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
	 
	            while (!fileStack.empty()) {
	            	File file = fileStack.peek();
	            	 // APPROACH #1: uploads first file using an InputStream
	                inputStream = new FileInputStream(file);
	                logger.info("start uploading xml file : " + file.getName());
	                boolean done = ftpClient.storeFile(file.getName(), inputStream);
	                inputStream.close();
	                if (done) {
	                    logger.info("file uploading successfully.");
	                    fileStack.pop();
	                } else {
	                    logger.error("The xml file is uploaded Fail! : "  + file.getName());
	                	throw new Exception("The xml file is uploaded Fail!(ftpClient.storeFile) : " + file.getName());
	                }
				}
			} catch (Exception e) {
				finalException = e;
				continue;
			} finally {
	            try {
	                if (ftpClient.isConnected()) {
	                    ftpClient.logout();
	                    ftpClient.disconnect();
	                }
	            	if (inputStream != null)
	                    inputStream.close();
	            } catch (IOException ex) {
	                ex.printStackTrace();
	            }
	        }
		}
		if (!fileStack.empty()) {
			//retry 횟수를 초과해서도 fileStack에 파일이 남아 있다면 오류를 발생한다
            logger.error("@@ Ambient Job - Fail to Send File : ");
            StringBuffer remainFiles = new StringBuffer();
            while (!fileStack.empty()) {
                logger.error("Fail : " + fileStack.peek());
                remainFiles.append("Fail : ").append(fileStack.pop()).append(FileUtil.RN);
			}
            //전송되지 못한 파일을 기록한다
    		Date now = new Date();
    		SimpleDateFormat targetFileSdf = new SimpleDateFormat("yyyyMMddHHmmss");
    		String targetFileName = errorDir + "FAIL_" + targetFileSdf.format(now) + ".txt";
            FileUtil.writeString(targetFileName, remainFiles.toString(), true);
			throw finalException;
		}
		return true;
	}
	
	public boolean sendXmlFilesToFtp(List<File> files) throws Exception {

		Properties prop = PropertiesLoader.loadProp(Constant.PROPERTIES_PATH);
		String serverAddress = prop.getProperty("ftp.address");
		int serverPort = Integer.parseInt(prop.getProperty("ftp.port"));
		String userId = prop.getProperty("ftp.id");
		String userPassword = prop.getProperty("ftp.password");
		
		FTPClient ftpClient = new FTPClient();
		InputStream inputStream = null;
        try {
 
            ftpClient.connect(serverAddress, serverPort);
            ftpClient.login(userId, userPassword);
           
           // ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
 
            for (int i = 0; i < files.size(); i++) {
            	File file = files.get(i);
            	 // APPROACH #1: uploads first file using an InputStream
                inputStream = new FileInputStream(file);
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
            	if (inputStream != null)
                    inputStream.close();
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
