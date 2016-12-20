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
 * 该类提供jws远程客户端服务实例管理
 *   当当前实例池无限制实例提供服务时直接返回无管理的客户端对象（将不再被资源池管理）
 *   插件服务发布需要在服务端成功发布后进行（即得到服务端通知后发布，这里的服务端一般为应用管控代理）
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
	 * 客户端资源池
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
		//没有初始化
		if(null == cl || !cl.isinit.get()) {
			throw new ClassNotFoundException("not found the client  map for the url\""+url+"\"");
		}
		return cl.service;
	}
	
	/**
	 * 获取ws客户调度服务对象
	 * @param <T>
	 * @param wsdlLocation  
	 *     ws服务端发布路径（wsdl描述文件访问路径）
	 * @return
	 * @throws ClassNotFoundException 
	 *     当未找到wsdlLocation对应的客户端注册对象时抛出该异常
	 */
	@SuppressWarnings("unchecked")
	public static <T extends IAction> T getWsClient(String wsdlLocation) throws ClassNotFoundException{
		T service = null;
		Client cl = pool.get(wsdlLocation);
		System.out.println(wsdlLocation+" :is init?" + (!cl.isinit.get()) );
		//没有初始化
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
		//有可能不成功，表示依据超过资源池长度，不进行对象共享
		if(null != service) cl.used.offer(service);
		
		return service;
	}
	
	/**
	 * 获取已发布的插件上下文
	 * @param wsdlLocation 
	 *      ws服务端发布路径（wsdl描述文件访问路径）
	 * @return 参数无效或未找到对应客户端注册信息返回null 
	 * @throws ClassNotFoundException
	 *      当未找到wsdlLocation对应的客户端注册对象时抛出该异常
	 */
	public static ActionContext getContext(String wsdlLocation){
		if(null == wsdlLocation) return null;
		Client cl = pool.get(wsdlLocation);
		
		return null == cl ? null : cl.context;
	}
	
	/**
	 * 释放客户对象实例
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
	 * 加载客户端。
	 * 若是更新调用该方法，则重新加载上下文，
	 * 清理客户端服务对象池、重新加载service，其他不会变
	 * @param context 服务上上文配置
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
			//可能是更新插件，接口和配置可能发生变化；需要重建相关对象实例
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
		
		//注册插件客户端
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
			//注册
			WSClientFactory.publishClient(pluginContext);
		}else{
			throw new Exception("the jar load fail,check the jar is ok");
		}
	}
	
	/**
	 * 卸载客户端,若是更新不应调用该方法
	 * @param context 服务上上文配置
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
     * 创建客户端
     * @param <T> webservice服务接口类型
     * @param url wsdl发布路径
     * @param nameSpace wsdl发布命名空间
     * @param servicename 服务名称
     * @param bindPort jws绑定的端口名
     * @param clz 接口类型
     * @return 返回客户端服务接口T，可能返回null
     */
	@SuppressWarnings("unchecked")
	private static <T extends IAction> T createClient(Client cl){
		T service = null;
		try {
			//加载插件接口类型描述
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
