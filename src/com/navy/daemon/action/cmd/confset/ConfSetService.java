package com.navy.daemon.action.cmd.confset;

import java.util.HashMap;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * 管控代理配置与插件更新接口
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
	 * 接口服务发布路径
	 */
	public final String wsdl = "http://IP:6553/com.navy.daemon/confSet/ConfSetService";
	
	/**
	 * 插件服务端更新发布
	 * @param pluginname 插件名
	 * @param pluginImplclasspath 插件服务接口实现类
	 * @param jarFtpPath 插件包在ftp服务位置
	 * @param option 操作类型：0为安装，1为更新，-1为卸载
	 * @param wsdlLocation 发布路径
	 * @param report_wsdlLocation 报告服务对象发布路径，可为null(即插件运行时不需要报告上报)
	 * @param report_method 报告目标方法
	 * @return 返回更新结果
	 */
	public HashMap<String, String> publishPluginServer(String pluginname, String pluginImplclasspath, 
			String jarFtpPath, int option, String wsdlLocation, String report_wsdlLocation, String report_method);

	
	
	
	/**
	 * 插件客户端更新发布
	 * @param context 
	 * @param wsdlLocation 发布路径
	 * @param nameSpace jws命名空间
	 * @param pluginName 插件名
	 * @param portName jws绑定端口
	 * @param serviceName jws服务名
	 * @param endpointInterface 插件服务接口
	 * @param jarFtpPath 插件包在ftp服务位置
	 * @param option 操作类型：0为安装，1为更新，-1为卸载
	 * @return 返回更新结果
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