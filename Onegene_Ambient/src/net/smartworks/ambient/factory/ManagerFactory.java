/* 
 * $Id$
 * created by    : yukm
 * creation-date : 2014. 9. 17.
 * =========================================================
 * Copyright (c) 2014 ManinSoft, Inc. All rights reserved.
 */

package net.smartworks.ambient.factory;

import net.smartworks.ambient.manager.FileManager;
import net.smartworks.ambient.manager.FtpManager;
import net.smartworks.ambient.manager.MailManager;

public class ManagerFactory {
	private static ManagerFactory managerFactory;
	private static FileManager fileManager;
	private static FtpManager ftpManager;
	private static MailManager mailManager;
	
	public static ManagerFactory getInstance() {
		if (managerFactory != null) {
			return managerFactory;
		} else {
			managerFactory = new ManagerFactory();
			return managerFactory;
		}
	}
	public FileManager getFileManager() throws Exception {
		if (fileManager != null) {
			return fileManager;
		} else {
			fileManager = new FileManager();
			return fileManager;
		}
	}
	public FtpManager getFtpManager() throws Exception {
		if (ftpManager != null) {
			return ftpManager;
		} else {
			ftpManager = new FtpManager();
			return ftpManager;
		}
	}
	public MailManager getMailManager() throws Exception {
		if (mailManager != null) {
			return mailManager;
		} else {
			mailManager = new MailManager();
			return mailManager;
		}
	}
}
