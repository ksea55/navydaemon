package com.navy.daemon.action;

import java.net.URL;
import java.security.Timestamp;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;

import com.navy.daemon.BootStart;


public class ActionContext {
	/**
	 * 客户端或者服务端发布时需要注册jws的发布描述信息
	 */
	protected static ConcurrentHashMap<String, ActionContext> actions = 
		new ConcurrentHashMap<String, ActionContext>();
	/**
	 * action对应的报告服务对象发布路径
	 */
	public String reporter_wsdlLocation;
	/**
	 * 报告接口方法，使用与一个报告类发布多个接口
	 */
	public String reportMethod;
	
	/**
	 * 1:代理端程序
	 * 0：管控中心
	 */
	public int isAgent = 1;
	
	/**
	 * 最近的更新操作
	 */
	public int option;
	/**
	 * 操作提示消息
	 */
	public String op_msg;
	/**
	 * 最近更新时间
	 */
	public Timestamp updateTime;
	/**
	 * 插件名
	 */
	public String pluginName;
	/**
	 * 插件包位置
	 */
	public String pluginJarPath;
	/**
	 * 插件包服务接口类全路径
	 */
	public String endpointInterface;
	/**
	 * 名称
	 */
	public String name;
	/**
	 * 插件发布接口命名空间
	 */
	public String nameSpace;
	/**
	 * 服务名
	 */
	public String serviceName;
	/**
	 * 端口名
	 */
	public String portName;
	/**
	 * action插件发布路径wsdlLocation的 sha1 16进制hash值
	 */
	public String wsid;
	/**
	 *  插件发布的位置
	 */
	public String wsdlLocation;
	/**
	 * 发布发布的位置path
	 */
	public String wspath;
	/**
	 * 发布服务端IP
	 */
	public String IP;
	/**
	 * 发布服务端端口
	 */
	public int port;
	/**
	 * 发布协议
	 */
	public String wsprotocal;
	
	/**
	 * 插件包服务接口实现类，即服务入口引导类定义
	 */
	public Class implementorclz;
	
	public String implementorpath;
	/**
	 * 插件包服务接口实现类，即服务入口引导类实例
	 */
	public IAction implementor;
	/**
	 * 发布地址
	 */
	public Endpoint address;
	/**
	 * 服务端服务对象
	 */
	public IAction action_server;
	/**
	 * 插件发布服务
	 * <p>
	 * <li>key:servicename</li>
	 * <li>value:插件发布后客户端service服务对象</li>
	 * </p>
	 */
	private ConcurrentHashMap<String, Service> servicees;
	
	/**
	 * 插件发布绑定端口
	 * <p>
	 * <li>key:portname</li>
	 * <li>value:插件暴露的服务接口实现类对象</li>
	 * </p>
	 */
	private ConcurrentHashMap<String, IAction> ports;
	
	public ActionContext( String pluginName, String wsdlUrl, 
			String endpointInterface, String name,  String nameSpace, String serviceName,
			String portName, String wsdlLocation, Class implementorclz){
		this.pluginName = pluginName;
		this.endpointInterface = endpointInterface;
		this.name = name;
		this.nameSpace = nameSpace;
		this.serviceName = serviceName;
		this.portName = portName;
		this.wsdlLocation = wsdlLocation;
		this.implementorclz = implementorclz;
		servicees = new ConcurrentHashMap<String, Service>();
		ports = new ConcurrentHashMap<String, IAction>();
	}
	
	public ActionContext(){
	}
	
	public ActionContext(IAction action){
		servicees = new ConcurrentHashMap<String, Service>();
		ports = new ConcurrentHashMap<String, IAction>();
		this.wsdlLocation = action.getWsdlLocation();
		this.endpointInterface = action.getEndpointInterface();
		this.implementorclz = action.getImplementor();
		this.name = action.getName();
		this.nameSpace = action.getNameSpace();
		this.portName = action.getPortName();
		this.serviceName = action.getServiceName();
		this.implementor = action;
	}

