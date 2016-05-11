package utilities;

public class Logger {

	static boolean log = false;
	
	public static void logln(String s){
		Logger.log(s+"\n");
	}
	
	public static void log(String s){
		if (log)
			System.out.print(s);

	}
	
	public static void logAlways(String s){
		System.out.print(s);
	}
	
	public static void loglnAlways(String s){
		Logger.logAlways(s+"\n");	
	}
	
}
