package com.navy.daemon.util;
/** *//** 
 * 获取系统信息的业务逻辑类接口. 
 * @author mupan 
 */  
public interface IMonitorService {  
    /** *//** 
     * 获得当前的监控对象. 
     * @param infoBean 服务器状态信息
     * （当不为null，本接口实现方法不会创建新对象，直接通过参数对象将状态值传出）
     * @return 返回构造好的监控对象 
     * @throws Exception 
     * @author amgkaka 
     * Creation date: 2008-4-25 - 上午10:45:08 
     */  
    public MonitorInfoBean getMonitorInfoBean(MonitorInfoBean infoBean) throws Exception;  
  
}  