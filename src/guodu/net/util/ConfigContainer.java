package guodu.net.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class ConfigContainer{
	
	private static ConfigContainer instance;  								//本类唯一实例
	
	private static List<String> read_from_redis = new ArrayList<String>();	//redis信息存储队列
	private static List<String> read_from_redis_error = new ArrayList<String>();	//redis信息存储队列
	private static int redis_max_active = 99;
	private static int redis_max_idle = 49;
	private static int redis_max_wait = 999;
	private static String redis_ip = "127.0.0.1";							//redis的ip
	private static int redis_port = 6380;									//redis端口
	private static int redis_read_time_out = 100000;
	private static int exception_sleep_time = 30000;	
	private static long sleep_time = 1000; 
	private static String key = "wml:20160929:test";						//redis的key
	private static Boolean button = false;									//是否从redis中抓取信息
	private static int read_thread_count = 3;								//fetch线程数
	private static int read_from_redis_limit = 500;							//每次抓取数量
	private static int analyseThread = 3;									//分析数据线程数
	private static int analyse_limt = 50;									//每次分析数据量
	private static String analyseEcodeService_key = "wml:20161101:test:json";//AnalyseEcodeService类分析结果key
	private static String error_key = "wml:20161103:error";					//error信息redis的key
	private static String analyseErrorLog_key = "wml:20161103:error:json";  //analyseErrorLog_key类分析结果key
	//package包
	private static String[] packageList = {"guodu.net.analyseService","guodu.net.analyseError"};
	
	private static Map<String , String> mapSegment = new HashMap<String , String>(); //纪录运营商号段表
	private static Map<String , String> mapProvince = new HashMap<String , String>();//纪录省份号段表
	
	private static JedisPool common_jedispool;								

	
	
	static synchronized public ConfigContainer getInstance() {
		if (instance == null) {
			instance = new ConfigContainer();
		}
		return instance;
	}
	
	private ConfigContainer() {
		
	}
	
	
	/**
	 * 配置文件信息加载，根据关键词查询配置文件，如果没有所查找的关键词，则采用默认值
	 * */
     public void load(){
    	 Loger.Info_log.info("[INFO]加载配置文件信息。。。");
    	 try {
			 Map<?,?> map = loadFunction("common" , "jesis");
			 redis_max_active = Integer.parseInt(getInfo("redis_max_active","99", map));
			 redis_max_idle = Integer.parseInt(getInfo("redis_max_idle","49", map));
			 redis_max_wait = Integer.parseInt(getInfo("redis_max_active","999", map));
			 redis_ip = getInfo("redis_ip","127.0.0.1", map);
			 redis_port = Integer.parseInt(getInfo("redis_port","6380", map));
			 redis_read_time_out = Integer.parseInt(getInfo("redis_read_time_out","100000", map));
			 exception_sleep_time = Integer.parseInt(getInfo("exception_sleep_time","30000", map)); 
			 sleep_time = Long.parseLong(getInfo("sleep_time","30000", map));
			 read_thread_count = Integer.parseInt(getInfo("read_thread_count","3", map));
			 read_from_redis_limit = Integer.parseInt(getInfo("read_from_redis_limit","500", map));
			 analyseThread = Integer.parseInt(getInfo("analyseThread","5", map));
			 analyse_limt = Integer.parseInt(getInfo("analyse_limt","50", map));
			 analyseEcodeService_key = getInfo("analyseEcodeService_key","wml:20161101:test:json", map);
			 analyseErrorLog_key = getInfo("analyseErrorLog_key","wml:20161103:error:json", map);
			 key = getInfo("key","wml:20160929:test", map);	
			 error_key = getInfo("error_key","wml:20161103:error", map);
			 String listPackage = getInfo("packageList","guodu.net.analyseService,guodu.net.analyseError", map);
			 packageList = listPackage.split(",");
			 
	    	 ConfigContainer.getcommon_jedispool();
			
	    	 /**
	    	  * 实现thread接口，来实时监控配置文件中的开关信息
	    	  * */
			 new Thread(){
				 public void run(){
					 Loger.Info_log.info("[INFO]程序开关信息加载线程启动。。。");
					 while(true){
						 Map<?,?> mapButton = loadFunction("switch" , "");
						 button = new Boolean(getInfo("button","false", mapButton));
						 try {
							sleep(sleep_time);
						} catch (InterruptedException e) {
							e.printStackTrace();
							Loger.Info_log.info("[ERROR]休眠异常：" , e);
						}
					 }
				 }
			 };
			 
		} catch (NumberFormatException e) {
			e.printStackTrace();
			Loger.Info_log.info("[ERROR]配置文件信息有误，请检查文件是否正常：" , e);
		}
     }
     
     
     
     /**
      * 从内存中获取信息
      * 
      * @param count 
      * 		int类型，每次从队列中抓取的数量
      * 
      * @return getListInformation
      * 		List<String> 信息存入的list
      * */
     public static synchronized List<String> getListInformation(int count , String type){
    	 if(count > read_from_redis_limit){
    		 count = read_from_redis_limit;
    	 }
    	 List<String> rs = new ArrayList<String>();
    	 String information = "";
    	 for(int i = 0 ; i < count ; i++){
    		 information = getInformation(type);
    		 if(null != information && !"".equals(information)){
    			 rs.add(information);
    		 }
    	 }
    	 return rs;
     }
     
     
     /**
      * 队列中取数据的具体方法
      * 
      * @return string
      * 		当队列为空，返回null，否则返回队列中信息
      * */
     public static synchronized String getInformation(String type){
    	 if("guodu.net.analyseService".equals(type)){
			 if(read_from_redis.isEmpty()){
				 return null;
			 }else{
				 return read_from_redis.remove(getRead_from_redis_length() -1);
			 }
		 }else if("guodu.net.analyseError".equals(type)){
			 if(read_from_redis_error.isEmpty()){
				 return null;
			 }else{
				 return read_from_redis_error.remove(getRead_from_errorlog_length() -1);
			 }
		 }else{
			 return null;
		 }
     }
     
     /**
      * 获取read_from_redis长度
      * 
      * @return int
      * 		队列长度
      * */
     public static int getRead_from_redis_length(){
    	 return read_from_redis.size();
     }
     
     /**
      * 获取read_from_redis_error长度
      * 
      * @return int
      * 		队列长度
      * */
     public static int getRead_from_errorlog_length(){
    	 return read_from_redis_error.size();
     }     
     
     //read_from_redis添加信息
     public static void add_to_read_from_redis(String a){ 	
    	 read_from_redis.add(a);
     }
     
     //read_from_redis添加信息
     public static void add_to_read_from_errorlog(String a){ 	
    	 read_from_redis_error.add(a);
     }
     
     public Jedis getCommonJedis() {     //获取jedis
 		Jedis jedis = null;
 		try {
 			jedis = getJedisResource();
 		} catch (Exception e) {
 			e.printStackTrace();
 			try {
 				Thread.sleep(30000);
 			} catch (NumberFormatException e1) {
 				e1.printStackTrace();
 			} catch (InterruptedException e1) {
 				e1.printStackTrace();
 			}
 			getcommon_jedispool();
 		}
 		if (jedis != null) {
 			return jedis;
 		}
 		Loger.Info_log.info("[ERROR]读写redis,取到空链接");
 		return null;
 	}
     
     //关闭jedis连接
 	public void common_close(Jedis jedis) {
		common_jedispool.returnResource(jedis);
	}
     	
    private static void getcommon_jedispool(){
    	JedisPoolConfig common_config = new JedisPoolConfig();
    	try {
 			common_config.setMaxActive(redis_max_active);
 			common_config.setMaxIdle(redis_max_idle);
 			common_config.setMaxWait(redis_max_wait);
 			common_config.setTestOnBorrow(true);
 			common_jedispool = new JedisPool(common_config, redis_ip, redis_port, redis_read_time_out);
 		 } catch (Exception e) {
 			Loger.Info_log.info("[ERROR]Jedis实例话失败：" , e);
 		 }
     }
     
     private static Jedis getJedisResource(){
    	 Jedis jedis = null;
    	 try {
			jedis = common_jedispool.getResource();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			getcommon_jedispool();
		}
    	 return jedis;
     }
     
     /**
     * 配置文件信息验证
     * 如果配置文件中已配置关键词的信息，则返回配置信息
     * 如果配置文件中没有配置关键词的信息，则返回默认值
     * @param e 关键词
     * @param defult 默认值
     * @param map
     * @return result
     * */
     public static String getInfo(String e , String  defult , Map<?,?> map)
     {
    	 String result = (String) map.get(e);
    	 if("".equals(result))
    	 {
    		 result = defult;
    	 }
    	 return result;
     }
     
     /**
      * 配置文件信息加载方法实现
      * @param e 节点
      * @param f 节点
      * @return map 
      * */
     public static Map<?, ?> loadFunction(String e , String f)
     {
   	  SAXReader saxreader = new SAXReader();
   	  
   	  Document doc = null;
   	  Map<String,String> map = null;
		try {
			doc = saxreader.read(new File("config" + File.separator + "config.xml"));
			map = new HashMap<String,String>();
			if(null != e && !"".equals(e)){
				Element root = doc.getRootElement().element(e);
		    	for ( Iterator<?> iterInner = root.elementIterator(); iterInner.hasNext(); ) {   
		    		Element elementInner = (Element) iterInner.next();
		    	    map.put(elementInner.getName(), root.elementText(elementInner.getName()));
		    	}
			}
			if(null != f && !"".equals(f)){
		    	Element rootf = doc.getRootElement().element(f);
		    	for ( Iterator<?> iterInner = rootf.elementIterator(); iterInner.hasNext(); ) {   
		    		Element elementInner = (Element) iterInner.next();
		    	    map.put(elementInner.getName(), rootf.elementText(elementInner.getName()));
		    	}
			}
		} catch (DocumentException e1) {
			e1.printStackTrace();
			Loger.Info_log.info("配置文件信息加载错误：" , e1);
		}
    	 return map;
     }
     
     public String toString(){
    	StringBuffer sb = new StringBuffer();
		sb.append("infomation:");
		sb.append(redis_max_active).append("|!");
		sb.append(redis_max_idle).append("|!");
		sb.append(redis_max_wait).append("|!");
		sb.append(redis_ip).append("|!");
		sb.append(redis_port).append("|!");
		sb.append(redis_read_time_out).append("|!");
		sb.append(exception_sleep_time).append("|!");
		sb.append(sleep_time).append("|!");
		sb.append(key).append("|!");
		sb.append(button).append("|!");
		sb.append(read_thread_count).append("|!");
		sb.append(read_from_redis_limit).append("|!");
		return sb.toString();
	}
     
    public static int getRedis_max_active() {
		return redis_max_active;
	}

	public static void setRedis_max_active(int redis_max_active) {
		ConfigContainer.redis_max_active = redis_max_active;
	}

	public static int getRedis_max_idle() {
		return redis_max_idle;
	}

	public static void setRedis_max_idle(int redis_max_idle) {
		ConfigContainer.redis_max_idle = redis_max_idle;
	}

	public static int getRedis_max_wait() {
		return redis_max_wait;
	}

	public static void setRedis_max_wait(int redis_max_wait) {
		ConfigContainer.redis_max_wait = redis_max_wait;
	}

	public static String getRedis_ip() {
		return redis_ip;
	}

	public static void setRedis_ip(String redis_ip) {
		ConfigContainer.redis_ip = redis_ip;
	}

	public static int getRedis_port() {
		return redis_port;
	}

	public static void setRedis_port(int redis_port) {
		ConfigContainer.redis_port = redis_port;
	}

	public static int getRedis_read_time_out() {
		return redis_read_time_out;
	}

	public static void setRedis_read_time_out(int redis_read_time_out) {
		ConfigContainer.redis_read_time_out = redis_read_time_out;
	}

	public static int getException_sleep_time() {
		return exception_sleep_time;
	}

	public static void setException_sleep_time(int exception_sleep_time) {
		ConfigContainer.exception_sleep_time = exception_sleep_time;
	}

	public static long getSleep_time() {
		return sleep_time;
	}

	public static void setSleep_time(long sleep_time) {
		ConfigContainer.sleep_time = sleep_time;
	}
	
	public static String getKey() {
		return key;
	}

	public static void setKey(String key) {
		ConfigContainer.key = key;
	}

	public static Boolean getButton() {
		return button;
	}

	public static void setButton(Boolean button) {
		ConfigContainer.button = button;
	}
	
	public static int getRead_thread_count() {
		return read_thread_count;
	}

	public static void setRead_thread_count(int read_thread_count) {
		ConfigContainer.read_thread_count = read_thread_count;
	}

	public static List<String> getRead_from_redis() {
		return read_from_redis;
	}

	public static void setRead_from_redis(List<String> read_from_redis) {
		ConfigContainer.read_from_redis = read_from_redis;
	}
	
	public static int getRead_from_redis_limit() {
		return read_from_redis_limit;
	}

	public static void setRead_from_redis_limit(int read_from_redis_limit) {
		ConfigContainer.read_from_redis_limit = read_from_redis_limit;
	}

	public static int getAnalyseThread() {
		return analyseThread;
	}

	public static void setAnalyseThread(int analyseThread) {
		ConfigContainer.analyseThread = analyseThread;
	}

	public static int getAnalyse_limt() {
		return analyse_limt;
	}

	public static void setAnalyse_limt(int analyse_limt) {
		ConfigContainer.analyse_limt = analyse_limt;
	}
		
	public static String getAnalyseEcodeService_key() {
		return analyseEcodeService_key;
	}

	public static void setAnalyseEcodeService_key(String analyseEcodeService_key) {
		ConfigContainer.analyseEcodeService_key = analyseEcodeService_key;
	}

	public static String getError_key() {
		return error_key;
	}

	public static void setError_key(String error_key) {
		ConfigContainer.error_key = error_key;
	}

	public static String getAnalyseErrorLog_key() {
		return analyseErrorLog_key;
	}

	public static void setAnalyseErrorLog_key(String analyseErrorLog_key) {
		ConfigContainer.analyseErrorLog_key = analyseErrorLog_key;
	}

	public static List<String> getRead_from_redis_error() {
		return read_from_redis_error;
	}

	public static void setRead_from_redis_error(List<String> read_from_redis_error) {
		ConfigContainer.read_from_redis_error = read_from_redis_error;
	}

	public static String[] getPackageList() {
		return packageList;
	}

	public static void setPackageList(String[] packageList) {
		ConfigContainer.packageList = packageList;
	}

	/**
	 * 内存添加运营商号段信息
	 * @param seg
	 * 			号段：号码的前3或者前4位，不考虑携号转网
	 * @param isp_id
	 * 			运营商 0:移动 1:联通 3:电信
	 * */
	public static void addMapSegment(String seg , String isp_id){
		mapSegment.put(seg, isp_id);
	}
	
	/**
	 * 根据号段获取运营商
	 * @param seg
	 * 			号段：号码的前3或者前4位，不考虑携号转网
	 * @return isp_id
	 * 			运营商 0:移动 1:联通 3:电信
	 * */
	public static String getSegment(String seg){
		return mapSegment.get(seg);
	}
	
	
	/**
	 * 内存添加省份号段信息
	 * @param seg
	 * 			号段：号码的前7位
	 * @param province
	 * 			省份
	 * */
	public static void addMapProvince(String seg , String province){
		mapProvince.put(seg, province);
	}
	
	
	/**
	 * 根据号段获取省份信息
	 * @param seg
	 * 			号段：号码的前7位
	 * @return province
	 * 			省份
	 * */
	public static String getProvince(String seg){
		return mapProvince.get(seg);
	}
	
	public static void main(String[] args){
    	 new ConfigContainer().load();
    }
}
