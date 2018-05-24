package guodu.net.impl;

import java.util.List;

public interface RedisInterface {
	//从redis队列中读取数据
	public List<String> getCommonByteToList(String listname, int count);
	
	//向redis中写入数据，以hash方式存储
	public void addSegmentHash(String listname, String field, String values);
	
	//向redis中写入json数据字符串
	public int addCommonByteToList(String msg , String key);
}