	public String getNameSpace() {
		return nameSpace;
	}

	public ConcurrentHashMap<String, Service> getServicees() {
		return servicees;
	}

	public ConcurrentHashMap<String, IAction> getPorts() {
		return ports;
	}

	public String getPluginName() {
		return pluginName;
	}

	public String getEndpointInterface() {
		return endpointInterface;
	}

	public String getName() {
		return name;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getPortName() {
		return portName;
	}

	public String getWsdlLocation() {
		return wsdlLocation;
	}

	public Class getImplementorclz() {
		return implementorclz;
	}

	public Endpoint getAddress() {
		return address;
	}

	public int getOption() {
		return option;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public IReporter getReporter() {
		if(null == reporter_wsdlLocation) return null;
		IReporter reporter = null;
		try {
			if(null != reporter_wsdlLocation) 
				reporter = WSClientFactory.<IReporter>getWsClient(reporter_wsdlLocation);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return reporter;
	}
	
	public boolean report(Object...reports){
		if(null == reportMethod || null == reporter_wsdlLocation) return false;
		boolean flag = true;
		try{
			String reporter_wsdlLocation0 = reporter_wsdlLocation;
			if(reporter_wsdlLocation0.endsWith("?wsdl")
					|| reporter_wsdlLocation0.endsWith("?WSDL")
			){
				int inx = reporter_wsdlLocation0.lastIndexOf("?wsdl");
				inx = inx > -1 ? inx : reporter_wsdlLocation0.lastIndexOf("?WSDL");
				reporter_wsdlLocation0 = reporter_wsdlLocation0.substring(
						inx, reporter_wsdlLocation0.length()
				);
			}
			ActionInvocationHandler.invoke(
					new URL(reporter_wsdlLocation0+"?"+reportMethod), 
					reports
			);
		}catch(Exception e){
			flag = false;
			e.printStackTrace();
		} catch (Throwable e) {
			flag = false;
			e.printStackTrace();
		}
		return flag;
	}
	
	public boolean report(String reportMethod, Object...reports){
		if(null == reportMethod || null == reporter_wsdlLocation) return false;
		boolean flag = true;
		try{
			String reporter_wsdlLocation0 = reporter_wsdlLocation;
			if(reporter_wsdlLocation0.endsWith("?wsdl")
					|| reporter_wsdlLocation0.endsWith("?WSDL")
			){
				int inx = reporter_wsdlLocation0.lastIndexOf("?wsdl");
				inx = inx > -1 ? inx : reporter_wsdlLocation0.lastIndexOf("?WSDL");
				reporter_wsdlLocation0 = reporter_wsdlLocation0.substring(
						inx, reporter_wsdlLocation0.length()
				);
			}
			ActionInvocationHandler.invoke(
					new URL(reporter_wsdlLocation0+"?"+reportMethod), 
					reports
			);
		}catch(Exception e){
			flag = false;
			e.printStackTrace();
		} catch (Throwable e) {
			flag = false;
			e.printStackTrace();
		}
		return flag;
	}

	public static ConcurrentHashMap<String, ActionContext> getActions() {
		return actions;
	}

	public String getOp_msg() {
		return op_msg;
	}

	public IAction getImplementor() {
		return implementor;
	}

	public String getWsid() {
		return wsid;
	}

	public String getPluginJarPath() {
		return pluginJarPath;
	}

	public String getReporter_wsdlLocation() {
		return reporter_wsdlLocation;
	}

	public int getIsAgent() {
		return isAgent;
	}

	public IAction getAction_server() {
		return action_server;
	}

	public int getPort() {
		return port;
	}

	public String getWsprotocal() {
		return wsprotocal;
	}

	public String getWspath() {
		return wspath;
	}

	public String getIP() {
		return IP;
	}

	public String getImplementorpath() {
		return implementorpath;
	}
	
}
