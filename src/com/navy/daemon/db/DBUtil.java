
/**
 * com.cetca.util.db
 * DBUtil.java
 * 
 * 2013-7-30-����09:09:57
 * 2013
 * 
 */
package com.navy.daemon.db;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>��Ŀ���� </b>cetcautil<br>
 * <b>������ </b>com.cetca.util.db.DBUtil<br>
 * <b>������ </b>TODO(���ݿ����������)<br>
 * <b>����ʱ�� </b>2013-7-30-����09:09:57<br>
 * <b>@author </b>mupan<br>
 * <b>@Copyright </b>2013
 */
public class DBUtil {
	private static final Map<Class<?>, String> beanTableMap = new ConcurrentHashMap<Class<?>, String>();
	
	/**
	 * <b>����������</b>TODO(Ϊ��ǰ�־û�����ע���Ӧ�����ݿ����)<br>
	 * @param clazz ����class����
	 * @param tableName ���ݿ����
	*/
	public static void registClass(Class<?> clazz, String tableName){
		if(null != clazz && null != tableName) beanTableMap.put(clazz, tableName);
	}
	
	/**
	 * <b>����������</b>TODO(ע��OR mapping)<br>
	 * @param clazz ����class����
	*/
	public static void unRegistClass(Class<?> clazz){
		if(null != clazz) beanTableMap.remove(clazz);
	}
	
	/**
	 * <b>����������</b>TODO(��ȡ��ǰ�����Ӧ�����ݿ����)<br>
	 * ��δע��clazzָ�������͵����ݿ����ʱ,��������Ϊ����
	 * @param clazz ����class����
	 * @return ���ض�Ӧ�����ݿ����
	*/
	private static String getTableName(Class<?> clazz){
		if(null == clazz) return null;
		synchronized (clazz) {
			if(beanTableMap.containsKey(clazz)){
				return beanTableMap.get(clazz);
			}else{
				return clazz.getSimpleName();
			}
		}
	}
	
	/**
	 * <b>����������</b>TODO(��obj��������PreparedStatement.executeUpdate()ͬ�����µ����ݿ�)<br>
	 * �÷���Ҫ��obj������������������ֱ�����ݿ�������ֶ���һ��
	 * @param <T>
	 * @param con ���ݿ�����
	 * @param obj  ͬ�����µ����ݿ�Ķ���
	 * @param updateFieldNames ÿ��������Ҫ���µ��ֶ���
	 * @param selectKeyNames ÿ��������²��õĲ�ѯ�����ֶ���
	 * @return ���µļ�¼�����ύ��Ч���ݷ��ؿգ����򷵻ض�Ӧ���³ɹ��ļ�¼����
	 * @throws SQLException
	*/
	public synchronized static <T>int excutUpdateStrictMap(Connection con, T obj, String[] updateFieldNames, String[] selectKeyNames) throws SQLException{
		if(null == updateFieldNames || null == selectKeyNames || null == obj || null == con || con.isClosed()){return 0;}
		int count = 0;
		PreparedStatement psta = null;
		try{
			Class<?> CL = obj.getClass();
			ArrayList<String> realUpdateFields = new ArrayList<String>();
			ArrayList<Object> values = new ArrayList<Object>();
			for(String fieldName : updateFieldNames){
				try {
					Field field = CL.getDeclaredField(fieldName);
					boolean isAccessible = field.isAccessible();
					field.setAccessible(true);
					Object value = field.get(obj);
					field.setAccessible(isAccessible);
					if(null != value){
						values.add(value);
						realUpdateFields.add(fieldName);
					}
				} catch (SecurityException e) {
				} catch (NoSuchFieldException e) {
				} catch (Exception e) {
				}
			}
			
			ArrayList<String> realSelectKeyFields = new ArrayList<String>();
			for(String fieldName : selectKeyNames){
				try {
					Field field = CL.getDeclaredField(fieldName);
					boolean isAccessible = field.isAccessible();
					field.setAccessible(true);
					Object value = field.get(obj);
					field.setAccessible(isAccessible);
					if(null != value){
						values.add(value);
						realSelectKeyFields.add(fieldName);
					}
				} catch (SecurityException e) {
				} catch (NoSuchFieldException e) {
				} catch (Exception e) {
				}
			}
			
			if(realUpdateFields.size() < 1) return 0;
			StringBuilder sqlbuf = new StringBuilder("UPDATE ");
			sqlbuf.append(getTableName(CL));
			sqlbuf.append(" SET ");
			
			for(String fieldName : realUpdateFields){
				sqlbuf.append(fieldName);
				sqlbuf.append("=?, ");
			}
			sqlbuf.deleteCharAt(sqlbuf.length()-2);
			
			if(realSelectKeyFields.size() > 0){
				sqlbuf.append("WHERE ");
				for(String keyFiledName : realSelectKeyFields){
					sqlbuf.append(keyFiledName);
					sqlbuf.append("=? AND ");
				}
				sqlbuf.delete(sqlbuf.length()-5, sqlbuf.length());
			}
			
			String sql = sqlbuf.toString();
			sqlbuf = null;
			System.out.println(sql);
			psta = con.prepareStatement(sql);
			
			for(int i = 0 , l = values.size(); i < l ; i++){
				psta.setObject(i+1,  values.get(i));
			}
			count = psta.executeUpdate();
		}catch(Exception e){
			throw new SQLException(e);
		}finally{
			try{if(null != psta) psta.close();}catch(Exception e){}
			try{if(null != con) con.close();}catch(Exception e){}
		}
		return count;
	}
	
