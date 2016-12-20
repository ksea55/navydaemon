
/**
 * com.cetca.util.db
 * ConnectionWrapper.java
 * 
 * 2013-7-23-下午05:42:40
 * 
 */
package com.navy.daemon.db;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * <b>项目名称 </b>JAVA<br>
 * <b>类名称 </b>com.cetca.util.db.ConnectionWrapper<br>
 * <b>类描述 </b>TODO(Connection接口的代理类，用于拦截Connection接口的方法调用。在调用Connection的close方法时，并非关闭连接，而是将该活跃的连接释放到空闲状态。要真实地关闭连接则调用本类的close方法)<br>
 * <b>创建时间 </b>2013-7-23-下午05:42:40<br>
 * <b>@author </b>ZhaoLongFei<br>
 * <b>@Copyright </b>2013-
 */
public final class ConnectionProxy implements InvocationHandler
{
  private static final String CLOSE_METHOD_NAME = "close";
 // private SimpleConnectionPool connectionPool;
  private Connection connection;
  private Connection originConnection;
  private long lastAccessTime;
  private boolean isClosed;
  private AtomicBoolean isavi = new AtomicBoolean(false);
  
  private static ConnectionProxy proxy;
  
  /**
	 * 获取数据库连接
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static Connection getConn_Sqlite(File conf) throws ClassNotFoundException, SQLException{
		String dburl = "jdbc:sqlite://"+conf.getAbsolutePath()+File.separator+"aca.db";
		System.out.println(dburl);
		String jdbccla = "org.sqlite.JDBC";
		String user = "main";
		String pwd = "aca1qaz_PL<";
		Class.forName(jdbccla);
		Connection conn = DriverManager.getConnection(dburl, user, pwd);
		//SQLiteConnection d;
		return conn;
	}
  
  public static synchronized Connection getConnection(File conf) throws SQLException, ClassNotFoundException{
	  if( null== proxy) {
		  proxy = new ConnectionProxy(getConn_Sqlite(conf));
	  }
	  return proxy.getConnection();
  }
  	/**
	 * 
	 * 创建新的实例.
	 * ConnectionProxy.
	 *
	 * @param conn
	 * @param connectionPool
	 * @throws SQLException
	 */
  public ConnectionProxy(Connection conn)
    throws SQLException
  {
    if ((conn == null) || (conn.isClosed()) ) {
      throw new IllegalArgumentException();
    }
    //返回一个指定接口的代理类实例
    this.connection = ((Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[] { Connection.class }, this));
    this.originConnection = conn;
    this.lastAccessTime = System.currentTimeMillis();
  }
  
  /**
	 * <b>方法描述：</b>TODO(关闭被代理的连接)<br>
	 * @throws SQLException
	 */
  public void close()
    throws SQLException
  {
    if ((this.connection != null) && (!this.originConnection.isClosed())) {
      this.originConnection.close();
      this.originConnection = null;
      this.connection = null;
      this.isClosed = true;
      System.out.println("close connection success!");
    }
  }
  
  /**
	 * <b>方法描述：</b>TODO(被代理的连接是否已关闭)<br>
	 * @return
	 */
  public boolean isClosed()
  {
    boolean isclosed = true;
    try {
      isclosed = this.originConnection.isClosed();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return isclosed;
  }
  /**
	 * <b>方法描述：</b>TODO(获得被接管后的连接)<br>
	 * @return
	 */
  public Connection getConnection()
  {
	isavi.compareAndSet(true, false);
    return this.connection;
  }

  /**
	 * <b>方法描述：</b>TODO(获得被代理的连接最后一次访问的时间毫秒数)<br>
	 * @return
	 */
  public long getLastAccessTime()
  {
    return this.lastAccessTime;
  }
  /* 
	 * 代理的拦截方法
	 */
  public Object invoke(Object proxy, Method method, Object[] args)
    throws Throwable
  {
	  Object obj = null;
    if (CLOSE_METHOD_NAME.equals(method.getName())) {
    	isavi.set(true);
    }else{
    	synchronized (this.originConnection) {
    		obj = method.invoke(this.originConnection, args);
		}
    }
    this.lastAccessTime = System.currentTimeMillis();
    return obj;
  }
}