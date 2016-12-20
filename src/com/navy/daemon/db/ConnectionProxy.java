
/**
 * com.cetca.util.db
 * ConnectionWrapper.java
 * 
 * 2013-7-23-����05:42:40
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
 * <b>��Ŀ���� </b>JAVA<br>
 * <b>������ </b>com.cetca.util.db.ConnectionWrapper<br>
 * <b>������ </b>TODO(Connection�ӿڵĴ����࣬��������Connection�ӿڵķ������á��ڵ���Connection��close����ʱ�����ǹر����ӣ����ǽ��û�Ծ�������ͷŵ�����״̬��Ҫ��ʵ�عر���������ñ����close����)<br>
 * <b>����ʱ�� </b>2013-7-23-����05:42:40<br>
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
	 * ��ȡ���ݿ�����
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
	 * �����µ�ʵ��.
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
    //����һ��ָ���ӿڵĴ�����ʵ��
    this.connection = ((Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[] { Connection.class }, this));
    this.originConnection = conn;
    this.lastAccessTime = System.currentTimeMillis();
  }
  
  /**
	 * <b>����������</b>TODO(�رձ����������)<br>
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
	 * <b>����������</b>TODO(������������Ƿ��ѹر�)<br>
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
	 * <b>����������</b>TODO(��ñ��ӹܺ������)<br>
	 * @return
	 */
  public Connection getConnection()
  {
	isavi.compareAndSet(true, false);
    return this.connection;
  }

  /**
	 * <b>����������</b>TODO(��ñ�������������һ�η��ʵ�ʱ�������)<br>
	 * @return
	 */
  public long getLastAccessTime()
  {
    return this.lastAccessTime;
  }
  /* 
	 * ��������ط���
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