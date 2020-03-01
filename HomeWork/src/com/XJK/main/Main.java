package com.XJK.main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * 
 * @author XJK
 *
 */

public class Main {
	public static void main(String[] args) throws Exception {
		// ��Ļ�ļ������ļ���
		String srcString = "F:\\fileForTest\\work\\src";    //�����õ�·��
		// ����ļ���
		String outString = "F:\\fileForTest\\work\\src";   //�����õ�·��
		// �Ƿ񽫴�������Ļд���ļ�
		boolean isOutputSubtitle = false;
		// �Ƿ��˳��˵�
		boolean ifExit = false;
		String  commandString = "";
		List<File> fList ;
		Set<String> fileSet; // ����һ��Set �����ų�ͬ�����ļ�
		
		//ʵ����
		SubtitleProcess sProcess = new SubtitleProcess(); 
		
		//���������в˵�
		Scanner scanner = new Scanner(System.in);

		System.out.println("������������������ ��ҵ : \"Ӣ��\"��Ļ��Ƶͳ��(�����ڰ�װ��jdk��windowsϵͳ)      ");
		while (!ifExit) {
			fList = new ArrayList<File>();   //���
			fileSet = new HashSet<String>(); //���
			isOutputSubtitle = false;   //�����־λ
			System.out.print("��������Ļ�ļ��������Ļ�ļ����ļ���(����·��):");
			srcString = scanner.next();
			System.out.print("�������Ž�����ļ���(����·��):");
			outString = scanner.next();
			File srcFile = new File(srcString); // ����
			File outDir = new File(outString); // ���
			if (srcFile.exists() && outDir.exists() && outDir.isDirectory()) { // �ļ��д���
				// �鿴�Ƿ�����Ļ�ļ�
				SubtitleProcess.getSubtitleFiles(srcFile, fList, fileSet);
				if (fList.size() > 0) { // �ҵ���Ļ�ļ�
					System.out.println("...");
					System.out.println("���� " + fList.size() + " ����Ļ�ļ�");
					sProcess.setOutFileString(outString);     //��������ļ�Ŀ¼
					if (sProcess.createNewOutFile()) { // ��������ļ�/�ļ���
						
						System.out.print("�Ƿ�����жϺ����Ļ�ļ�[������ʱ]?(y/n):");
						commandString = scanner.next();
						
						if (commandString.equals("y") || commandString.equals("Y")) {
							isOutputSubtitle = true;
						}
						System.out.print("����ͳ��...");
						//ͳ�Ʒ��ֵ���Ļ�ļ�
						sProcess.repSta(fList,isOutputSubtitle);
						System.out.println("...");
						System.out.println("ͳ�����");
						System.out.println("��Ŀ¼  \""+outString+"\\wordsFrequence"+sProcess.getWordsFrequenceCount()+"\" �鿴���");
						System.out.print("\"q\"�˳�! ,  \"c\"����!    :");

					} else {
						System.out.println("����ļ�����ʧ�ܣ������ļ���Ȩ��");
					}

				} else {
					System.out.println("������ļ�/�ļ�����δ�ҵ���Ļ�ļ�(���·�����Ƿ��пո�)");
					System.out.print("\"q\"�˳�! ,  \"c\"����!    :");
//					commandString = scanner.next();
//					if (commandString.equals("q") || commandString.equals("Q")) {
//						ifExit = true;
//					}
				}
			} else {
				if (!srcFile.exists()) {
					System.out.println("�������Ļ�ļ�/��Ļ�ļ��в�����(���·�����Ƿ��пո�)");
				}
				if (!outDir.exists()) {
					System.out.println("���Ŀ¼������(���·�����Ƿ��пո�)");
				}
				if (!outDir.isDirectory()) {
					System.out.println("��Ž����·������һ��Ŀ¼");
				}

				System.out.print("\"q\"�˳�! ,  \"c\"����!    :");
			}
			commandString = scanner.next();
			if (commandString.equals("q") || commandString.equals("Q")) {
				ifExit = true;
			}
		}
		System.out.println("���������������������˳�!");
		scanner.close();    //�ر�����
	}
}
