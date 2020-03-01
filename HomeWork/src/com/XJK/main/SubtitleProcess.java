package com.XJK.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.XJK.main.JudgeCharset;

/**
 * 
 * @author XJK
 *
 */
public class SubtitleProcess {
	
	//����ʡ�ڴ�
	//0~15����-->�ַ���������ַ� + " --> " + 0~15����-->�ַ���������ַ�
	static final Pattern regexTimeGaragraph = Pattern.compile("([^\\-^\\>]{0,18})\\s\\-\\-\\>\\s([^\\-^\\>]{0,18})");    
	//��ȡʱ���֡��롢�����������ʽ
	static final Pattern regexHhMsSsM = Pattern.compile("([0-2][0-4])\\:([0-6][0-9])\\:([0-6][0-9])\\,([0-9]{0,4})");  
	//�ж��Ƿ�ΪӢ�ľ��ӵ�������ʽ
	//Ӣ�ĵ���ֻ��a~z��A~Z��,��.��?��'
	static final Pattern regexEngWord = Pattern.compile("[a-zA-Z\\'\\-]{2,20}|a|A");  
    //�ж��Ƿ�Ϊ��ŵ�������ʽ
	static final Pattern regexNumber = Pattern.compile("[0-9]{1,6}");  

	private int wordsFrequenceCount = 1;
	
	//��Ҫ�´������ļ�/�ļ���
	private File  newOutDir = null;                    //����ļ����ڵĸ�Ŀ¼
	private File  newOutSubtitleDir = null;            //�������Ļ�ļ������Ŀ¼
	private File  newFreFile = null;                   //ͳ�ƺ�õ��ĸ���Ļ�ļ���Ƶ���
	
	//�û�������������ļ���
	private String outFileString;                      //����ļ��������Ŀ¼
	
