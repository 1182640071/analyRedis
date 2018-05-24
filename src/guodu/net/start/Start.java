package guodu.net.start;

import guodu.net.impl.AnalyseImpl;
import guodu.net.impl.interf.AnalyseInter;
import guodu.net.threads.GetErrorLogFromRedis;
import guodu.net.threads.GetMessageFomRedis;
import guodu.net.threads.SegmentThread;
import guodu.net.util.ConfigContainer;
import guodu.net.util.Loger;

/**
 *@author wml 20161011
 *
 * 此程序需要配合日志监控的python脚本一起使用
 * 用于将存入redis中的日志信息取出并分析，然后将分析结果插入redis中供其他接口使用
 * 
 * 此程序控制部分采用适配器加代理模式完成所以使用此程序只需要在guodu.net.analyse此package中
 * 添加AnalyselmplEx抽象类的实现类，实现analyse方法即可
 * 
 * AnalyseEcodeService.java guodu.net.analyse.Test1.java均为参考demo，供使用者参考
 * */
public class Start {
	static{
		Loger.Info_log.info("[INFO]start....");
	}

	public static void main(String[] args){
		//加载运营商号段表
		SegmentThread.loadSegment();
		
		//加载省份号段表
		SegmentThread.loadProvince();
		
		//加载配置文件
		ConfigContainer.getInstance().load();
		
		//启动读取redis信息线程
		for(int i = 0 ; i < ConfigContainer.getRead_thread_count() ; i++){
			new GetMessageFomRedis().start();
		}
		
		//启动读取error日志redis信息线程
		for(int i = 0 ; i < ConfigContainer.getRead_thread_count() ; i++){
			new GetErrorLogFromRedis().start();
		}
		
		//启动号段表信息加载线程
		new SegmentThread().start();
		
		//分析数据
		AnalyseImpl ai = null;
		for(String packageName : ConfigContainer.getPackageList()){
			ai = new AgentImpl(new AnalyseInter(packageName.trim()) , packageName.trim());
			ai.analyse();
		}
	}	
}
