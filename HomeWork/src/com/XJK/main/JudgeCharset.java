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
  * �����Ƿ��ȡ��ʱ�������ж���Ļ�ļ��ı���
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
	  * �ж��Ƿ���ȡ��ʱ����
	 * @param fileString
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	public static boolean isThisCharset(String fileString,Charset charset) throws IOException {
		boolean isFlag =false;
		String string = "";            //��ʱ�ַ���
		float Count = 0;           //���յ�ʱ����
		int  lineCount = 0;           //ͳ�ƴ��������
		int thresholdCount = 2 ;            //�ҵ����ٴ�ʱ���ᣬ���жϾ��Ǹñ���
		try(Reader reader = new FileReader(fileString,charset);BufferedReader br = new BufferedReader(reader)){   //��ȡ��Ļ�ļ�
			while ((string = br.readLine()) != null) {   //�ж��Ƿ�Ϊʱ����
				lineCount++;
				Matcher mNumber= SubtitleProcess.regexNumber.matcher(string);
				if(!string.equals("") && !mNumber.matches()) {                    //���в����������Ҳ���ǿ��У��ͽ���ʱ��ͳ�ƻ�����ͳ��
					if (SubtitleProcess.getTimes(string) != (float)-1) {        //������ʱ���ᣬͳ��ʱ��
						Count++;
					} 
				}	
				if (lineCount > 50) {    //�ж�50��
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
