package org.brain2.test.cloud.storage;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
 
public class SendMailSSL {
	public static void main(String[] args) {
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
 
		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication("tantrieuf31.database","hellboy113");
				}
			});
 
		try {
 
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("tantrieuf31.database@gmail.com"));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("tantrieuf31.database@gmail.com"));
			message.setSubject("Testing Subject 2");
			message.setText("Dear Mail Crawler," +"\n\n No spam to my email, please!");
		    // create the second message part
		    MimeBodyPart mbp2 = new MimeBodyPart();

		    // attach the file to the message
		    mbp2.attachFile("D:/data/my-second-brain.zip");
		    Multipart mp = new MimeMultipart();		    
		    mp.addBodyPart(mbp2);

		    // add the Multipart to the message
		    message.setContent(mp);
		    
 
			Transport.send(message);
 
			System.out.println("Done");
 
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}