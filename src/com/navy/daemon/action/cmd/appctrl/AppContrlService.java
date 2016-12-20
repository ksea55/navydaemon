package com.navy.daemon.action.cmd.appctrl;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.navy.daemon.action.IAction;
import com.navy.daemon.entity.ClusterConfig;

@WebService(endpointInterface = "com.navy.daemon.action.cmd.appctrl.AppContrlService",
		targetNamespace="http://com.navy.daemon/appctrl/",
		portName="AppContrlServicePort",
		serviceName="AppContrlService"
)
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface AppContrlService extends IAction{
	/**
	 * 接口服务发布路径
	 */
	public final String wsdl = "http://IP:6553/com.navy.daemon/appctrl/AppContrlService";
	/**
	 * 接收mes数据通知
	 * @param app 应用信息及应用控制命令
	 */
	@WebMethod(operationName="excuteCMD")
	@WebResult(name = "oack")
	public String excuteCMD(@WebParam(name = "app")ClusterConfig app);
	
	/**
	 * 接收mes数据通知
	 * @param app 应用信息及应用控制命令
	 */
	@WebMethod(operationName="testCmandReport")
	@WebResult(name = "oack")
	public String testCmandReport(@WebParam(name = "app")ClusterConfig app);
}
