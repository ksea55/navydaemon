package com.navy.daemon.entity;


import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 节点文件类
 */
public class ClusterConfig {
    /**
     * 更新操作
     */
    public final  static String UPDATE="update";
    /**
     * 安装操作
     */
    public final  static String INSTALL="install";
    /**
     * 卸载操作
     */
    public final  static String UNINSTALL ="uninstall";
    /**
     * 启动操作
     */
    public final  static String START="start";
    /**
     * 停止操作
     */
    public final  static String STOP="stop";
    /**
     * 软件名称
     */
    private String appName;
    /**
     * 操作ID
     */
    private String id;
    /**
     * 应用所在ip
     */
    private String ip;
    /**
     * 端口
     */
    private String port;
    /**
     * 进程号
     */
    private Integer processNo;
    /**
     * 进程名
     */
    private String processName;
    /**
     * 软件类型
     */
    private String  type;
    /**
     * 文件在ftp上的路径
     */
    private String  ftpPath;
    /**
     * ftp上的文件名
     */
    private String ftpFileName;
    /**
     * 执行的操作(取值仅限本类定义的操作类型)
     */
    private String action;
    /**
     * 操作时间
     */
    private Date  actionDate;
    /**
     * 执行文件的路径
     */
    private String actionPath;
    /**
     * 结果码
     * 0:失败
     * 1:成功
     */
    private Integer code;
    /**
     * 进程信息
     */
    private List<ProcessInfo> processInfoList;
    /**
     * 当前取到的进程数
     */
    private volatile int processCount;
    
    private AtomicInteger processCountv = new AtomicInteger(1);
    
    /**
     * 结果消息
     */
    private String mes;
    /**
     * 操作ID
     */
    public String getId() {
        return id;
    }
    /**
     * ftp上的文件名
     */
    public String getFtpFileName() {
        return ftpFileName;
    }
    /**
     * 进程号
     */
    public void setProcessNo(Integer processNo) {
        this.processNo = processNo;
    }
    /**
     * 结果码
     * 0:失败
     * 1:成功
     */
    public Integer getCode() {
        return code;
    }
    /**
     * 结果码
     * 0:失败
     * 1:成功
     */
    public void setCode(Integer code) {
        this.code = code;
    }
    /**
     * 结果消息
     */
    public String getMes() {
        return mes;
    }
    /**
     * 结果消息
     */
    public void setMes(String mes) {
        this.mes = mes;
    }
    /**
     * ftp上的文件名
     */
    public void setFtpFileName(String ftpFileName) {
        this.ftpFileName = ftpFileName;
    }
    /**
     * 执行文件的路径
     */
    public String getActionPath() {
        return actionPath;
    }
    /**
     * 进程名
     */
    public String getProcessName() {
        return processName;
    }
    /**
     * 进程名
     */
    public void setProcessName(String processName) {
        this.processName = processName;
    }
    /**
     * 执行文件的路径
     */
    public void setActionPath(String actionPath) {
        this.actionPath = actionPath;
    }
    /**
     * 操作ID
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * 操作时间
     */
    public Date getActionDate() {
        return actionDate;
    }
    /**
     * 操作时间
     */
    public void setActionDate(Date actionDate) {
        this.actionDate = actionDate;
    }
    /**
     * 执行的操作(取值仅限本类定义的操作类型)
     */
    public String getAction() {
        return action;
    }
    /**
     * 执行的操作(取值仅限本类定义的操作类型)
     */
    public void setAction(String action) {
        this.action = action;
    }
    /**
     * 软件类型
     */
    public String getType() {
        return type;
    }
    /**
     * 软件类型
     */
    public void setType(String type) {
        this.type = type;
    }
    /**
     * 文件在ftp上的路径
     */
    public String getFtpPath() {
        return ftpPath;
    }
    /**
     * 文件在ftp上的路径
     */
    public void setFtpPath(String ftpPath) {
        this.ftpPath = ftpPath;
    }
    /**
     * 应用所在ip
     */
    public String getIp() {
        return ip;
    }
    /**
     * 应用所在ip
     */
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    /**
     * 端口
     */
    public String getPort() {
        return port;
    }
    
    /**
     * 端口
     */
    public void setPort(String port) {
        this.port = port;
    }
    
    /**
     * 进程信息
     */
	public List<ProcessInfo> getProcessInfoList() {
		return processInfoList;
	}
	/**
     * 进程信息
     */
	public void setProcessInfoList(List<ProcessInfo> processInfoList) {
		this.processInfoList = processInfoList;
	}
	
	/**
     * 进程号
     */
	public Integer getProcessNo() {
		return processNo;
	}
	/**
     * 软件名称
     */
	public String getAppName() {
		return appName;
	}
	/**
     * 软件名称
     */
	public void setAppName(String appName) {
		this.appName = appName;
	}
	/**
     * 当前取到的进程数
     */
	public int getProcessCount() {
		return processCount;
	}
	/**
     * 当前取到的进程数
     */
	public void setProcessCount(int processCount) {
		this.processCount = processCount;
	}
	public AtomicInteger getProcessCountv() {
		return processCountv;
	}
	public void setProcessCountv(AtomicInteger processCountv) {
		this.processCountv = processCountv;
	}
	
	
}
