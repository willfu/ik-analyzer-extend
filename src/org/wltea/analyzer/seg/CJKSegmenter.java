package org.wltea.analyzer.seg;

import org.wltea.analyzer.Context;
import org.wltea.analyzer.Lexeme;
import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.dic.Hit;
import org.wltea.analyzer.help.CharacterHelper;

import java.util.LinkedList;
import java.util.List;

/**
 * 中文（CJK）词元处理子分词器，涵盖一下范围
 * 1.中文词语
 * 2.姓名
 * 3.地名
 * 4.未知词（单字切分）
 * 5.日文/韩文（单字切分）
 *
 * @author 林良益
 * @version 3.2
 */
public class CJKSegmenter implements ISegmenter {
	/*
	 * 已完成处理的位置
	 */
	private int doneIndex;
	/*
	 * Hit对列，记录匹配中的Hit对象
	 */
	private List<Hit> hitList; // 记录前缀词

	public CJKSegmenter() {
		doneIndex = -1;
		hitList = new LinkedList<>();
	}

	/* (non-Javadoc)
	 * @see org.wltea.org.wltea.analyzer.seg.ISegmenter#nextLexeme(org.wltea.org.wltea.analyzer.Context)
	 */
	public void nextLexeme(char[] segmentBuffer, Context context) {
		int cursorPosition = context.getCursor();
		char inputChar = segmentBuffer[cursorPosition];

		if (CharacterHelper.isCJKCharacter(inputChar)) { // 是（CJK）字符，则进行处理
			if (hitList.size() > 0) {
				// 处理词段队列
				Hit[] tmpArray = hitList.toArray(new Hit[hitList.size()]);
				for (Hit hit : tmpArray) {
					hit = Dictionary.matchWithHit(segmentBuffer, cursorPosition, hit);

					if (hit.isMatch()) { // 匹配成词
						// 判断是否有不可识别的词段
						if (hit.getBegin() > doneIndex + 1) {
							// 输出并处理从doneIndex+1 到 seg.start - 1之间的未知词段
							processUnknown(segmentBuffer, context, doneIndex + 1, hit.getBegin() - 1);
						}
						// 输出当前的词
						Lexeme newLexeme = new Lexeme(context.getBuffOffset(), hit.getBegin(),
								cursorPosition - hit.getBegin() + 1, Lexeme.Type.TYPE_CJK_NORMAL);
						context.addLexeme(newLexeme);
						// 更新goneIndex，标识已处理
						if (doneIndex < cursorPosition) {
							doneIndex = cursorPosition;
						}

						if (hit.isPrefix()) {
							// 同时也是前缀，留在list中，等待下次匹配到更长的词
						} else {
							// 后面不再可能有匹配了, 移出当前的hit
							hitList.remove(hit);
						}

					} else if (hit.isPrefix()) { // 前缀，未匹配成词

					} else if (hit.isUnMatch()) { // 不匹配
						// 移出当前的hit
						hitList.remove(hit);
					}
				}
			}
			processNewHit(context, segmentBuffer, cursorPosition);
			processSurName(context, segmentBuffer, cursorPosition);
		} else { // 输入的不是中文(CJK)字符
			if (hitList.size() > 0 && doneIndex < cursorPosition - 1) {
				for (Hit hit : hitList) {
					// 判断是否有不可识别的词段
					if (doneIndex < hit.getEnd()) {
						// 输出并处理从doneIndex+1 到 seg.end之间的未知词段
						processUnknown(segmentBuffer, context, doneIndex + 1, hit.getEnd());
					}
				}
			}
			// 清空词段队列
			hitList.clear();
			// 更新doneIndex，标识已处理
			if (doneIndex < cursorPosition) {
				doneIndex = cursorPosition;
			}
		}

		// 缓冲区结束临界处理
		if (cursorPosition == context.getAvailable() - 1) { // 读取缓冲区结束的最后一个字符
			if (hitList.size() > 0 // 队列中还有未处理词段
					&& doneIndex < context.getCursor()) { // 最后一个字符还未被输出过
				for (Hit hit : hitList) {
					// 判断是否有不可识别的词段
					if (doneIndex < hit.getEnd()) {
						// 输出并处理从doneIndex+1 到 seg.end之间的未知词段
						processUnknown(segmentBuffer, context, doneIndex + 1, hit.getEnd());
					}
				}
			}
			// 清空词段队列
			hitList.clear();
		}

		// 判断是否锁定缓冲区
		if (hitList.size() == 0) {
			context.unlockBuffer(this);
		} else {
			context.lockBuffer(this);
		}
	}

