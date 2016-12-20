package com.navy.daemon.action.report;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


import com.navy.daemon.BootStart;
import com.navy.daemon.action.AbstractAction;
import com.navy.daemon.action.IReporter;
import com.navy.daemon.action.WSServerFactory;
import com.navy.daemon.conf.SynConfig;

/**
 * ����java��ʱ��TimerTaskʵ��״̬���й���
 * <li>������IReporter�ӿ�ָ���Ķ�ʱ����������й���</li>
 * <li>��IReporter</li>
 * @author mup
 */
public abstract class AbstarctReporter extends AbstractAction implements IReporter{
	/**
	 * Ĭ�ϳ�ʼ����ʱʱ�䣨0�룩
	 */
	public final static long default_initialDelay = 0;
	/**
	 * Ĭ��ִ�����ڼ����5�룩
	 */
	public final static long default_period = 3;
	/**
	 * ��ʼ����ʱʱ��/ִ�����ڼ����ʱ�䵥λ���룩
	 */
	public final static TimeUnit default_unit = TimeUnit.SECONDS;
	
	
	public static final ConcurrentHashMap<String, ScheduledThreadPoolExecutor> repoterRunners 
		= new ConcurrentHashMap<String, ScheduledThreadPoolExecutor>();
	
	public static final ConcurrentHashMap<String, QuartzCron> quartzCrons
		= new ConcurrentHashMap<String, QuartzCron>();
	
	/**
	 * reporter ��ʱִ�й���
	 * @author mup
	 *
	 */
	static final class QuartzCron{
		private IReporter impObject;
		private long initialDelay;
		private long period;
		private TimeUnit unit;
		/**
		 * �Ƿ�������
		 */
		private AtomicBoolean timerStarted = new AtomicBoolean(false);
	}
	
	@Override
	public final void setQuartzCron(long initialDelay, long period, TimeUnit unit) {
		QuartzCron cron = null;
		String reporterId = this.getReporterId();
		synchronized (reporterId) {
			if(quartzCrons.containsKey(reporterId)){
				cron = quartzCrons.get(reporterId);
			}else{
				cron = new QuartzCron();
				cron.impObject = this;
				quartzCrons.put(reporterId, cron);
			}
			cron.initialDelay = initialDelay;
			cron.period = period;
			cron.unit = unit;
		}
	}
	
	@Override
	public final boolean startRuner() {
		List<? extends Runnable> tasks = this.getTasks();
		System.out.println("startRuner:"+this.getReporterId()+",with:"+(null == tasks?0:tasks.size()));
		System.out.println("startRuner task is null?:"+(null == tasks));
		if(null == tasks || tasks.size() < 1) return false;
		QuartzCron cron = null;
		//��ȡ�����б�
		synchronized (this.getReporterId()) {
			cron = quartzCrons.get(this.getReporterId());
			
			if(null == cron){
				cron = new QuartzCron();
				quartzCrons.put(this.getReporterId(), cron);
				cron.initialDelay = AbstarctReporter.default_initialDelay;
				cron.period = AbstarctReporter.default_period;
				cron.unit = AbstarctReporter.default_unit;
				cron.impObject = this;
			}
			
			if(cron.timerStarted.compareAndSet(false, true)){
				cron.timerStarted.set( call(cron) );
			}
		}
		
		return null == cron ? false : cron.timerStarted.get();
	}

