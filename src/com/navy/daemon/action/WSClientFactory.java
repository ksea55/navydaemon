package com.navy.daemon.action;

import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import com.navy.daemon.BootStart;
import com.navy.daemon.conf.SynConfig;



/**
 * �����ṩjwsԶ�̿ͻ��˷���ʵ������
 *   ����ǰʵ����������ʵ���ṩ����ʱֱ�ӷ����޹���Ŀͻ��˶��󣨽����ٱ���Դ�ع���
 *   ������񷢲���Ҫ�ڷ���˳ɹ���������У����õ������֪ͨ�󷢲�������ķ����һ��ΪӦ�ùܿش���
 * @author mup
 *
 */
public class WSClientFactory {
	public static String centerIP = "192.168.3.110";
	public static int port = 6552;
	public static int servicelength = 10;
	
	protected static ConcurrentHashMap<String, ActionContext> actions = ActionContext.actions;
	private static ConcurrentHashMap<String, Client> pool = new ConcurrentHashMap<String, Client>();
	
	/**
	 * �ͻ�����Դ��
	 */
	static class Client{
		private Service service;
		private ActionContext context;
		private AtomicBoolean isinit = new AtomicBoolean(false);
		private AtomicInteger length = new AtomicInteger(0);
		private ArrayBlockingQueue<IAction> used = new ArrayBlockingQueue<IAction>(servicelength);
		private ArrayBlockingQueue<IAction> aviliab = new ArrayBlockingQueue<IAction>(servicelength);
	}
	
	public static Service getService(String url) throws ClassNotFoundException{
		Client cl = pool.get(url);
		//û�г�ʼ��
		if(null == cl || !cl.isinit.get()) {
			throw new ClassNotFoundException("not found the client  map for the url\""+url+"\"");
		}
		return cl.service;
	}
	
	/**
	 * ��ȡws�ͻ����ȷ������
	 * @param <T>
	 * @param wsdlLocation  
	 *     ws����˷���·����wsdl�����ļ�����·����
	 * @return
	 * @throws ClassNotFoundException 
	 *     ��δ�ҵ�wsdlLocation��Ӧ�Ŀͻ���ע�����ʱ�׳����쳣
	 */
	@SuppressWarnings("unchecked")
	public static <T extends IAction> T getWsClient(String wsdlLocation) throws ClassNotFoundException{
		T service = null;
		Client cl = pool.get(wsdlLocation);
		System.out.println(wsdlLocation+" :is init?" + (!cl.isinit.get()) );
		//û�г�ʼ��
		if(null == cl || !cl.isinit.get()) {
			throw new ClassNotFoundException("not found the client  map for the url\""+wsdlLocation+"\"");
		}
		IAction c = (T) cl.aviliab.poll();
		if(null == c){
			service = createClient(cl);
		}else{
			service = (T)c;
		}
		System.out.println("null == service?"+(null == service));
		//�п��ܲ��ɹ�����ʾ���ݳ�����Դ�س��ȣ������ж�����
		if(null != service) cl.used.offer(service);
		
		return service;
	}
	
	/**
	 * ��ȡ�ѷ����Ĳ��������
	 * @param wsdlLocation 
	 *      ws����˷���·����wsdl�����ļ�����·����
	 * @return ������Ч��δ�ҵ���Ӧ�ͻ���ע����Ϣ����null 
	 * @throws ClassNotFoundException
	 *      ��δ�ҵ�wsdlLocation��Ӧ�Ŀͻ���ע�����ʱ�׳����쳣
	 */
	public static ActionContext getContext(String wsdlLocation){
		if(null == wsdlLocation) return null;
		Client cl = pool.get(wsdlLocation);
		
		return null == cl ? null : cl.context;
	}
	
	/**
	 * �ͷſͻ�����ʵ��
	 * @param action
	 */
	public static void close(IAction action){
		Client cl = pool.get(action.getWsdlLocation());
		if(null != cl){
			cl.used.remove(action);
			cl.aviliab.offer(action);
		}
	}
	
