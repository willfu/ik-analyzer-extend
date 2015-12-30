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
		char inputChar = segmentBuffer[context.getCursor()];

		if (CharacterHelper.isCJKCharacter(inputChar)) { // 是（CJK）字符，则进行处理
			if (hitList.size() > 0) {
				// 处理词段队列
				Hit[] tmpArray = hitList.toArray(new Hit[hitList.size()]);
				for (Hit hit : tmpArray) {
					hit = Dictionary.matchWithHit(segmentBuffer, context.getCursor(), hit);

					if (hit.isMatch()) { // 匹配成词
						// 判断是否有不可识别的词段
						if (hit.getBegin() > doneIndex + 1) {
							// 输出并处理从doneIndex+1 到 seg.start - 1之间的未知词段
							processUnknown(segmentBuffer, context, doneIndex + 1, hit.getBegin() - 1);
						}
						// 输出当前的词
						Lexeme newLexeme = new Lexeme(context.getBuffOffset(), hit.getBegin(),
								context.getCursor() - hit.getBegin() + 1, Lexeme.Type.TYPE_CJK_NORMAL);
						context.addLexeme(newLexeme);
						// 更新goneIndex，标识已处理
						if (doneIndex < context.getCursor()) {
							doneIndex = context.getCursor();
						}

						if (hit.isPrefix()) { // 同时也是前缀，留在list中，等待下次匹配到更长的词

						} else { //后面不再可能有匹配了
							//移出当前的hit
							hitList.remove(hit);
						}

					} else if (hit.isPrefix()) { // 前缀，未匹配成词

					} else if (hit.isUnMatch()) { // 不匹配
						// 移出当前的hit
						hitList.remove(hit);
					}
				}
			}

			// 处理以input为开始的一个新hit
			Hit hit = Dictionary.matchInMainDict(segmentBuffer, context.getCursor(), 1);
			if (hit.isMatch()) { // 匹配成词
				// 判断是否有不可识别的词段
				if (context.getCursor() > doneIndex + 1) {
					// 输出并处理从doneIndex+1 到 context.getCursor()- 1之间的未知
					processUnknown(segmentBuffer, context, doneIndex + 1, context.getCursor() - 1);
				}
				// 输出当前的词
				Lexeme newLexeme = new Lexeme(context.getBuffOffset(), context.getCursor(), 1, Lexeme.Type.TYPE_CJK_NORMAL);
				context.addLexeme(newLexeme);
				// 更新doneIndex，标识已处理
				if (doneIndex < context.getCursor()) {
					doneIndex = context.getCursor();
				}

				if (hit.isPrefix()) { // 同时也是前缀
					hitList.add(hit);
				}

			} else if (hit.isPrefix()) { // 前缀，未匹配成词
				hitList.add(hit);

			} else if (hit.isUnMatch()) { // 不匹配，当前的input不是词，也不是词前缀，将其视为分割性的字符
				if (doneIndex >= context.getCursor()) {
					// 当前不匹配的字符已经被处理过了，不需要再processUnknown
					return;
				} else {
					// 输出从doneIndex到当前字符（含当前字符）之间的未知词
					processUnknown(segmentBuffer, context, doneIndex + 1, context.getCursor());
					//更新doneIndex，标识已处理
					doneIndex = context.getCursor();
				}
			}
		} else { // 输入的不是中文(CJK)字符
			if (hitList.size() > 0 && doneIndex < context.getCursor() - 1) {
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
			if (doneIndex < context.getCursor()) {
				doneIndex = context.getCursor();
			}
		}

		// 缓冲区结束临界处理
		if (context.getCursor() == context.getAvailable() - 1) { // 读取缓冲区结束的最后一个字符
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

	/**
	 * 处理未知词段
	 */
	private void processUnknown(char[] segmentBuff, Context context, int uBegin, int uEnd) {
		Lexeme newLexeme;

		Hit hit = Dictionary.matchInPrepDict(segmentBuff, uBegin, 1);
		if (hit.isUnMatch()) { // 不是副词或介词
			if (uBegin >= 0) { // 处理姓氏
				int index = uBegin;
				while (index < uEnd) {
					hit = Dictionary.matchInSurnameDict(segmentBuff, index, 1);
					boolean match = false;
					if (hit.isMatch()) {
						// 输出姓氏
						newLexeme = new Lexeme(context.getBuffOffset(), index, 1, Lexeme.Type.TYPE_CJK_SUR_NAME);
						context.addLexeme(newLexeme);
					} else if (hit.isPrefix()) {
						// 处理多字姓氏。目前只支持双字
						if (index + 1 < segmentBuff.length) {
							Hit nameHit = hit.getMatchedDictSegment().match(segmentBuff, index + 1, 1);
							if (nameHit.isMatch()) {
								newLexeme = new Lexeme(context.getBuffOffset(), index, 2, Lexeme.Type.TYPE_CJK_SUR_NAME);
								context.addLexeme(newLexeme);
								match = true;
							}
						}
					}
					if (match) {
						index += 2; // match复姓
					} else {
						index ++; // match单姓或者没有match
					}
				}
			}
		}

		// 以单字输出未知词段
		for (int i = uBegin; i <= uEnd; i++) {
			newLexeme = new Lexeme(context.getBuffOffset(), i, 1, Lexeme.Type.TYPE_CJK_UNKNOWN);
			context.addLexeme(newLexeme);
		}

		hit = Dictionary.matchInPrepDict(segmentBuff, uEnd, 1);
		if (hit.isUnMatch()) {//不是副词或介词
			int length = 1;
			while (uEnd < context.getAvailable() - length) { // 处理后缀词
				hit = Dictionary.matchInSuffixDict(segmentBuff, uEnd + 1, length);
				if (hit.isMatch()) {
					//输出后缀
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
		//重置已处理标识
		doneIndex = -1;
		hitList.clear();
	}
}
