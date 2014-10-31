/* 
 * $Id$
 * created by    : yukm
 * creation-date : 2014. 9. 17.
 * =========================================================
 * Copyright (c) 2014 ManinSoft, Inc. All rights reserved.
 */

package net.smartworks.ambient.job;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import net.smartworks.ambient.factory.ManagerFactory;
import net.smartworks.ambient.util.CommonUtil;
import net.smartworks.ambient.util.Constant;
import net.smartworks.ambient.util.FileUtil;
import net.smartworks.ambient.util.PropertiesLoader;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AmbientJob implements Job{

	static Logger logger = Logger.getLogger(AmbientJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		logger.info("@@ Ambient Job - Start! : " + new Date());
		
		Properties prop = PropertiesLoader.loadProp(Constant.PROPERTIES_PATH);
		
		String sourceDirPrefix = prop.getProperty("dir.source.prefix");
		String sourceFilePrefix = prop.getProperty("file.source.prefix");
		String exportDir = prop.getProperty("dir.export");
		String partnerCode = prop.getProperty("code.partner");
		
		boolean isExcludeNgDataToXml = CommonUtil.toBoolean(prop.getProperty("xml.exclude.NG"));
		try {
			
			StringBuffer xmlContentBuffer = new StringBuffer();
			
			String sourceDir1 = prop.getProperty("dir.source.1");
			String flagDir1 = prop.getProperty("dir.flag.1");
			
			ManagerFactory.getInstance().getFileManager().getFileNParseNAppendXmlBuffer(xmlContentBuffer, flagDir1, sourceDir1, sourceDirPrefix, sourceFilePrefix, partnerCode, isExcludeNgDataToXml);

			String sourceDir2 = prop.getProperty("dir.source.2");
			String flagDir2 = prop.getProperty("dir.flag.2");
			
			ManagerFactory.getInstance().getFileManager().getFileNParseNAppendXmlBuffer(xmlContentBuffer, flagDir2, sourceDir2, sourceDirPrefix, sourceFilePrefix, partnerCode, isExcludeNgDataToXml);
			
			StringBuffer xmlBuff = null;
			if (xmlContentBuffer.length() != 0) {
				xmlBuff = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(FileUtil.RN);
				xmlBuff.append("<line>").append(FileUtil.RN);
				xmlBuff.append(xmlContentBuffer.toString());
				xmlBuff.append("</line>").append(FileUtil.RN);
				
				//파일 생성
				//ftp로 보내질 XML 파일명
				Date now = new Date();
				SimpleDateFormat targetFileSdf = new SimpleDateFormat("yyyyMMddHHmmss");
				String targetFileName = partnerCode + targetFileSdf.format(now) + ".xml";
				String targetPath = exportDir + targetFileName;
						
				FileUtil.writeString(targetPath, xmlBuff.toString(), true, "utf-8");
				logger.info("Make Xml File Complete!! (" + targetPath + ")");
				
				ManagerFactory.getInstance().getFtpManager().sendXmlFileToFtp(targetPath);
			} else {
				//파일컨텐츠없음
				logger.warn("Not Exist Append Xml Content!!!!!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e, e);
			//ERROR MAIL SEND!!!!
			try {
				ManagerFactory.getInstance().getMailManager().sendMail(e);
			} catch (Exception e2) {
				logger.error(e, e);
			}
		} finally {
			logger.info("@@ Ambient Job - End! : " + new Date());
		}
	}
}
