package com.navy.daemon.excuter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import com.navy.daemon.excuter.TaskExcuterGroup.DataStack;



public abstract class TaskListener<R> implements Callable<R>{
	public abstract void doListen(Object data);
	public abstract void setParams(Object... params);
	//-1：线程初始化状态，0：已请求关闭，1：已结束
	private AtomicInteger isStoped = new AtomicInteger(-1);
	private DataStack stack;
	private TaskExcuterGroup group;
	private String listenerName;
	private ArrayBlockingQueue<Boolean> complete = new ArrayBlockingQueue<Boolean>(1);
	
	/**
	 * 等待线程结束
	 * @return
	 * @throws InterruptedException
	 */
	public final boolean waitForComplete() throws InterruptedException{
		if(isStoped.get() == 1){
			return true;
		}
		boolean flag = complete.take();
		complete = null;
		return flag;
	}
	
	public final void setListenerName(String listenerName){
		this.listenerName = listenerName;
	}
	
	public final String getListenerName(){
		return listenerName;
	}
	
	final void setStack(DataStack stack){
		this.stack = stack;
	}
	
	public final DataStack getStack(){
		return this.stack;
	}
	
	final void setTaskExcuterGroup(TaskExcuterGroup group){
		this.group = group;
	}
	
	public final TaskExcuterGroup getTaskExcuterGroup(){
		return this.group;
	}
	
	public void stopAccept(){
		//设置为请求结束状态
		if(isStoped.compareAndSet(-1, 0)){
			System.out.println("request stopAccept listen Thread.........."+listenerName);
			//Thread.currentThread().interrupt();
		}
	}
	
	@Override
	public final R call() throws Exception {
		Object o = null;
		try{
			while(true){
				o = stack.pollData();
				if(o!=null) doListen(o);
			}
		}catch(InterruptedException e){
			if(-1 == isStoped.get()){
				e.printStackTrace();
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			System.out.println("stoped listen"+listenerName);
			try{
				stack.removeTaskListener(this);
			}catch(Exception e){
				e.printStackTrace();
			}
			/*try {
				complete.put(true);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
			//设置为已结束状态
			isStoped.set(1);
		}
		return null;
	}
}
