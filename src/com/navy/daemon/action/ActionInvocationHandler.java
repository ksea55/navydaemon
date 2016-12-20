package com.navy.daemon.action;

import java.lang.reflect.Method;
import java.net.URL;

import javax.jws.WebMethod;

public class ActionInvocationHandler{
	
	public static Object invoke(URL request, Object...params)
			throws Throwable {
		
		Object result = null;
		Method method = null;
		
		String urlstr = request.toString();
		String methodname = null;
		String wsdl = null;
		Class<?> endpointInterface = null;
		int methodindx = urlstr.lastIndexOf("?");
		
		if( methodindx > -1 && methodindx < urlstr.length()-1){
			wsdl = urlstr.substring(0, methodindx)+"";//?wsdl
			methodname = urlstr.substring(methodindx+1, urlstr.length());
			//基于jws客户端远程调用
			try {
				IAction action = WSClientFactory.getWsClient(wsdl);
				endpointInterface = Class.forName(WSClientFactory.getContext(wsdl).getEndpointInterface());
				//查找方法根据@WebMethod的operationName
				Method[] ms = endpointInterface.getMethods();
				WebMethod wmano = null;
				for(Method m : ms){
					wmano = m.getAnnotation(WebMethod.class);
					if(null != wmano && methodname.equals(wmano.operationName())){
						method = m;
					}
				}
				//接口的方法申明，通过实现类调用
				if(null != method)result = method.invoke(action, params);
				else System.out.println("not found method:"+methodname);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}else{
			throw new IllegalArgumentException("request url not found the request method, the url must match to 'http://IP+PORT/项目域名/模块名/模块服务类名?方法名'");
		}
		
		
		return result;
	}
}
