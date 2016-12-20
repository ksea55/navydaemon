package com.navy.daemon.excuter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;



public class TaskExcuterGroup<T,R>{
	/**
	 * 操作系统cpu个数，用于创建线程池
	 */
	public static final int cpucount = Runtime.getRuntime().availableProcessors();
	private int producterThreads = 1;
	private int listenThreads = 1;
	private int stacks = cpucount;
	private int stackLength = cpucount*10;
	
	private ArrayList<DataStack<T,R>> created_stacks = new ArrayList<DataStack<T,R>>();
	
	/**
	 * 线程池，用于执行生产者线程
	 */
	protected ExecutorService producterPool;
	/**
	 * 线程池，用于执行消费者线程
	 */
	protected ExecutorService listenerPool;
	/**
	 * 生产者线程执行器
	 */
	protected ExecutorCompletionService<T> producterCompletionService;
	/**
	 * 消费者线程执行器
	 */
	protected ExecutorCompletionService<R> listenCompletionService;
	
	Class<? extends TaskProducter> proclz;
	Class<? extends TaskListener> listenclz;
	
	public TaskExcuterGroup(){
		producterPool = Executors.newFixedThreadPool(this.stacks * this.producterThreads);
		listenerPool = Executors.newFixedThreadPool(this.stacks * this.listenThreads);
		
		producterCompletionService = new ExecutorCompletionService<T>(producterPool);
		listenCompletionService = new ExecutorCompletionService<R>(listenerPool);
	}
	
	public TaskExcuterGroup(int stacks, int stackLength, int producterThreads, int listenThreads){
		this.stacks = stacks > 0 ? stacks : this.stacks;
		this.stackLength = stackLength > 0 ? stackLength : this.stackLength;
		
		this.producterThreads = producterThreads > 0 ? producterThreads : this.producterThreads;
		this.listenThreads = listenThreads > 0 ? listenThreads : this.listenThreads;
		
		producterPool = Executors.newFixedThreadPool(this.stacks * this.producterThreads);
		listenerPool = Executors.newFixedThreadPool(this.stacks * this.listenThreads);
		
		producterCompletionService = new ExecutorCompletionService<T>(producterPool);
		listenCompletionService = new ExecutorCompletionService<R>(listenerPool);
	}
	
	public Future<T> callTaskProducter(Object...params) throws Exception{
		TaskProducter<T> producter = null;
		Future<T> fu;
		producter = proclz.newInstance();
		producter.setTaskExcuterGroup(this);
		producter.setParams(params);
		
		Collections.sort(created_stacks, new DataStackComparator());
		DataStack<T,R> dataStak = created_stacks.get(0);
		
		dataStak.addTaskProducter(producter);
		producter.setStack(dataStak);
		
		StringBuffer procname = new StringBuffer();
		procname.append(dataStak.getId()).append("_").append(proclz.getName()).append("[")
			.append(dataStak.getTaskProducters().size()+1).append("]");
		producter.setProcName(procname.toString());
		procname.delete(0, procname.length());
		
		fu = producterCompletionService.submit(producter);
		dataStak.addTaskProducterFuture(fu);
		
		return fu;
	}
	
