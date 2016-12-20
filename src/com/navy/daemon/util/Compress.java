package com.navy.daemon.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;



public class Compress {
	/**
	 * ���ļ����ļ�Ŀ¼����ѹ��
	 * 
	 * @param srcPath
	 *            Ҫѹ����Դ�ļ�·�������ѹ��һ���ļ�����Ϊ���ļ���ȫ·�������ѹ��һ��Ŀ¼����Ϊ��Ŀ¼�Ķ���Ŀ¼·��
	 * @param zipPath
	 *            ѹ���ļ������·����ע�⣺zipPath������srcPath·���µ����ļ���
	 * @param zipFileName
	 *            ѹ���ļ���
	 * @throws Exception
	 */
	public static void zip(String srcPath, String zipPath, String zipFileName)
			throws Exception {
		System.out.println("srcPath:"+srcPath+", zipPath:"+zipPath+", zipFileName:"+zipFileName);
		if (StringUtils.isEmpty(srcPath) || StringUtils.isEmpty(zipPath)
				|| StringUtils.isEmpty(zipFileName)) {
			throw new Exception("param is null");
		}
		CheckedOutputStream cos = null;
		ZipOutputStream zos = null;
		try {
			File srcFile = new File(srcPath);
			File zipDir = new File(zipPath);
			srcPath = srcFile.getAbsolutePath();
			if(srcFile.isFile()){
				srcPath = srcPath + srcFile.getName();
			}
			zipPath = zipDir.getAbsolutePath();
			// �ж�ѹ���ļ������·���Ƿ�ΪԴ�ļ�·�������ļ��У�����ǣ����׳��쳣����ֹ���޵ݹ�ѹ���ķ�����
			if (srcFile.isDirectory() && zipPath.indexOf(srcPath) != -1) {
				throw new Exception(
						"zipPath must not be the child directory of srcPath.");
			}

			// �ж�ѹ���ļ������·���Ƿ���ڣ���������ڣ��򴴽�Ŀ¼
			
			if (!zipDir.exists() || !zipDir.isDirectory()) {
				zipDir.mkdirs();
			}

			// ����ѹ���ļ�������ļ�����
			String zipFilePath = zipPath + File.separator + zipFileName;
			File zipFile = new File(zipFilePath);
			if (zipFile.exists()) {
				// ����ļ��Ƿ�����ɾ�������������ɾ���������׳�SecurityException
				///SecurityManager securityManager = new SecurityManager();
				//securityManager.checkDelete(zipFilePath);
				// ɾ���Ѵ��ڵ�Ŀ���ļ�
				zipFile.delete();
			}

			cos = new CheckedOutputStream(new FileOutputStream(zipFile),
					new CRC32());
			zos = new ZipOutputStream(cos);

			// ���ֻ��ѹ��һ���ļ�������Ҫ��ȡ���ļ��ĸ�Ŀ¼
			String srcRootDir = srcPath;
			if (srcFile.isFile()) {
				int index = srcPath.lastIndexOf(File.separator);
				if (index != -1) {
					srcRootDir = srcPath.substring(0, index);
				}
			}
			// ���õݹ�ѹ����������Ŀ¼���ļ�ѹ��
			zip(srcRootDir, srcFile, zos);
			zos.flush();
			deleteFile(srcFile);
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (zos != null) {
					zos.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				if (cos != null) {
					cos.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * �ݹ�ѹ���ļ���
	 * 
	 * @param srcRootDir
	 *            ѹ���ļ��и�Ŀ¼����·��
	 * @param file
	 *            ��ǰ�ݹ�ѹ�����ļ���Ŀ¼����
	 * @param zos
	 *            ѹ���ļ��洢����
	 * @throws Exception
	 */
	private static void zip(String srcRootDir, File file, ZipOutputStream zos)
			throws Exception {
		if (file == null) {
			return;
		}

		// ������ļ�����ֱ��ѹ�����ļ�
		if (file.isFile()) {
			System.out.println("zip file:"+file.getName());
			int count, bufferLen = 1024;
			byte data[] = new byte[bufferLen];
			
			// ��ȡ�ļ������ѹ���ļ��и�Ŀ¼����·��
			String subPath = file.getAbsolutePath();
			System.out.println("subPath:"+subPath+",srcRootDir"+srcRootDir);
			int index = subPath.indexOf(srcRootDir);
			if (index != -1) {
				subPath = subPath.substring(srcRootDir.length()
						+ File.separator.length());
			}
			System.out.println("subPath:"+subPath+",srcRootDir"+srcRootDir);
			ZipEntry entry = new ZipEntry(subPath);
			zos.putNextEntry(entry);
			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream(file));
			while ((count = bis.read(data, 0, bufferLen)) != -1) {
				zos.write(data, 0, count);
			}
			bis.close();
			//zos.closeEntry();
			System.out.println("ziped file:"+file.getName());
		}
		// �����Ŀ¼����ѹ������Ŀ¼
		else {
			// ѹ��Ŀ¼�е��ļ�����Ŀ¼
			File[] childFileList = file.listFiles();
			for (int n = 0; n < childFileList.length; n++) {
				childFileList[n].getAbsolutePath().indexOf(
						file.getAbsolutePath());
				System.out.println("childFileList"+childFileList[n]);
				zip(srcRootDir, childFileList[n], zos);
			}
		}
	}

	/**
	 * ��ѹ��zip��
	 * 
	 * @param zipFilePath
	 *            zip�ļ���ȫ·��
	 * @param unzipFilePath
	 *            ��ѹ����ļ������·��
	 * @param includeZipFileName
	 *            ��ѹ����ļ������·���Ƿ����ѹ���ļ����ļ�����true-������false-������
	 */
	@SuppressWarnings("unchecked")
	public static void unzip(String zipFilePath, String unzipFilePath,
			boolean includeZipFileName) throws Exception {
		System.out.println("unzip zipFilePath:"+zipFilePath+", unzipFilePath:"+unzipFilePath+",includeZipFileName:"+includeZipFileName);
		if (StringUtils.isEmpty(zipFilePath)
				|| StringUtils.isEmpty(unzipFilePath)) {
			throw new Exception("parameter is null");
		}
		File zipFile = new File(zipFilePath);
		// �����ѹ����ļ�����·������ѹ���ļ����ļ�������׷�Ӹ��ļ�������ѹ·��
		if (includeZipFileName) {
			String fileName = zipFile.getName();
			if (StringUtils.isNotEmpty(fileName)) {
				fileName = fileName.substring(0, fileName.lastIndexOf("."));
			}
			unzipFilePath = unzipFilePath + File.separator + fileName;
		}
		// ������ѹ���ļ������·��
		File unzipFileDir = new File(unzipFilePath);
		if (!unzipFileDir.exists() || !unzipFileDir.isDirectory()) {
			unzipFileDir.mkdirs();
		}

		// ��ʼ��ѹ
		ZipEntry entry = null;
		ZipFile zip = null;
		String entryFilePath = null, entryDirPath = null;
		File entryFile = null, entryDir = null;
		int index = 0, count = 0, bufferSize = 1024;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		byte[] buffer = new byte[bufferSize];
		try{
			zip = new ZipFile(zipFile);
			Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
			// ѭ����ѹ�������ÿһ���ļ����н�ѹ
			while (entries.hasMoreElements()) {
				try{
					entry = entries.nextElement();
					// ����ѹ������һ���ļ���ѹ�󱣴���ļ�ȫ·��
					entryFilePath = unzipFilePath + File.separator + entry.getName();
					
					// ������ѹ�ļ�
					entryFile = new File(entryFilePath);
					
					// ������ѹ�󱣴���ļ���·��
					index = entryFile.getAbsolutePath().lastIndexOf(File.separator);
					if (index != -1) {
						entryDirPath = entryFilePath.substring(0, index);
					} else {
						entryDirPath = "";
					}
					
					
					System.out.println(entryFile.getAbsolutePath());
					// ����ļ���·�������ڣ��򴴽��ļ���
					if(entry.isDirectory()){
						entryFile.mkdirs();
						//System.out.println("mkdirs.."+entryFile.getAbsolutePath());
						continue;
					}else if (!entryFile.getParentFile().exists()) {
						//System.out.println("mk parent dirs.."+entryFile.getParentFile().getAbsolutePath());
						entryFile.getParentFile().mkdirs();
					}
		
					
					if (entryFile.exists()) {
						// ����ļ��Ƿ�����ɾ�������������ɾ���������׳�SecurityException
						//SecurityManager securityManager = new SecurityManager();
						//securityManager.checkDelete(entryFilePath);
						// ɾ���Ѵ��ڵ�Ŀ���ļ�
						entryFile.delete();
					}
		
					// д���ļ�
					bos = new BufferedOutputStream(new FileOutputStream(entryFile));
					bis = new BufferedInputStream(zip.getInputStream(entry));
					while ((count = bis.read(buffer, 0, bufferSize)) != -1) {
						bos.write(buffer, 0, count);
					}
				}catch(Exception e0){
				}finally{
					bos.flush();
					bos.close();
				}
			}
		}catch(Exception e){
		}finally{
			try{bos.flush();}catch(Exception e){}
			try{bos.close();}catch(Exception e){}
			try{zip.close();}catch(Exception e){}
			
		}
			
		
	}
	
	
	private static void deleteFile(File file) {
		if (file.exists()) {// �ж��ļ��Ƿ����
			if (file.isFile()) {// �ж��Ƿ����ļ�
				file.delete();// ɾ���ļ�
			} else if (file.isDirectory()) {// �����������һ��Ŀ¼
				File[] files = file.listFiles();// ����Ŀ¼�����е��ļ� files[];
				for (int i = 0; i < files.length; i++) {// ����Ŀ¼�����е��ļ�
					deleteFile(files[i]);// ��ÿ���ļ�������������е���
				}
				file.delete();// ɾ���ļ���
			}
		} else {
			System.out.println("��ɾ�����ļ�������");
		}
	}
	
	
	public static void main(String[] args){
		String zipFilePath = "F:\\navy_aca\\plsqldev.exe.zip";
		String unzipFilePath = "F:\\navy_aca\\plsqldev.exe\\";
		try {
			unzip(zipFilePath, unzipFilePath, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
