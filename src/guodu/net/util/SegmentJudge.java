package guodu.net.util;

public class SegmentJudge {
	/**
	 * 获取运营商
	 * @param mobile
	 * 			手机号码
	 * @return isp_id
	 * 			运营商
	 * */
	public static String getIsp_id(String mobile){
		String isp_id = "9";
		if("".equals(mobile) || null == mobile || mobile.length() < 11 ){
			return isp_id;
		}
		if(mobile.startsWith("170")){
			isp_id = ConfigContainer.getSegment(mobile.substring(0, 4));
		}else{
			isp_id = ConfigContainer.getSegment(mobile.substring(0, 3));
		}
		if("9".equals(isp_id)){
			isp_id = "unknow";
		}else if("0".equals(isp_id)){
			isp_id = "cmpp";
		}else if("1".equals(isp_id)){
			isp_id = "sgip";
		}else if("2".equals(isp_id)){
			isp_id = "smgp";
		}
		return isp_id;
	}
	
	
	/**
	 * 获取省份
	 * @param mobile
	 * 			手机号码
	 * @return province
	 * 			省份
	 * */
	public static String getProvince(String mobile){
		String province = "未知";
		if("".equals(mobile) || null == mobile || mobile.length() < 11 ){
			return province;
		}
		province = ConfigContainer.getProvince(mobile.substring(0, 7));
		if(null == province || "".equals(province)){
			province = "未知";
		}
		return province;
	}
}