	/**
	 * 注册生产和监听线程
	 * @param proclz 生产线程 Class
	 * @param listenclz 监听线程 Class
	 */
	public void regist(Class<? extends TaskProducter> proclz, 
			Class<? extends TaskListener> listenclz) {
		this.proclz = proclz;
		this.listenclz = listenclz;
		
		DataStack<T,R> stack = null;
		TaskProducter<T> producter = null;
		TaskListener<R> listen = null;
		StringBuffer lisname = new StringBuffer();
		for(int k = 0; k < stacks; k++){
			stack = new DataStack<T,R>("TaskExcuterGroup["+this.hashCode()+"]_"+k);
			created_stacks.add(stack);
		
			for(int i=0; i< listenThreads; i++){
				try {
					listen = listenclz.newInstance();
					listen.setStack(stack);
					lisname.append(stack.getId()).append("_").append(listenclz.getName())
						.append("[").append(k).append("]");
					listen.setListenerName(lisname.toString());
					lisname.delete(0, lisname.length());
					stack.addTaskListener(listen);
					System.out.println("add listen:"+stack.getTaskListeners().size());
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void writeData(T data) throws InterruptedException{
		Collections.sort(created_stacks, new DataStackComparator());
		DataStack<T,R> dataStak = created_stacks.get(0);
		dataStak.writeData(data);
	}
	
	public T pollData() throws InterruptedException{
		Collections.sort(created_stacks, new DataStackComparator());
		DataStack<T,R> dataStak = created_stacks.get(created_stacks.size()-1);
		return dataStak.pollData();
	}
	
	/**
	 * 启动监听和生产线程组
	 */
	public void start(){
		for(DataStack<T,R> stack : created_stacks){
			//先启动监听
			List<TaskListener<R>> listens = stack.getTaskListeners();
			for(TaskListener<R> listen : listens){
				stack.addTaskListenerFuture(listenCompletionService.submit(listen));
			}
			
			//后启动生产
			/*List<TaskProducter<T>> pros = stack.getTaskProducters();
			for(TaskProducter<T> pro : pros){
				stack.addTaskProducterFuture(producterCompletionService.submit(pro));
			}*/
		}
	}
	
	/**
	 * stop监听和生产线程组
	 */
	public void stop(){
		int profsize = 0;
		int listensize = 0;
		
		producterPool.shutdown();
		listenerPool.shutdown();
		
		//先关闭生产,后关闭监听
		
		for(DataStack<T,R> stack : created_stacks){
			List<TaskProducter<T>> ps = stack.getTaskProducters();
			for(TaskProducter<T> p : ps){
				p.stopAccept();
			}
		}
		producterPool.shutdownNow();
		
		for(DataStack<T,R> stack : created_stacks){
			List<TaskListener<R>> ls = stack.getTaskListeners();
			for(TaskListener<R> s : ls){
				s.stopAccept();
			}
			
		}
		listenerPool.shutdownNow();
	}
	
	/**
	 * 释放资源
	 */
	public void release(){
		
	}
	
	/**
	 * 数据栈
	 * @author Administrator
	 *
	 */
	public class DataStack <T,R>{
		/**
		 * 数据栈可用空间
		 */
		AtomicInteger avilspace = new AtomicInteger();
		private String id;
		
		public String getId() {
			return id;
		}

		private ArrayBlockingQueue<T> dataStak;
		private List<TaskProducter<T>> producters;
		private List<TaskListener<R>> listens;
		
		private List<Future<T>> producterFs;
		private List<Future<R>> listenFs;
		
		public List<TaskProducter<T>> getTaskProducters(){
			return producters;
		}
		
		public List<TaskListener<R>> getTaskListeners(){
			return listens;
		}
		
		public void addTaskProducter(TaskProducter<T> pro){
			producters.add(pro);
		}
		
		public void addTaskListener(TaskListener<R> listen){
			listens.add(listen);
		}
		
		public void removeTaskListener(TaskListener<R> listen){
			listens.remove(listen);
		}
		
		public void removeTaskProducter(TaskProducter<T> pro){
			producters.remove(pro);
		}
		
		
		public List<Future<T>> getTaskProducterFutures(){
			return producterFs;
		}
		
		public List<Future<R>> getTaskListenerFutures(){
			return listenFs;
		}
		
		public void addTaskProducterFuture(Future<T> proF){
			producterFs.add(proF);
		}
		
		public void addTaskListenerFuture(Future<R> listenF){
			listenFs.add(listenF);
		}
		
		public void removeTaskListenerFuture(Future<R> listenF){
			listenFs.remove(listenF);
		}
		
		public void removeTaskProducterFuture(Future<T> proF){
			producterFs.remove(proF);
		}
		
		
		public DataStack(String id){
			this.id = id;
			avilspace.set(stackLength);
			dataStak = new ArrayBlockingQueue<T>(stackLength);
			
			producters = new ArrayList<TaskProducter<T>>();
			listens = new ArrayList<TaskListener<R>>();
			
			producterFs = new ArrayList<Future<T>>();
			listenFs = new ArrayList<Future<R>>();
		}
		
		public void writeData(T data) throws InterruptedException{
			dataStak.put(data);
			avilspace.decrementAndGet();
		}
		
		public T pollData() throws InterruptedException{
			T data = null;
			data = dataStak.take();
			avilspace.incrementAndGet();
			return data;
		}
	}
	
	class DataStackComparator implements Comparator<DataStack<T,R>>{
		@Override
		public int compare(DataStack<T,R> d1, DataStack<T,R> d2) {
	        if(d1.avilspace.get() < d2.avilspace.get()){
	            return 1;
	        }else{
	            return 0;
	        }
		}
	}
}
