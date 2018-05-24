package guodu.net.util;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Loger {

	
	static {
		PropertyConfigurator.configure("config/Log4j.properties");
	}
	
	public static final Logger Info_log = Logger.getLogger("message");




}
