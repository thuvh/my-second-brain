package org.brain2.test.ws;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.brain2.ws.core.annotations.RestHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.google.gson.Gson;

public class HelloWorld extends AbstractHandler {
	
	@RestHandler
	public void exitserver(){
		System.out.println("Killing Yopco-WS server ...");
		System.exit(0);
	}
	
	@RestHandler
	public String getstring(){
		return "hello, cuc gach";
	}
	
	@RestHandler
	public List<String> getlist(){
		List list = new ArrayList<String>();
		list.add("a1");
		list.add(1);
		return list;
	}
	
	@RestHandler
	public List<Map<String, Object>> getlistobject(){
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> obj1 = new HashMap<String, Object>();		
		obj1.put("name","a1");
		obj1.put("id",11);
		
		Map<String, Object> obj2 = new HashMap<String, Object>();		
		obj2.put("name","a1");
		obj2.put("id",11);
		
		list.add(obj1);
		list.add(obj2);
		
		return list;
	}
	
	@RestHandler
	public List<Photo> getlistphoto(){
		List<Photo> list = new ArrayList<Photo>();
		
		
		
		list.add(new Photo("name1", 111));
		list.add(new Photo("name2", 222));
		list.add(new Photo("name3", 33));
		list.add(new Photo("name4", 444));
		
		return list;
	}
	
	public void processTargetHandler(String target, String queryStr, HttpServletRequest request, HttpServletResponse response ){
		try {			
			PrintWriter writer = response.getWriter();
			String[] toks = target.split("/");
			if(toks.length < 3){
				return;
			}
			response.setContentType("text/html;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			
			String[] queryToks = new String[0];
			String[] params = new String[0];
			if(queryStr != null){
				queryToks = queryStr.split("&");
				params = new String[queryToks.length];
			}
			
			System.out.println(toks[1]);
			System.out.println(toks[2]);
			Method method = this.getClass().getDeclaredMethod(toks[2]);
			Object result = method.invoke(this, params);
			Gson gson = new Gson();
			writer.println("result: " + gson.toJson(result) );
		} 
		catch (java.lang.NoSuchMethodException e) {
			System.out.println("Not found handler for the target: " + target + " !");		
		}
		catch (java.lang.IllegalArgumentException e) {
			System.out.println("wrong number of arguments for the target: " + target + " !");		
		}		
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	class Photo {
		String name;
		long id;
		public Photo(String name, long id) {
			super();
			this.name = name;
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		
	}
	
	
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		baseRequest.setHandled(true);		
		processTargetHandler(target,request.getQueryString(),request,response);
//		response.getWriter().println(target);
//		response.getWriter().println(request.getQueryString());
		
	}

	public static void main(String[] args) throws Exception {
//		int port = 9999;
//		Server server = new Server(port);
//		server.setHandler(new HelloWorld());
//
//		System.out.println("Starting Yopco-WS server at port " + port + " ...");
//		server.start();
//		server.join();	
		
		String a = null;
		if(a.equals(null)){
			System.out.println("1");
		}
		if("null".equals(a)){
			System.out.println("2");
		}
		if(a == null){
			System.out.println("3");
		}
	}
	
	
}