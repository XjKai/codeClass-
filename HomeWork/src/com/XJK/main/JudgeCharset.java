package com.XJK.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import com.XJK.main.SubtitleProcess;
/**
  * 根据是否读取到时间轴来判断字幕文件的编码
 * @author MSI
 *
 */
public class JudgeCharset {
	
	
	public static Charset getCharset(String fileString) throws IOException {
		if (isThisCharset(fileString, StandardCharsets.US_ASCII)) {
			return StandardCharsets.US_ASCII;
		} else if (isThisCharset(fileString, StandardCharsets.UTF_16LE)) {
			return StandardCharsets.UTF_16LE;
		} else if (isThisCharset(fileString, StandardCharsets.UTF_8)) {
			return StandardCharsets.UTF_8;
		} else if (isThisCharset(fileString, StandardCharsets.UTF_16)) {
			return StandardCharsets.UTF_16;
		} else if (isThisCharset(fileString, StandardCharsets.UTF_16BE)) {
			return StandardCharsets.UTF_16BE;
		} else {
			return StandardCharsets.ISO_8859_1;
		}
	}
	/**
	  * 判断是否提取到时间轴
	 * @param fileString
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	public static boolean isThisCharset(String fileString,Charset charset) throws IOException {
		boolean isFlag =false;
		String string = "";            //临时字符串
		float Count = 0;           //最终的时间结果
		int  lineCount = 0;           //统计处理的行数
		int thresholdCount = 2 ;            //找到多少次时间轴，则判断就是该编码
		try(Reader reader = new FileReader(fileString,charset);BufferedReader br = new BufferedReader(reader)){   //读取字幕文件
			while ((string = br.readLine()) != null) {   //判断是否为时间轴
				lineCount++;
				Matcher mNumber= SubtitleProcess.regexNumber.matcher(string);
				if(!string.equals("") && !mNumber.matches()) {                    //该行不是数字序号也不是空行，就进行时间统计或字数统计
					if (SubtitleProcess.getTimes(string) != (float)-1) {        //该行是时间轴，统计时长
						Count++;
					} 
				}	
				if (lineCount > 50) {    //判断50行
					if (Count >= thresholdCount) {
						isFlag = true;
					}
					break;
				}
			}
		}
		return isFlag;
	}
}
