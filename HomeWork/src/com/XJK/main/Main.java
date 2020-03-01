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
		// 字幕文件或者文件夹
		String srcString = "F:\\fileForTest\\work\\src";    //测试用的路径
		// 输出文件夹
		String outString = "F:\\fileForTest\\work\\src";   //测试用的路径
		// 是否将处理后的字幕写入文件
		boolean isOutputSubtitle = false;
		// 是否退出菜单
		boolean ifExit = false;
		String  commandString = "";
		List<File> fList ;
		Set<String> fileSet; // 创建一个Set 用于排除同名的文件
		
		//实例化
		SubtitleProcess sProcess = new SubtitleProcess(); 
		
		//简易命令行菜单
		Scanner scanner = new Scanner(System.in);

		System.out.println("》》》》》》》》》 作业 : \"英文\"字幕词频统计(运行在安装了jdk的windows系统)      ");
		while (!ifExit) {
			fList = new ArrayList<File>();   //清空
			fileSet = new HashSet<String>(); //清空
			isOutputSubtitle = false;   //置零标志位
			System.out.print("请输入字幕文件或包含字幕文件的文件夹(绝对路径):");
			srcString = scanner.next();
			System.out.print("请输入存放结果的文件夹(绝对路径):");
			outString = scanner.next();
			File srcFile = new File(srcString); // 输入
			File outDir = new File(outString); // 输出
			if (srcFile.exists() && outDir.exists() && outDir.isDirectory()) { // 文件夹存在
				// 查看是否有字幕文件
				SubtitleProcess.getSubtitleFiles(srcFile, fList, fileSet);
				if (fList.size() > 0) { // 找到字幕文件
					System.out.println("...");
					System.out.println("发现 " + fList.size() + " 个字幕文件");
					sProcess.setOutFileString(outString);     //设置输出文件目录
					if (sProcess.createNewOutFile()) { // 创建输出文件/文件夹
						
						System.out.print("是否输出判断后的字幕文件[输出会耗时]?(y/n):");
						commandString = scanner.next();
						
						if (commandString.equals("y") || commandString.equals("Y")) {
							isOutputSubtitle = true;
						}
						System.out.print("正在统计...");
						//统计发现的字幕文件
						sProcess.repSta(fList,isOutputSubtitle);
						System.out.println("...");
						System.out.println("统计完毕");
						System.out.println("在目录  \""+outString+"\\wordsFrequence"+sProcess.getWordsFrequenceCount()+"\" 查看结果");
						System.out.print("\"q\"退出! ,  \"c\"继续!    :");

					} else {
						System.out.println("输出文件创建失败，请检查文件夹权限");
					}

				} else {
					System.out.println("输入的文件/文件夹中未找到字幕文件(检查路径名是否含有空格)");
					System.out.print("\"q\"退出! ,  \"c\"继续!    :");
//					commandString = scanner.next();
//					if (commandString.equals("q") || commandString.equals("Q")) {
//						ifExit = true;
//					}
				}
			} else {
				if (!srcFile.exists()) {
					System.out.println("输入的字幕文件/字幕文件夹不存在(检查路径名是否含有空格)");
				}
				if (!outDir.exists()) {
					System.out.println("输出目录不存在(检查路径名是否含有空格)");
				}
				if (!outDir.isDirectory()) {
					System.out.println("存放结果的路径不是一个目录");
				}

				System.out.print("\"q\"退出! ,  \"c\"继续!    :");
			}
			commandString = scanner.next();
			if (commandString.equals("q") || commandString.equals("Q")) {
				ifExit = true;
			}
		}
		System.out.println("《《《《《《《《《已退出!");
		scanner.close();    //关闭输入
	}
}
