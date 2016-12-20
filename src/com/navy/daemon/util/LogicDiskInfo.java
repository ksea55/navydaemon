package com.navy.daemon.util;
/**
 * 系统逻辑分区信息
 * @author mup
 *
 */
public class LogicDiskInfo {
	/**
	 *标准名
	 */
	public String Caption;
	/**
	 * 分区设备描述（设备类型）
	 */
	public String Description;
	/**
	 * 分区唯一ID（盘符或路径）
	 */
	public String DeviceID;
	/**
	 * 分区类型
	 */
	public String DriveType;
	/**
	 * 文件系统
	 */
	public String FileSystem;
	/**
	 * 剩余空间单位为M
	 * ，值为-1，表示设备空挂，不可用
	 */
	public double FreeSpace;
	/**
	 * 分区大小单位为M
	 * ，值为-1，表示设备空挂，不可用
	 */
	public double Size;
	/**
	 * 分区名
	 */
	public String VolumeName;
}
