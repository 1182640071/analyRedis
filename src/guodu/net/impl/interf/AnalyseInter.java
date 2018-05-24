package guodu.net.impl.interf;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import guodu.net.impl.AnalyseImpl;
import guodu.net.impl.AnalyselmplEx;
import guodu.net.util.ConfigContainer;
import guodu.net.util.Loger;

public class AnalyseInter extends AnalyselmplEx{
	private String type = "";
	public AnalyseInter(String type){
		this.type = type;
	}
	@SuppressWarnings("rawtypes")
	public void analyse(final Object clazz){
		for(int i = 0 ; i < 5 ; i++){  	//ConfigContainer.getAnalyseThread()
			
			new Thread(){
				
				@SuppressWarnings("unchecked")
				public void run(){
					
					while(true){
						if("guodu.net.analyseService".equals(type)){
							if(ConfigContainer.getRead_from_redis().isEmpty()){
								try {
									sleep(100);
								} catch (Exception e) {
									e.printStackTrace();
								}
								continue;
							}
						}else if("guodu.net.analyseError".equals(type)){
							if(ConfigContainer.getRead_from_redis_error().isEmpty()){
								try {
									sleep(100);
								} catch (Exception e) {
									e.printStackTrace();
								}
								continue;
							}
						}
						
						//获取数据
						List<String> listInfo = ConfigContainer.getListInformation(ConfigContainer.getAnalyse_limt() , type);
						if(null == listInfo || listInfo.isEmpty()){
							try {
								sleep(100);
							} catch (Exception e) {
								e.printStackTrace();
							}
							continue;
						}
						
						//将package包中所有类执行
						for(Object c : (List<Object>)clazz){
//							System.out.println(type + "   " + ((Class) c).getName());
							try {
								Class<?> cz = Class.forName(((Class) c).getName());
								Constructor<?> cx = cz.getDeclaredConstructor();  
						        cx.setAccessible(true);  
								AnalyseImpl ail = (AnalyseImpl)cx.newInstance();
								ail.analyse(listInfo);
							} catch (InstantiationException e) {
								e.printStackTrace();
								Loger.Info_log.info("[ERROR]" , e);
							} catch (IllegalAccessException e) {
								e.printStackTrace();
								Loger.Info_log.info("[ERROR]" , e);
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
								Loger.Info_log.info("[ERROR]" , e);
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
								Loger.Info_log.info("[ERROR]" , e);
							} catch (InvocationTargetException e) {
								e.printStackTrace();
								Loger.Info_log.info("[ERROR]" , e);
							} catch (SecurityException e) {
								e.printStackTrace();
								Loger.Info_log.info("[ERROR]" , e);
							} catch (NoSuchMethodException e) {
								e.printStackTrace();
								Loger.Info_log.info("[ERROR]" , e);
							}
						}
						
						try {
							sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
		}
	}
}
