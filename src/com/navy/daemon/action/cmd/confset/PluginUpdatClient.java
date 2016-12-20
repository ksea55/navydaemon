package com.navy.daemon.action.cmd.confset;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Callable;

import com.navy.daemon.BootStart;
import com.navy.daemon.action.ActionContext;
import com.navy.daemon.action.WSClientFactory;

import com.navy.daemon.util.FileOperate;
/**
 * ������¿ͻ��˵�����,���ڴ��������Ӧ�ͻ��˷�����󣬸��¼��ز������
 * @author mup
 *
 */
public class PluginUpdatClient implements Callable<Object>{
	private ActionContext context;
	private String jarFtpPath;
    private int option;
    private ActionContext pluginContext = new ActionContext();
	
	public PluginUpdatClient(ActionContext context, String wsdlLocation, String nameSpace,
			String pluginName, String portName, String serviceName, String endpointInterface,
			 String jarFtpPath, int option ){
		pluginContext.wsdlLocation = wsdlLocation;
		pluginContext.endpointInterface = endpointInterface;
		pluginContext.nameSpace = nameSpace;
		pluginContext.option = option;
		pluginContext.pluginName = pluginName;
		pluginContext.portName = portName;
		pluginContext.serviceName = serviceName;
		this.context = context;
		this.jarFtpPath = jarFtpPath;
		this.option = option;
	}
	
	public Object call(){
		StringBuffer buf = new StringBuffer();
		/**
		 * ���ºͰ�װ�����Ҫ�������
		 */
		if(0 == option || 1 == option ){
			File plugin = null;
			buf.append(BootStart.getBoot().getDownloadTemp().getAbsolutePath())
				.append(File.separator).append(pluginContext.pluginName).append(".jar");
			File localFile = new File(buf.toString());
			buf.delete(0, buf.length());
			try {
				BootStart.getBoot().getFc().getClient().download(jarFtpPath, localFile);
			} catch (Exception e) {
				//״̬�ϱ�-���������ʧ��
				context.report(null);
				e.printStackTrace();
			}
			//������ע��ԭ����ͻ���
			if(1 == option){
				WSClientFactory.uninstallClient(pluginContext);
			}
			
			buf.append(BootStart.getBoot().getPluginlib().getAbsolutePath())
			.append(File.separator).append(pluginContext.pluginName).append(".jar");
			
			plugin = new File(buf.toString());
			buf.delete(0, buf.length());
			//��������jar��
			if(FileOperate.copyGeneralFile(localFile.getAbsolutePath(), plugin.getAbsolutePath())){
				try {
					WSClientFactory.publishClient(pluginContext, plugin.toURL(), pluginContext.pluginName,
							pluginContext.endpointInterface, option, pluginContext.wsdlLocation);
				} catch (Exception e) {
					e.printStackTrace();
					//״̬�ϱ�-��������
					context.report(e.getMessage());
				}
			}
		}
		return null;
	}
}
