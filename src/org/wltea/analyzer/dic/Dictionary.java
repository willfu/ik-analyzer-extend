package org.wltea.analyzer.dic;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * IK Analyzer v3.2
 * 词典管理类,单子模式
 *
 * @author 林良益
 */
public class Dictionary {
	/*
	 * 分词器默认字典路径 
	 */
	public static final String PATH_DIC_MAIN = "main.dic";
	public static final String PATH_DIC_SURNAME = "surname.dic";
	public static final String PATH_DIC_QUANTIFIER = "quantifier.dic";
	public static final String PATH_DIC_SUFFIX = "suffix.dic";
	public static final String PATH_DIC_PREP = "preposition.dic";
	public static final String PATH_DIC_STOP = "stopword.dic";


	/*
	 * 词典单子实例
	 */
	private static final Dictionary singleton;

	/*
	 * 词典初始化
	 */
	static {
		singleton = new Dictionary();
	}

	/*
	 * 主词典对象
	 */
	private DictSegment _MainDict;
	/*
	 * 姓氏词典
	 */
	private DictSegment _SurnameDict;
	/*
	 * 量词词典
	 */
	private DictSegment _QuantifierDict;
	/*
	 * 后缀词典
	 */
	private DictSegment _SuffixDict;
	/*
	 * 副词，介词词典
	 */
	private DictSegment _PrepDict;
	/*
	 * 停止词集合
	 */
	private DictSegment _StopWords;

	private Dictionary() {
		try {
			_MainDict = load(Dictionary.PATH_DIC_MAIN); //建立一个主词典实例
			_SurnameDict = load(Dictionary.PATH_DIC_SURNAME); //建立一个姓氏词典实例
			_QuantifierDict = load(Dictionary.PATH_DIC_QUANTIFIER); //建立一个量词典实例
			_SuffixDict = load(Dictionary.PATH_DIC_SUFFIX); //建立一个后缀词典实例
			_PrepDict = load(Dictionary.PATH_DIC_PREP); //建立一个介词\副词词典实例
			_StopWords = load(Dictionary.PATH_DIC_STOP); //建立一个停止词典实例
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private DictSegment load(String path) throws IOException {
		DictSegment segment = new DictSegment((char) 0);
		//读取主词典文件
		InputStream is = new BufferedInputStream(new FileInputStream(new File("dict" + File.separator + path)));
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
		String theWord;
		do {
			theWord = br.readLine();
			if (theWord != null && !"".equals(theWord.trim())) {
				segment.fillSegment(theWord.trim().toCharArray());
			}
		} while (theWord != null);
		is.close();
		return segment;
	}

	/**
	 * 词典初始化
	 * 由于IK Analyzer的词典采用Dictionary类的静态方法进行词典初始化
	 * 只有当Dictionary类被实际调用时，才会开始载入词典，
	 * 这将延长首次分词操作的时间
	 * 该方法提供了一个在应用加载阶段就初始化字典的手段
	 * 用来缩短首次分词时的时延
	 *
	 * @return Dictionary
	 */
	public static Dictionary getInstance() {
		return Dictionary.singleton;
	}

	/**
	 * 检索匹配主词典
	 *
	 * @return Hit 匹配结果描述
	 */
	public static Hit matchInMainDict(char[] charArray, int begin, int length) {
		return singleton._MainDict.match(charArray, begin, length);
	}

	/**
	 * 检索匹配主词典,从已匹配的Hit中直接取出DictSegment，继续向下匹配
	 */
	public static Hit matchWithHit(char[] charArray, int currentIndex, Hit matchedHit) {
		DictSegment ds = matchedHit.getMatchedDictSegment();
		return ds.match(charArray, currentIndex, 1, matchedHit);
	}

	/**
	 * 检索匹配姓氏词典
	 *
	 * @return Hit 匹配结果描述
	 */
	public static Hit matchInSurnameDict(char[] charArray, int begin, int length) {
		return singleton._SurnameDict.match(charArray, begin, length);
	}

	/**
	 * 检索匹配量词词典
	 *
	 * @return Hit 匹配结果描述
	 */
	public static Hit matchInQuantifierDict(char[] charArray, int begin, int length) {
		return singleton._QuantifierDict.match(charArray, begin, length);
	}

	/**
	 * 检索匹配在后缀词典
	 *
	 * @return Hit 匹配结果描述
	 */
	public static Hit matchInSuffixDict(char[] charArray, int begin, int length) {
		return singleton._SuffixDict.match(charArray, begin, length);
	}

	/**
	 * 检索匹配介词、副词词典
	 *
	 * @return Hit 匹配结果描述
	 */
	public static Hit matchInPrepDict(char[] charArray, int begin, int length) {
		return singleton._PrepDict.match(charArray, begin, length);
	}

	/**
	 * 判断是否是停止词
	 *
	 * @return boolean
	 */
	public static boolean isStopWord(char[] charArray, int begin, int length) {
		return singleton._StopWords.match(charArray, begin, length).isMatch();
	}
}
