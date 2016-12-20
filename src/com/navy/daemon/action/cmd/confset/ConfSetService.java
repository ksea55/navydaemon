package com.navy.daemon.action.cmd.confset;

import java.util.HashMap;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * �ܿش��������������½ӿ�
 * @author mup
 *
 */
@WebService(endpointInterface = "com.navy.daemon.action.cmd.appctrl.AppContrlService",
		targetNamespace="http://com.navy.daemon/confSet/",
		portName="ConfSetServicePort",
		serviceName="ConfSetService"
)
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface ConfSetService {
	/**
	 * �ӿڷ��񷢲�·��
	 */
	public final String wsdl = "http://IP:6553/com.navy.daemon/confSet/ConfSetService";
	
	/**
	 * �������˸��·���
	 * @param pluginname �����
	 * @param pluginImplclasspath �������ӿ�ʵ����
	 * @param jarFtpPath �������ftp����λ��
	 * @param option �������ͣ�0Ϊ��װ��1Ϊ���£�-1Ϊж��
	 * @param wsdlLocation ����·��
	 * @param report_wsdlLocation ���������󷢲�·������Ϊnull(���������ʱ����Ҫ�����ϱ�)
	 * @param report_method ����Ŀ�귽��
	 * @return ���ظ��½��
	 */
	public HashMap<String, String> publishPluginServer(String pluginname, String pluginImplclasspath, 
			String jarFtpPath, int option, String wsdlLocation, String report_wsdlLocation, String report_method);

	
	
	
	/**
	 * ����ͻ��˸��·���
	 * @param context 
	 * @param wsdlLocation ����·��
	 * @param nameSpace jws�����ռ�
	 * @param pluginName �����
	 * @param portName jws�󶨶˿�
	 * @param serviceName jws������
	 * @param endpointInterface �������ӿ�
	 * @param jarFtpPath �������ftp����λ��
	 * @param option �������ͣ�0Ϊ��װ��1Ϊ���£�-1Ϊж��
	 * @return ���ظ��½��
	 */
	public HashMap<String, String> publishPluginClient(String wsdlLocation, String nameSpace,
			String pluginName, String portName, String serviceName, String endpointInterface,
			 String jarFtpPath, int option );
	
	/**
	 * 
	 * @return
	 */
	public HashMap<String, String> configSet();
}