package org.wltea.analyzer;

import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.dic.Hit;
import org.wltea.analyzer.seg.ISegmenter;

import java.util.HashSet;
import java.util.Set;

/**
 * 分词器上下文状态
 *
 * @author 林良益
 */
public class Context {

	//是否使用最大词长切分（粗粒度）
	private boolean isMaxWordLength = false;
	//记录Reader内已分析的字串总长度
	//在分多段分析词元时，该变量累计当前的segmentBuff相对于reader的位移
	private int buffOffset;
	//最近一次读入的,可处理的字串长度
	private int available;
	//最近一次分析的字串长度
	private int lastAnalyzed;
	//当前缓冲区位置指针
	private int cursor;
	//字符窜读取缓冲
	private char[] segmentBuffer;
	/*
	 * 记录正在使用buffer的分词器对象
	 * 如果set中存在有分词器对象，则buffer不能进行位移操作（处于locked状态）
	 */
	private Set<ISegmenter> buffLocker;
	/*
	 * 词元结果集，为每次游标的移动，存储切分出来的词元
	 */
	private IKSortedLinkSet lexemeSet;


	Context(char[] segmentBuffer, boolean isMaxWordLength) {
		this.isMaxWordLength = isMaxWordLength;
		this.segmentBuffer = segmentBuffer;
		this.buffLocker = new HashSet<>(4);
		this.lexemeSet = new IKSortedLinkSet();
	}

	/**
	 * 重置上下文
	 */
	public void resetContext() {
		buffLocker.clear();
		lexemeSet = new IKSortedLinkSet();
		buffOffset = 0;
		available = 0;
		lastAnalyzed = 0;
		cursor = 0;
	}

	public boolean isMaxWordLength() {
		return isMaxWordLength;
	}

	public void setMaxWordLength(boolean isMaxWordLength) {
		this.isMaxWordLength = isMaxWordLength;
	}

	public int getBuffOffset() {
		return buffOffset;
	}


	public void setBuffOffset(int buffOffset) {
		this.buffOffset = buffOffset;
	}

	public int getLastAnalyzed() {
		return lastAnalyzed;
	}


	public void setLastAnalyzed(int lastAnalyzed) {
		this.lastAnalyzed = lastAnalyzed;
	}


	public int getCursor() {
		return cursor;
	}


	public void setCursor(int cursor) {
		this.cursor = cursor;
	}

	public void lockBuffer(ISegmenter segmenter) {
		this.buffLocker.add(segmenter);
	}

	public void unlockBuffer(ISegmenter segmenter) {
		this.buffLocker.remove(segmenter);
	}

	/**
	 * 只要buffLocker中存在ISegmenter对象
	 * 则buffer被锁定
	 *
	 * @return boolean 缓冲去是否被锁定
	 */
	public boolean isBufferLocked() {
		return this.buffLocker.size() > 0;
	}

	public int getAvailable() {
		return available;
	}

	public void setAvailable(int available) {
		this.available = available;
	}

	public void pickNames() {
		Lexeme element = lexemeSet.peekFirst();
		while (element != null) {
			if (element.getType() == Lexeme.Type.TYPE_CJK_SUR_NAME) {
				int begin = element.getBegin();
				int end = element.getBegin() + element.getLength();
				// 下一个字是否在其他lexeme中，并且不是姓
				Lexeme nextElement = element.getNext();
				if (nextElement != null) {
					Hit surNameHit = Dictionary.matchInSurnameDict(segmentBuffer, nextElement.getBegin(), 1);
					if (surNameHit.isUnMatch() && end == nextElement.getBegin() && nextElement.getLength() == 1) {
						// 下两个字是否在其他lexeme中，并且不是姓
						end = nextElement.getEndPosition();
						nextElement = nextElement.getNext();
						Lexeme newLexeme = null;
						if (nextElement != null) {
							surNameHit = Dictionary.matchInSurnameDict(segmentBuffer, nextElement.getBegin(), 1);
							if (surNameHit.isUnMatch() && end == nextElement.getBegin() && nextElement.getLength() == 1) {
								newLexeme = new Lexeme(getBuffOffset(), begin, element.getLength() + 2, Lexeme.Type.TYPE_CJK_FULL_NAME);
							}
						} else {
							newLexeme = new Lexeme(getBuffOffset(), begin, element.getLength() + 1, Lexeme.Type.TYPE_CJK_FULL_NAME);
						}
						if (newLexeme != null) {
							addLexeme(newLexeme);
						}
					}
				}
			}
			element = element.getNext();
		}
	}

	/**
	 * 取出分词结果集中的首个词元
	 *
	 * @return Lexeme 集合的第一个词元
	 */
	public Lexeme firstLexeme() {
		return this.lexemeSet.pollFirst();
	}

	/**
	 * 取出分词结果集中的最后一个词元
	 *
	 * @return Lexeme 集合的最后一个词元
	 */
	public Lexeme lastLexeme() {
		return this.lexemeSet.pollLast();
	}

	/**
	 * 向分词结果集添加词元
	 *
	 * @param lexeme
	 */
	public void addLexeme(Lexeme lexeme) {
		if (!Dictionary.isStopWord(segmentBuffer, lexeme.getBegin(), lexeme.getLength())) {
			this.lexemeSet.addLexeme(lexeme);
		}
	}

	/**
	 * 获取分词结果集大小
	 *
	 * @return int 分词结果集大小
	 */
	public int getResultSize() {
		return this.lexemeSet.size();
	}

	/**
	 * 排除结果集中完全交叠（彼此包含）的词元
	 * 进行最大切分的时候，过滤长度较小的交叠词元
	 */
	public void excludeOverlap() {
		this.lexemeSet.excludeOverlap();
	}
}
