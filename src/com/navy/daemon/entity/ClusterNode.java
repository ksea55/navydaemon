package com.navy.daemon.entity;



/**
 * 集群节点
 */

public class ClusterNode {
    /**
     * ID
     */
    private String id;


    /**
     * CPU核数
     */
    private String totalCpuNum;

    /**
     * 硬盘总计
     */
    private Double totalHardDiskInfo;

    /**
     * 内存总计
     */
    private Double totalMemoryInfo;


    /**
     * 节点IP
     */
    private String ip;
    /**
     * 节点名
     */
    private String nodeName;
    /**
     * cpu使用率
     */
    private Double cpuUsage;
    /**
     * 使用中内存
     */
    private Double memoryInuse;

    /**
     * 使用中硬盘
     */
    private Double hardDiskInuse;
    ;
    /**
     * 内存使用率
     */
    private Double memoryUsage;
    /**
     * 节点状态
     */
    private String nodeState;
    /**
     * 是否安装服务总线
     * 0:未安装
     * 1:已安装
     */
    private int busIn;
    /**
     * 是否安装数据总线
     * 0:未安装
     * 1:已安装
     */
    private int dataIn;


    /**
     * 节点下服务总线数
     */
    private int busNum;

    /**
     * 节点下数据总线数
     */
    private int dataNum;


    public ClusterNode() {

    }

    public ClusterNode(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getMemoryInuse() {
        return totalMemoryInfo*memoryUsage/100;
    }

    public void setMemoryInuse(Double memoryInuse) {
        this.memoryInuse = memoryInuse;
    }

    public String getIp() {
        return ip;
    }


    public String getNodeName() {
        return nodeName;
    }



    public Double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(Double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }
    public String getTotalCpuNum() {
        return totalCpuNum;
    }

    public Double getCpuUsage() {
        return cpuUsage;
    }

    public Double getHardDiskInuse() {
        return hardDiskInuse;
    }

    public String getNodeState() {
        return nodeState;
    }

    public int getBusNum() {
        return busNum;
    }

    public Double getTotalHardDiskInfo() {
        return totalHardDiskInfo;
    }

    public Double getTotalMemoryInfo() {
        return totalMemoryInfo;
    }
    public int getBusIn() {
        return busIn;
    }

    public void setBusIn(int busIn) {
        this.busIn = busIn;
    }
    public int getDataIn() {
        return dataIn;
    }

    public void setDataIn(int dataIn) {
        this.dataIn = dataIn;
    }
    
    public int getDataNum() {
        return dataNum;
    }

    public void setDataNum(int dataNum) {
        this.dataNum = dataNum;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }



    public void setNodeState(String nodeState) {
       this.nodeState = nodeState;
    }

    public void setBusNum(int busNum) {
        this.busNum = busNum;
    }


    public void setTotalHardDiskInfo(Double totalHardDiskInfo) {
        this.totalHardDiskInfo = totalHardDiskInfo;
    }

    public void setTotalMemoryInfo(Double totalMemoryInfo) {
        this.totalMemoryInfo = totalMemoryInfo;
    }

    public void setTotalCpuNum(String totalCpuNum) {
        this.totalCpuNum = totalCpuNum;
    }

    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }
    public void setHardDiskInuse(Double hardDiskInuse) {
        this.hardDiskInuse = hardDiskInuse;
    }
}
