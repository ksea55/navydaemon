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
			//����jws�ͻ���Զ�̵���
			try {
				IAction action = WSClientFactory.getWsClient(wsdl);
				endpointInterface = Class.forName(WSClientFactory.getContext(wsdl).getEndpointInterface());
				//���ҷ�������@WebMethod��operationName
				Method[] ms = endpointInterface.getMethods();
				WebMethod wmano = null;
				for(Method m : ms){
					wmano = m.getAnnotation(WebMethod.class);
					if(null != wmano && methodname.equals(wmano.operationName())){
						method = m;
					}
				}
				//�ӿڵķ���������ͨ��ʵ�������
				if(null != method)result = method.invoke(action, params);
				else System.out.println("not found method:"+methodname);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}else{
			throw new IllegalArgumentException("request url not found the request method, the url must match to 'http://IP+PORT/��Ŀ����/ģ����/ģ���������?������'");
		}
		
		
		return result;
	}
}
