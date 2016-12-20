package com.navy.daemon.action;

import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.ws.Endpoint;
import com.navy.daemon.BootStart;
import com.navy.daemon.conf.SynConfig;


public class WSServerFactory {
	public static int port = 6552;
	public static String ip = "192.168.3.154";
	protected static ConcurrentHashMap<String, ActionContext> actions = ActionContext.actions;
	
	/**
	 * ��������¼���
	 * @param url ����·��
	 * @param acontext.wsdlLocation ��������ӿڶ���
	 * @param imp ��������ӿ�ʵ���ඨ��
	 */
	public static final void publish(ActionContext acontext, IAction imp){
		//Ӧ�ò������
		Endpoint address = Endpoint.publish(acontext.getWsdlLocation(), imp);
		acontext.address = address;
		acontext.action_server = imp;
		SynConfig.saveActionSet(acontext);
		actions.put(acontext.wsdlLocation, acontext);
		//����action���Ϊ��ʱ��������ʱ����ע�ᣬ�Ա���action���������������ʱ����
		if(null != imp && IReporter.class.isAssignableFrom(imp.getClass()) ){
			System.out.println("is reporter :"+imp.getClass().getClass().getName());
			IReporter report = (IReporter)imp;
			BootStart.rePorters.put(report.getReporterId(), report);
		}
		System.out.println("finished publish server...:"+acontext.wsdlLocation);
	}
	
	/**
	 * 
	 * @param old_acontext
	 * @param jarurls
	 * @param pluginname
	 * @param pluginImplclasspath
	 * @param option
	 * @param wsdlLocation
	 */
	public static void publish(ActionContext old_acontext, URL jarurls,String pluginname,
			String pluginImplclasspath, int option, String wsdlLocation, 
			String report_wsdlLocation, String report_method) throws Exception{
		
		boolean isload = false;
		ActionContext acontext = old_acontext;
		if(null != jarurls){
			isload = ActionClassloader.loadJars(pluginImplclasspath, jarurls);
		}else{
			isload = true;
		}
		
		//�����������
		if(isload){
			//��ȡjws������Ϣ
			IAction action = null;
			try{
				action = ActionClassloader.<IAction>newInstance(pluginImplclasspath);
			}catch(Exception e){
				if(null == action) return;
			}
			if(null == acontext)acontext = new ActionContext(action);
			acontext.pluginName = pluginname;
			acontext.option = option;
			acontext.wsdlLocation=wsdlLocation;
			acontext.reporter_wsdlLocation = report_wsdlLocation;
			acontext.reportMethod = report_method;
			System.out.println("start publish server...:"+acontext.wsdlLocation);
			//ע��IP����������������������
			if(!acontext.wsdlLocation.startsWith("http://")){
				if(acontext.wsdlLocation.startsWith("/")){
					acontext.wsdlLocation = BootStart.getBoot().getAgentPluginWS_Url_Head()+":"
					+BootStart.getBoot().getMonictrl_port()
					+acontext.wsdlLocation;
				}else{
					acontext.wsdlLocation = BootStart.getBoot().getAgentPluginWS_Url_Head()+":"
					+BootStart.getBoot().getMonictrl_port()
					+"/"+acontext.wsdlLocation;
				}
				URL u = new URL(acontext.wsdlLocation);
				acontext.wsprotocal = u.getProtocol();
				acontext.port = BootStart.getBoot().getMonictrl_port();
			}else{
				//
				URL u = new URL(acontext.wsdlLocation);
				acontext.port = u.getPort();
				acontext.wsprotocal = u.getProtocol();
				String lip = u.getHost();
				acontext.wsdlLocation = acontext.wsdlLocation.replace(lip, BootStart.getBoot().getAgentIP());
			}
			acontext.IP = BootStart.getBoot().getAgentIP();
			
			//��������
			((AbstractAction)action).context = acontext;
			//��ʼ��
			action.init();
			//��������
			WSServerFactory.publish(acontext, action);
		}else{
			throw new Exception("the jar load fail,check the jar is ok");
		}
	}
	
	/**
	 * ��ȡAction�������������
	 * @param wsdlLocation ����·��������Ψһ��
	 * @return ��δ�ҵ�����·����Ӧ������ʱ������null
	 */
	public static ActionContext getContext(String wsdlLocation){
		if(null == wsdlLocation) return null;
		return actions.get(wsdlLocation);
	}
	
	
	/*public static final void publish(URL url, Class interfaceClz, 
			Class serviceClz, String ftpPath){
	}*/
	/**
	 * 
	 */
	public static final void destroy(String wsdlLocation){
		if(null == wsdlLocation) return ;
		ActionContext context = actions.get(wsdlLocation);
		if(null != context && null != context.address && context.address.isPublished()){
			context.address.stop();
			try{
				context.action_server.close();
			}catch(Exception e){
				e.printStackTrace();
			}
			try{
				context.action_server.destroy();
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
		actions.remove(wsdlLocation);
	}
	
	/**
	 * ֹͣ����jws���񣬲�������������
	 */
	public static final void destroy(){
		Iterator<ActionContext> as = actions.values().iterator();
		ActionContext con = null;
		while(as.hasNext()){
			con = as.next();
			con.address.stop();
			con.getServicees().clear();
			con.getPorts().clear();
		}
		actions.clear();
	}
}
