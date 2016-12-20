package com.navy.daemon.action.cmd.appctrl;

import com.navy.daemon.BootStart;
import com.navy.daemon.action.ActionClassloader;
import com.navy.daemon.action.ActionContext;
import com.navy.daemon.action.IAction;
import com.navy.daemon.action.WSServerFactory;

public class TestPublishService {

	/**
	 * 测试
	 * @param args
	 */
	public static void main(String[] args){
		ActionContext acontext = null;
		String pluginImplclasspath="com.navy.daemon.action.cmd.appctrl.AppContrlServiceImpl";
		boolean isload = true;
		/*try {
			isload = ActionClassloader.loadJars(pluginImplclasspath, new URL[]{plugin.toURL()});
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		//启动插件服务
		if(isload){
			//读取jws配置信息
			IAction action = null;
			try {
				action = ActionClassloader.<IAction>newInstance(pluginImplclasspath);
				if(null == acontext)acontext = new ActionContext(action);
				System.out.println("WsdlLocation:"+action.getWsdlLocation());
				System.out.println("EndpointInterface:"+action.getEndpointInterface());
				System.out.println("Name:"+action.getName());
				System.out.println("NameSpace:"+action.getNameSpace());
				System.out.println("PortName:"+action.getPortName());
				System.out.println("ServiceName:"+action.getServiceName());
				System.out.println("Implementor:"+action.getImplementor());
				
				
				acontext.pluginName = "appctrl";
				acontext.option = 0;
				acontext.wsdlLocation="/com.navy.daemon/appctrl/AppContrlService";
				if(!acontext.wsdlLocation.startsWith("http://")){
					if(acontext.wsdlLocation.startsWith("/")){
						acontext.wsdlLocation = BootStart.getBoot().getAgentPluginWS_Url_Head()+":"+BootStart.getBoot().getMonictrl_port()
						+acontext.wsdlLocation;
					}else{
						acontext.wsdlLocation = BootStart.getBoot().getAgentPluginWS_Url_Head()+":"+BootStart.getBoot().getMonictrl_port()
						+"/"+acontext.wsdlLocation;
					}
				}
				
				System.out.println(acontext.wsdlLocation);
				
				//初始化
				action.init();
				//发布服务
				WSServerFactory.publish(acontext, action);
			} catch (ClassCastException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		//Endpoint address = Endpoint.publish("http://192.168.3.110:6552/com.navy.daemon/app/AppContrlService", new AppContrlServiceImpl());
	}

}
