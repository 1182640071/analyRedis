package guodu.net.threads;

import java.util.List;

import guodu.net.impl.RedisInterface;
import guodu.net.impl.interf.RedisImpl;
import guodu.net.util.ConfigContainer;
import guodu.net.util.Loger;

/**
 * 从redis中取出需要分析的数据存入内存队列中
 * */
public class GetMessageFomRedis extends Thread{
	
	public void run(){
		
		Loger.Info_log.info("[INFO]redis读取队列启动。。。");
		List<String> lists = null;
		
		while (true) {
			
			try{	//判断当前内存队列是否大于规定内存最大队列数
				if (ConfigContainer.getRead_from_redis_length() > ConfigContainer.getRead_from_redis_limit()) {
					try {
						sleep(100);
					} catch (Exception e) {
						e.printStackTrace();
					}
					continue;
				}
				RedisInterface redis = new RedisImpl();
				//取数据
				lists = redis.getCommonByteToList(ConfigContainer.getKey(),ConfigContainer.getAnalyse_limt());

				//将取出的合法数据存入内存队列中
				if(null == lists || lists.isEmpty()) {
					sleep(100);
					continue;
				}else{
					for(String loginfo : lists){
						if(null != loginfo && !"".equals(loginfo)){
							ConfigContainer.add_to_read_from_redis(loginfo);
						}
					}
				}
				
				sleep(100);
				
			}catch (Exception e) {
				e.printStackTrace();
				Loger.Info_log.info("[ERROR]从redis中读取日志信息出错" , e);
			}
		}
	}
}
