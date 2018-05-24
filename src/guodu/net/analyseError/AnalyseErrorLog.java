package guodu.net.analyseError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import guodu.net.impl.AnalyselmplEx;
import guodu.net.impl.RedisInterface;
import guodu.net.impl.interf.RedisImpl;
import guodu.net.util.ConfigContainer;
import guodu.net.util.Loger;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class AnalyseErrorLog extends AnalyselmplEx{
private static Boolean flagInstance = false;
	
	private static Map<String , Integer> ErrorMap = new HashMap<String , Integer>(); 
	private static Boolean button = false;
	private static JSONObject jsonObj = new JSONObject();
	
	//创建线程，定时处理内存信息分析记录
	static{
		new Thread(){
			public void run(){
				Loger.Info_log.info("[INFO]AnalyseErrorLog将拼接好的json串写入redis线程启动");
				while(true){
					try {
						sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(!button){
						button = !button;
						getInstance().analyseEx(null);
					}
				}
			}
		}.start();
	}
	
	private static AnalyseErrorLog singleton = null;  
    
    // 同步的获取实例方法  
    public static synchronized AnalyseErrorLog getInstance(){  
        if(null == singleton){  
            singleton = new AnalyseErrorLog();  
        }  
        return singleton;  
    }  
	
	
	// 构造方法私有  
    private AnalyseErrorLog(){  
    	StackTraceElement stack[] = Thread.currentThread().getStackTrace();
		try {
			if(!stack[2].getClassName().startsWith("sun.reflect")){
				if(!flagInstance)  
			    {  
					flagInstance = !flagInstance;  
			    }  
			    else  
			    {  
			        throw new RuntimeException("单例模式被侵犯！");  
			    } 
			}
		} catch (Exception e) {
			e.printStackTrace();
			Loger.Info_log.info("[ERROR]AnalyseErrorLog实例话错误" , e);
			
			if(!flagInstance)  
		    {  
				flagInstance = !flagInstance;  
		    }
		} 
    }  
	
	
	/**
	 * 接口的实现方法
	 * 分析数据入口
	 * 
	 * @param object
	 * 			带分析的数据list
	 * */
	@Override
	public void analyse(Object list) {
		getInstance().analyseEx(list);
	}
	
	
	/**
	 * 将带发送的数据进行分析，拼接入json串中
	 * 当button开关关闭时，停止数据分析，将已分析完的json串插入redis中
	 * 然后将内存数据清空
	 * 
	 * @param Object
	 * 			待分析的数据list
	 * */
	@SuppressWarnings("unchecked")
	private synchronized void analyseEx(Object list) {
		if(button){
			RedisInterface redis = new RedisImpl();
			try {
				jsonObj.put("ERROR", ErrorMap.get("ERROR"));
				redis.addCommonByteToList(jsonObj.toString() , ConfigContainer.getAnalyseErrorLog_key());	
				Loger.Info_log.info("[INFO]AnalyseErrorLog类" + jsonObj.toString() + "成功插入redis");
			} catch (Exception e) {
				e.printStackTrace();
				Loger.Info_log.info("[ERROR]AnalyseErrorLog类" + jsonObj.toString() + "信息入redis错误" , e);
			}finally {
				//开关关闭，内存清空
				button = !button;
				ErrorMap.clear();
				jsonObj = new JSONObject();	
			}
		}else{
			try {
				List<String> information = (ArrayList<String>) list;
				for(int i = 0 ; i < information.size() ; i++ ){
					if(ErrorMap.containsKey("ERROR")){
						ErrorMap.put("ERROR", ErrorMap.get("ERROR") + 1);
					}else{
						ErrorMap.put("ERROR", 1);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				Loger.Info_log.info("[ERROR]json信息拼接错误" , e);
			}
		}
	}
}
