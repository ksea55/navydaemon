package com.navy.daemon.action;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.ws.Endpoint;

import com.navy.daemon.BootStart;

public class ActionClassloader extends URLClassLoader {
	/**
	 * ������Ӧ���������
	 */
	private static ConcurrentHashMap<String, ActionClassloader> classloaders = new ConcurrentHashMap<String, ActionClassloader>();

	/**
	 * �ֽ����ļ���׺
	 */
	private final String fileType = ".class";
	/**
	 * ����·��
	 */
	private URL url;
	/**
	 * ���������ص���
	 */
	private ConcurrentHashMap<String, Class<?>> dynaclazns = new ConcurrentHashMap<String, Class<?>>();

	/**
	 * ��ʼ���������
	 * 
	 * @param urls
	 *            �������jar�� ��Դ·��
	 */
	public ActionClassloader(URL url) {
		super(new URL[] { url }, ClassLoader.getSystemClassLoader());
		this.url = url;
	}

	/**
	 * ��ȡ.class�ļ����ֽ�����
	 * 
	 * @param name
	 *            ����
	 * @return
	 */
	private byte[] loaderClassData(String name) {
		InputStream is = null;
		byte[] data = null;
		ByteArrayOutputStream baos = null;
		try {
			String name0 = name.replace(".", "/") + fileType;
			// System.out.println("class path:"+name0);
			File f = new File(url.toURI());

			if (f.exists()
					&& (f.getName().endsWith(".jar") || f.getName().endsWith(
							".zip"))) {
				JarFile jf = new JarFile(f.getAbsolutePath());
				Enumeration<JarEntry> jfs = jf.entries();
				while (jfs.hasMoreElements()) {
					JarEntry jfn = jfs.nextElement();
					if (jfn.getName().equals(name0)) {
						System.out.println("jar file:" + jfn.getName()
								+ " found ? " + (jfn.getName().equals(name0)));
						is = jf.getInputStream(jfn);
						baos = new ByteArrayOutputStream();
						int c = 0;
						while (-1 != (c = is.read())) {
							baos.write(c);
						}
						data = baos.toByteArray();
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != is)
					is.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				if (null != baos)
					baos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return data;
	}
	
	/**
	 * ���ر�classloader ����url jar�ļ��ڵ� class
	 */
	private void loadJarClasses() throws Exception{
		File jar = null;
		try {
			jar = new File(url.toURI());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		if (null != jar
				&& jar.exists()
				&& (jar.getName().endsWith(".jar") || jar.getName().endsWith(
						".zip"))) {

			InputStream is = null;
			byte[] data = null;
			ByteArrayOutputStream baos = null;

			try {
				JarFile jf = new JarFile(jar.getAbsolutePath());
				Enumeration<JarEntry> jfs = jf.entries();
				while (jfs.hasMoreElements()) {
					try {
						JarEntry jfn = jfs.nextElement();
						System.out.println("load jar file:" + jfn.getName());
						int classindx = jfn.getName().lastIndexOf(fileType);
						if (classindx <= -1)
							continue;
						is = jf.getInputStream(jfn);
						baos = new ByteArrayOutputStream();
						int c = 0;
						while (-1 != (c = is.read())) {
							baos.write(c);
						}
						data = baos.toByteArray();
						String className = jfn.getName();
						className = className.replace("/", ".");
						classindx = className.lastIndexOf(fileType);
						className = className.substring(0, className
								.lastIndexOf(fileType));
						
						Class<?> clzo = super.defineClass(className, data, 0,
								data.length);
						
						System.out.println("load class:" + className);
						dynaclazns.put(className, clzo);
					} catch (Exception e) {
						dynaclazns.clear();
						throw e;
					} finally {
						try {
							if (null != is)
								is.close();
						} catch (Exception e) {
							e.printStackTrace();
						}

						try {
							if (null != baos)
								baos.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {

			}
		}
	}

	/**
	 * ��ȡClass����
	 * 
	 * @param className
	 *            ���� ͬ class.getName()
	 */
	/*@Override
	public Class<?> findClass(String className) throws ClassNotFoundException {
		System.out.println("find class:" + className);
		if (dynaclazns.containsKey(className)) {
			System.out.println(className+"class has loaded");
			return dynaclazns.get(className);
		}
		Class<?> clzo = null;
		
		byte[] data = loaderClassData(className);
		if (null != data) {
			clzo = this.defineClass(className, data, 0, data.length);
		}else{
			try{
				clzo = super.findClass(className);
			}catch(Exception e){
			}
		}
		
		if(null == clzo){
			try{
				System.out.println("Class.forName");
				clzo = Class.forName(className);
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		if(null == clzo){
			System.out.println("not found class:" + className);
			throw new ClassNotFoundException(
					"ActionClassloader can not found class:" + className);
		}
		dynaclazns.put(className, clzo);
		System.out.println("find class:" + className + " load byte ");
		return clzo;
	}*/


	/**
	 * ��дclassloader ʵ�����滻����ͬ����ȼ��أ�
	 * 
	 * @param className
	 *            ���� ͬ class.getName()
	 * @param resolve
	 *            ǿ����
	 * @return Class<?> ���ֽ���
	 */
	/*@Override
	protected Class<?> loadClass(String className, boolean resolve)
			throws ClassNotFoundException {
		Class<?> cls = null;
		cls = findLoadedClass(className);
		if (!this.dynaclazns.containsKey(className) && cls == null)
			cls = getSystemClassLoader().loadClass(className);
		if (cls == null)
			throw new ClassNotFoundException(className);
		if (resolve)
			resolveClass(cls);
		return cls;
	}*/

	/**
	 * ��дclassloader ʵ�����滻����ͬ����ȼ��أ�
	 * 
	 * @param className
	 *            ���� ͬ class.getName()
	 * @return Class<?> ���ֽ���
	 */
	
	/*@Override
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		Class<?> cls = null;
		cls = findLoadedClass(className);
		if (!this.dynaclazns.containsKey(className) && cls == null)
			cls = getSystemClassLoader().loadClass(className);
		if (cls == null)
			throw new ClassNotFoundException(className);
		resolveClass(cls);
		return cls;
	}*/

	/**
	 * ж��jar����ǰ�����ö���ȫ���������
	 * 
	 * @param jar��λ��
	 * @return
	 */
	public static boolean unloadJars(URL url) {
		if (null == url) return false;
		String path = null;
		try {
			path = url.toURI().toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		if(null == path ) return false;
		
		// ���������������
		ActionClassloader myClassLoader = classloaders.get(path);
		if(null != myClassLoader){
			classloaders.remove(path);
			myClassLoader = null;
			System.gc();
		}
		
		return true;
	}

	/**
	 * ���ز����
	 * 
	 * @param clz
	 *            �������ӿڵ�ʵ����ȫ·������
	 * @param jarpaths
	 *            ���������Դ·��
	 */
	/*public static boolean loadJars(String clz, URL jarpath) {
		ActionClassloader myClassLoader = null;
		boolean flag = true;
		try {
			// ���������������
			if (null != jarpath) {
				File jar = new File(jarpath.toURI());
				if (!jar.getName().endsWith(".jar")) return false;
				
				myClassLoader = classloaders.get(jarpath.toURI().toString());
				if (null != myClassLoader){
					
				}else {
					System.out.println("loadJars:"+jar.getName());
					myClassLoader = new ActionClassloader(jarpath);
					myClassLoader.url = jarpath;
					classloaders.put(jarpath.toURI().toString(), myClassLoader);
					myClassLoader.loadJarClasses();
				}
				
				Class<?> clz0 = myClassLoader.findClass(clz);

				if (null == clz0) {
					throw new ClassNotFoundException(
							"ActionClassloader can not found class:" + clz);
				}
				if (!IAction.class.isAssignableFrom(clz0)) {
					throw new IllegalAccessException(
							"not a ssignable From IAction type");
				}
			}

		} catch (ClassNotFoundException e) {
			if(null != myClassLoader) unloadJars(myClassLoader.url);
			flag = false;
		} catch (IllegalAccessException e) {
			if(null != myClassLoader) unloadJars(myClassLoader.url);
			flag = false;
		} catch (Exception e) {
			if(null != myClassLoader) unloadJars(myClassLoader.url);
			flag = false;
			e.printStackTrace();
		}
		return flag;
	}*/
	
	/**
	 * ���ز����
	 * 
	 * @param clz
	 *            �������ӿڵ�ʵ����ȫ·������
	 * @param jarpaths
	 *            ���������Դ·��
	 */
	/*public static boolean loadJars(URL jarpath) {
		ActionClassloader myClassLoader = null;
		boolean flag = true;
		try {
			// ���������������
			if (null != jarpath) {
				File jar = new File(jarpath.toURI());
				
				if (jar.getName().endsWith(".jar")) return false;
				
				myClassLoader = classloaders.get(jarpath.toURI().toString());
				if (null != myClassLoader)
					myClassLoader.url = jarpath;
				else {
					myClassLoader = new ActionClassloader(jarpath);
					myClassLoader.url = jarpath;
					classloaders.put(jarpath.toURI().toString(), myClassLoader);
				}
				
				myClassLoader.loadJarClasses();
				
				System.out.println("load jars:" + jarpath.toURI().toString());
			}

		} catch (ClassNotFoundException e) {
			if(null != myClassLoader) unloadJars(myClassLoader.url);
			flag = false;
		} catch (IllegalAccessException e) {
			if(null != myClassLoader) unloadJars(myClassLoader.url);
			flag = false;
		} catch (Exception e) {
			if(null != myClassLoader) unloadJars(myClassLoader.url);
			flag = false;
			e.printStackTrace();
		}
		return flag;
	}*/

	
	public static boolean loadJars(String clz, URL url) {
		boolean flag = true;
		if (url != null ) {
			// ��URLClassLoader���л�ȡ�������ļ��еķ���
			// ����jar�ļ����������Ϊһ�����class�ļ����ļ��� Method
			Method method = null;
			try {
				method = URLClassLoader.class.getDeclaredMethod("addURL",
						URL.class);
			} catch (NoSuchMethodException e1) {
				e1.printStackTrace();
			} catch (SecurityException e1) {
				e1.printStackTrace();
			}
			if (null == method)
				return false;
			boolean accessible = method.isAccessible(); // ��ȡ�����ķ���Ȩ��
			try {
				if (accessible == false) {
					method.setAccessible(true); // ���÷����ķ���Ȩ��
				} 
				// ��ȡϵͳ������� 
				URLClassLoader classLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
				
				try {
					method.invoke(classLoader, url);
					classloaders.put(clz, new ActionClassloader(url));
					Class clz0 = classLoader.loadClass(clz);
					if (!IAction.class.isAssignableFrom(clz0)) {
						throw new IllegalAccessException(
								"not a ssignable From IAction type");
					}
					
					System.out.println("��ȡjar�ļ�:" + url.toString());
				} catch (Exception e) {
					classloaders.remove(clz);
					flag = false;
					System.out.println("��ȡjar�ļ�ʧ��:" + url.toString());
				}
			} catch (Exception e) {
				classloaders.remove(clz);
				flag = false;
				e.printStackTrace();
			} finally {
				method.setAccessible(accessible);
			}
		}
		return flag;
	}/**/

	/**
	 * ��ȡ��·��ָ����Class
	 * 
	 * @param clz
	 *            �������ӿڵ�ʵ����ȫ·������
	 * @return ���Ҳ�����Ӧ���෵��null
	 */
	public static Class<?> getClass(String clz) {
		if (null == clz)
			return null;
		Class<?> cls = null;
		Iterator<ActionClassloader> loders = classloaders.values().iterator();
		ActionClassloader loader = null;
		
		try {
			cls = Class.forName(clz);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		if(null == cls){
			while(loders.hasNext()){
				loader = loders.next();
				try {
					cls = loader.findClass(clz);
					System.out.println(cls.getClassLoader().getClass().getName());//?????????????????????
					if(null != cls) break;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		
		return cls;
	}

	/**
	 * ��ȡ��·��ָ����ActionClassloader�������
	 * 
	 * @param clz
	 *            �������ӿڵ�ʵ����ȫ·������
	 * @return ���Ҳ�����Ӧ���෵��null
	 */
	public static ActionClassloader getClassLoder(String clz) {
		if (null == clz)
			return null;
		ActionClassloader cload = null;
		try {
			cload = (ActionClassloader) getClass(clz).getClassLoader();
		} catch (Exception e) {
		}

		return cload;
	}

	/**
	 * ��clz����������һ��ʵ��������
	 * 
	 * @param clz
	 *            �������ӿڵ�ʵ����ȫ·������
	 * @return ���Ҳ�����Ӧ���෵��null
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static <T extends IAction> T newInstance(String clz)
			throws InstantiationException, IllegalAccessException,
			ClassCastException {
		if (null == clz)
			return null;
		Class<T> clzo = null;
		Class<?> clzo1 = getClass(clz);
		if (null == clzo1 || !IAction.class.isAssignableFrom(clzo1)) {
			throw new IllegalAccessException(
					"not a ssignable From IAction type");
		}
		clzo = (Class<T>) clzo1;
		return clzo.newInstance();
	}

	/**
	 * 
	 * @param <T>
	 * @param clz
	 *            �������ӿڵ�ʵ����Class��������
	 * @return ������û���ṩ�޲�ʵ�������캯�����׳��쳣
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static <T extends IAction> T newInstance(Class<T> clz)
			throws InstantiationException, IllegalAccessException {
		if (null == clz)
			return null;
		return clz.newInstance();
	}

	/**
	 * ������Ĺ��캯��ʵ����һ��
	 * 
	 * @param <T>
	 *            clzָ����������
	 * @param clz
	 *            ���������������
	 * @param parameterclz
	 *            clzָ����Ĺ��캯��������������
	 * @param params
	 *            ���캯������ֵ
	 * @return ��û����clzָ�������ҵ�parameterclzָ���Ĺ��캯��������������׳��쳣
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static <T extends IAction> T newInstance(Class<T> clz,
			Class[] parameterclz, Object[] params)
			throws InstantiationException, IllegalAccessException {
		if (null == clz)
			return null;
		T obj = null;
		try {
			obj = clz.getConstructor(parameterclz).newInstance(params);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	
	public static void main(String args[]){
		String classz = "cn.com.sinux.hjqb.websevice.service.imp.MMPWebServiceImpl";
		File jar = new File("F:\\Work\\workspace3\\navydaemon\\pluginlib\\cntest.jar");
		File jar2 = new File("F:\\Work\\workspace3\\navydaemon\\pluginlib\\cntest2.jar");
		System.out.println(BootStart.class.getClassLoader().getClass().getName());
		try {
			loadJars(classz, jar.toURL());
			Object imp = newInstance(classz);
			String url ="http://192.168.3.156:6553/cn.com.sinux.hjqb.websevice.service/MMPWebService";
			Endpoint address = Endpoint.publish(url, imp);
			
			Thread.sleep(15000);
			address.stop();
			System.out.println("stoped");
			Thread.sleep(10000);
			loadJars(classz, jar2.toURL());
			imp = newInstance(classz);
			address = Endpoint.publish(url, imp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	}
}
