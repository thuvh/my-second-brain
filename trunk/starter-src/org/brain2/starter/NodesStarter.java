package org.brain2.starter;


public class NodesStarter {
	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) throws Exception {
		int min = 512, max = 1024;
		if(args.length == 2){
			min = Integer.parseInt(args[0]);
			max = Integer.parseInt(args[1]);
		}		
		String cmd = "java -jar -Xms"+min+"m -Xmx"+max+"m -XX:-UseParallelGC agent.jar";
		Runtime.getRuntime().exec(cmd);
		System.out.println("exec job :" + cmd);
		
//		Runtime.getRuntime().exec( new String[] {
//                "java",
//                "-jar",
//                "-Xms512m",
//                "-Xmx2048m",
//                "-UseParallelGC",
//                "agent.jar",
//                "My Parser"});		
		
		
		System.out.println("started ...");
	}
}
