/* 
 * $Id$
 * created by    : yukm
 * creation-date : 2014. 3. 17.
 * =========================================================
 * Copyright (c) 2014 ManinSoft, Inc. All rights reserved.
 */

package net.smartworks.ambient.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class PropertiesLoader {

	private static Properties prop;
	
	// c:/tes/test.properties
	public static Properties loadPropByPhysicalPath(String propFileName) {
		prop = new Properties();
		try {
			prop.load(new FileInputStream(propFileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop;
	}
	// /net/smartworks/config.properties
	public static Properties loadPropByClassPath(String propFileName) {
		prop = new Properties();
		try {
			InputStream in = PropertiesLoader.class.getResourceAsStream(propFileName);
			prop.load(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prop;
	}
	// jar파일과 같은 위치
	public static Properties loadProp(String propFileName){
		prop = new Properties();
		try {
			URL url = new URL(PropertiesLoader.class.getProtectionDomain().getCodeSource().getLocation(), propFileName);
			prop.load(url.openStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prop;
	}
}