	@Override
	public final boolean stop() {
		QuartzCron cron = null;
		synchronized (this.getReporterId()) {
			cron = quartzCrons.get(this.getReporterId());
		}
		if(null != cron && cron.timerStarted.get()){
			try{
				ScheduledThreadPoolExecutor stpe = repoterRunners.get(this.getReporterId());
				stpe.shutdown();
		        while(!stpe.isTerminated()){
		        	try {
						stpe.awaitTermination(cron.period, cron.unit);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
		        }
		        try {
		        	stpe.shutdownNow();
		        } catch (Exception e) {
					e.printStackTrace();
				}
		        
		        repoterRunners.remove(this.getReporterId());
		        stpe = null;
		        cron.timerStarted.set(false);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	/**
	 * Ϊĳ���������ö�ʱ���й�����������ʱ������Ч��
	 * @param reporterId
	 * @param initialDelay
	 * @param period
	 * @param unit
	 */
	public static void setQuartzCron_0(String reporterId, long initialDelay, long period, TimeUnit unit) {
		QuartzCron cron = null;
		System.out.println("request start reporter:"+reporterId);
		if(null == reporterId) return;
		//�жϱ�������Ƿ��ѷ���
		IReporter ireport = BootStart.rePorters.get(reporterId);
		if(null == ireport) return;
		
		synchronized (reporterId) {
			if(quartzCrons.containsKey(reporterId)){
				cron = quartzCrons.get(reporterId);
			}else{
				cron = new QuartzCron();
				cron.impObject = ireport;
				quartzCrons.put(reporterId, cron);
			}
			cron.initialDelay = initialDelay;
			cron.period = period;
			cron.unit = unit;
		}
	}
	
	/**
	 * ������ʱ�߳�
	 * @param cron ��ʱ����ͱ������
	 * @return
	 */
	private static final boolean call(QuartzCron cron){
		boolean flag = false;
		try{
			List<? extends Runnable> tasks = cron.impObject.getTasks();
			if(null == tasks || tasks.size() < 1) return false;
			
			ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(SynConfig.apps.size());
			stpe.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
			stpe.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
			System.out.println("pre started"+stpe.getTaskCount());
			for(Runnable task : tasks){
				try{
					stpe.scheduleAtFixedRate(task, cron.initialDelay, cron.period, cron.unit);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			repoterRunners.put(cron.impObject.getReporterId(), stpe);
			System.out.println("started"+stpe.getTaskCount());
			flag = true;
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return flag;
	}
	
	
	/**
	 * ����reportIdָ���ı���
	 * @param reportId ����ʵ����ȫ·����
	 * @return
	 */
	public static boolean startTheRuner_0(String reportId) {
		System.out.println("request start reporter:"+reportId);
		if(null == reportId) return false;
		//�жϱ�������Ƿ��ѷ���
		IReporter ireport = BootStart.rePorters.get(reportId);
		if(null == ireport) return false;
		
		QuartzCron cron = null;
		synchronized (reportId) {
			
			cron = quartzCrons.get(reportId);
			
			if(null == cron){
				cron = new AbstarctReporter.QuartzCron();
				quartzCrons.put(reportId, cron);
				cron.initialDelay = AbstarctReporter.default_initialDelay;
				cron.period = AbstarctReporter.default_period;
				cron.unit = AbstarctReporter.default_unit;
				cron.impObject = ireport;
			}
			
			if(cron.timerStarted.compareAndSet(false, true)){
				cron.timerStarted.set( call(cron) );
			}
		}
		
		if(null != cron && !cron.timerStarted.get()){
			System.out.println("has reporter:"+reportId);
			cron.timerStarted.set( call(cron) );
		}
		
		return null == cron ? false : cron.timerStarted.get();
	}
	
	/**
	 * �ر�reportIdָ���ı���
	 * @param reportId ����ʵ����ȫ·����
	 * @return
	 */
	public static boolean stopTheReport_0(String reportId){
		System.out.println("request stop reporter:"+reportId);
		if(null == reportId) return false;
		//�жϱ�������Ƿ��ѷ���
		IReporter ireport = BootStart.rePorters.get(reportId);
		if(null == ireport) return false;
		
		QuartzCron cron = null;
		synchronized (reportId) {
			cron = quartzCrons.get(reportId);
		}
		if(null != cron && cron.timerStarted.get()){
			System.out.println("has reporter:"+reportId);
			try{
				ScheduledThreadPoolExecutor stpe = repoterRunners.get(reportId);
				stpe.shutdown();
				System.out.println("reporter:"+reportId+" shutdown");
		        while(!stpe.isTerminated()){
		        	try {
						stpe.awaitTermination(cron.period, cron.unit);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
		        }
		        try {
		        	stpe.shutdownNow();
		        } catch (Exception e) {
					e.printStackTrace();
				}
		        
		        repoterRunners.remove(reportId);
		        stpe = null;
		        cron.timerStarted.set(false);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	@Override
	public void close() {
		 stop();
		 quartzCrons.remove(this.getReporterId());
	}

	@Override
	public void destroy() {
	}

	@Override
	public void init() {
	}

	@Override
	public String getReporterId() {
		return this.getClass().getName();
	}

}
