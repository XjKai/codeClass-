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
	
	//这样省内存
	//0~15个除-->字符外的任意字符 + " --> " + 0~15个除-->字符外的任意字符
	static final Pattern regexTimeGaragraph = Pattern.compile("([^\\-^\\>]{0,18})\\s\\-\\-\\>\\s([^\\-^\\>]{0,18})");    
	//获取时、分、秒、毫秒的正则表达式
	static final Pattern regexHhMsSsM = Pattern.compile("([0-2][0-4])\\:([0-6][0-9])\\:([0-6][0-9])\\,([0-9]{0,4})");  
	//判断是否为英文句子的正则表达式
	//英文单词只有a~z、A~Z、,、.、?、'
	static final Pattern regexEngWord = Pattern.compile("[a-zA-Z\\'\\-]{2,20}|a|A");  
    //判断是否为序号的正则表达式
	static final Pattern regexNumber = Pattern.compile("[0-9]{1,6}");  

	private int wordsFrequenceCount = 1;
	
	//需要新创建的文件/文件夹
	private File  newOutDir = null;                    //输出文件所在的根目录
	private File  newOutSubtitleDir = null;            //处理后字幕文件的输出目录
	private File  newFreFile = null;                   //统计后得到的各字幕文件词频结果
	
	//用户输入的输出结果文件夹
	private String outFileString;                      //输出文件的最外层目录
	
	//构造函数
	public SubtitleProcess() {
		
	}
	//构造函数
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
	/**静态方法，只用于获取文件
	  *  是否是字幕文件
	 * @param f   文件
	 * @return   true/false
	 */
	private static boolean isSubtitleFile(File f) { 
		if(f.getName().endsWith(".srt") && (!f.getName().startsWith("~$"))) {    //~$表示这是一个被word打开的隐藏文件(对字幕文件读取来说，是无效文件)
			if(f.isHidden()) {    //提示有隐藏文件
				System.out.println("含有隐藏文件: "+f.getName());
			}
			return true;
		}
		return false;
	}
	/**静态方法，只用于获取文件
	  * 获取一个文件下的所有文件(包括子文件下的文件),并排除同名文件
	 * @param f  文件夹/文件
	 * @param FileList   符合条件文件的存放列表
	 */
	public static void getSubtitleFiles(File f, List<File> fileList, Set<String> fileSet) {
		if(f.isDirectory()) {
			for (File i : f.listFiles()) {
				if (i.isDirectory()) {   //是文件夹就递归
					getSubtitleFiles(i, fileList, fileSet);
				} else if (i.isFile() && isSubtitleFile(i)) {    //是文件就加入fileList
					if (fileSet.add(i.getName())) {   //能够加到Set里说明没重复
						fileList.add(i);
					}
				}
			}
		} else if (f.isFile() && isSubtitleFile(f)) {    //是文件就加入fileList
			if (fileSet.add(f.getName())) {   //能够加到Set里说明没重复
				fileList.add(f);
			}
		}
	}
	/**
	 * 创建输出目录
	 * @return
	 * @throws IOException
	 */
	public boolean createNewOutFile() throws IOException {
		
		//检查outFileString目录下是否有wordsFrequence文件夹
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
		//生成输出结果根目录
		newOutDir =new File(outFileString+"\\wordsFrequence" + wordsFrequenceCount);
		//生成词频结果文件
		newFreFile =new File(newOutDir.getAbsolutePath()+"\\frequenceResult.txt");
		//生成字幕文件夹
		newOutSubtitleDir =new File(newOutDir.getAbsolutePath()+"\\SubtitleWordsResult");

		newOutDir.mkdir();
		newFreFile.createNewFile();
		newOutSubtitleDir.mkdir();
		
		if(newOutDir.exists() && newFreFile.exists() && newOutSubtitleDir.exists()) {
				File[] f = newOutSubtitleDir.listFiles();    //删除存放字幕结果文件夹里存在的文件
				for (File i : f) {
					i.delete();
				}
				
			return true;
		}
		return false;
	}
	/**
     * 将float格式化为指定小数位的String，不足小数位用0补全
     *
     * @param v     需要格式化的数字
     * @param scale 小数点后保留几位
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
	 * 向词频文件写入词频结果
	 * @param writerToNewFreFile    词频文件
	 * @param fname                 当前字幕文件的文件名
	 * @param count                 第几个字幕文件
	 * @param timeCount             当前字幕文件的总时间
	 * @param wordsCount            当前字幕文件的总词数
	 * @throws IOException
	 */
	private void writeResToFreFile(Writer writerToNewFreFile, String fname,int count, Float timeCount, Float wordsCount) throws IOException {
		//writerToNewFreFile.write("("+count+")"+"      ");
		writerToNewFreFile.write("\r\n");
		if (timeCount != 0) { 
			//writerToNewFreFile.write("总时间: " + (new BigDecimal(timeCount)).setScale(4, RoundingMode.HALF_UP).floatValue()+"(分钟) , " + "总词数: " + wordsCount+ " , " + "词频: " + (new BigDecimal((wordsCount/timeCount))).setScale(4, RoundingMode.HALF_UP).floatValue()+"(个/分钟)"  );
			writerToNewFreFile.write("("+count+")"+"      "+roundByScale(wordsCount/timeCount,4)+"         "+fname );
	
		} else {
			//writerToNewFreFile.write("总时间: " + (new BigDecimal(timeCount)).setScale(4, RoundingMode.HALF_UP).floatValue()+"(分钟) , " + "总词数: " + wordsCount+ " , " + "词频: " + 0 +"(个/分钟)" );
			writerToNewFreFile.write( "("+count+")"+"      "+"0.0000"+"         "+fname );
					
		}
		writerToNewFreFile.write("\r\n");
		writerToNewFreFile.write("\r\n");
	}
	/**
	  * 向新的字幕文件写处理后的字幕
	 * @param writeToSubtitleFile
	 * @param s        写入的字符串
	 * @param i        换行数
	 * @throws IOException
	 */
	private void writeSubtitleToSubtitleDir(Writer writeToSubtitleFile,String s,int i) throws IOException {
		if(writeToSubtitleFile != null) {       //需要写入字幕文件才写
			if(i==0) {   //不换行
				writeToSubtitleFile.write(s);
			}
			if(i==1) {   //换一行
				writeToSubtitleFile.write(s);
				writeToSubtitleFile.write("\r\n");
			}
			if (i==2) {   //换两行
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
	  * 判断一个字符串是否是时间轴
         * 时间轴： 00:04:00,450 --> 00:04:04,080
	  * 时间轴特点: 具有符号"-->" , 且"-->"前后只有数字、":"、 "," 
	 * @param s     输入字符串
	 * @param       regexTime   时间轴字符串的正则表达式
	 * @return      是时间轴就返回经过的时间，不是时间轴返回-1
	 */
	public static float getTimes (String s) {
		Matcher m = regexTimeGaragraph.matcher(s);
		if (m.matches()) {     //是时间轴,必须先调用matches()判断是否匹配成功，再获取group
			
			//这一段是用来计算一个时间轴中所经过的时间
			String s1 = m.group(1);
			String s2 = m.group(2);
			Matcher frontTime = regexHhMsSsM.matcher(s1);
			Matcher reatTime  = regexHhMsSsM.matcher(s2);
			if(frontTime.matches() && reatTime.matches()) {
				int resTimeHour = Integer.parseInt(reatTime.group(1)) - Integer.parseInt(frontTime.group(1));    //小时差
				int resTimeMinute = Integer.parseInt(reatTime.group(2)) - Integer.parseInt(frontTime.group(2));    //分钟差
				int resTimeSecond = Integer.parseInt(reatTime.group(3)) - Integer.parseInt(frontTime.group(3));    //秒差
				int resTimeMillisecond = Integer.parseInt(reatTime.group(4)) - Integer.parseInt(frontTime.group(4));    //毫秒差
				//全部转化为秒
				float res = resTimeHour*60*60 + resTimeMinute*60 + resTimeSecond + resTimeMillisecond/(float)1000;
				return res;
			}
			
			/*
			//仅获取时间轴结束时间的值,单位为分钟
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
     * 判断是否为句子,并将判断的字幕结果输入到文件
     * @param s
     * @param writerToNewFreFile
     * @return 不是句子返回0
     * @throws IOException 
     */
	private  int getWordsNum(String s,Writer writeToSubtitleFile) throws IOException {
		int count = 0;
		
	   Matcher mWord = regexEngWord.matcher(s);     
       while (mWord.find()) {   //.find() 会自动筛选符合条件的一段字符串
    	   
           String sub = s.substring(mWord.start(), mWord.end());
           if (!sub.equals("-") && !sub.equals("'")) {
           		count++;
           		writeSubtitleToSubtitleDir(writeToSubtitleFile,sub+" ",0);
			}
       }
       writeSubtitleToSubtitleDir(writeToSubtitleFile,null,-1);  //换行
       writeSubtitleToSubtitleDir(writeToSubtitleFile,"单词个数： " + count,2);
       return count;
	}
	/**
	 * 统计一个字幕文件中单词的总数，获得电影总时长，并计算词频数
	 * @param f
	 * @throws IOException
	 */
	private  float[] Statistics(File f,boolean isOutputSubtitle) throws IOException {
		String string = "";            //临时字符串
		float res[] = new float[2];
		float	tempTimeCount = 0;     //临时存放时间轴上显示的时间
		float timeCount = 0;           //最终的时间结果
		int   wordsCount = 0;          //统计单词个数
		int   lineCount = 0;           //统计处理的行数
		Writer writeToSubtitleFile = null;
		
		try(Reader reader = new FileReader(f.getAbsolutePath(),JudgeCharset.getCharset(f.getAbsolutePath()));BufferedReader br = new BufferedReader(reader)){   //读取字幕文件
			
			if(isOutputSubtitle) {    //获取属于当前字幕文件的新的字幕文件
				writeToSubtitleFile = new FileWriter(newOutSubtitleDir.getAbsolutePath()+"\\"+f.getName().substring(0,f.getName().length()-4)+".txt");
				writeSubtitleToSubtitleDir(writeToSubtitleFile,"Location  :  " + f.getAbsolutePath(), 2);    //写入location
			}

			while ((string = br.readLine()) != null) {   //统计文件内字符的个数，和总时长
				lineCount++;
				Matcher mNumber= regexNumber.matcher(string);
				if(!string.equals("") && !mNumber.matches()) {                    //该行不是数字序号也不是空行，就进行时间统计或字数统计
					if ((tempTimeCount = getTimes(string)) != (float)-1) {        //该行是时间轴，统计时长
						//if(tempTimeCount > timeCount) {                           //新更新的时间大于旧时间，才将旧时间覆盖
							timeCount += tempTimeCount;
						//}
					} else {     //为句子，统计单词个数
					    writeSubtitleToSubtitleDir(writeToSubtitleFile,"第" + lineCount +"行：",0);
						wordsCount += getWordsNum(string,writeToSubtitleFile);				
					}
				}	
			}
			
			if (writeToSubtitleFile != null) {   //关闭新创建的字幕文件
				writeToSubtitleFile.close();
			}
			res[0] = timeCount;
			res[1] = wordsCount;
	    }
		return res;
	}
	/**
	 * 对一个文件数组进行重复统计,并词频结果写入词频文件
	 * @param f                待处理文件的List列表
	 * @param isOutputSubtitle
	 * @throws IOException     是否输出正则后的字幕
	 */
	public void repSta(List<File> f,boolean isOutputSubtitle) throws IOException {
		int fileCount = 0;   
		float[] res = new float[2];
		//获取词频结果文件
		try(Writer writerToNewFreFile = new FileWriter(newFreFile.getAbsolutePath())){   //try，不用另外添加文件关闭语句
			writerToNewFreFile.write("序号"+"     "+"词频(词/分钟)    "+"    "+"文件全路径");
			writerToNewFreFile.write("\r\n");
			for(File iFile : f) {
				fileCount+=1;
				res = Statistics(iFile,isOutputSubtitle);
				//向词频结果文件写入词频
				writeResToFreFile(writerToNewFreFile,iFile.getAbsolutePath(),fileCount, res[0]/60, res[1]);
			}
		}
		
	}
}