	// 处理以input为开始的一个新hit
	private void processNewHit(Context context, char[] segmentBuffer, int cursorPosition) {
		Hit hit = Dictionary.matchInMainDict(segmentBuffer, cursorPosition, 1);
		if (hit.isMatch()) { // 匹配成词
			// 判断是否有不可识别的词段
			if (cursorPosition > doneIndex + 1) {
				// 输出并处理从doneIndex+1 到 context.getCursor()- 1之间的未知
				processUnknown(segmentBuffer, context, doneIndex + 1, cursorPosition - 1);
			}
			// 输出当前的词
			Lexeme newLexeme = new Lexeme(context.getBuffOffset(), cursorPosition, 1, Lexeme.Type.TYPE_CJK_NORMAL);
			context.addLexeme(newLexeme);
			// 更新doneIndex，标识已处理
			if (doneIndex < cursorPosition) {
				doneIndex = cursorPosition;
			}

			if (hit.isPrefix()) { // 同时也是前缀
				hitList.add(hit);
			}

		} else if (hit.isPrefix()) { // 前缀，未匹配成词
			hitList.add(hit);

		} else if (hit.isUnMatch()) { // 不匹配，当前的input不是词，也不是词前缀，将其视为分割性的字符
			if (doneIndex < cursorPosition) {
				// 输出从doneIndex到当前字符（含当前字符）之间的未知词
				processUnknown(segmentBuffer, context, doneIndex + 1, cursorPosition);
				//更新doneIndex，标识已处理
				doneIndex = cursorPosition;
			} else {
				// 当前不匹配的字符已经被处理过了，不需要再processUnknown
			}
		}
	}

	// 处理姓氏
	private void processSurName(Context context, char[] segmentBuffer, int cursorPosition) {
		Hit hit = Dictionary.matchInSurnameDict(segmentBuffer, cursorPosition, 1);
		if (hit.isMatch()) {
			// 输出姓氏
			Lexeme newLexeme = new Lexeme(context.getBuffOffset(), cursorPosition, 1, Lexeme.Type.TYPE_CJK_SUR_NAME);
			context.addLexeme(newLexeme);
		} else if (hit.isPrefix()) {
			// 处理多字姓氏。目前只支持双字
			if (cursorPosition + 1 < segmentBuffer.length) {
				Hit nameHit = hit.getMatchedDictSegment().match(segmentBuffer, cursorPosition + 1, 1);
				if (nameHit.isMatch()) {
					Lexeme newLexeme = new Lexeme(context.getBuffOffset(), cursorPosition, 2, Lexeme.Type.TYPE_CJK_SUR_NAME);
					context.addLexeme(newLexeme);
				}
			}
		}
	}

	/**
	 * 处理未知词段
	 * uBegin : unknown区域的起始位置
	 * uEnd : unknown区域的结束位置
	 */
	private void processUnknown(char[] segmentBuff, Context context, int uBegin, int uEnd) {
		Lexeme newLexeme;
		// 以单字输出未知词段
		for (int i = uBegin; i <= uEnd; i++) {
			newLexeme = new Lexeme(context.getBuffOffset(), i, 1, Lexeme.Type.TYPE_CJK_UNKNOWN);
			context.addLexeme(newLexeme);
		}

		Hit hit = Dictionary.matchInPrepDict(segmentBuff, uEnd, 1);
		if (hit.isUnMatch()) { // 不是副词或介词
			int length = 1;
			while (uEnd < context.getAvailable() - length) { // 处理后缀词
				hit = Dictionary.matchInSuffixDict(segmentBuff, uEnd + 1, length);
				if (hit.isMatch()) {
					// 输出后缀
					newLexeme = new Lexeme(context.getBuffOffset(), uEnd + 1, length, Lexeme.Type.TYPE_CJK_SF);
					context.addLexeme(newLexeme);
					break;
				}
				if (hit.isUnMatch()) {
					break;
				}
				length++;
			}
		}
	}

	public void reset() {
		// 重置已处理标识
		doneIndex = -1;
		hitList.clear();
	}
}