	//���캯��
	public SubtitleProcess() {
		
	}
	//���캯��
	public SubtitleProcess(String outFileString) {
		this.outFileString = outFileString;
	}
	
	
	public String getOutFileString() {
		return outFileString;
	}
	public void setOutFileString(String outFileString) {
		this.outFileString = outFileString;
	}
	public int getWordsFrequenceCount() {
		return wordsFrequenceCount;
	}
	/**��̬������ֻ���ڻ�ȡ�ļ�
	  *  �Ƿ�����Ļ�ļ�
	 * @param f   �ļ�
	 * @return   true/false
	 */
	private static boolean isSubtitleFile(File f) { 
		if(f.getName().endsWith(".srt") && (!f.getName().startsWith("~$"))) {    //~$��ʾ����һ����word�򿪵������ļ�(����Ļ�ļ���ȡ��˵������Ч�ļ�)
			if(f.isHidden()) {    //��ʾ�������ļ�
				System.out.println("���������ļ�: "+f.getName());
			}
			return true;
		}
		return false;
	}
	/**��̬������ֻ���ڻ�ȡ�ļ�
	  * ��ȡһ���ļ��µ������ļ�(�������ļ��µ��ļ�),���ų�ͬ���ļ�
	 * @param f  �ļ���/�ļ�
	 * @param FileList   ���������ļ��Ĵ���б�
	 */
	public static void getSubtitleFiles(File f, List<File> fileList, Set<String> fileSet) {
		if(f.isDirectory()) {
			for (File i : f.listFiles()) {
				if (i.isDirectory()) {   //���ļ��о͵ݹ�
					getSubtitleFiles(i, fileList, fileSet);
				} else if (i.isFile() && isSubtitleFile(i)) {    //���ļ��ͼ���fileList
					if (fileSet.add(i.getName())) {   //�ܹ��ӵ�Set��˵��û�ظ�
						fileList.add(i);
					}
				}
			}
		} else if (f.isFile() && isSubtitleFile(f)) {    //���ļ��ͼ���fileList
			if (fileSet.add(f.getName())) {   //�ܹ��ӵ�Set��˵��û�ظ�
				fileList.add(f);
			}
		}
	}
	/**
	 * �������Ŀ¼
	 * @return
	 * @throws IOException
	 */
	public boolean createNewOutFile() throws IOException {
		
		//���outFileStringĿ¼���Ƿ���wordsFrequence�ļ���
		File file = new File(outFileString);
		File[] files = file.listFiles();
		if (files.length == 0) {
		} else {
			for (File f : files) {
				if (f.getName().startsWith("wordsFrequence")) {
					wordsFrequenceCount=Integer.parseInt((f.getName().substring(14))) + 1;
				}
			}
		}
		//������������Ŀ¼
		newOutDir =new File(outFileString+"\\wordsFrequence" + wordsFrequenceCount);
		//���ɴ�Ƶ����ļ�
		newFreFile =new File(newOutDir.getAbsolutePath()+"\\frequenceResult.txt");
		//������Ļ�ļ���
		newOutSubtitleDir =new File(newOutDir.getAbsolutePath()+"\\SubtitleWordsResult");

		newOutDir.mkdir();
		newFreFile.createNewFile();
		newOutSubtitleDir.mkdir();
		
		if(newOutDir.exists() && newFreFile.exists() && newOutSubtitleDir.exists()) {
				File[] f = newOutSubtitleDir.listFiles();    //ɾ�������Ļ����ļ�������ڵ��ļ�
				for (File i : f) {
					i.delete();
				}
				
			return true;
		}
		return false;
	}
	/**
     * ��float��ʽ��Ϊָ��С��λ��String������С��λ��0��ȫ
     *
     * @param v     ��Ҫ��ʽ��������
     * @param scale С���������λ
     * @return
     */
    public static String roundByScale(float v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The   scale   must   be   a   positive   integer   or   zero");
        }
        if(scale == 0){
            return new DecimalFormat("0").format(v);
        }
        String formatStr = "0.";
        for(int i=0;i<scale;i++){
            formatStr = formatStr + "0";
        }
        return new DecimalFormat(formatStr).format(v);
    }
	/**
	 * ���Ƶ�ļ�д���Ƶ���
	 * @param writerToNewFreFile    ��Ƶ�ļ�
	 * @param fname                 ��ǰ��Ļ�ļ����ļ���
	 * @param count                 �ڼ�����Ļ�ļ�
	 * @param timeCount             ��ǰ��Ļ�ļ�����ʱ��
	 * @param wordsCount            ��ǰ��Ļ�ļ����ܴ���
	 * @throws IOException
	 */
	private void writeResToFreFile(Writer writerToNewFreFile, String fname,int count, Float timeCount, Float wordsCount) throws IOException {
		//writerToNewFreFile.write("("+count+")"+"      ");
		writerToNewFreFile.write("\r\n");
		if (timeCount != 0) { 
			//writerToNewFreFile.write("��ʱ��: " + (new BigDecimal(timeCount)).setScale(4, RoundingMode.HALF_UP).floatValue()+"(����) , " + "�ܴ���: " + wordsCount+ " , " + "��Ƶ: " + (new BigDecimal((wordsCount/timeCount))).setScale(4, RoundingMode.HALF_UP).floatValue()+"(��/����)"  );
			writerToNewFreFile.write("("+count+")"+"      "+roundByScale(wordsCount/timeCount,4)+"         "+fname );
	
		} else {
			//writerToNewFreFile.write("��ʱ��: " + (new BigDecimal(timeCount)).setScale(4, RoundingMode.HALF_UP).floatValue()+"(����) , " + "�ܴ���: " + wordsCount+ " , " + "��Ƶ: " + 0 +"(��/����)" );
			writerToNewFreFile.write( "("+count+")"+"      "+"0.0000"+"         "+fname );
					
		}
		writerToNewFreFile.write("\r\n");
		writerToNewFreFile.write("\r\n");
	}
	/**
	  * ���µ���Ļ�ļ�д��������Ļ
	 * @param writeToSubtitleFile
	 * @param s        д����ַ���
	 * @param i        ������
	 * @throws IOException
	 */
	private void writeSubtitleToSubtitleDir(Writer writeToSubtitleFile,String s,int i) throws IOException {
		if(writeToSubtitleFile != null) {       //��Ҫд����Ļ�ļ���д
			if(i==0) {   //������
				writeToSubtitleFile.write(s);
			}
			if(i==1) {   //��һ��
				writeToSubtitleFile.write(s);
				writeToSubtitleFile.write("\r\n");
			}
			if (i==2) {   //������
				writeToSubtitleFile.write(s);
				writeToSubtitleFile.write("\r\n");
				writeToSubtitleFile.write("\r\n");		
			}	
			if (i==-1) {
				writeToSubtitleFile.write("\r\n");
			}
		}		
	}
	/**
	  * �ж�һ���ַ����Ƿ���ʱ����
         * ʱ���᣺ 00:04:00,450 --> 00:04:04,080
	  * ʱ�����ص�: ���з���"-->" , ��"-->"ǰ��ֻ�����֡�":"�� "," 
	 * @param s     �����ַ���
	 * @param       regexTime   ʱ�����ַ�����������ʽ
	 * @return      ��ʱ����ͷ��ؾ�����ʱ�䣬����ʱ���᷵��-1
	 */
	public static float getTimes (String s) {
		Matcher m = regexTimeGaragraph.matcher(s);
		if (m.matches()) {     //��ʱ����,�����ȵ���matches()�ж��Ƿ�ƥ��ɹ����ٻ�ȡgroup
			
			//��һ������������һ��ʱ��������������ʱ��
			String s1 = m.group(1);
			String s2 = m.group(2);
			Matcher frontTime = regexHhMsSsM.matcher(s1);
			Matcher reatTime  = regexHhMsSsM.matcher(s2);
			if(frontTime.matches() && reatTime.matches()) {
				int resTimeHour = Integer.parseInt(reatTime.group(1)) - Integer.parseInt(frontTime.group(1));    //Сʱ��
				int resTimeMinute = Integer.parseInt(reatTime.group(2)) - Integer.parseInt(frontTime.group(2));    //���Ӳ�
				int resTimeSecond = Integer.parseInt(reatTime.group(3)) - Integer.parseInt(frontTime.group(3));    //���
				int resTimeMillisecond = Integer.parseInt(reatTime.group(4)) - Integer.parseInt(frontTime.group(4));    //�����
				//ȫ��ת��Ϊ��
				float res = resTimeHour*60*60 + resTimeMinute*60 + resTimeSecond + resTimeMillisecond/(float)1000;
				return res;
			}
			
			/*
			//����ȡʱ�������ʱ���ֵ,��λΪ����
			String s2 = m.group(2);
			Matcher reatTime  = regexHhMsSsM.matcher(s2);
			if(reatTime.matches()) {
				float res = Integer.parseInt(reatTime.group(1))*60 + Integer.parseInt(reatTime.group(2)) + (float)Integer.parseInt(reatTime.group(3))/60 + (float)Integer.parseInt(reatTime.group(4))/60/1000;
				return res;
			}
			*/
		}
		return (float) -1;
	}
    /**
     * �ж��Ƿ�Ϊ����,�����жϵ���Ļ������뵽�ļ�
     * @param s
     * @param writerToNewFreFile
     * @return ���Ǿ��ӷ���0
     * @throws IOException 
     */
	private  int getWordsNum(String s,Writer writeToSubtitleFile) throws IOException {
		int count = 0;
		
	   Matcher mWord = regexEngWord.matcher(s);     
       while (mWord.find()) {   //.find() ���Զ�ɸѡ����������һ���ַ���
    	   
           String sub = s.substring(mWord.start(), mWord.end());
           if (!sub.equals("-") && !sub.equals("'")) {
           		count++;
           		writeSubtitleToSubtitleDir(writeToSubtitleFile,sub+" ",0);
			}
       }
       writeSubtitleToSubtitleDir(writeToSubtitleFile,null,-1);  //����
       writeSubtitleToSubtitleDir(writeToSubtitleFile,"���ʸ����� " + count,2);
       return count;
	}
	/**
	 * ͳ��һ����Ļ�ļ��е��ʵ���������õ�Ӱ��ʱ�����������Ƶ��
	 * @param f
	 * @throws IOException
	 */
	private  float[] Statistics(File f,boolean isOutputSubtitle) throws IOException {
		String string = "";            //��ʱ�ַ���
		float res[] = new float[2];
		float	tempTimeCount = 0;     //��ʱ���ʱ��������ʾ��ʱ��
		float timeCount = 0;           //���յ�ʱ����
		int   wordsCount = 0;          //ͳ�Ƶ��ʸ���
		int   lineCount = 0;           //ͳ�ƴ��������
		Writer writeToSubtitleFile = null;
		
		try(Reader reader = new FileReader(f.getAbsolutePath(),JudgeCharset.getCharset(f.getAbsolutePath()));BufferedReader br = new BufferedReader(reader)){   //��ȡ��Ļ�ļ�
			
			if(isOutputSubtitle) {    //��ȡ���ڵ�ǰ��Ļ�ļ����µ���Ļ�ļ�
				writeToSubtitleFile = new FileWriter(newOutSubtitleDir.getAbsolutePath()+"\\"+f.getName().substring(0,f.getName().length()-4)+".txt");
				writeSubtitleToSubtitleDir(writeToSubtitleFile,"Location  :  " + f.getAbsolutePath(), 2);    //д��location
			}

			while ((string = br.readLine()) != null) {   //ͳ���ļ����ַ��ĸ���������ʱ��
				lineCount++;
				Matcher mNumber= regexNumber.matcher(string);
				if(!string.equals("") && !mNumber.matches()) {                    //���в����������Ҳ���ǿ��У��ͽ���ʱ��ͳ�ƻ�����ͳ��
					if ((tempTimeCount = getTimes(string)) != (float)-1) {        //������ʱ���ᣬͳ��ʱ��
						//if(tempTimeCount > timeCount) {                           //�¸��µ�ʱ����ھ�ʱ�䣬�Ž���ʱ�串��
							timeCount += tempTimeCount;
						//}
					} else {     //Ϊ���ӣ�ͳ�Ƶ��ʸ���
					    writeSubtitleToSubtitleDir(writeToSubtitleFile,"��" + lineCount +"�У�",0);
						wordsCount += getWordsNum(string,writeToSubtitleFile);				
					}
				}	
			}
			
			if (writeToSubtitleFile != null) {   //�ر��´�������Ļ�ļ�
				writeToSubtitleFile.close();
			}
			res[0] = timeCount;
			res[1] = wordsCount;
	    }
		return res;
	}
	/**
	 * ��һ���ļ���������ظ�ͳ��,����Ƶ���д���Ƶ�ļ�
	 * @param f                �������ļ���List�б�
	 * @param isOutputSubtitle
	 * @throws IOException     �Ƿ������������Ļ
	 */
	public void repSta(List<File> f,boolean isOutputSubtitle) throws IOException {
		int fileCount = 0;   
		float[] res = new float[2];
		//��ȡ��Ƶ����ļ�
		try(Writer writerToNewFreFile = new FileWriter(newFreFile.getAbsolutePath())){   //try��������������ļ��ر����
			writerToNewFreFile.write("���"+"     "+"��Ƶ(��/����)    "+"    "+"�ļ�ȫ·��");
			writerToNewFreFile.write("\r\n");
			for(File iFile : f) {
				fileCount+=1;
				res = Statistics(iFile,isOutputSubtitle);
				//���Ƶ����ļ�д���Ƶ
				writeResToFreFile(writerToNewFreFile,iFile.getAbsolutePath(),fileCount, res[0]/60, res[1]);
			}
		}
		
	}
}