	/**
	 * ���ؿͻ��ˡ�
	 * ���Ǹ��µ��ø÷����������¼��������ģ�
	 * ����ͻ��˷������ء����¼���service�����������
	 * @param context ��������������
	 * @return
	 */
	public static boolean publishClient(ActionContext context){
		Client c = null;
		System.out.println("publish client:"+context.wsdlLocation);
		synchronized (context.wsdlLocation) {
			if(!pool.containsKey(context.wsdlLocation)){
				c = new Client();
				c.context = context;
				//System.out.println("add:"+context.wsdlLocation);
				pool.put(context.wsdlLocation, c);
			}else{
				c = pool.get(context.wsdlLocation);
				c.context = context;
			}
		}
		if(c.isinit.compareAndSet(false, true)){
			//�����Ǹ��²�����ӿں����ÿ��ܷ����仯����Ҫ�ؽ���ض���ʵ��
			SynConfig.saveActionSet(context);
			actions.put(context.wsdlLocation, context);
			c.aviliab.clear();
			c.used.clear();
			c.length.set(0);
			try {
				System.out.println("finished publish client:"+c.context.getWsdlLocation()+"\n"
						+c.context.getNameSpace()+"\n"
						+c.context.getServiceName()
						);
				try{
					c.service = Service.create(new URL(c.context.getWsdlLocation()), 
							new QName(c.context.getNameSpace(), c.context.getServiceName()));
				}catch(Exception e){}
			} catch (Exception e) {
				e.printStackTrace();
				context.op_msg = e.getMessage();
				c.context = null;
				c.used = null;
				c.aviliab = null;
				c = null;
				return false;
			}
		}
		return true;
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
	public static void publishClient(ActionContext pluginContext, URL jarurls,String pluginname,
			String endpointInterface, int option, String wsdlLocation) throws Exception{
		boolean isload = false;
		if(null != jarurls ){
			isload = ActionClassloader.loadJars(endpointInterface, jarurls);
		}else{
			isload = true;
		}
		
		//ע�����ͻ���
		if(isload){
			URL u = null;
			System.out.println("start publish client...:"+wsdlLocation);
			if(!wsdlLocation.startsWith("http://")){
				if(wsdlLocation.startsWith("/")){
					wsdlLocation = BootStart.getBoot().getAgentPluginWS_Url_Head()+":"
					+BootStart.getBoot().getMonictrl_port()
					+wsdlLocation;
				}else{
					wsdlLocation = BootStart.getBoot().getAgentPluginWS_Url_Head()+":"
					+BootStart.getBoot().getMonictrl_port()
					+"/"+wsdlLocation;
				}
			}
			u = new URL(wsdlLocation);
			pluginContext.wsprotocal = u.getProtocol();
			pluginContext.IP = u.getHost();
			pluginContext.port =u.getPort();
			pluginContext.wsdlLocation = wsdlLocation;
			//ע��
			WSClientFactory.publishClient(pluginContext);
		}else{
			throw new Exception("the jar load fail,check the jar is ok");
		}
	}
	
	/**
	 * ж�ؿͻ���,���Ǹ��²�Ӧ���ø÷���
	 * @param context ��������������
	 * @return
	 */
	public static boolean uninstallClient(ActionContext context){
		Client c = null;
		synchronized (context.wsdlLocation) {
			if(!pool.containsKey(context.wsdlLocation)){
			}else{
				c = pool.get(context.wsdlLocation);
			}
		}
		if(null != c && c.isinit.compareAndSet(true, false)){
			actions.remove(context.wsdlLocation);
			pool.remove(context.wsdlLocation);
			c.aviliab.clear();
			c.used.clear();
			c.length.set(0);
			c.context = null;
		}
		return true;
	}
	
    /**
     * �����ͻ���
     * @param <T> webservice����ӿ�����
     * @param url wsdl����·��
     * @param nameSpace wsdl���������ռ�
     * @param servicename ��������
     * @param bindPort jws�󶨵Ķ˿���
     * @param clz �ӿ�����
     * @return ���ؿͻ��˷���ӿ�T�����ܷ���null
     */
	@SuppressWarnings("unchecked")
	private static <T extends IAction> T createClient(Client cl){
		T service = null;
		try {
			//���ز���ӿ���������
			Class c = Class.forName(cl.context.getEndpointInterface());
			if(IAction.class.isAssignableFrom(c)){
				Class<T> clz = (Class<T>) c;
				c = null;
				if(null == cl.service){
					try{
						cl.service = Service.create(new URL(cl.context.getWsdlLocation()), 
								new QName(cl.context.getNameSpace(), cl.context.getServiceName()));
					}catch(Exception e){}
				}
				if(null != cl.service){
					service = cl.service.getPort(
							new QName(cl.context.getNameSpace(), cl.context.getPortName()),
								clz);
				}
			}else{
				System.out.println("the webservice interface is not AssignableFrom IAction..");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return service;
	}
	
	
	
}
