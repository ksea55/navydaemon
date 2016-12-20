package com.navy.daemon.action;

import java.net.URL;
import java.security.Timestamp;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;

import com.navy.daemon.BootStart;


public class ActionContext {
	/**
	 * �ͻ��˻��߷���˷���ʱ��Ҫע��jws�ķ���������Ϣ
	 */
	protected static ConcurrentHashMap<String, ActionContext> actions = 
		new ConcurrentHashMap<String, ActionContext>();
	/**
	 * action��Ӧ�ı��������󷢲�·��
	 */
	public String reporter_wsdlLocation;
	/**
	 * ����ӿڷ�����ʹ����һ�������෢������ӿ�
	 */
	public String reportMethod;
	
	/**
	 * 1:����˳���
	 * 0���ܿ�����
	 */
	public int isAgent = 1;
	
	/**
	 * ����ĸ��²���
	 */
	public int option;
	/**
	 * ������ʾ��Ϣ
	 */
	public String op_msg;
	/**
	 * �������ʱ��
	 */
	public Timestamp updateTime;
	/**
	 * �����
	 */
	public String pluginName;
	/**
	 * �����λ��
	 */
	public String pluginJarPath;
	/**
	 * ���������ӿ���ȫ·��
	 */
	public String endpointInterface;
	/**
	 * ����
	 */
	public String name;
	/**
	 * ��������ӿ������ռ�
	 */
	public String nameSpace;
	/**
	 * ������
	 */
	public String serviceName;
	/**
	 * �˿���
	 */
	public String portName;
	/**
	 * action�������·��wsdlLocation�� sha1 16����hashֵ
	 */
	public String wsid;
	/**
	 *  ���������λ��
	 */
	public String wsdlLocation;
	/**
	 * ����������λ��path
	 */
	public String wspath;
	/**
	 * ���������IP
	 */
	public String IP;
	/**
	 * ��������˶˿�
	 */
	public int port;
	/**
	 * ����Э��
	 */
	public String wsprotocal;
	
	/**
	 * ���������ӿ�ʵ���࣬��������������ඨ��
	 */
	public Class implementorclz;
	
	public String implementorpath;
	/**
	 * ���������ӿ�ʵ���࣬���������������ʵ��
	 */
	public IAction implementor;
	/**
	 * ������ַ
	 */
	public Endpoint address;
	/**
	 * ����˷������
	 */
	public IAction action_server;
	/**
	 * �����������
	 * <p>
	 * <li>key:servicename</li>
	 * <li>value:���������ͻ���service�������</li>
	 * </p>
	 */
	private ConcurrentHashMap<String, Service> servicees;
	
	/**
	 * ��������󶨶˿�
	 * <p>
	 * <li>key:portname</li>
	 * <li>value:�����¶�ķ���ӿ�ʵ�������</li>
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
