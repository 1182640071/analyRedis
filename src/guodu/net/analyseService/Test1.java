package guodu.net.analyseService;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import guodu.net.impl.AnalyselmplEx;
import guodu.net.util.Loger;
import guodu.net.util.SegmentJudge;

public class Test1 extends AnalyselmplEx{
	private static Boolean flagInstance = false;
	
	private static Element root = null;
	private static String xml = "<root></root>";
	
	//创建线程，将拼接好的xml串写入redis中，供使用者分析
	static{
		new Thread(){
			public void run(){
				Loger.Info_log.info("[INFO]test1将拼接好的xml串写入redis线程启动，具体实现看业务需求");
			}
		}.start();
	}
	
	private static Test1 singleton = null;  
      
    // 同步的获取实例方法  
    public static synchronized Test1 getInstance(){  
        if(null == singleton){  
            singleton = new Test1();  
        }  
        return singleton;  
    }  
	
	
	// 构造方法私有 
    private Test1(){  
    	try {
			StackTraceElement stack[] = Thread.currentThread().getStackTrace();
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
			Loger.Info_log.info("[ERROR]Test1实例话错误" , e);
			
			if(!flagInstance)  
		    {  
				flagInstance = !flagInstance;  
		    }
		} 
    }  

	
	/**
	 * 拼接xml串，统计日志信息
	 * @param xml
	 * 			当前xml串
	 * @param ecode
	 * 			提交状态
	 * @param province
	 * 			省份信息
	 * @param isp_id
	 * 			运营商
	 * */
	  private static void getXML(String xml , String ecode , String provinceEx , String isp_id) {
		  
		  String province = "";
		  
		  if("0".equals(ecode)){
			  province = provinceEx + "_suc";
		  }else{
			  province = provinceEx + "_fail";
		  }
		  
		    try {
		    	
		      SAXReader saxReader = new SAXReader();
		      Document document = saxReader.read(new ByteArrayInputStream(xml.getBytes("utf-8")));		      
		      root = document.getRootElement();  
		      		      
		      Iterator<?> iterator = root.elementIterator();//取得根元素
		      
		      Element eStudent = null;
		      Element eStudentEX = null;
		      Boolean flag = true;
		      
		      while(iterator.hasNext() && flag){
		    	  
		    	  eStudent = (Element) iterator.next();
		    	  
		    	  if(eStudent.getName() == isp_id || eStudent.getName().equals(isp_id)){
		    		  Iterator<?> iteratorEx = eStudent.elementIterator();
		    		  
		    		  while(iteratorEx.hasNext() && flag){
		    			  
		    			  eStudentEX = (Element)iteratorEx.next();
		    			  
		    			  if(eStudentEX.getName().equals(province)){
		    				  eStudentEX.setText(String.valueOf(Integer.parseInt(eStudentEX.getText()) + 1));
		    				  flag = false;
		    			  }
		    			  
		    		  }
		    		  if(flag){
		    			  Element e = DocumentHelper.createElement(province);
		    			  e.setText("1");
		    			  eStudent.add(e);
		    			  flag = false;
		    		  }
		    	  }   	  
		      }
		      if(flag){
		    	  Element e = DocumentHelper.createElement(isp_id);
		    	  root.add(e);
		    	  getXML(root.asXML() , ecode , provinceEx , isp_id );
		      }
		    } catch (Exception e) {
		      e.printStackTrace();
		      return;
		    }
		    return;
		  }
	  
	  
		@Override
		public void analyse(Object list) {
			System.out.println("Test1 start ...");
//			getInstance().analyseEx(list);
		}
		
		@SuppressWarnings("unchecked")
		private void analyseEx(Object list) {
			List<String> information = (ArrayList<String>) list;
			for(String im : information){
				String ecode = im.split("\\|!")[24];
				String isp_id = SegmentJudge.getIsp_id(im.split("\\|!")[22]);
				String province = SegmentJudge.getProvince(im.split("\\|!")[22]);
				synchronized (this) {
					try {
						getXML(xml , ecode , province , isp_id);
						xml = root.asXML();
						System.out.println(xml);
					} catch (Exception e) {
						e.printStackTrace();
						Loger.Info_log.info("[ERROR]xml信息拼接错误" , e);
					}
				}
			}
		}

}
