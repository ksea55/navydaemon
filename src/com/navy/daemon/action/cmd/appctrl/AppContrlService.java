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
	 * �ӿڷ��񷢲�·��
	 */
	public final String wsdl = "http://IP:6553/com.navy.daemon/appctrl/AppContrlService";
	/**
	 * ����mes����֪ͨ
	 * @param app Ӧ����Ϣ��Ӧ�ÿ�������
	 */
	@WebMethod(operationName="excuteCMD")
	@WebResult(name = "oack")
	public String excuteCMD(@WebParam(name = "app")ClusterConfig app);
	
	/**
	 * ����mes����֪ͨ
	 * @param app Ӧ����Ϣ��Ӧ�ÿ�������
	 */
	@WebMethod(operationName="testCmandReport")
	@WebResult(name = "oack")
	public String testCmandReport(@WebParam(name = "app")ClusterConfig app);
}
