package com.navy.daemon.conf;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.navy.daemon.BootStart;
import com.navy.daemon.action.ActionContext;
import com.navy.daemon.action.WSClientFactory;
import com.navy.daemon.action.WSServerFactory;
import com.navy.daemon.entity.ClusterConfig;
import com.navy.daemon.security.SHAI;
import com.navy.daemon.util.StringUtils;
/**
 * 配置同步类，将插件包配置、环境同步到数据库；在管控中心不能连通情况下将告警等数据缓存到数据库
 * @author mup
 *
 */
public class SynConfig {
	/**
	 * 受管应用
	 * key:   应用主进程名
	 * value: 应用状态
	 */
	public static ConcurrentHashMap<String, ClusterConfig> apps = BootStart.apps;

	/**
	 * 加载已安装应用
	 * 
	 */
	public static void loadApp(){
		Connection con =null;
		PreparedStatement sta = null;
		ResultSet rs = null;
		try {
			con = BootStart.getBoot().getConnection();
			if(null == con) return;
			sta = con.prepareStatement("select * from conf_appinfo");
			rs = sta.executeQuery();
			ClusterConfig app = null;
			if(rs.next()) {
				app = new ClusterConfig();
				app.setAppName( rs.getString("appname") );
				app.setIp( rs.getString("ip") );
				app.setActionPath( rs.getString("path") );
				app.setProcessName( rs.getString("processName") );
				String ftppath = rs.getString("packgefilepath");
				String filename = null;
				int indxfilename = null != ftppath ? ftppath.lastIndexOf("/") : -1;
				if(indxfilename > -1){
					filename = ftppath.substring(indxfilename, ftppath.length());
					ftppath = ftppath.substring(0, indxfilename-1);
				}
				app.setFtpPath(ftppath);
				app.setFtpFileName(filename);
				apps.put(app.getAppName(), app);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{ if(null != rs) rs.close(); }catch(Exception e){e.printStackTrace();}
			try{ if(null != sta) sta.close(); }catch(Exception e){e.printStackTrace();}
			try{ if(null != con) con.close(); }catch(Exception e){e.printStackTrace();}
			rs = null;
			sta = null;
			con = null;
		}
	}
	
	/**
	 * 保存受管应用变更（更新主要是软件进程名发生变化）
	 * @param app 受管应用信息
	 * @return
	 */
	public static boolean saveApp(ClusterConfig app){
		if(null == app || null == app.getAppName() || null == app.getProcessName()) return false;
		Connection con =null;
		PreparedStatement sta = null;
		ResultSet rs = null;
		boolean flag = false;
		StringBuffer sqlbuf = new StringBuffer();
		try{
			con = BootStart.getBoot().getConnection();
			if(null == con) return false;
			System.out.println("save app:......."+app.getAppName());
			ClusterConfig app0 = apps.get(app.getAppName());
			
			
			//存在原配置，则更新配置
			if(null == app0 || !app.getProcessName().equals(app0.getProcessName()) ){
				//不存在插入配置
				sqlbuf.append("insert into conf_appinfo (")
				.append("appname, ")
				.append("ip, ")
				.append("path, ")
				.append("processName, ")
				.append("packgefilepath")
				.append(") values(")
				
				.append("?,")
				.append("?,")
				.append("?,")
				.append("?,")
				.append("?)");
				
				System.out.println(sqlbuf.toString());
				sta = con.prepareStatement(sqlbuf.toString());
				sqlbuf.delete(0, sqlbuf.length());
				
				int i = 1;
				sta.setObject(i++, app.getAppName());
				sta.setObject(i++, app.getIp());
				sta.setObject(i++, app.getActionPath());
				sta.setObject(i++, app.getProcessName());
				String ftppath =app.getFtpPath();
				if(!app.getFtpPath().endsWith("/") && !app.getFtpFileName().startsWith("/")){
					ftppath = ftppath + "/";
				}
				sta.setObject(i++, ftppath+app.getFtpFileName());
				
				
				flag = sta.executeUpdate() > 0;
				if(flag) apps.put(app.getAppName(), app);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			sqlbuf.delete(0, sqlbuf.length());
			sqlbuf = null;
			try{ if(null != rs) rs.close(); }catch(Exception e){e.printStackTrace();}
			try{ if(null != sta) sta.close(); }catch(Exception e){e.printStackTrace();}
			try{ if(null != con) con.close(); }catch(Exception e){e.printStackTrace();}
			rs = null;
			sta = null;
			con = null;
		}
		return flag;
	}
	
	
	/**
	 * 查询Action配置（包括插件）,并加载插件包，发布插件服务
	 * @param wsdlLocation 插件的发布路径，当该参数为null时查询所有
	 * @return 未找到或者数据库连接错误返回null
	 */
	public static List<ActionContext> initActionSet(){
		Connection con =null;
		Statement sta = null;
		ResultSet rs = null;
		List<ActionContext> acts = null; 
		StringBuffer sqlbuf = new StringBuffer();
		try {
			con = BootStart.getBoot().getConnection();
			if(null == con) return null;
			sta = con.createStatement();
			rs = sta.executeQuery("select * from conf_actiondefine");
			ActionContext context = null;
			ArrayList<ActionContext> actionSets = new ArrayList<ActionContext>();
			while(rs.next()){
				context = new ActionContext();
				context.wsid = rs.getString("wsid");
				context.pluginName = rs.getString("pluginname");
				context.wsdlLocation = rs.getString("wsdlLocation");
				context.endpointInterface = rs.getString("endpointInterface");
				context.nameSpace = rs.getString("nameSpace");
				context.serviceName = rs.getString("serviceName");
				context.portName = rs.getString("portName");
				context.pluginJarPath = rs.getString("pluginjarpath");
				context.implementorpath = rs.getString("implementor");
				context.isAgent = rs.getInt("isagent");
				context.reporter_wsdlLocation = rs.getString("reporter");
				context.reportMethod = rs.getString("reportMethod");
				context.IP = rs.getString("ip");
				context.port = rs.getInt("port");
				context.wsprotocal = rs.getString("wsprotocal");
				
				System.out.println("load from database:"+context.wsdlLocation );
				//標記為初始化
				context.option = -2;
				actionSets.add(context);
			}
			rs.close();
			sta.close();
			con.close();
			//兼容sqlplit(不能同时打开两个sqllit)
			for(ActionContext context0 : actionSets){
				System.out.println("load:"+context0.wsdlLocation );
				
				//判斷本服務是否在本端（即發佈服務的標記是否和守護進程標準一致）
				//1表示代理，0表示管控中心，-1表示同时发布注册客户端和服务端
				if(-1 != context0.isAgent && BootStart.getBoot().getIsagent() != context0.isAgent){
					try {
						URL jarurl = null;
						//发布为客户端对象
						if(StringUtils.isNotBlank(context0.pluginJarPath)){
							File plug = BootStart.getBoot().getPluginlib();
							String jarpath = plug.getAbsolutePath();
							plug = null;
							if(context0.pluginJarPath.startsWith("/") 
									|| context0.pluginJarPath.startsWith("\\")){
								jarpath = jarpath +File.separator+context0.pluginJarPath.substring(1, context0.pluginJarPath.length());
							}else{
								jarpath = jarpath +File.separator+context0.pluginJarPath;
							}
							File jar = new File(jarpath);
							System.out.println("jar path:"+jar.getAbsolutePath());
							if(jar.exists()){
								jarurl = jar.toURL();
							}
						}
						
						WSClientFactory.publishClient(context0, jarurl, context0.pluginName,
								context0.endpointInterface, -2, context0.wsdlLocation);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}else{
					try {
						URL jarurl = null;
						if(StringUtils.isNotBlank(context0.pluginJarPath)){
							File plug = BootStart.getBoot().getPluginlib();
							String jarpath = plug.getAbsolutePath();
							plug = null;
							if(context0.pluginJarPath.startsWith("/") 
									|| context0.pluginJarPath.startsWith("\\")){
								jarpath = jarpath + File.separator +context0.pluginJarPath.substring(1, context0.pluginJarPath.length());
							}else{
								jarpath = jarpath + File.separator +context0.pluginJarPath;
							}
							File jar = new File(jarpath);
							System.out.println("jar path:"+jar.getAbsolutePath());
							if(jar.exists()){
								jarurl =jar.toURL();
							}
						}
						
						if(-1 == context0.isAgent || BootStart.getBoot().getIsagent() == context0.isAgent){
							/**根据wsid查询出WsdlLocation，
							 *ActionContext的getReporter方法可依据WsdlLocation得到对应report的服务端对象
							*/
							if(null != context0.reporter_wsdlLocation){
								String reporterWsdlLocation = searchReport(context0.reporter_wsdlLocation);
								System.out.println("reporterWsdlLocation:"+reporterWsdlLocation);
								//reporterstr查询出对应的repoter服务的wsdlLocation
								context0.reporter_wsdlLocation = reporterWsdlLocation;
							}
							
							WSServerFactory.publish(context0, jarurl, context0.pluginName,
									context0.implementorpath, -2, context0.wsdlLocation, 
									context0.reporter_wsdlLocation, context0.reportMethod);
						}
						
						//表示即發佈客戶端也發佈客戶端
						if(-1 == context0.isAgent){
							WSClientFactory.publishClient(context0, jarurl, context0.pluginName,
									context0.endpointInterface, -2, context0.wsdlLocation);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			sqlbuf.delete(0, sqlbuf.length());
			sqlbuf = null;
			try{ if(null != rs) rs.close(); }catch(Exception e){e.printStackTrace();}
			try{ if(null != sta) sta.close(); }catch(Exception e){e.printStackTrace();}
			try{ if(null != con) con.close(); }catch(Exception e){e.printStackTrace();}
			rs = null;
			sta = null;
			con = null;
		}
		return acts;
	}
	/**
	 * 保存插件配置信息
	 * @param context
	 * @return
	 */
	public static boolean saveActionSet(ActionContext context){
		if(null == context || null == context.wsdlLocation) return false;
		Connection con =null;
		PreparedStatement sta = null;
		ResultSet rs = null;
		boolean flag = false;
		StringBuffer sqlbuf = new StringBuffer();
		try{
			con = BootStart.getBoot().getConnection();
			if(null == con) return false;
			//System.out.println("wsdlLocation:......."+context.wsdlLocation);
			context.wsid = SHAI.doEncrypt_HexString_0(context.wsdlLocation);
			//System.out.println(context.wsid);
			String wsdlLocation = searchReport(context.wsid);
			//存在原配置，则更新配置
			if(null != wsdlLocation && -2 != context.option){
				sqlbuf.append("update conf_actiondefine ")
				.append(" set ")
				.append(" endpointInterface=?, ")
				.append(" implementor=?, ")
				.append(" isagent=?, ")
				.append(" nameSpace=?, ")
				.append(" pluginjarpath=?, ")
				.append(" pluginname=?, ");
				
				if(null != context.portName) sqlbuf.append(" portName=?, ");
				if(null != context.reporter_wsdlLocation) sqlbuf.append(" reporter=?, ");
				if(null != context.reportMethod) sqlbuf.append(" reportMethod=?, ");
				sqlbuf.append(" serviceName=?, ")
				.append(" ip=?, ")
				.append(" port=?, ")
				.append(" wsprotocal=?")
				
				
				.append(" where  ")
				.append(" wsid=?");
				
				System.out.println(sqlbuf.toString());
				sta = con.prepareStatement(sqlbuf.toString());
				sqlbuf.delete(0, sqlbuf.length());
				
				int i = 1;
				sta.setObject(i++, context.endpointInterface);
				sta.setObject(i++, null == context.implementorclz ? context.implementorpath : context.implementorclz.getName());
				sta.setObject(i++, context.isAgent);
				sta.setObject(i++, context.nameSpace);
				sta.setObject(i++, context.pluginJarPath);
				sta.setObject(i++, context.pluginName);
				if(null != context.portName) sta.setObject(i++, context.portName);
				if(null != context.reporter_wsdlLocation) sta.setObject(i++, SHAI.doEncrypt_HexString_0(context.reporter_wsdlLocation));
				if(null != context.reportMethod) sta.setObject(i++, context.reportMethod);
				sta.setObject(i++, context.serviceName);
				sta.setObject(i++, context.IP);
				sta.setObject(i++, context.port);
				sta.setObject(i++, context.wsprotocal);
				
				sta.setObject(i++, context.wsid);
				
				flag = sta.executeUpdate() > 0;
			}else if(null == wsdlLocation){
				//不存在插入配置
				sqlbuf.append("insert into conf_actiondefine (")
				.append("wsid, ")
				.append("wsdlLocation, ")
				.append("endpointInterface, ")
				.append("implementor, ")
				.append("isagent, ")
				.append("nameSpace, ")
				.append("pluginjarpath, ")
				.append("pluginname, ");
				if(null != context.portName) sqlbuf.append("portName, ");
				if(null != context.reporter_wsdlLocation) sqlbuf.append("reporter, ");
				if(null != context.reportMethod) sqlbuf.append("reportMethod, ");
				sqlbuf.append("serviceName, ")
				.append("ip, ")
				.append("port, ")
				.append("wsprotocal")
				
				
				.append(") values(")
				
				.append("?,")
				.append("?,")
				.append("?,")
				.append("?,")
				.append("?,")
				.append("?,")
				.append("?,")
				.append("?,");
				if(null != context.portName) sqlbuf.append("?,");
				if(null != context.reporter_wsdlLocation) sqlbuf.append("?,");
				if(null != context.reportMethod) sqlbuf.append("?,");
				sqlbuf.append("?,");
				sqlbuf.append("?,");
				sqlbuf.append("?,");
				sqlbuf.append("?)");
				
				System.out.println(sqlbuf.toString());
				sta = con.prepareStatement(sqlbuf.toString());
				sqlbuf.delete(0, sqlbuf.length());
				
				int i = 1;
				sta.setObject(i++, context.wsid);
				sta.setObject(i++, context.wsdlLocation);
				sta.setObject(i++, context.endpointInterface);
				sta.setObject(i++,  null == context.implementorclz ? context.implementorpath : context.implementorclz.getName());
				sta.setObject(i++, context.isAgent);
				sta.setObject(i++, context.nameSpace);
				sta.setObject(i++, context.pluginJarPath);
				sta.setObject(i++, context.pluginName);
				if(null != context.portName) sta.setObject(i++, context.portName);
				if(null != context.reporter_wsdlLocation) sta.setObject(i++, SHAI.doEncrypt_HexString_0(context.reporter_wsdlLocation));
				if(null != context.reportMethod) sta.setObject(i++, context.reportMethod);
				sta.setObject(i++, context.serviceName);
				sta.setObject(i++, context.IP);
				sta.setObject(i++, context.port);
				sta.setObject(i++, context.wsprotocal);
				
				
				flag = sta.executeUpdate() > 0;
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			sqlbuf.delete(0, sqlbuf.length());
			sqlbuf = null;
			try{ if(null != rs) rs.close(); }catch(Exception e){e.printStackTrace();}
			try{ if(null != sta) sta.close(); }catch(Exception e){e.printStackTrace();}
			try{ if(null != con) con.close(); }catch(Exception e){e.printStackTrace();}
			rs = null;
			sta = null;
			con = null;
		}
		return flag;
	}
	
	/**
	 * 依据wsid查询wsdlLocation发布路径
	 * @param wsid
	 * @return
	 */
	public static String searchReport(String wsid){
		Connection con =null;
		String wsdlLocation = null;
		PreparedStatement sta = null;
		ResultSet rs = null;
		try {
			con = BootStart.getBoot().getConnection();
			if(null == con) return null;
			sta = con.prepareStatement("select * from conf_actiondefine where wsid=?");
			sta.setObject(1, wsid);
			rs = sta.executeQuery();
			if(rs.next()) {
				wsdlLocation = rs.getString("wsdlLocation");
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{ if(null != rs) rs.close(); }catch(Exception e){e.printStackTrace();}
			try{ if(null != sta) sta.close(); }catch(Exception e){e.printStackTrace();}
			try{ if(null != con) con.close(); }catch(Exception e){e.printStackTrace();}
			rs = null;
			sta = null;
			con = null;
		}
		return wsdlLocation;
	}
	
	public static void main(String[] args){
		/*try {
			Connection con = BootStart.getConn_Sqlite(new File("F:\\Work\\服务总线\\设计与规范"));
			Statement sta = con.createStatement();
			ResultSet rs = sta.executeQuery("select * from conf_actiondefine");
			while(rs.next()){
				System.out.println(rs.getString(1));
			}
			rs.close();
			sta.close();
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		searchReport("a0e1cd5b65a262683f93270e204ae2a692a0ab3");
		System.out.println(
				SHAI.doEncrypt_HexString_0("http://192.168.3.109:6553/com.navy.daemon/appctrl/AppContrlService")
		);
		
	}
}
