package org.brain2.test.parser.dynamic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MyClassLoader extends ClassLoader {

	public MyClassLoader(ClassLoader parent) {
		super(parent);
	}

	public Class loadClass(String name) throws ClassNotFoundException {
		if (!"reflection.MyObject".equals(name))
			return super.loadClass(name);

		try {
			String url = "file:D:/data/MyParser.class";
			URL myUrl = new URL(url);
			URLConnection connection = myUrl.openConnection();
			InputStream input = connection.getInputStream();
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int data = input.read();

			while (data != -1) {
				buffer.write(data);
				data = input.read();
			}

			input.close();

			byte[] classData = buffer.toByteArray();

			return defineClass("org.brain2.test.parser.dynamic.MyParser", classData, 0,
					classData.length);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void main(String[] args) throws Exception {

		ClassLoader parentClassLoader = MyClassLoader.class.getClassLoader();
		MyClassLoader classLoader = new MyClassLoader(parentClassLoader);
		Class myObjectClass = classLoader.loadClass("org.brain2.test.parser.dynamic.MyParser");
		Object obj = myObjectClass.newInstance();
		Method doParsingMethod = myObjectClass.getDeclaredMethod("doParsing", new Class[] { String.class });
		Object result = doParsingMethod.invoke(obj, "http://vnexpress.net/gl/vi-tinh/giai-tri/2011/12/10-clip-quang-cao-gay-sot-tren-youtube-nam-2011/");
		System.out.println(result);

	}
}