	/**
	 * <b>����������</b>TODO(��objs���ж�����������PreparedStatement.executeBatch()ͬ�����µ����ݿ�)<br>
	 * �÷���Ҫ��objs������������������ֱ�����ݿ�������ֶ���һ��
	 * @param <T>
	 * @param con ���ݿ�����
	 * @param objs ͬ�����µ����ݿ�Ķ���
	 * @param updateFieldNames ÿ��������Ҫ���µ��ֶ���
	 * @param selectKeyNames ÿ��������²��õĲ�ѯ�����ֶ���
	 * @return int[] ���µļ�¼�����ύ��Ч���ݷ��ؿգ����򷵻ض�Ӧ���³ɹ��ļ�¼����
	 * @throws SQLException
	*/
	public synchronized static <T>int[] excutUpdateStrictMap(Connection con, T[] objs, String[] updateFieldNames, String[] selectKeyNames) throws SQLException{
		if(null==updateFieldNames || null==selectKeyNames || null==objs || null== con || con.isClosed()){return null;}
		int[] counts = null;
		PreparedStatement psta = null;
		boolean autoCommit = con.getAutoCommit();
		try{
			Class<?> CL = objs[0].getClass();
			ArrayList<String> realUpdateFields = new ArrayList<String>();
			for(String fieldName : updateFieldNames){
				try {
					CL.getDeclaredField(fieldName);
					realUpdateFields.add(fieldName);
				} catch (SecurityException e) {
				} catch (NoSuchFieldException e) {
				} catch (Exception e) {
				}
			}
			
			ArrayList<String> realSelectKeyFields = new ArrayList<String>();
			for(String fieldName : selectKeyNames){
				try {
					CL.getDeclaredField(fieldName);
					realSelectKeyFields.add(fieldName);
				} catch (SecurityException e) {
				} catch (NoSuchFieldException e) {
				} catch (Exception e) {
				}
			}
			if(realUpdateFields.size() < 1) return null;
			
			StringBuilder sqlbuf = new StringBuilder("UPDATE ");
			sqlbuf.append(getTableName(CL));
			sqlbuf.append(" SET ");
			
			for(String fieldName : realUpdateFields){
				sqlbuf.append(fieldName);
				sqlbuf.append("=?, ");
			}
			sqlbuf.deleteCharAt(sqlbuf.length()-2);
			
			if(realSelectKeyFields.size() > 0){
				sqlbuf.append("WHERE ");
				for(String keyFiledName : realSelectKeyFields){
					sqlbuf.append(keyFiledName);
					sqlbuf.append("=? AND ");
				}
				sqlbuf.delete(sqlbuf.length()-5, sqlbuf.length());
			}
			String sql = sqlbuf.toString();
			sqlbuf = null;
			System.out.println(sql);
			con.setAutoCommit(false);
			psta = con.prepareStatement(sql);
			for(Object obj : objs){
				if(null == obj) continue;
				int i = 1;
				for(String name : realUpdateFields){
					try {
						Field field = CL.getDeclaredField(name);
						boolean isAccessible = field.isAccessible();
						field.setAccessible(true);
						Object vobj = field.get(obj);
						field.setAccessible(isAccessible);
						psta.setObject(i,  vobj);
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
					i++;
				}
				
				for(String name : realSelectKeyFields){
					try {
						Field field = CL.getDeclaredField(name);
						boolean isAccessible = field.isAccessible();
						field.setAccessible(true);
						Object vobj = field.get(obj);
						field.setAccessible(isAccessible);
						psta.setObject(i,  vobj);
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
					i++;
				}
				psta.addBatch();
			}
			counts = psta.executeBatch();
			con.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try{if(null != con) con.setAutoCommit(autoCommit);}catch(Exception e){}
			try{if(null != psta) psta.close();}catch(Exception e){}
			try{if(null != con) con.close();}catch(Exception e){}
		}
		return counts;
	}
	
	/**
	 * <b>����������</b>TODO(ִ��Ԥ����sql�������ݿ��¼)<br>
	 * @param con
	 * @param sql ִ�е�sqlԤ������䣬����Ԥ�������ռλ����?��
	 * @param preparams Ԥ�������ֵ�����Ⱥ�˳��Ӧ�ú�sql�е�Ԥ�������ռλ��������˳�򱣳�һ��
	 * @return int ���سɹ����µļ�¼����
	 * @throws SQLException
	*/
	public synchronized static int excutUpdate(Connection con, String sql, Object[] preparams) throws SQLException{
		if(null == preparams || preparams.length < 1 || null == con || con.isClosed()){return 0;}
		PreparedStatement psta = con.prepareStatement(sql);
		int count = 0;
		try{
			int paramlength = preparams.length;
			for(int i = 0; i< paramlength; i++){
				psta.setObject(i+1, preparams[i]);
			}
			count = psta.executeUpdate();
		}catch(Exception e){
			throw new SQLException(e);
		}finally{
			try{if(null != psta) psta.close();}catch(Exception e){}
			try{if(null != con) con.close();}catch(Exception e){}
		}
		return count;
	}
	
	/**
	 * <b>����������</b>TODO(����ִ��sql�������ݿ��¼)<br>
	 * @param con
	 * @param sql ִ�е�sqlԤ������䣬����Ԥ�������ռλ����?��
	 * @param allpreparams Ԥ�������ֵ�����Ⱥ�˳��Ӧ�ú�sql�е�Ԥ�������ռλ��������˳�򱣳�һ��
	 * @return int[] ���µļ�¼�����ύ��Ч���ݷ��ؿգ����򷵻ض�Ӧ���³ɹ��ļ�¼����
	 * @throws SQLException
	*/
	public synchronized static int[] excutUpdate(Connection con, String sql, List<Object[]> allpreparams) throws SQLException{
		if(null == allpreparams || allpreparams.size()<1 || null== con || con.isClosed()){return null;}
		boolean autoCommit = con.getAutoCommit();
		int[] counts = null;
		PreparedStatement psta = null;
		try{
			con.setAutoCommit(false);
			psta = con.prepareStatement(sql);
			for(int i = 0 , l = allpreparams.size(); i < l; i++){
				Object[] params = allpreparams.get(i);
				if(null == params) continue;
				for(int j = 0, k = params.length; j < k; j++){
					psta.setObject(j+1, params[j]);
				}
				psta.addBatch();
			}
			counts = psta.executeBatch();
			con.commit();
		}catch(Exception e){
			throw new SQLException(e);
		}finally{
			try{if(null != con) con.setAutoCommit(autoCommit);}catch(Exception e){}
			try{if(null != psta) psta.close();}catch(Exception e){}
			try{if(null != con) con.close();}catch(Exception e){}
		}
		return counts;
	}
	
	/**
	 * <b>����������</b>TODO(��������)<br>
	 * @param con
	 * @param sqls ��Ԥ����������
	 * @return int[] ���µļ�¼�����ύ��Ч���ݷ��ؿգ����򷵻ض�Ӧ���³ɹ��ļ�¼����
	 * @throws SQLException
	*/
	@Deprecated
	public synchronized static int[] excutUpdate(Connection con, String[] sqls) throws SQLException{
		if(null == con || con.isClosed()){return null;}
		boolean autoCommit = con.getAutoCommit();
		con.setAutoCommit(false);
		Statement sta = con.createStatement();
		int[] counts = null;
		try{
			for(int i = 0, l = sqls.length; i < l; i++){
				sta.addBatch(sqls[i]);
			}
			counts = sta.executeBatch();
			con.commit();
		}catch(Exception e){
			throw new SQLException(e);
		}finally{
			try{if(null != con) con.setAutoCommit(autoCommit);}catch(Exception e){}
			try{if(null != sta) sta.close();}catch(Exception e){}
			try{if(null != con) con.close();}catch(Exception e){}
		}
		return counts;
	}
	
	/**
	 * <b>����������</b>TODO(ִ��sql��ѯ���)<br>
	 * T��������������ϢҪ������ݿ���ֶ�������һ��
	 * @param <T>
	 * @param con 
	 * @param CL ���ؽ�������Class��Ϣ
	 * @param sql ��Ԥ���������sql���
	 * @param int startrow ��ʼ��ȡ��λ�ã�1��ʾ��λ����һ����¼��  -1 ��ʾ��λ�����һ����¼��-2��ʾ��λ�������ڶ�����¼
	 * @param fetchSize ��ѯ��ȡ����
	 * @return List<T> ��ѯ�ɹ�����T����ļ������ͣ�ʧ�ܻ�����Ч��Ӧ��¼����null
	 * @throws SQLException
	*/
	public static <T>List<T> executeQueryStrictMap(Connection con,
			Class<T> CL, String sql, int startrow, int fetchSize)
			throws SQLException {
		if((fetchSize != -1 && fetchSize < 1) || startrow < 1) return null;
		Statement sta = null;
		ResultSet rs = null;
		ArrayList<T> results = new ArrayList<T>();
		try{
			Field[] fields = CL.getDeclaredFields();
			sta = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			sql += sql.contains("limit ") || sql.contains("LIMIT ")?"":" limit "+(startrow-1)+","+fetchSize;
			rs = sta.executeQuery(sql);
			while(rs.next()){
				T newObj = CL.newInstance();
				int fieldcount = fields.length;
				boolean hasValue = false;
				for(int i = 0 ; i < fieldcount ; i++){
					Field field = fields[i];
					Object v = null;
					try{
						v = rs.getObject(field.getName());
						boolean accessible = field.isAccessible();
						field.setAccessible(true);
						if(v != null){
							hasValue = true;
							field.set(newObj, v);
							field.setAccessible(accessible);
						}
					}catch(Exception e){continue;}
				}
				if(hasValue) results.add(newObj);
			}
		}catch(Exception e){
			throw new SQLException(e);
		}finally{
			try{if(null != rs) rs.close();}catch(Exception e){}
			try{if(null != sta) sta.close();}catch(Exception e){}
			try{if(null != con) con.close();}catch(Exception e){}
		}
		if(results.size() < 1) results = null;
		return results;
	}
	
	/**
	 * <b>����������</b>TODO(ִ��Ԥ����sql��ѯ���)<br>
	 * @param <T>
	 * @param con
	 * @param CL ���ؽ�������Class��Ϣ
	 * @param sql sqlԤ������䣬����Ԥ�������ռλ����?��
	 * @param selectKeyParamV Ԥ�������ֵ�����Ⱥ�˳��Ӧ�ú�sql�е�Ԥ�������ռλ��������˳�򱣳�һ��
	 * @param fetchSize ��ѯ��ȡ����
	 * @return List<T> ��ѯ�ɹ�����T����ļ������ͣ�ʧ�ܻ�����Ч��Ӧ��¼����null
	 * @return List<T> ��ѯ�ɹ�����T����ļ������ͣ�ʧ�ܻ�����Ч��Ӧ��¼����null
	 * @throws SQLException
	*/
	public static <T> List<T> executeQueryStrictMap(Connection con,
			Class<T> CL, String sql, Object[] selectKeyParamV, int startrow,
			int fetchSize) throws SQLException {
		PreparedStatement psta = null;
		ResultSet rs = null;
		if(selectKeyParamV == null || selectKeyParamV.length < 1 || (fetchSize != -1 && fetchSize < 1) || startrow < 1) return null;
		ArrayList<T> results = new ArrayList<T>();
		try{
			Field[] fields = CL.getDeclaredFields();
			sql += sql.contains("limit ") || sql.contains("LIMIT ")?"":" limit "+(startrow-1)+","+fetchSize;
			psta = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			
			int length = null==selectKeyParamV ? 0 : selectKeyParamV.length;
			for(int i = 0; i < length; i++){
				Object v = selectKeyParamV[i];
				psta.setObject(i+1, v);
			}
			
			rs = psta.executeQuery();
			//rs.absolute(startrow);
			if(fetchSize != -1) rs.setFetchSize(fetchSize+1);
			while(rs.next()){
				T newObj = CL.newInstance();
				int fieldcount = fields.length;
				boolean hasValue = false;
				for(int i = 0 ; i < fieldcount ; i++){
					Field field = fields[i];
					Object v = null;
					try{
						v = rs.getObject(field.getName());
						boolean accessible = field.isAccessible();
						field.setAccessible(true);
						if(v != null){
							hasValue = true;
							field.set(newObj, v);
							field.setAccessible(accessible);
						}
					}catch(Exception e){continue;}
				}
				if(hasValue) results.add(newObj);
			}
		}catch(Exception e){
			throw new SQLException(e);
		}finally{
			try{if(null != rs) rs.close();}catch(Exception e){}
			try{if(null != psta) psta.close();}catch(Exception e){}
			try{if(null != con) con.close();}catch(Exception e){}
		}
		if(results.size() < 1) results = null;
		return results;
	}
	
	/**
	 * <b>����������</b>TODO(ִ��Ԥ�����ѯ��䣬��ѯ����resultKeyNamesָ�����ֶΣ�)<br>
	 * @param <T>
	 * @param con
	 * @param obj
	 * @param selectKeyNames
	 * @param resultKeyNames
	 * @param startrow
	 * @param fetchSize
	 * @return
	 * @throws SQLException
	*/
	public static <T> List<T> executeQueryStrictMap(Connection con, T obj,
			String[] selectKeyNames, String[] resultKeyNames, int startrow,
			int fetchSize) throws SQLException {
		PreparedStatement psta = null;
		ResultSet rs = null;
		ArrayList<T> results = null;
		if(obj==null || (fetchSize != -1 && fetchSize < 1) || startrow < 1) return null;
		Class<T> CL = (Class<T>) obj.getClass();
		try{
			ArrayList<Object> values = new ArrayList<Object>();
			
			ArrayList<String> realSelectResultFields = null;
			if(resultKeyNames != null){
				realSelectResultFields = new ArrayList<String>();
				for(String fieldName : resultKeyNames){
					try {
						Field field = CL.getDeclaredField(fieldName);
						realSelectResultFields.add(fieldName);
					} catch (SecurityException e) {
					} catch (NoSuchFieldException e) {
					} catch (Exception e) {
					}
				}
			}
			
			ArrayList<String> realSelectKeyFields = null;
			if(null != selectKeyNames){
				realSelectKeyFields = new ArrayList<String>();
				for(String fieldName : selectKeyNames){
					try {
						Field field = CL.getDeclaredField(fieldName);
						boolean isAccessible = field.isAccessible();
						field.setAccessible(true);
						Object value = field.get(obj);
						field.setAccessible(isAccessible);
						if(null != value){
							values.add(value);
							realSelectKeyFields.add(fieldName);
						}
					} catch (SecurityException e) {
					} catch (NoSuchFieldException e) {
					} catch (Exception e) {
					}
				}
			}
			
			results = new ArrayList<T>();
			StringBuilder sqlbuf = new StringBuilder("SELECT ");
			if(null ==realSelectResultFields || realSelectResultFields.size() < 1){
				sqlbuf.append(" *");
			}else{
				for(String name : realSelectResultFields){
					sqlbuf.append(name);
					sqlbuf.append(',');
				}
				sqlbuf.deleteCharAt(sqlbuf.length()-1);
			}
			sqlbuf.append(" FROM ");
			sqlbuf.append(getTableName(CL));
			if(null !=realSelectKeyFields && realSelectKeyFields.size() > 0){
				sqlbuf.append(" WHERE ");
				for(String name : realSelectKeyFields){
					sqlbuf.append(name);
					sqlbuf.append("=? AND ");
				}
				sqlbuf.delete(sqlbuf.length()-5, sqlbuf.length());
			}
			sqlbuf.append(" limit ").append(startrow-1).append(",").append(fetchSize);
			String sql = sqlbuf.toString();
			sqlbuf = null;
			
			psta = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			for(int i = 0, l = values.size(); i < l; i++){
				psta.setObject(i+1, values.get(i));
			}
			System.out.println(sql);
			rs = psta.executeQuery();
			//rs.absolute(startrow);
			if(fetchSize != -1) rs.setFetchSize(fetchSize+1);
			if(realSelectResultFields.size() > 0){
				while(rs.next()){
					T newObj = CL.newInstance();
					boolean hasValue = false;
					for(String fieldName: realSelectResultFields){
						Field field = CL.getDeclaredField(fieldName);
						boolean accessible = field.isAccessible();
						field.setAccessible(true);
						Object v = rs.getObject(field.getName());
						if(v != null){
							hasValue = true;
							field.set(newObj, v);
							field.setAccessible(accessible);
						}
					}
					if(hasValue) results.add(newObj);
				}
			}else{
				while(rs.next()){
					T newObj = CL.newInstance();
					Field[] fields = CL.getDeclaredFields();
					int fieldcount = fields.length;
					boolean hasValue = false;
					for(int i = 0 ; i < fieldcount ; i++){
						Field field = fields[i];
						boolean accessible = field.isAccessible();
						field.setAccessible(true);
						Object v = rs.getObject(field.getName());
						if(v != null){
							hasValue = true;
							field.set(newObj, v);
							field.setAccessible(accessible);
						}
					}
					if(hasValue) results.add(newObj);
				}
			}
		}catch(Exception e){
			throw new SQLException(e);
		}finally{
			try{if(null != rs) rs.close();}catch(Exception e){}
			try{if(null != psta) psta.close();}catch(Exception e){}
			try{if(null != con) con.close();}catch(Exception e){}
		}
		if(results.size() < 1) results = null;
		return results;
	}
	
	public synchronized static <T> int executeInsertStrictMap(Connection con, T obj) throws SQLException {
		if(obj == null) return 0;
		PreparedStatement psta = null;
		Class<?> CL = obj.getClass();
		Field[] fields = CL.getDeclaredFields();
		int count = 0;
		try {
			StringBuilder sqlbuf = new StringBuilder("INSERT INTO ");
			sqlbuf.append(getTableName(CL));
			sqlbuf.append("(");
			
			StringBuilder sqlbufv = new StringBuilder(" VALUES(");
			
			ArrayList<Object> values = new ArrayList<Object>();
			int length = fields.length;
			for(int i = 0; i< length; i++){
				Field field = fields[i];
				boolean accessible = field.isAccessible();
				field.setAccessible(true);
				Object v = field.get(obj);
				field.setAccessible(accessible);
				if(null != v){
					values.add(v);
					sqlbuf.append(field.getName());
					if(i+1 < length) {
						sqlbuf.append(",");
						sqlbufv.append("?,");
					}else {
						sqlbufv.append("?)");
						sqlbuf.append(")");
					}
				}
			}
			sqlbuf.append(sqlbufv.toString());
			sqlbufv = null;
			String sql = sqlbuf.toString();
			sqlbuf = null;
			
			if(values.size() > 0){
				psta = con.prepareStatement(sql);
				int vlength = values.size();
				for(int i = 0; i < vlength; i++){
					psta.setObject(i+1, values.get(i));
				}
				count = psta.executeUpdate();
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}catch (Exception e) {
			throw new SQLException(e);
		}finally{
			try{if(null != psta) psta.close();}catch(Exception e){}
			try{if(null != con) con.close();}catch(Exception e){}
		}
		return count;
	}
	
	public synchronized static <T> int[] executeInsertStrictMap(Connection con, T[] objs, String[] insertFieldName) throws SQLException {
		if(null == objs || null == insertFieldName) return null;
		Class<?> CL = null;
		for(int i = 0; i<  objs.length; i++){
			if(objs[i]!= null) {
				CL = objs[i].getClass();
				break;
			}
		}
		if(null == CL) return null;
		
		PreparedStatement psta = null;
		int counts[] = null;
		boolean autoCommit = con.getAutoCommit();
		try {
			ArrayList<String> realInsertFieldName = new ArrayList<String>();
			for(int i = 0; i < insertFieldName.length; i++){
				try{
					CL.getDeclaredField(insertFieldName[i]);
				}catch(Exception e){continue;}
				realInsertFieldName.add(insertFieldName[i]);
			}
			
			if(realInsertFieldName.size() > 0){
				StringBuilder sqlbufv = new StringBuilder(" VALUES(");
				StringBuilder sqlbuf = new StringBuilder("INSERT INTO ");
				sqlbuf.append(getTableName(CL));
				sqlbuf.append("(");
				
				for(String inserField : realInsertFieldName){
					sqlbuf.append(inserField);
					sqlbuf.append(",");
					sqlbufv.append("?,");
				}
				sqlbuf.deleteCharAt(sqlbuf.length()-1);
				sqlbufv.deleteCharAt(sqlbufv.length()-1);
				sqlbuf.append(")");
				sqlbufv.append(")");
				
				sqlbuf.append(sqlbufv.toString());
				sqlbufv = null;
				String sql = sqlbuf.toString();
				sqlbuf = null;
				System.out.println(sql);
				
				con.setAutoCommit(false);
				psta = con.prepareStatement(sql);
				
				int realInsertFieldLength = realInsertFieldName.size();
				for(int i = 0; i<  objs.length; i++){
					if(objs[i] == null) continue;
					for(int j = 0; j < realInsertFieldLength ; j++){
						String fieldName = realInsertFieldName.get(j);
						Field field = CL.getDeclaredField(fieldName);
						boolean accessible = field.isAccessible();
						field.setAccessible(true);
						Object v = field.get(objs[i]);
						field.setAccessible(accessible);
						psta.setObject(j+1, v);
					}
					psta.addBatch();
				}
				
				counts = psta.executeBatch();
				con.commit();
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}catch (Exception e) {
			throw new SQLException(e);
		}finally{
			try{if(null != con) con.setAutoCommit(autoCommit);}catch(Exception e){}
			try{if(null != psta) psta.close();}catch(Exception e){}
			try{if(null != con) con.close();}catch(Exception e){}
		}
		return counts;
	}
	
	/*public static void main(String[] args) throws Exception{
		DataSource.initDataSource(DataSource.defaultDataSourceName, "simpleDBCP.properties");
		Connection con = DataSource.getConnection();
		
		String[] sqls = new String[]{
				"update messageInfo set sender=? where id=?",
				"delete from logInfo where identifier=537"
		};
		
		LogInfo[] logs = new LogInfo[]{
				new LogInfo("test insert","com","com","mupan","dbutil","databsetest",775,1003),
				new LogInfo("test insert2","com","com","mupan","dbutil","databsetest",776,1003)
		};
		logs[0].setIdentifier(540);
		logs[1].setIdentifier(539);
		
		logs[0].setLevel(LevelEnum.PRIO_EVENT);
		logs[1].setLevel(LevelEnum.PRIO_EVENT);
		
		logs[0].setConfirmTime(new Timestamp(System.currentTimeMillis()));
		
		String[] updateFieldNames = new String[]{"user", "confirmed", "confirmTime", "threadID"};
		String[] selectKeyNames = new String[]{"identifier", "level"};
		String[] insertFieldName = new String[]{"name","level","subject","object","type","user","detail","occurTime","fileLine","threadID","parameter"};
		
		ArrayList<Object[]> params = new ArrayList<Object[]>();
		params.add(new Object[]{"mupantest1", Integer.valueOf(1)});
		params.add(new Object[]{"mupantest2", Integer.valueOf(2)});
		DBUtil.registClass(LogInfo.class, "LogInfo");
		List<LogInfo> s = DBUtil.executeQueryStrictMap(con, logs[0], selectKeyNames, new String[]{"identifier","name","level"}, 0, -1);
		System.out.println(s.size());
		for(LogInfo log : s){
			System.out.println(log.getIdentifier()+"-"+log.getName()+"-"+log.getLevel());
		}
		System.out.println(count);
		System.out.println("1:"+count[0]);
		System.out.println("2:"+count[1]);
	}*/
}
