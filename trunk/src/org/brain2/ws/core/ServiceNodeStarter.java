package org.brain2.ws.core;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.brain2.test.dao.VnExpressDao;
import org.brain2.ws.core.utils.FileUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.component.LifeCycle;

import com.google.gson.Gson;

public class ServiceNodeStarter extends AbstractHandler {
	

	private static Map<String, ServiceHandler> servicesMap = new HashMap<String, ServiceHandler>();

	public void handle(String target, Request baseRequest, final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, ServletException {
		baseRequest.setHandled(true);
		
		// response.getWriter().println(request.getRequestURI());
		// response.getWriter().println(request.getQueryString());
		if("true".equals(request.getParameter("keep-alive")) ){
			//callby: http://localhost:10001/?keep-alive=true&keep-time=10000
			final PrintStream writer = new PrintStream(response.getOutputStream(), true, "UTF-8");
			System.out.println(" connection keep-alive=true ");
			try {
				int timeSleep = Integer.parseInt(request.getParameter("keep-time"));
				if(timeSleep > 0){
					
					writer.print("");
					Thread.sleep(timeSleep);
					writer.print("setTotalJobCount("+VnExpressDao.getTotalJobCount()+");");
					writer.print("setJobCount("+VnExpressDao.getJobCount()+");");
					writer.print("setWorkFinished("+VnExpressDao.getWorkFinished()+");");
					
				}
				writer.flush();
			} catch (Exception e) {				
				e.printStackTrace();
			}	
			new Thread(new Runnable() {				
				@Override
				public void run() {
					//TODO	
				}
			}).start();
		} else {
			processTargetHandler(target, request.getQueryString(), request, response);	
		}
	}
	
	
	public void initLifeCycleListener() {
		// TODO Auto-generated method stub
		super.addLifeCycleListener(new Listener() {
			
			@Override
			public void lifeCycleStopping(LifeCycle arg0) {
				// TODO Auto-generated method stub				
			}
			
			@Override
			public void lifeCycleStopped(LifeCycle arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void lifeCycleStarting(LifeCycle arg0) {				
				System.out.println("node starting ...");
			}
			
			@Override
			public void lifeCycleStarted(LifeCycle arg0) {				
				System.out.println("node Started ...");
			}
			
			@Override
			public void lifeCycleFailure(LifeCycle arg0, Throwable arg1) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	/*
	 * 
	 */
	public void processTargetHandler(String target, String queryStr, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			//PrintWriter writer = response.getWriter();
			PrintStream writer = new PrintStream(response.getOutputStream(), true, "UTF-8");
			String[] toks = target.split("/");
			if (toks.length <= 3) {
				return;
			}
			response.setContentType("text/html;charset=UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);

			Map params = new HashMap();
			Enumeration<String> p = request.getParameterNames();

			while (p.hasMoreElements()) {
				String name = p.nextElement();
				params.put(name, request.getParameter(name));
				
			}

			// System.out.println(toks[1]);
			// System.out.println(toks[2]);
			System.out.println(target);
			System.out.println(queryStr);

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
			} else if(toks[3].toLowerCase().equals("html")){
				System.out.println(gson.toJson(result));		
				String path = request.getParameter("path");
				if(path != null){
					String filepath = "/resources/html/"+ path +".html";			
					String html = "404 not found!";
					try {
						html = FileUtils.readFileAsString(filepath);					
					} catch (IOException e) {					
						e.printStackTrace();
					}
					writer.print(html);
				} else {
					writer.print("");
				}				
			} else {
				String filepath = "/resources/html/target_response.html";			
				String html = "";
				try {
					html = FileUtils.readFileAsString(filepath);
				} catch (IOException e) {					
					e.printStackTrace();
				}				
				html = html.replace("_json", gson.toJson(result));
				writer.print(html);
			}
			writer.flush();
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
		ServiceNodeStarter theHandler = new ServiceNodeStarter();
		theHandler.initLifeCycleListener();		
		server.setHandler(theHandler);		

		System.out.println("Starting My Second Brain Agent at port " + port + " ...");
		server.start();
		server.join();		
	}

	

}
