package com.navy.daemon.action.cmd.confset;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

import com.navy.daemon.BootStart;
import com.navy.daemon.action.ActionClassloader;
import com.navy.daemon.action.ActionContext;
import com.navy.daemon.action.WSServerFactory;
import com.navy.daemon.util.FileOperate;


public class PluginUpdate implements Callable<Object>{
	private ActionContext serverContext;
	private String pluginname;
	private String pluginImplclasspath;
	private String jarFtpPath;
    private int option;
    private String wsdlLocation;
    private String report_wsdlLocation;
    private String report_method;
	
	public PluginUpdate(ActionContext context, String pluginname,
			String pluginImplclasspath, String jarFtpPath, int option, String wsdlLocation, 
			String report_wsdlLocation, String report_method){
		
		this.wsdlLocation = wsdlLocation;
		this.serverContext = context;
		this.pluginname = pluginname;
		this.pluginImplclasspath = pluginImplclasspath;
		this.jarFtpPath = jarFtpPath;
		this.option = option;
		
		this.report_wsdlLocation = report_wsdlLocation;
		this.report_method = report_method;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public Object call(){
		StringBuffer buf = new StringBuffer();
		/**
		 * 更新和安装插件需要下载软件
		 */
		if(0 == option || 1 == option ){
			File plugin = null;
			buf.append(BootStart.getBoot().getDownloadTemp().getAbsolutePath())
				.append(File.separator).append(pluginname).append(".jar");
			File localFile = new File(buf.toString());
			buf.delete(0, buf.length());
			try {
				BootStart.getBoot().getFc().getClient().download(jarFtpPath, localFile);
			} catch (Exception e) {
				//状态上报-插件包下载
				serverContext.report(null);
				e.printStackTrace();
			}
			
			buf.append(BootStart.getBoot().getPluginlib().getAbsolutePath())
			.append(File.separator).append(pluginname).append(".jar");
			
			plugin = new File(buf.toString());
			buf.delete(0, buf.length());
			
			ActionContext acontext = null;
			//更新先停止原来的插件
			if(1 == option){
				acontext = WSServerFactory.getContext(wsdlLocation);
				WSServerFactory.destroy(wsdlLocation);
				//卸载插件包
				try {
					ActionClassloader.unloadJars(plugin.toURL());
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
			
			//拷贝加载jar包
			if(FileOperate.copyGeneralFile(localFile.getAbsolutePath(), plugin.getAbsolutePath())){
				
				try {
					WSServerFactory.publish(acontext, plugin.toURL(), pluginname,
							pluginImplclasspath, option, wsdlLocation, report_wsdlLocation, report_method);
				} catch (Exception e) {
					e.printStackTrace();
					//状态上报-加载问题
					serverContext.report(e.getMessage());
				}
			}
		}else if(-1 == option){
			WSServerFactory.destroy(wsdlLocation);
			
			ActionContext acontext = WSServerFactory.getContext(wsdlLocation);
			File plug = BootStart.getBoot().getPluginlib();
			String jarpath = plug.getAbsolutePath();
			plug = null;
			if(acontext.pluginJarPath.startsWith("/") 
					|| acontext.pluginJarPath.startsWith("\\")){
				jarpath = jarpath +File.separator+acontext.pluginJarPath.substring(1, acontext.pluginJarPath.length());
			}else{
				jarpath = jarpath +File.separator+acontext.pluginJarPath;
			}
			File jar = new File(jarpath);
			System.out.println("jar path:"+jar.getAbsolutePath());
			if(jar.exists()){
				//卸载插件包
				try {
					ActionClassloader.unloadJars(jar.toURL());
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
			
		}
		return null;
	}
	
	
}
