package com.navy.daemon.action;


/**
 * �Ȳ�����壬��
 * @author mup
 *
 */
public interface IAction {
	public String getEndpointInterface();
	
	public String getName();
	
	public String getNameSpace();
	
	public String getServiceName();
	
	public String getPortName();
	
	public String getWsdlLocation();
	
	public Class getImplementor();
	
	public void addActionListener(Class<IReporter> reporter_clz);
	
	public void init();
	
	public void destroy();
	
	public void close();
}
