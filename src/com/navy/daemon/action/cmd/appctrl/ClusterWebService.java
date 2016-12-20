package com.navy.daemon.action.cmd.appctrl;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.navy.daemon.entity.ClusterConfig;

@WebService(targetNamespace="http://service.websevice.basic.hjqb.sinux.com.cn/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface ClusterWebService {
    
   /**
    * 软件控制过程状态上报
    * @param clusterConfig
    * @return 
    */
	@WebMethod
	@WebResult(name = "oack")
    public ClusterConfig updateClusterConfig(@WebParam(name = "clusterConfig")ClusterConfig clusterConfig);
}
