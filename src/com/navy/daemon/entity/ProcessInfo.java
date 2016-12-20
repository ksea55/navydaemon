package com.navy.daemon.entity;

public class ProcessInfo {
    public String id;
    /**
     * 进程ID
     */
    public long ProcessId;
    /**
     * 父进程号
     */
    public long PProcessId;
    /**
     * 进程名称
     */
    public String ProcessName;
    /**
     * cpu使用率
     */
    public Double cpuUsage;
    /**
     * 内存使用率
     */
    public Double memoryUsage;
    /**
     * 
     */
    public Double VirtualSize;

    /**
     * 使用中内存
     */
    public Double memoryInuse;
}
