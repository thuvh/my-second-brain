package org.brain2.ws.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.brain2.ws.core.utils.FileUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.google.gson.Gson;

public class ServiceNodeStarter extends AbstractHandler {

	private static Map<String, ServiceHandler> servicesMap = new HashMap<String, ServiceHandler>();

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		baseRequest.setHandled(true);
		processTargetHandler(target, request.getQueryString(), request, response);
		// response.getWriter().println(request.getRequestURI());
		// response.getWriter().println(request.getQueryString());
	}

	/*
	 * 
	 */
	public void processTargetHandler(String target, String queryStr, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			PrintWriter writer = response.getWriter();
			String[] toks = target.split("/");
			if (toks.length <= 3) {
				return;
			}
			response.setContentType("text/html;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);

			Map params = new HashMap();
			Enumeration<String> p = request.getParameterNames();

			while (p.hasMoreElements()) {
				String name = p.nextElement();
				params.put(name, request.getParameter(name));
				
			}

			// System.out.println(toks[1]);
			// System.out.println(toks[2]);
			// System.out.println(target);
			// System.out.println(queryStr);

			// TODO use config here
			String key = "org.brain2.ws.services.linkmarking.LinkDataHandler";
			Class clazz = Class.forName(key);

			if (!servicesMap.containsKey(key)) {
				servicesMap.put(key, (ServiceHandler) clazz.newInstance());
			}

			Method method = clazz.getDeclaredMethod(toks[2], new Class[] { Map.class });

//			System.out.println(clazz);
//			System.out.println(method);

			Object result = method.invoke(servicesMap.get(key), params);
			
			Gson gson = new Gson();	
			if(toks[3].toLowerCase().equals("json")){
				writer.print(gson.toJson(result));
			} else {
				String filepath = "/resources/html/target_response.html";			
				String html = "";
				try {
					html = readFileAsString(filepath);
				} catch (IOException e) {					
					e.printStackTrace();
				}				
				html = html.replace("_json", gson.toJson(result));
				writer.print(html);
			}
			

		} catch (java.lang.NoSuchMethodException e) {
			System.out.println("Not found handler for the target: " + target + " !");
		} catch (java.lang.IllegalArgumentException e) {
			System.out.println("wrong number of arguments for the target: " + target + " !");
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		int port = 10001;// TODO use config here
		Server server = new Server(port);
		server.setHandler(new ServiceNodeStarter());

		System.out.println("Starting My Second Brain Agent at port " + port + " ...");
		server.start();
		server.join();
	}

	
	private static String readFileAsString(String filePath) throws java.io.IOException {
		String fullpath = FileUtils.getRuntimeFolderPath() + filePath;
		
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(fullpath));
		char[] buf = new char[2048];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			fileData.append(buf, 0, numRead);
		}
		reader.close();
		return fileData.toString();
	}

}
