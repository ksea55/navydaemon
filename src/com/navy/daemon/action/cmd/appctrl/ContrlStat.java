package com.navy.daemon.action.cmd.appctrl;



import com.navy.daemon.entity.ClusterConfig;
import com.navy.daemon.excuter.TaskListener;
/**
 * 调用管控平台中心webservice 上报中心关于软件控制的进度
 * @author mup
 *
 */
public class ContrlStat extends TaskListener<ClusterConfig>{
	@Override
	public void doListen(Object data) {
		ClusterConfig appinfo = null != data 
		&& data instanceof ClusterConfig ? (ClusterConfig) data : null;
		//调用服务端webservice
		System.out.println("call center ws.....");
	}

	@Override
	public void setParams(Object... params) {
		
	}
}
