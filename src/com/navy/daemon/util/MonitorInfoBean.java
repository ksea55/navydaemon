package com.navy.daemon.util;

import java.util.List;

/**
 * 监视信息的JavaBean类. 
 * @author  mup
 */  
public class MonitorInfoBean {  
    /**可使用内存*/
    public long totalMemory;
      
    /**剩余内存*/
    public long freeMemory;
      
    /**最大可使用内存*/
    public long maxMemory;
      
    /**操作系统*/
    public String osName;
      
    /**
     * 总的物理内存
     */  
    public long totalMemorySize;
      
    /**
     * 剩余的物理内存
     **/  
    public long freePhysicalMemorySize;
      
    /** 
     * 已使用的物理内存
     */
    public long usedMemory;
      
    /**线程总数*/
    public int totalThread;
      
    /**cpu使用率*/
    public double cpuRatio;
    
    /**
     * 驻留服务节点IP
     */
    public String IP;
    
    /**
     * 驻留服务器名称
     */
    public String hostName;
    /**
     * cpu数
     */
    public int avliaProcessCount;
    /**
     * 磁盘总大小
     */
    public double diskTotalSize;
    /**
     * 磁盘可用空间
     */
    public double diskTotalFreeSize;
    /**
     * 分区数
     */
    public int avliaLogicDiskCount;
    /**
     * 分区信息
     */
    public List<LogicDiskInfo> logicDiskInfos;
} 