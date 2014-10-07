/* 
 * $Id$
 * created by    : yukm
 * creation-date : 2014. 9. 26.
 * =========================================================
 * Copyright (c) 2014 ManinSoft, Inc. All rights reserved.
 */

package net.smartworks.ambient.manager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import net.smartworks.ambient.util.Constant;
import net.smartworks.ambient.util.PropertiesLoader;

import org.apache.log4j.Logger;

public class MailManager {

	static Logger logger = Logger.getLogger(MailManager.class);

	public void sendMail(Throwable e) {
		
		StringBuffer mailContent = new StringBuffer();
		mailContent.append(" Ambient System Error Occurred at ").append(new Date()).append("</br>");
		mailContent.append(" Check this Log Or LogFile!</br></br>");
		mailContent.append(" <hr size=1 width=50% align=\"left\"></br>");
		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		mailContent.append(sw.toString());
		sendMail(mailContent.toString());
	}
	
	public void sendMail(String mailContent) {

		Properties prop = PropertiesLoader.loadProp(Constant.PROPERTIES_PATH);

		// Assuming you are sending email from localhost
		String host = prop.getProperty("mail.smtp.host");
		// mail port
		String port = prop.getProperty("mail.smtp.port");
		// Sender's email ID needs to be mentioned
		String from = prop.getProperty("mail.manager.from");
		// Recipient's email ID needs to be mentioned.
		String to = prop.getProperty("mail.manager.to");
		
		String subject = prop.getProperty("mail.subject");

		String authId = prop.getProperty("mail.auth.id");
		String authPassword = prop.getProperty("mail.auth.password");
		
		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		properties.setProperty("mail.smtp.host", host);
		properties.setProperty("mail.smtp.port", port);
		//properties.setProperty("mail.smtp.starttls.enable", "true");
		properties.setProperty("mail.smtp.auth", "true");
		//properties.setProperty("mail.smtp.ssl.enable", "true");

		// authentication
		SMTPAuthenticator auth = new SMTPAuthenticator(authId, authPassword);
		
		// Get the default Session object.
		Session session = Session.getDefaultInstance(properties, auth);

		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			// Set To: header field of the header.
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// Set Subject: header field
			message.setSubject(subject);

			// Send the actual HTML message, as big as you like
			message.setContent(mailContent, "text/html");

			// Send message
			Transport.send(message);
			System.out.println("Sent message successfully....!");
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}
	private static class SMTPAuthenticator extends Authenticator {
		private String id;
		private String password;
		
		public SMTPAuthenticator() {
			super();
		}
		public SMTPAuthenticator(String id, String password) {
			this.id = id;
			this.password = password;
		}
		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(id, password);
		}
	}
}
