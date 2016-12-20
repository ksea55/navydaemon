package com.navy.daemon.action;

import javax.jws.WebService;

public class JWS {
	
	public String getEndpointInterface(){
		return this.getClass().getAnnotation(WebService.class).endpointInterface();
	}
	
	public String getName(){
		return this.getClass().getAnnotation(WebService.class).name();
	}
	
	public String getNameSpace(){
		return this.getClass().getAnnotation(WebService.class).targetNamespace();
	}
	
	public String getServiceName(){
		return this.getClass().getAnnotation(WebService.class).serviceName();
	}
	
	public String getPortName(){
		return this.getClass().getAnnotation(WebService.class).portName();
	}
	
	public String getWsdlLocation(){
		return this.getClass().getAnnotation(WebService.class).wsdlLocation();
	}
	
	public Class getImplementor() {
		return this.getClass();
	}
}
