package com.navy.daemon.action;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * 状态上报（采集器接口定义）
 * 此接口在IAction基础上构建循环定时采集线程运行与管控机制
 * @author mup
 *
 */
public interface IReporter extends IAction{
/**
 * 获取报告的唯一ReporterId
 * 默认为报告类的类全路径
 * @return
 */
public String getReporterId();
/**
 * 定时规则 ：（需重启定时器后生效）
 * 创建并执行一个在给定初始延迟后首次启用的定期操作，后续操作具有给定的周期；
 * 也就是将在 initialDelay 后开始执行，
 * 然后在initialDelay+period 后执行，接着在 initialDelay + 2 * period 后执行，依此类推。
 * @param initialDelay 初始延迟时间
 * @param period 间隔周期
 * @param TimeUnitunit unit时间单位
 */
 public void setQuartzCron(long initialDelay, long period, TimeUnit unit);
 /**
  * 上报停止方法
  * @return 
  *    停止成功返回true，失败返回false
  */
 public boolean stop();
 
 /**
  * 启动上报线程
  * @return 
  *    启动成功返回true，失败返回false
  */
 public boolean startRuner();
 
 /**
  * 获取报告任务列表
  * @return
  */
 public ArrayList<? extends Runnable> getTasks();
}
