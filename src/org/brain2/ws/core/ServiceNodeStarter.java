package org.brain2.ws.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.google.gson.Gson;

public class ServiceNodeStarter extends AbstractHandler {
	
	private static Map<String,ServiceHandler> servicesMap = new HashMap<String,ServiceHandler>();

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		baseRequest.setHandled(true);
		processTargetHandler(target, request.getQueryString(), request, response);
		//response.getWriter().println(request.getRequestURI());
		// response.getWriter().println(request.getQueryString());

	}

	public void processTargetHandler(String target, String queryStr, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			PrintWriter writer = response.getWriter();
			String[] toks = target.split("/");
			if (toks.length < 3) {
				return;
			}
			response.setContentType("text/html;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
						
			Map params = new HashMap();			
			Enumeration<String> p = request.getParameterNames();
			
			while(p.hasMoreElements()){				
				String name = p.nextElement();
				params.put(name, request.getParameter(name));
			}					

//			System.out.println(toks[1]);
//			System.out.println(toks[2]);
//			System.out.println(target);
//			System.out.println(queryStr);
			
			//TODO use config here
			String key = "org.brain2.ws.services.linkmarking.LinkDataHandler";			
			Class clazz = Class.forName(key);			
			
			if( ! servicesMap.containsKey(key)){
				servicesMap.put(key, (ServiceHandler) clazz.newInstance());
			}
			
			Method method = clazz.getDeclaredMethod(toks[2],new Class[]{Map.class} );		
						
			System.out.println(clazz);
			System.out.println(method);	
			
			Object result = method.invoke(servicesMap.get(key), params);
			Gson gson = new Gson();
			writer.println(gson.toJson(result));
		} catch (java.lang.NoSuchMethodException e) {
			System.out.println("Not found handler for the target: " + target + " !");
		} catch (java.lang.IllegalArgumentException e) {
			System.out.println("wrong number of arguments for the target: " + target + " !");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		int port = 10001;//TODO use config here
		Server server = new Server(port);
		server.setHandler(new ServiceNodeStarter());

		System.out.println("Starting Yopco-WS server at port " + port + " ...");
		server.start();
		server.join();
	}

}
