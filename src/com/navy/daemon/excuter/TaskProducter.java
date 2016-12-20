package com.navy.daemon.excuter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import com.navy.daemon.excuter.TaskExcuterGroup.DataStack;

public abstract class TaskProducter <T> extends Thread implements Callable<T>{
	public abstract T doProduct();
	public abstract void setParams(Object... params);
	private DataStack stack;
	private TaskExcuterGroup group;
	//-1：线程初始化状态，0：已请求关闭，1：已结束
	private AtomicInteger isStoped = new AtomicInteger(-1);
	private String procName;
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
	
	public final void setProcName(String procName){
		this.procName = procName;
	}
	
	public final String getProcName(){
		return procName;
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
			System.out.println("request stopAccept product Thread.........."+procName);
			//Thread.currentThread().interrupt();
		}
	}
	
	@Override
	public final T call() {
		T o = null;
		try{
			o = doProduct();
			if(null != o) stack.writeData(o);
		}catch(InterruptedException e){
			if(-1 == isStoped.get()){
				e.printStackTrace();
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			System.out.println("stoped productor:"+procName);
			try{
				stack.removeTaskProducter(this);
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
		return o;
	}
}
