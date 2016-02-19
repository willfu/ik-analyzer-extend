package org.wltea.analyzer.seg;

import org.wltea.analyzer.Context;
import org.wltea.analyzer.Lexeme;
import org.wltea.analyzer.help.CharacterHelper;
import org.wltea.analyzer.help.PatternUtil;

/**
 * 负责处理字母的子分词器，涵盖一下范围
 * 1.英文单词、英文加阿拉伯数字、专有名词（公司名）
 * 2.IP地址、Email、URL
 *
 * @author 林良益
 */
public class LetterSegmenter implements ISegmenter {

	//链接符号
	public static final char[] CONNECTOR = new char[]{':', '+', '-', '_', '.', '@', '&', '/', '\\'};
	/*
	 * 词元的开始位置，
	 * 同时作为子分词器状态标识
	 * 当start > -1 时，标识当前的分词器正在处理字符
	 */
	private int start;
	/*
	 * 记录词元结束位置
	 * end记录的是在词元中最后一个出现的Letter但非Sign_Connector的字符的位置
	 */
	private int end;

	/*
	 * 字母起始位置
	 */
	private int letterStart;

	/*
	 * 字母结束位置
	 */
	private int letterEnd;

	public LetterSegmenter() {
		start = -1;
		end = -1;
		letterStart = -1;
		letterEnd = -1;
	}

	/* (non-Javadoc)
	 * @see org.wltea.org.wltea.analyzer.ISegmenter#nextLexeme(org.wltea.org.wltea.analyzer.IKSegmentation.Context)
	 */
	public void nextLexeme(char[] segmentBuff, Context context) {

		//读取当前位置的char	
		char input = segmentBuff[context.getCursor()];

		boolean bufferLockFlag;
		//处理混合字母
		bufferLockFlag = this.processMixLetter(input, context);
		//处理英文字母
		bufferLockFlag = this.processEnglishLetter(input, context) || bufferLockFlag;

		//判断是否锁定缓冲区
		if (bufferLockFlag) {
			context.lockBuffer(this);
		} else {
			//对缓冲区解锁
			context.unlockBuffer(this);
		}
	}

	/**
	 * 处理数字字母混合输出
	 * 如：windows2000 | linliangyi2005@gmail.com
	 */
	private boolean processMixLetter(char input, Context context) {
		if (start == -1) { // 当前的分词器尚未开始处理字符
			if (isAcceptedCharStart(input)) {
				//记录起始指针的位置,标明分词器进入处理状态
				start = context.getCursor();
				end = start;
			}
		} else {//当前的分词器正在处理字符
			if (isAcceptedChar(input)) {
				// 不在忽略尾部的链接字符
				end = context.getCursor();
			} else {
				// 生成已切分的词元
				addLexemeMixed(context);
				// 设置当前分词器状态为“待处理”
				start = -1;
				end = -1;
			}
		}

		// 读取缓冲区最后一个字符，直接输出
		if (context.getCursor() == context.getAvailable() - 1) {
			if (start != -1 && end != -1) {
				// 生成已切分的词元
				addLexemeMixed(context);
			}
			// 设置当前分词器状态为“待处理”
			start = -1;
			end = -1;
		}

		// 判断是否锁定缓冲区
		return start != -1 || end != -1;
	}

	private void addLexemeMixed(Context context) {
		String text = context.text(start, end - start + 1);
		boolean isUrl = PatternUtil.isUrl(text);
		boolean isEmail = PatternUtil.isEmail(text);
		Lexeme newLexeme;
		if (isUrl) {
			newLexeme = new Lexeme(context.getBuffOffset(), start, end - start + 1, Lexeme.Type.TYPE_URL);
		} else if (isEmail) {
			newLexeme = new Lexeme(context.getBuffOffset(), start, end - start + 1, Lexeme.Type.TYPE_EMAIL);
		} else {
			newLexeme = new Lexeme(context.getBuffOffset(), start, end - start + 1, Lexeme.Type.TYPE_LETTER);
		}
		context.addLexeme(newLexeme);
	}

	/**
	 * 处理纯英文字母输出
	 */
	private boolean processEnglishLetter(char input, Context context) {
		if (letterStart == -1) { // 当前的分词器尚未开始处理数字字符
			if (CharacterHelper.isEnglishLetter(input)) {
				// 记录起始指针的位置,标明分词器进入处理状态
				letterStart = context.getCursor();
				letterEnd = letterStart;
			}
		} else { // 当前的分词器正在处理数字字符
			if (CharacterHelper.isEnglishLetter(input)) {
				// 记录当前指针位置为结束位置
				letterEnd = context.getCursor();
			} else {
				// 生成已切分的词元
				Lexeme newLexeme = new Lexeme(context.getBuffOffset(), letterStart, letterEnd - letterStart + 1, Lexeme.Type.TYPE_LETTER);
				context.addLexeme(newLexeme);
				// 设置当前分词器状态为“待处理”
				letterStart = -1;
				letterEnd = -1;
			}
		}

		// 读取缓冲区最后一个字符，直接输出
		if (context.getCursor() == context.getAvailable() - 1) {
			if (letterStart != -1 && letterEnd != -1) {
				//生成已切分的词元
				Lexeme newLexeme = new Lexeme(context.getBuffOffset(), letterStart, letterEnd - letterStart + 1, Lexeme.Type.TYPE_LETTER);
				context.addLexeme(newLexeme);
			}
			//设置当前分词器状态为“待处理”
			letterStart = -1;
			letterEnd = -1;
		}

		//判断是否锁定缓冲区
		return letterStart != -1 || letterEnd != -1;
	}

	private boolean isLetterConnector(char input) {
		for (char c : CONNECTOR) {
			if (c == input) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断char是否是可接受的起始字符
	 */
	private boolean isAcceptedCharStart(char input) {
		return CharacterHelper.isEnglishLetter(input) || CharacterHelper.isArabicNumber(input);
	}

	/**
	 * 判断char是否是可接受的字符
	 */
	private boolean isAcceptedChar(char input) {
		return isLetterConnector(input)
				|| CharacterHelper.isEnglishLetter(input)
				|| CharacterHelper.isArabicNumber(input);
	}

	public void reset() {
		start = -1;
		end = -1;
		letterStart = -1;
		letterEnd = -1;
	}
}
