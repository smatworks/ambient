/* 
 * $Id$
 * created by    : yukm
 * creation-date : 2014. 9. 30.
 * =========================================================
 * Copyright (c) 2014 ManinSoft, Inc. All rights reserved.
 */

package net.smartworks.ambient.util;

public class CommonUtil {
	public static boolean toBoolean(Object bool) {
		if (bool == null)
			return false;
		if (bool instanceof Boolean) {
			boolean b = ((Boolean) bool).booleanValue();
			return b;
		}
		bool = bool.toString();
		if (bool.equals("true") || bool.equals("y") || 
				bool.equals("on") || bool.equals("1"))
			return true;
		return false;
	}
}
