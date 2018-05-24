package guodu.net.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import guodu.net.util.Loger;

public class OperDb {	
	/**
	 * 获取数据库表信息
	 * @param sql
	 * 			sql语句
	 * @return List<Object[]>
	 * 			查询结果
	 * */
    public static List<Object[]> selectDb(String sql){
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		List<Object[]> result = new ArrayList<Object[]>();
		List<Object> list = new ArrayList<Object>();
		DBManagerConnection_Main DBManagerConnection = DBManagerConnection_Main.getInstance();
		try {
			conn = DBManagerConnection.getConnection();
		    if (null == conn) {
			    Loger.Info_log.info("[ERROR]数据库连接失败");
				return null;
			}	
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			rsmd = rs.getMetaData();
			while(rs.next()){
		    	for(int i = 1 ; i <= rsmd.getColumnCount(); i++){
		    		Object a = new Object();
		    		a = rs.getObject(i);
		    		if(a == null|| "".equals(a)){
		    			list.add((Object)"");
		    		}else{
				    	list.add(a);	
		    		}
		    	}
			    if(list == null || list.size() == 0){
			    	result = null;
			    }else{
			    	result.add(list.toArray());	
			    	list.clear();
			    }
		    }
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBManagerConnection_Main.closekind(rs , st , conn);
		}		
		return result;
    } 
    
    
    public static void main(String[] arg){
//    	fetchInfo("GD_WX_INFOMATION" , "50");
    }
}
