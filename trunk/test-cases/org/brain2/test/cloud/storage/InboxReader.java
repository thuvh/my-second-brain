package org.brain2.test.cloud.storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.SearchTerm;

public class InboxReader {

	public static void main(String args[]) {
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		try {
			Session session = Session.getDefaultInstance(props, null);
			Store store = session.getStore("imaps");
			store.connect("imap.gmail.com", "tantrieuf31.database2", "hellboy113");
			System.out.println(store);

			Folder inbox = store.getFolder("Inbox");
			SearchTerm term = new SearchTerm() {				
				@Override
				public boolean match(Message msg) {
					try {
						if(msg.getSubject().startsWith("[my-secound-brain]"))
						return true;
					} catch (MessagingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return false;
				}
			};;;
			
			inbox.open(Folder.READ_WRITE);
			//Message messages[] = inbox.getMessages();
			Message messages[] = inbox.search(term );
			System.out.println("messages.length: "+messages.length);
			for (Message message : messages) {
				System.out.println(message.getSubject());
				Object content = message.getContent();

				if (content instanceof Multipart) {
					Multipart multipart = (Multipart) message.getContent();

					for (int i = 0, n = multipart.getCount(); i < n; i++) {
						Part part = multipart.getBodyPart(i);
						System.out.println("   " + part.getFileName());
						System.out.println("   " + part.getSize());
						String disposition = part.getDisposition();
						System.out.println("   disposition " + disposition);
						if ("my-second-brain.zip".equals(part.getFileName()) && "ATTACHMENT".equals(disposition)) {
							try {
								File f = new File("D:/data/" + part.getFileName());
								if (!f.exists()) {
									f.createNewFile();
								}
								InputStream inputStream = part.getInputStream();
								OutputStream out = new FileOutputStream(f);
								byte buf[] = new byte[2048];
								int len;
								while ((len = inputStream.read(buf)) > 0)
									out.write(buf, 0, len);
								out.close();
								inputStream.close();
								System.out.println("\nFile is created.................");

							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							message.setFlag(Flags.Flag.DELETED, true);
						}

					}
				}
			}
			inbox.close(true);
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(2);
		}

	}
}