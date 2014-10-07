package net.smartworks.ambient.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channel;

import org.apache.log4j.Logger;

public class FileUtil {
	private static Logger logger = Logger.getLogger(FileUtil.class);
	public static final String RN = "\r\n";
	public static final String TAB = "	";
	public static final String ENCODING_UTF8 = "UTF-8";
	public static final String ENCODING_EUCKR = "EUC-KR";

	
	protected static void createFile(String path, boolean overwrite) throws Exception {
		File file = new File(path);
		createFile(file, overwrite);
	}
	protected static void createFile(File file, boolean overwrite) throws Exception {
		File dir = file.getParentFile();
		if(dir != null && !dir.exists())
			dir.mkdirs();
		if (file.exists()) {
			if (!overwrite)
				throw new Exception("The file is already exist path:" + file.getAbsolutePath());
		} else {
			file.createNewFile();
		}
	}
	public static Object read(String path) throws Exception {
		InputStream is = null;
		ObjectInputStream ois = null;
		try {
			File file = new File(path);
			if(!file.exists())
				return null;
			is = new FileInputStream(file);
			ois = new ObjectInputStream(is);
			Object obj = ois.readObject();
			ois.close();
			return obj;
			
		} catch(Exception e) {
			throw e;
		} finally {
			close(ois);
			close(is);
		}
	}
	public static byte[] readBytes(String path) throws Exception {
		InputStream is = null;
		try {
			File file = new File(path);
			if(!file.exists())
				return null;
			
			long length = file.length();
		    
	        // Create the byte array to hold the data
	        byte[] bytes = new byte[(int)length];
	    
	        is = new FileInputStream(file);
	        // Read in the bytes
	        int offset = 0;
	        int numRead = 0;
	        while (offset < bytes.length
	               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	            offset += numRead;
	        }
	    
	        // Ensure all the bytes have been read in
	        if (offset < bytes.length) {
	            throw new IOException("Could not completely read file "+file.getName());
	        }
	    
	        // Close the input stream and return bytes
	        is.close();
	        return bytes;			
		} catch(Exception e) {
			throw e;
		} finally {
			close(is);
		}
	}
	public static File write(String path, byte[] bt, boolean overwrite) throws Exception {
		OutputStream os = null;
		try {
			File file = new File(path);
			createFile(file, overwrite);
			
			os = new FileOutputStream(file);
			os.write(bt);
			os.flush();
			
			return file;
		
		} catch(Exception e) {
			throw e;
		} finally {
			close(os);
		}
	}
	public static File write(String path, Object obj, boolean overwrite)throws Exception {
		ObjectOutputStream oos = null;
		OutputStream os = null;
		try {
			File file = new File(path);
			createFile(file, overwrite);
			
			os = new FileOutputStream(file);
			oos = new ObjectOutputStream(os);
			oos.writeObject(obj);
			oos.flush();
			os.flush();
			return file;
		} catch(Exception e) {
			throw e;
		} finally {
			close(os);
			close(oos);
		}
	}
	public static String readString(File file) throws Exception {
		return readString(file, null);
	}
	public static String readString(File file, String encoding) throws Exception {
		if (encoding == null)
			encoding = ENCODING_UTF8;
		InputStreamReader fr = null;
		BufferedReader br = null;
		try {
			StringBuffer buf = new StringBuffer();
			fr = new InputStreamReader(new FileInputStream(file), encoding);
			br = new BufferedReader(fr);
			long line = 0;
			String str;
			while((str = br.readLine()) != null) {
				if (line++ != 0)
					buf.append(RN);
				buf.append(str);
			}
			return buf.toString();
		} catch (FileNotFoundException e) {
			if (logger.isInfoEnabled())
				logger.info("No such file: " + file.getAbsolutePath());
			return null;
		} catch (Exception e) {
			throw e;
		} finally {
			close(br);
			close(fr);
		}
	}
	public static String readString(String path) throws Exception {
		return readString(path, null);
	}
	public static String readString(String path, String encoding) throws Exception {
		if (encoding == null)
			encoding = ENCODING_UTF8;
		InputStreamReader fr = null;
		BufferedReader br = null;
		try {
			StringBuffer buf = new StringBuffer();
			fr = new InputStreamReader(new FileInputStream(path), encoding);
			br = new BufferedReader(fr);
			long line = 0;
			String str;
			while((str = br.readLine()) != null) {
				if (line++ != 0)
					buf.append(RN);
				buf.append(str);
			}
			return buf.toString();
		} catch (FileNotFoundException e) {
			File file = new File(path);
			if (logger.isInfoEnabled())
				logger.info("No such file: " + file.getAbsolutePath());
			return null;
		} catch (Exception e) {
			throw e;
		} finally {
			close(br);
			close(fr);
		}
	}
	public static void writeString(String path, String content, boolean overwrite) throws Exception {
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			createFile(path, overwrite);
			fw = new FileWriter(path, false);
			bw = new BufferedWriter(fw);
			bw.write(content);
			bw.flush();
			fw.flush();
		} catch (Exception e) {
			throw e;
		} finally {
			close(bw);
			close(fw);
		}
	}
	public static void writeString(String path, String content, boolean overwrite, String encoding) throws Exception {
		createFile(path, overwrite);
		OutputStreamWriter osw = null;
		try {
		    osw = new OutputStreamWriter(new FileOutputStream(path), encoding);
		    osw.write(content);
		} catch(IOException ioe) {
		    ioe.printStackTrace();
		} finally {
		    if(osw != null) try { osw.close(); } catch(Exception e) {}
		}
	}
	public static void delete(String path) throws Exception {
		File file = new File(path);
		delete(file);
	}
	public static void delete(File file) throws Exception {
		if (!file.exists())
			return;
		boolean success = file.delete();
		if (!success)
			throw new Exception("Delete failed path:" + file.getAbsolutePath());
	}
	public static void deleteOnExit(String path) throws Exception {
		File file = new File(path);
		deleteOnExit(file);
	}
	public static void deleteOnExit(File file) throws Exception {
		if (!file.exists())
			return;
		file.deleteOnExit();
	}
	public static void deleteAll(String path) throws Exception {
		File file = new File(path);
		deleteAll(file);
	}
	public static void deleteAll(File file) throws Exception {
		if (!file.exists())
			return;
		if(file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				int fileLength = files.length;
				for(int i=0; i<fileLength; i++) {
					File childFile = files[i];
					if(childFile.isDirectory()) {
						deleteAll(childFile);
					} else {
						childFile.delete();
					}
				}
			}
		}
		boolean success = file.delete();
		if (!success)
			throw new Exception("Delete failed path:" + file.getAbsolutePath());
	}
	public static void close(InputStream obj) {
		if (obj == null)
			return;
		try {
			obj.close();
		} catch (Exception e) {
			logger.warn(e, e);
		}
	}
	public static void close(OutputStream obj) {
		if (obj == null)
			return;
		try {
			obj.close();
		} catch (Exception e) {
			logger.warn(e, e);
		}
	}
	public static void close(Channel obj) {
		if (obj == null)
			return;
		try {
			obj.close();
		} catch (Exception e) {
			logger.warn(e, e);
		}
	}
	public static void close(Reader obj) {
		if (obj == null)
			return;
		try {
			obj.close();
		} catch (Exception e) {
			logger.warn(e, e);
		}
	}
	public static void close(Writer obj) {
		if (obj == null)
			return;
		try {
			obj.close();
		} catch (Exception e) {
			logger.warn(e, e);
		}
	}
}
