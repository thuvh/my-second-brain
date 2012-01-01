package org.brain2.ws.core;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.brain2.ws.core.utils.FileUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.component.LifeCycle;

import com.google.gson.Gson;

public class ServiceNodeStarter extends AbstractHandler {
	

	private static final Map<String, ServiceHandler> servicesMap = new HashMap<String, ServiceHandler>(30);
	private static final Map<String, String> cachePool = new HashMap<String, String>(100);
	private static final Map<String, String> agentsQueue = new HashMap<String, String>(10000);
	protected static final boolean USE_CACHE = false;
	private static final int BUFSIZE = 2048;

	public void handle(String target, Request baseRequest, final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, ServletException {
		
		//TODO logging request here		
		System.out.println("HTTP Method: " + baseRequest.getMethod() + " ,target: " + target);
		
		response.setCharacterEncoding("UTF-8");
		response.setStatus(HttpServletResponse.SC_OK);
		
		if(target.equals("/favicon.ico")){		
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
			try {
				response.setContentType("text/javascript");				
				int timeSleep = Integer.parseInt(request.getParameter("keep-time"));
				final PrintStream writer = new PrintStream(response.getOutputStream(), true, "UTF-8");
				writer.flush();
				if(timeSleep > 0){
					System.out.println("###connection keep-alive=true in millis: " + timeSleep);
					Thread.sleep(timeSleep);
					writer.print(processStatusData());
				}
				writer.flush();
				
			} catch (Exception e) {				
				e.printStackTrace();
			}
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
				System.out.println("node starting, loading init config ...");
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
				if(USE_CACHE) {
					cachePool.put(target,data);
				}
			}	
			if(target.endsWith(".js")){				
				response.setContentType("text/javascript");
			} else if(target.endsWith(".css")){	
				response.setContentType("text/css");
			}
			response.getWriter().print(data);
			response.getWriter().flush();
		} else if(target.endsWith("/")){
			File[] files = FileUtils.listFilesInForder(target);
			StringBuilder sb = new StringBuilder();
			sb.append("<ul>");
			for (File file : files) {		
				if( ! file.getName().contains(".svn")){
					String uri = "http://localhost:10001" + target + file.getName();
					String a = "<a href='" + uri + "'>" + uri + "</a>";
					sb.append("<li>").append(a).append("</li>");
				}
			}
			sb.append("</ul>");			
	        response.setContentType("text/html" );
			response.getWriter().print(sb.toString());
			response.getWriter().flush();	        
		} else {
			ServletOutputStream op = response.getOutputStream();
			DataInputStream stream = FileUtils.readFileAsStream(target);
			if(stream != null){
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
			} else {
				response.setStatus(404);
			}
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
			
			ServiceMapperLoader mapperLoader = new ServiceMapperLoader("/services-mapper.json");
			String namespace = toks[1];
			Class clazz = mapperLoader.getMapperClass(namespace );
			String key = clazz.getName();

			//check to cache service in pool
			ServiceHandler handler = servicesMap.get(key);
			if ( handler == null ) {
				handler = (ServiceHandler) clazz.newInstance(); 
				servicesMap.put(key, handler);
			}

			Method method = clazz.getDeclaredMethod(toks[2], new Class[] { Map.class });

//			System.out.println(clazz);
//			System.out.println(method);

			Object result = "";
			try {
				//inject req + res into the service
				handler.setHttpServletRequest(request);
				handler.setHttpServletResponse(response);
				result = method.invoke(servicesMap.get(key), params);
			} catch (Throwable e1) {
				//e1.printStackTrace();
				result = e1.getCause().getClass().getName() + ":" + e1.getCause().getMessage();
			}
			//System.out.println(result);
			
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
			} else if(toks[3].toLowerCase().equals("string")){
				writer.print(result);
			} else {
				String filepath = "/resources/html/target_response.html";			
				String html = "";
				try {
					html = FileUtils.readFileAsString(filepath);
				} catch (IOException e) {					
					e.printStackTrace();
				}				
				html = html.replace("{Origin}", request.getHeader("Origin"));
				html = html.replace("{json}", gson.toJson(result));
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
		return "console.log('"+(new Date()).toString() + "'); ";
	}
	
//	protected String processStatusData() {
//		StringBuilder sb = new StringBuilder();
//		sb.append("setTotalJobCount("+VnExpressImporter.getTotalJobCount()+");");
//		sb.append("setJobCount("+VnExpressImporter.getJobCount()+");");
//		sb.append("setWorkFinished("+VnExpressImporter.getWorkFinished()+");");
//		sb.append("setTotalJobFailed("+VnExpressImporter.getTotalJobFailed()+");");
//		sb.append("setTotalDieLinks("+VnExpressImporter.getTotalDieLinks()+");");
//		sb.append("isWorking = "+VnExpressImporter.isWorking() + " ;");
//		return sb.toString();
//	}

	public static void main(String[] args) throws Exception {
		int port = 10001;// TODO use config here
		Server server = new Server(port);		
		ServiceNodeStarter theHandler = new ServiceNodeStarter();
		theHandler.initLifeCycleListener();		
		server.setHandler(theHandler);		

		System.out.println("Starting Agent Pools at port " + port + " ...");
		server.start();
		server.join();		
	}	

}
