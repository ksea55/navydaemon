package com.navy.daemon.action;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import com.navy.daemon.action.report.IBaseRepoter;


/**
 * 事件构建父类,定义统一的请求方式，插件注册信息
 * <p>
 * <li>提供事件命令解析，按url调用服务组件callAction</li>
 * <li>提供服务组件服务接口获取方法getService</li>
 * <li>提供当前插件发布信息查询接口ActionContext</li>
 * </p>
 * @author mup
 *
 */
public abstract class AbstractAction extends JWS implements IAction{
	/**
	 * 该命令对象的上下文
	 */
	protected ActionContext context;
	
	/**
	 * 获取服务实例
	 * @param namespace
	 * @return 服务实例，按服务接口方式返回
	 */
	public static final <T extends IAction> T getService(URL wsdlurl){
		T action = null;
		try {
			action = WSClientFactory.<T>getWsClient(wsdlurl.toString());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return action;
	}
	
	/**
	 * 释放客户端对象
	 */
	public final void closeClient(){
		WSClientFactory.close(this);
	}
	
	/**
	 * 关闭服务
	 */
	public final void closeServer(){
		WSServerFactory.destroy(this.getWsdlLocation());
	}
	
	/**
	 * 
	 * @param <T>
	 * @param request 请求url，格式http://IP+PORT/项目域名/模块名/模块服务类名!方法名
	 * @param param 
	 * @return
	 */
	public static final <T> T callAction(URL request, Object...param) throws IllegalArgumentException{
		T result = null;
		request.toString();
		String urlstr = request.toString();
		String methodname = null;
		String wsdl = null;
		int methodindx = urlstr.lastIndexOf("!");
		if( methodindx > -1 && methodindx < urlstr.length()-1){
			wsdl = urlstr.substring(0, methodindx)+"?wsdl";
			methodname = urlstr.substring(methodindx+1, urlstr.length());
			//基于jws客户端远程调用
			try {
				IAction action = WSClientFactory.getWsClient(wsdl);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}else{
			throw new IllegalArgumentException("request url not found the request method, the url must match to 'http://IP+PORT/项目域名/模块名/模块服务类名!方法名?参数'");
		}
		System.out.println(wsdl);
		System.out.println(methodname);
		return result;
	}
	/**
	 * 用户可以选择为该插件服务组件注册报告
	 */
	public void addActionListener(Class<IReporter> reporter_clz){
		
	}
	/**
	 * 获取报告对象
	 * @return
	 */
	public IReporter getIReporter(){
		return context.getReporter();
	}
	/**
	 * 状态上报
	 * 当当前服务注册有report时该方法调用有效
	 * @param reports 报告数据对象
	 */
	public void report(Object...reports){
		context.report(reports);
	}

	public ActionContext getContext() {
		return context;
	}
	
	/**
	 * 设置报告启动方式
	 * 设置对应报告定时执行规则，该规则只在管控代理启动期间有效，管控代理掉线重启后恢复为默认规则
	 * @param ip 报告所在服务器ip
	 * @param port 报告所在服务器发布端口
	 * @param reporterId 报告服务发布接口实现类（同class.getName()）
	 * @param initialDelay 初始延时时间
	 * @param period 定时执行间隔时间
	 * @param unit TimeUnit类型时间单位
	 * @return
	 */
	public static boolean resetReport(String ip, int port, String reporterId, 
			long initialDelay, long period, TimeUnit unit){
		
		boolean flag = false;
		
		IBaseRepoter repotermanage = getreportmanage_server(ip, port);
		if(null == repotermanage) return false;
		
		try{
			//先关闭报告定时器
			flag = repotermanage.stopTheReport(reporterId);
			//设置报告定时运行规则
			if(flag) repotermanage.setQuartzCron(reporterId, initialDelay, period, unit);
			//启动报告定时器
			if(flag) flag = repotermanage.startTheRuner(reporterId);
		}catch(Exception e){
			e.printStackTrace();
		}
		return flag;
	}
	
	/**
	 * 启动指定的报告
	 * @param ip 报告所在服务器ip
	 * @param port 报告所在服务器发布端口
	 * @param reporterId 报告服务发布接口实现类（同class.getName()）
	 * @return
	 */
	public static boolean startReport(String ip, int port, String reporterId){
		boolean flag = false;
		
		IBaseRepoter repotermanage = getreportmanage_server(ip, port);
		if(null == repotermanage) return false;
		
		try{
			flag = repotermanage.startTheRuner(reporterId);
		}catch(Exception e){
			e.printStackTrace();
		}
		return flag;
	}
	
	/**
	 * 停止指定的报告
	 * @param ip 报告所在服务器ip
	 * @param port 报告所在服务器发布端口
	 * @param reporterId 报告服务发布接口实现类（同class.getName()）
	 * @return
	 */
	public static boolean stopReport(String ip, int port, String reporterId){
		boolean flag = false;
		
		IBaseRepoter repotermanage = getreportmanage_server(ip, port);
		if(null == repotermanage) return false;
		
		try{
			flag = repotermanage.stopTheReport(reporterId);
		}catch(Exception e){
			e.printStackTrace();
		}
		return flag;
	}
	
	/**
	 * 获取报告服务管理对象
	 * @param ip 报告所在服务器ip
	 * @param port 报告所在服务器发布端口
	 * @return
	 */
	public static IBaseRepoter getreportmanage_server(String ip, int port){
		IBaseRepoter repotermanage = null;
		
		StringBuffer reportmanage_server = new StringBuffer("http://");
		reportmanage_server.append(ip).append(":");
		if(port <= 0){
			reportmanage_server.append("6553");
		}else{
			reportmanage_server.append(port);
		}
		reportmanage_server.append("/com.navy.daemon/Report/ReportService");
		System.out.println("reportmanage_server:"+reportmanage_server);
		try{
			repotermanage = WSClientFactory.<IBaseRepoter>getWsClient(reportmanage_server.toString() );
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return repotermanage;
	}
	
	public static boolean getRemoteConfSetService(boolean isPublish2Agent){
		return false;
	}
}
