/* 
 * $Id$
 * created by    : yukm
 * creation-date : 2014. 9. 17.
 * =========================================================
 * Copyright (c) 2014 ManinSoft, Inc. All rights reserved.
 */

package net.smartworks.ambient.manager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.smartworks.ambient.factory.ManagerFactory;
import net.smartworks.ambient.util.FileUtil;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class FileManager {

	static Logger logger = Logger.getLogger(FileManager.class);
	
	public int getFileNParseNExportXmlNSendFtp(String sourceDir, String sourceDirPrefix, String sourceFilePrefix, String exportDir, String partnerCode, boolean isExcludeNgDataToXml) throws Exception {
		try {
			
			//��¥ �� �ð����� ���� �� ���ϸ��� �����Ͽ� ������ �����´�
			Date now = new Date();
			now.setTime(now.getTime() - 1800000);
			
			//�Ϸ�1�������Ǵ� ������
			SimpleDateFormat sourceFolderSdf = new SimpleDateFormat("yyyyMMdd");			
			String sourceFolderName = sourceDirPrefix + sourceFolderSdf.format(now);
			
			//�ð��� text ���ϸ�
			SimpleDateFormat sourceFileSdf = new SimpleDateFormat("yyyyMMdd_HH");
			String sourceFileName = sourceFilePrefix + sourceFileSdf.format(now) + ".txt";

			String sourcePath = sourceDir + sourceFolderName + "/" + sourceFileName;
			
			String fileContent = FileUtil.readString(sourcePath);

			if (fileContent == null) {
				logger.warn("Not Exist File : " + sourcePath);
				return 0;
			}
			
			//fileContent �� layer ������ ������ ������ ���� �� �߼�(FTP)�Ѵ�
			String[] rows = StringUtils.tokenizeToStringArray(fileContent, FileUtil.RN);	
			String layerFlag = "";
			boolean first = true;
			StringBuffer layerBuff = new StringBuffer();
			
			List<File> fileList = new ArrayList<File>();
			
			for (int i = 0; i < rows.length; i++) {
				String rowStr = rows[i].replace("[","").replace("]", "");
				String[] datas = StringUtils.tokenizeToStringArray(rowStr, ",");
				
				String dataLayerFlag = datas[8];
				if (first) {
					layerFlag = dataLayerFlag;
					first = false;
				}
				if (layerFlag.equalsIgnoreCase(dataLayerFlag)) {
					layerBuff.append(rows[i]).append(FileUtil.RN);
				} else {
					parsingContentNMakeXmlFile(fileList, partnerCode, exportDir, layerBuff.toString(), isExcludeNgDataToXml);
					layerBuff.setLength(0);
					layerBuff.append(rows[i]).append(FileUtil.RN);
				}
				layerFlag = dataLayerFlag;
			}
			parsingContentNMakeXmlFile(fileList, partnerCode, exportDir, layerBuff.toString(), isExcludeNgDataToXml);
			
			//send file to ftp
			ManagerFactory.getInstance().getFtpManager().sendXmlFilesToFtp(fileList);
			
			return fileList.size();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e, e);
			throw e;
		}
	}

	private boolean parsingContentNMakeXmlFile(List<File> fileList, String partnerCode, String exportDir, String content, boolean isExcludeNgDataToXml) throws Exception {
		
		logger.debug("####################################################");
		logger.debug(content);
		
		if (content == null || content.length() == 0)
			return false;
		
		String[] rows = StringUtils.tokenizeToStringArray(content, FileUtil.RN);		
		StringBuffer masterBuff = new StringBuffer();
		StringBuffer detailBuff = new StringBuffer();
		int detailCnt = 1;
		for (int i = 0; i < rows.length; i++) {
			String rowStr = rows[i].replace("[","").replace("]", "");
			String[] datas = StringUtils.tokenizeToStringArray(rowStr, ",");
			if (datas == null || datas.length != 9)
				continue;
			if ((masterBuff.length() == 0 && datas[6].equalsIgnoreCase("49")) || (masterBuff.length() == 0 && rows.length -1 == i && !isExcludeNgDataToXml)) {

				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				Date date = sdf.parse(datas[0]);
				SimpleDateFormat resultFormat = new SimpleDateFormat("HH:mm:ss");
				String endTestTimeStr = resultFormat.format(date);
				date.setTime(date.getTime() - 50000);
				String startTestTimeStr = resultFormat.format(date);	
				
				masterBuff.append(getMasterEle(datas, startTestTimeStr+"~"+endTestTimeStr));
			}
			String detailStr = getDetailEle(datas, isExcludeNgDataToXml, detailCnt);
			if (detailStr != null && detailStr.length() != 0) {
				detailBuff.append(detailStr);
				detailCnt+=1;
			}
		}
		
		//�����Ϳ� �������� ���ٸ� ������ �������� �ʴ´�.
		if (masterBuff.length() == 0 && detailBuff.length() == 0)
			return false;
		
		StringBuffer xmlBuff = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(FileUtil.RN);
		xmlBuff.append("<line>").append(FileUtil.RN);
		xmlBuff.append(masterBuff.toString()).append(detailBuff.toString());
		xmlBuff.append("</line>").append(FileUtil.RN);
		
		//���� ����
		//ftp�� ������ XML ���ϸ�
		Date now = new Date();
		SimpleDateFormat targetFileSdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String targetFileName = partnerCode + targetFileSdf.format(now) + ".xml";
		String targetPath = exportDir + targetFileName;
				
		FileUtil.writeString(targetPath, xmlBuff.toString(), true, "utf-8");
		if (fileList != null)
			fileList.add(new File(targetPath));
		//1�� ������
		Thread.sleep(1000);
		
		return true;
		
	}
	private String getDetailEle(String[] datas, boolean isExcludeNgDataToXml, int cnt) throws Exception {

		String dateStr = datas[0];
		String yearMonthDay = dateStr.substring(0, 8);
		
		String result = datas[6].equalsIgnoreCase("49") ? "OK" : "NG";
		if (result.equalsIgnoreCase("NG") && isExcludeNgDataToXml)
			return "";
		
		StringBuffer detailBuff = new StringBuffer(FileUtil.TAB).append("<Detail>").append(FileUtil.RN);
		detailBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<INSPECDT>").append(yearMonthDay).append("</INSPECDT>").append(FileUtil.RN);
		detailBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<LOTNO>").append(datas[1]).append("</LOTNO>").append(FileUtil.RN);
		detailBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<ITEMNO>D152ATBBA01</ITEMNO>").append(FileUtil.RN);
		detailBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<PCB_BARCODE></PCB_BARCODE>").append(FileUtil.RN);
		detailBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<STEPNO>1</STEPNO>").append(FileUtil.RN);
		detailBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<SUBJECT>AGING</SUBJECT>").append(FileUtil.RN);
		detailBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<TESTITEM>�µ�(#").append(cnt).append(")</TESTITEM>").append(FileUtil.RN);
		String testCon = datas[3];
		if (datas[3] != null && datas[3].length() == 3)
			testCon = datas[3].substring(1,3);
		detailBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<TESTCON>").append(testCon).append("��10��C").append("</TESTCON>").append(FileUtil.RN);
		String testValue = datas[4];
		if (datas[4] != null && datas[4].length() == 3)
			testValue = datas[4].substring(1,3);
		detailBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<TESTVALUE>").append(testValue).append("</TESTVALUE>").append(FileUtil.RN);
		detailBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<DECISION>").append(result).append("</DECISION>").append(FileUtil.RN);
		detailBuff.append(FileUtil.TAB).append("</Detail>").append(FileUtil.RN);
		return detailBuff.toString();
		
	}
	private String getMasterEle(String[] datas, String testFrom) throws Exception {
		
		String dateStr = datas[0];
		String yearMonthDay = dateStr.substring(0, 8);
		String prodDt = dateStr.substring(0, 8) + ":" + dateStr.substring(8,10) + ":" + dateStr.substring(10,12) + ":" + dateStr.substring(12,14);
		
		StringBuffer masterBuff = new StringBuffer().append(FileUtil.TAB).append("<Master>").append(FileUtil.RN);
		masterBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<INSPECDT>").append(yearMonthDay).append("</INSPECDT>").append(FileUtil.RN);
		masterBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<LOTNO>").append(datas[1]).append("</LOTNO>").append(FileUtil.RN);
		masterBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<ITEMNO>D152ATBBA01</ITEMNO>").append(FileUtil.RN);
		masterBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<ITEMNM>AMBIENT</ITEMNM>").append(FileUtil.RN);
		masterBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<OEMITEMNO>96985-3X000</OEMITEMNO>").append(FileUtil.RN);
		masterBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<INSPECEQ>AGING</INSPECEQ>").append(FileUtil.RN);
		masterBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<CARTYPE>ALL</CARTYPE>").append(FileUtil.RN);
		masterBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<TYPE>ALL</TYPE>").append(FileUtil.RN);
		masterBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<SPEC></SPEC>").append(FileUtil.RN);
		masterBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<LOCALE>ALL</LOCALE>").append(FileUtil.RN);
		masterBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<PRODDT>").append(prodDt).append("</PRODDT>").append(FileUtil.RN);
		masterBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<VIGO></VIGO>").append(FileUtil.RN);
		masterBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<CKD>ALL</CKD>").append(FileUtil.RN);
		masterBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<ALC_BARCODE></ALC_BARCODE>").append(FileUtil.RN);
		masterBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<PCB_BARCODE></PCB_BARCODE>").append(FileUtil.RN);
		
		String result = datas[6].equalsIgnoreCase("49") ? "OK" : "NG";
		
		masterBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<INSPECRT>").append(result).append("</INSPECRT>").append(FileUtil.RN);
		masterBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<MODEL>ALL</MODEL>").append(FileUtil.RN);
		masterBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<PROJECT>ALL</PROJECT>").append(FileUtil.RN);
		masterBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<TESTDT>").append(yearMonthDay).append("</TESTDT>").append(FileUtil.RN);
		masterBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<TESTFROM>").append(testFrom).append("</TESTFROM>").append(FileUtil.RN);
		masterBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<FFTNM></FFTNM>").append(FileUtil.RN);
		String worker = StringUtils.replace(datas[2], "#", "");
		masterBuff.append(FileUtil.TAB).append(FileUtil.TAB).append("<WORKER>").append(worker).append("</WORKER>").append(FileUtil.RN);
		masterBuff.append(FileUtil.TAB).append("</Master>").append(FileUtil.RN);
		return masterBuff.toString();
	}	
}
