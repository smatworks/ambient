/* 
 * $Id$
 * created by    : yukm
 * creation-date : 2014. 9. 17.
 * =========================================================
 * Copyright (c) 2014 ManinSoft, Inc. All rights reserved.
 */

package net.smartworks.ambient.job;

import java.util.Date;
import java.util.Properties;

import net.smartworks.ambient.factory.ManagerFactory;
import net.smartworks.ambient.util.CommonUtil;
import net.smartworks.ambient.util.Constant;
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
		String exportDir1 = prop.getProperty("dir.export.1");
		String exportDir2 = prop.getProperty("dir.export.2");
		String partnerCode = prop.getProperty("code.partner");
		
		boolean isExcludeNgDataToXml = CommonUtil.toBoolean(prop.getProperty("xml.exclude.NG"));
		int sendingCnt = 0;
		int sendingCnt2 = 0;
		try {
			String sourceDir1 = prop.getProperty("dir.source.1");
			sendingCnt = ManagerFactory.getInstance().getFileManager().getFileNParseNExportXmlNSendFtp(sourceDir1, sourceDirPrefix, sourceFilePrefix, exportDir1, partnerCode, isExcludeNgDataToXml);
			logger.info("@@ Ambient Job ExportDir1 Xml File Parsing, sending Success! " + (sendingCnt) + " Files." );
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e, e);
			//ERROR MAIL SEND!!!!
			try {
				ManagerFactory.getInstance().getMailManager().sendMail(e);
			} catch (Exception e2) {
				logger.error(e, e);
			}
		}
		try {
			String sourceDir2 = prop.getProperty("dir.source.2");
			sendingCnt2 = ManagerFactory.getInstance().getFileManager().getFileNParseNExportXmlNSendFtp(sourceDir2, sourceDirPrefix, sourceFilePrefix, exportDir2, partnerCode, isExcludeNgDataToXml);
			logger.info("@@ Ambient Job ExportDir2 Xml File Parsing, sending Success! " + (sendingCnt2) + " Files." );
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e, e);
			//ERROR MAIL SEND!!!!
			try {
				ManagerFactory.getInstance().getMailManager().sendMail(e);
			} catch (Exception e2) {
				logger.error(e, e);
			}
		}
		logger.info("@@ Ambient Job - End! : " + new Date());
	}

}
