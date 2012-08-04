package au.net.moon.tUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Send SSL secure mail using javax.mail
 */
public class SimpleSSLMail {

	private static String SMTP_HOST_NAME;
	private static int SMTP_HOST_PORT;
	private static String SMTP_AUTH_USER;
	private static String SMTP_AUTH_PWD;
	private static String RECIPIENT;

	Properties props;

	public SimpleSSLMail() {
		Properties getprops = new Properties();
		try {
			//load a properties file
			getprops.load(new FileInputStream("tArchiver.properties"));
			// Email account for error messages
			SMTP_HOST_NAME = getprops.getProperty("SMTP_HOST_NAME");
			SMTP_HOST_PORT = Integer.parseInt(getprops.getProperty("SMTP_HOST_PORT"));
			SMTP_AUTH_USER = getprops.getProperty("SMTP_AUTH_USER");
			SMTP_AUTH_PWD = getprops.getProperty("SMTP_AUTH_PWD");
			RECIPIENT = getprops.getProperty("RECIPIENT");

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		
		props = new Properties();
		props.put("mail.transport.protocol", "smtps");
		props.put("mail.smtps.host", SMTP_HOST_NAME);
		props.put("mail.smtps.auth", "true");
		// props.put("mail.smtps.quitwait", "false");
	}

	/**
	 * Send a mail message
	 * 
	 * @param subject
	 *            the subject line for the message
	 * @param text
	 *            the body text for the message
	 */
	public void sendMessage(String subject, String text) {
		Session mailSession = Session.getDefaultInstance(props);
		// mailSession.setDebug(true);
		try {
			Transport transport;
			transport = mailSession.getTransport();

			MimeMessage message = new MimeMessage(mailSession);
			message.setSubject(subject);
			message.setContent(text, "text/plain");

			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					RECIPIENT));

			transport.connect(SMTP_HOST_NAME, SMTP_HOST_PORT, SMTP_AUTH_USER,
					SMTP_AUTH_PWD);
			transport.sendMessage(message,
					message.getRecipients(Message.RecipientType.TO));
			transport.close();
		} catch (NoSuchProviderException e) {
			System.err
					.println("SimpleSSLMail: email transport provider not found");
			e.printStackTrace();
		} catch (MessagingException e) {
			System.err.println("SimpleSSLMail: messagingException");
			e.printStackTrace();
		}
	}
}