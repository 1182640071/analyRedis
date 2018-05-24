package guodu.net.impl.interf;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import guodu.net.impl.RedisInterface;
import guodu.net.util.ConfigContainer;
import guodu.net.util.Loger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class RedisImpl implements RedisInterface{
	
	ConfigContainer pool = ConfigContainer.getInstance();
	
	/**
	 * 从redis中取数据方法具体实现
	 * @param listname
	 * 				redis队列的key
	 * @param count	
	 * 				每次从redis中取数据的量
	 * @return List<String>
	 * 				从redis中取出的数据集合
	 * */
	@Override
	public List<String> getCommonByteToList(String listname, int count) {
		Jedis jedis = pool.getCommonJedis();
		try {
			List<String> list = new ArrayList<String>();
			String log = "";
			if (jedis != null) {
				String b = "";
				for (int i = 0; i < count; i++) {
					try {
						log = jedis.rpop(listname);
						if("" != log && !"".equals(log) && null != log){
							b = new String(log.getBytes("ISO-8859-2") , "gbk");
						}else{
							continue;
						}
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						Loger.Info_log.info("[ERROR]redis信息取出编码格式转换有误 "+e);
					}
					if (b != null && !"".equals(b) && "" != b) {
						list.add(b); 
					}
					else
					{
						break;
					}
				}
				if (null != list && list.size() != 0) {
					return list;
				} else {
					return null;
				}
			}
			Loger.Info_log.info("[INFO]网关连接池内未取到连接,无法取得队列数据");
			return null;
		} finally {
			pool.common_close(jedis);
		}
	}
	
	
	
	/**
	 * 将string信息转为byte方法存入redis中
	 * 
	 * @param msg
	 * 			待插入的string信息
	 * @param key
	 * 			redis的key
	 * @return int
	 * 			1:成功插入 0:插入失败
	 * */
	@Override
	public int addCommonByteToList(String msg , String key) {
		Jedis jedis=pool.getCommonJedis();
		try{		
			if (jedis!=null)
			{
				Pipeline p = jedis.pipelined();
				p.lpush(key.getBytes(),msg.getBytes());
				p.sync();
				return 1;
			}
			Loger.Info_log.info("[ERROR]写入redis连接池内未取到连接,无法加入队列数据");
			return 0;
		}catch(Exception e) {
			e.printStackTrace();
			Loger.Info_log.info("[ERROR]写入redis错误，无法加入队列数据" , e);
			return 0;
		}finally{
			pool.common_close(jedis);
		}
	}
	
	
	@Override
	public void addSegmentHash(String listname, String field, String values) {
		Jedis jedis = pool.getCommonJedis();
		try {
			if (jedis != null) {
				try {
					jedis.hset(listname, field, values);
				} catch (Exception e) {
					e.printStackTrace();
					Loger.Info_log.info("[ERROR]redis数据插入错误" , e);
				}
				return ;
			}
			Loger.Info_log.info("[ERROR]网关连接池内未取到连接,无法加入hash");
			return;
		} finally {
			pool.common_close(jedis);
		}
	}

}
