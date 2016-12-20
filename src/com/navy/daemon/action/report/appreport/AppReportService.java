package com.navy.daemon.action.report.appreport;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.navy.daemon.action.IReporter;
import com.navy.daemon.entity.ClusterConfig;
/**
 * �ܹ�Ӧ�����״̬�ɼ����ϱ�
 * @author mup
 *
 */
@WebService(endpointInterface = "com.navy.daemon.action.report.appreport.AppReportService",
		targetNamespace="http://com.navy.daemon/appReport/",
		portName="AppReportServicePort",
		serviceName="AppReportService"
)
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface AppReportService extends IReporter{
	/**
	 * �ӿڷ��񷢲�·��
	 */
	public final String wsdl = "http://IP:6553/com.navy.daemon/appReport/AppReportService";
	/**
	 * ��ѯapp״̬
	 * @param appName Ӧ������ 
	 * @return Ӧ����Ϣ(���̣�Ӧ��״̬)
	 */
	@WebMethod(operationName="getAppStat")
	@WebResult(name = "appStat")
	public ClusterConfig getAppStat(String appName);
}
