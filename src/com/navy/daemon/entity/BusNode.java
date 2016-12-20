package com.navy.daemon.entity;
/**
 * @author 付浩洋
 * @date 2016/10/14 0014
 * <p/>
 * 服务总线节点
 */
public class BusNode {
    private String id;
    /**
     * 集群节点
     */
    private ClusterNode clusterNode;
    /**
     * IP
     */
    private String ip;
    /**
     *端口
     */
    private String port;
    /**
     *服务安装路径
     */
    private String serverPath;
    /**
     *状态
     */
    private String status;
    /**
     * 进程号
     */
    private String process;
    /**
     *挂载服务个数
     */
    private Integer serverAppCount;
    public BusNode() {
    }
    public BusNode(String id) {
        this.id = id;
    }

    public String getId() {
        return createId(this.ip,this.port);
    }

    public ClusterNode getClusterNode() {
        return clusterNode;
    }
    public String getIp() {
        return ip;
    }
    public String getPort() {
        return port;
    }

    public String getServerPath() {
        return serverPath;
    }

    public String getStatus() {
        return status;
    }

    public String getProcess() {
        return process;
    }
    public Integer getServerAppCount() {
        return serverAppCount;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setClusterNode(ClusterNode clusterNode) {
        this.clusterNode = clusterNode;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setServerPath(String serverPath) {
        this.serverPath = serverPath;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public void setServerAppCount(Integer serverAppCount) {
        this.serverAppCount = serverAppCount;
    }


    public static String createId(String ip,String port)  {
        return ip + "@" + port;
    }

}
