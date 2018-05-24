package guodu.net.analyseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import guodu.net.impl.AnalyselmplEx;
import guodu.net.impl.RedisInterface;
import guodu.net.impl.interf.RedisImpl;
import guodu.net.util.ConfigContainer;
import guodu.net.util.Loger;
import guodu.net.util.SegmentJudge;
import net.sf.json.JSONObject;

public class AnalyseEcodeService extends AnalyselmplEx{
	private static Boolean flagInstance = false;
	
	private static Map<String , Integer> CmppMap = new HashMap<String , Integer>(); 
	private static Map<String , Integer> SgipMap = new HashMap<String , Integer>();
	private static Map<String , Integer> SmgpMap = new HashMap<String , Integer>();
	private static Map<String , Integer> UnknowMap = new HashMap<String , Integer>();
	private static int total = 0;
	private static Boolean button = false;
	private static JSONObject jsonObj = new JSONObject();
	
	//创建线程，定时处理内存信息分析记录
	static{
		new Thread(){
			public void run(){
				Loger.Info_log.info("[INFO]AnalyseEcodeService将拼接好的json串写入redis线程启动");
				while(true){
					try {
						sleep(3000);
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
	
	private static AnalyseEcodeService singleton = null;  
    
    // 同步的获取实例方法  
    public static synchronized AnalyseEcodeService getInstance(){  
        if(null == singleton){  
            singleton = new AnalyseEcodeService();  
        }  
        return singleton;  
    }  
	
	
	// 构造方法私有  
    private AnalyseEcodeService(){  
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
			Loger.Info_log.info("[ERROR]AnalyseEcodeService实例话错误" , e);
			
			if(!flagInstance)  
		    {  
				flagInstance = !flagInstance;  
		    }
		} 
    }  
	

	/**
	 * 将map信息加入json数据记录中
	 * 
	 * @param ecode
	 * 				提交ecode值
	 * @param province
	 * 				省份
	 * @param isp_id
	 * 				运营商
	 * */
	private static void getJson(String ecode , String province , String isp_id){
		if("cmpp".equals(isp_id)){
			jointJson(CmppMap , ecode , province);
			jsonObj.put("cmpp", CmppMap);
		}else if("sgip".equals(isp_id)){
			jointJson(SgipMap , ecode , province);
			jsonObj.put("sgip", SgipMap);
		}else if("smgp".equals(isp_id)){
			jointJson(SmgpMap , ecode , province);
			jsonObj.put("smgp", SmgpMap);
		}else{
			jointJson(UnknowMap , ecode , province);
			jsonObj.put("unknow", UnknowMap);
		}
	}
	
	/**
	 * 将信息写入对应的map中
	 * 
	 * @param Map<String , Integer>
	 * 				记录省份的发送情况
	 * @param ecode
	 * 				提交情况
	 * @param province
	 * 				省份
	 * */
	private static void jointJson(Map<String , Integer> map , String ecode , String province){
		total++;
		ecode = "0";
		
		if("0".equals(ecode)){
			ecode = "suc";
		}else{
			ecode = "fail";
		}
		
		if(map.containsKey(province + "_" + ecode)){
			map.put(province + "_" + ecode, map.get(province + "_" + ecode) + 1);
		}else{
			map.put(province + "_" + ecode, 1);
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
				jsonObj.put("total", total);
				redis.addCommonByteToList(jsonObj.toString() , ConfigContainer.getAnalyseEcodeService_key());	
				Loger.Info_log.info("[INFO]" + jsonObj.toString() + "成功插入redis");
			} catch (Exception e) {
				e.printStackTrace();
				Loger.Info_log.info("[ERROR]" + jsonObj.toString() + "信息入redis错误" , e);
			}finally {
				//开关关闭，内存清空
				button = !button;
				CmppMap.clear();
				SgipMap.clear();
				SmgpMap.clear();
				total = 0;
				UnknowMap.clear();
				jsonObj = new JSONObject();	
			}
		}else{
			try {
				List<String> information = (ArrayList<String>) list;			
				for(String im : information){				
					String ecode = im.split("\\|!")[24];
					String isp_id = SegmentJudge.getIsp_id(im.split("\\|!")[22]);
					String province = SegmentJudge.getProvince(im.split("\\|!")[22]);				
					getJson(ecode , province , isp_id);
				}
			} catch (Exception e) {
				e.printStackTrace();
				Loger.Info_log.info("[ERROR]json信息拼接错误" , e);
			}
		}
	}
}
