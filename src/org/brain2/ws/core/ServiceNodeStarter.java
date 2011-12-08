package org.brain2.ws.core;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.brain2.test.vneappcrawler.VnExpressImporter;
import org.brain2.ws.core.utils.FileUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.component.LifeCycle;

import com.google.gson.Gson;

public class ServiceNodeStarter extends AbstractHandler {
	

	private static final Map<String, ServiceHandler> servicesMap = new HashMap<String, ServiceHandler>();
	private static final Map<String, String> cachePool = new HashMap<String, String>(100);
	private static final int BUFSIZE = 2048;

	public void handle(String target, Request baseRequest, final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, ServletException {
		System.out.println("### target: " + target);
		response.setCharacterEncoding("UTF-8");
		
		if(target.equals("/favicon.ico")){
//			System.out.println(request.getHeader("Host"));
//			System.out.println(request.getRequestURL().toString());			
			response.sendRedirect("/resources/images/favicon.ico");
			return;
		} else if (target.startsWith("/resources/")){
			processTargetResource(target, request, response);			
			return;
		}
		
		baseRequest.setHandled(true);		
		// response.getWriter().println(request.getRequestURI());
		// response.getWriter().println(request.getQueryString());
		if("true".equals(request.getParameter("keep-alive")) ){
			//callby: http://localhost:10001/?keep-alive=true&keep-time=10000
			
			System.out.println(" connection keep-alive=true ");
			try {
				int timeSleep = Integer.parseInt(request.getParameter("keep-time"));
				final PrintStream writer = new PrintStream(response.getOutputStream(), true, "UTF-8");
				if(timeSleep > 0){
					Thread.sleep(timeSleep);
					writer.print(processStatusData());
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
			try {
				processTargetHandler(target, request.getQueryString(), request, response);
			} catch (Exception e) {				
				e.printStackTrace();
			}	
		}
	}
	
	
	public void initLifeCycleListener() {
		// TODO Auto-generated method stub
		super.addLifeCycleListener(new Listener() {
			
			@Override
			public void lifeCycleStopping(LifeCycle arg0) {
			}
			
			@Override
			public void lifeCycleStopped(LifeCycle arg0) {
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
			}
		});
	}
	
	/**
	 * static resource handler 
	 * 
	 * @param target
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	protected void processTargetResource(String target, HttpServletRequest request,HttpServletResponse response) throws IOException {
		if(target.endsWith(".js")||target.endsWith(".css")){					
			String data = cachePool.get(target);
			if(data == null){					
				data = FileUtils.readFileAsString(target);
				cachePool.put(target,data);
			}			
			response.getWriter().print(data);
			response.getWriter().flush();
		} else {
			ServletOutputStream op = response.getOutputStream();
			DataInputStream stream = FileUtils.readFileAsStream(target);
			byte[] bbuf = new byte[BUFSIZE];
			int length = 0, totalLength = 0;
			while ((stream != null) && ((length = stream.read(bbuf)) != -1))
	        {
	            op.write(bbuf,0,length);
	            if(length > 0){
	            	totalLength += length;
	            }
	        }
			stream.close();
	        op.flush();
	        op.close();		      
	        response.setContentType("application/octet-stream" );
	        response.setContentLength( totalLength );
		}
	}

	/*
	 * 
	 */
	protected void processTargetHandler(String target, String queryStr, HttpServletRequest request,HttpServletResponse response) throws Exception {
		PrintStream writer = new PrintStream(response.getOutputStream(), true, "UTF-8");
		try {
			//PrintWriter writer = response.getWriter();
			
			String[] toks = target.split("/");
			
			if (toks.length <= 1) {
				System.out.println("target must in format [/class-key/method-name/response-type]: "+target);
				return;
			}else if(toks.length == 2){
				toks = new String[] {"",toks[1],"getServiceName","json"};
			}else if(toks.length == 3){
				toks = new String[] {"",toks[1],toks[2],"json"};
			}
			
			response.setContentType("text/html;charset=UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);

			Map params = new HashMap();
			Enumeration<String> p = request.getParameterNames();

			while (p.hasMoreElements()) {
				String name = p.nextElement();
				params.put(name, request.getParameter(name));
			}

			System.out.println("Service handler namespace: "+toks[1]);
			System.out.println("Service handler actionname: "+toks[2]);
			System.out.println(target);
			System.out.println(queryStr);
			
			ServiceMapperLoader mapperLoader = new ServiceMapperLoader("/services-mapper.json");
			String namespace = toks[1];
			Class clazz = mapperLoader.getMapperClass(namespace );
			String key = clazz.getName();

			if (!servicesMap.containsKey(key)) {
				servicesMap.put(key, (ServiceHandler) clazz.newInstance());
			}

			Method method = clazz.getDeclaredMethod(toks[2], new Class[] { Map.class });

//			System.out.println(clazz);
//			System.out.println(method);

			Object result = method.invoke(servicesMap.get(key), params);
			
			Gson gson = new Gson();	
			if(toks[3].toLowerCase().equals("json")){
				if(toks[2].equals("getServiceName")){
					Map<String, String> obj = new HashMap<String, String>(2);
					obj.put("service-name", result.toString());
					writer.print(gson.toJson(obj));
				} else {
					writer.print(gson.toJson(result));
				}
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
			writer.print("Not found handler for the target: " + target + " !");
		} catch (java.lang.IllegalArgumentException e) {
			writer.print("wrong number of arguments for the target: " + target + " !");
		} catch (Exception e) {	
			if(e instanceof java.lang.reflect.InvocationTargetException){
				Throwable c = e.getCause();
				//System.err.println(c.getClass().getName());
				c.printStackTrace();
			} else {
				e.printStackTrace();
			}
			writer.print(e.getMessage());
		}
	}
	
	protected String processStatusData() {
		StringBuilder sb = new StringBuilder();
		sb.append("setTotalJobCount("+VnExpressImporter.getTotalJobCount()+");");
		sb.append("setJobCount("+VnExpressImporter.getJobCount()+");");
		sb.append("setWorkFinished("+VnExpressImporter.getWorkFinished()+");");
		sb.append("setTotalJobFailed("+VnExpressImporter.getTotalJobFailed()+");");
		sb.append("setTotalDieLinks("+VnExpressImporter.getTotalDieLinks()+");");
		sb.append("isWorking = "+VnExpressImporter.isWorking() + " ;");
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		int port = 10001;// TODO use config here
		Server server = new Server(port);		
		ServiceNodeStarter theHandler = new ServiceNodeStarter();
		theHandler.initLifeCycleListener();		
		server.setHandler(theHandler);		

		System.out.println("Starting Agent at port " + port + " ...");
		server.start();
		server.join();		
	}	

}
