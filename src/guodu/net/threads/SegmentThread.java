package guodu.net.threads;

import java.util.List;
import guodu.net.db.OperDb;
import guodu.net.util.ConfigContainer;
import guodu.net.util.Loger;

/**
 * 号段表加载线程
 * 将省份，运营商号段表信息加载到redis中
 * */
public class SegmentThread extends Thread{
	public void run(){
		while(true){
			try {
				
				sleep(1000*60*30);
				
				//加载运营商号段表
				loadSegment();
				
				//加载省份号段表
				loadProvince();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
				Loger.Info_log.info("[ERROR]号段表加载错误 " + e);
			}
		
		}
	}
	
	//加载运营商号段表
	public static void loadSegment(){
		String sql = "select t.seg , t.gwkind  from ccb_ctrl_segment t where length(seg) in (3,4)";//abc_segment ccb_ctrl_segment
		List<Object[]> listsegment = OperDb.selectDb(sql);
		String seg = null , isp_id = null;
		if(null != listsegment && !"".equals(listsegment)){
			for(Object[] o : listsegment){
				try {
					seg = o[0].toString();
					isp_id = o[1].toString();
					ConfigContainer.addMapSegment(seg, isp_id);
				} catch (Exception e) {
					e.printStackTrace();
					Loger.Info_log.info("[ERROR]redis数据插入错误，插入信息" + seg + "  " + isp_id , e);
				}
			}
		}
		Loger.Info_log.info("[INFO]号段表信息加载完毕");
	}
	
	//加载省份号段表
	public static void loadProvince(){
		String sql = "select t.seg , t.province from full_cityname t";
		List<Object[]> listsegment = OperDb.selectDb(sql);
		String seg = null , province = null;
		if(null != listsegment && !listsegment.isEmpty()){
			for(Object[] o : listsegment){
				try {
					seg = o[0].toString();
					province = o[1].toString();
					ConfigContainer.addMapProvince(seg, province);
				} catch (Exception e) {
					e.printStackTrace();
					Loger.Info_log.info("[ERROR]redis数据插入错误，插入信息" + seg + "  " + province , e);
				}
			}
		}
		Loger.Info_log.info("[INFO]省份表信息加载完毕");
	}
	
}
