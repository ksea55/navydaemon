package com.navy.daemon.action;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * ״̬�ϱ����ɼ����ӿڶ��壩
 * �˽ӿ���IAction�����Ϲ���ѭ����ʱ�ɼ��߳�������ܿػ���
 * @author mup
 *
 */
public interface IReporter extends IAction{
/**
 * ��ȡ�����ΨһReporterId
 * Ĭ��Ϊ���������ȫ·��
 * @return
 */
public String getReporterId();
/**
 * ��ʱ���� ������������ʱ������Ч��
 * ������ִ��һ���ڸ�����ʼ�ӳٺ��״����õĶ��ڲ����������������и��������ڣ�
 * Ҳ���ǽ��� initialDelay ��ʼִ�У�
 * Ȼ����initialDelay+period ��ִ�У������� initialDelay + 2 * period ��ִ�У��������ơ�
 * @param initialDelay ��ʼ�ӳ�ʱ��
 * @param period �������
 * @param TimeUnitunit unitʱ�䵥λ
 */
 public void setQuartzCron(long initialDelay, long period, TimeUnit unit);
 /**
  * �ϱ�ֹͣ����
  * @return 
  *    ֹͣ�ɹ�����true��ʧ�ܷ���false
  */
 public boolean stop();
 
 /**
  * �����ϱ��߳�
  * @return 
  *    �����ɹ�����true��ʧ�ܷ���false
  */
 public boolean startRuner();
 
 /**
  * ��ȡ���������б�
  * @return
  */
 public ArrayList<? extends Runnable> getTasks();
}
