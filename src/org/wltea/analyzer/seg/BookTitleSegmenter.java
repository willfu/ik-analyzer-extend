package org.wltea.analyzer.seg;

import org.wltea.analyzer.Context;
import org.wltea.analyzer.Lexeme;

/**
 * <pre>
 *     书名分词，仅支持双书名号：《》
 * </pre>
 * <pre>
 *     做法：最大匹配，记录第一个遇到的《和最后一个遇到的》
 * </pre>
 * <pre>
 *     简单处理，暂时不处理xxx《xxx《yyy》zzz》这种情况，会被匹配成《xxx《yyy》
 * </pre>
 */
public class BookTitleSegmenter implements ISegmenter {

	private int start = -1;
	private boolean foundStart = false;
	private int foundLength = 0;

	@Override
	public void nextLexeme(char[] segmentBuff, Context context) {
		int current = context.getCursor();
		char c = segmentBuff[current];
		if (foundStart) {
			if (c == '》') {
				Lexeme newLexeme = new Lexeme(context.getBuffOffset(), start, current - start, Lexeme.Type.TYPE_BOOK_TITLE);
				context.addLexeme(newLexeme);
				reset();
			} else {
				foundLength++;
				if (foundLength >= 20) { // 限制书名最大字数
					reset();
				}
			}
		} else {
			if (c == '《') {
				foundStart = true;
				start = current + 1;
				foundLength++;
			}
		}
		if (foundStart) {
			context.lockBuffer(this);
		} else {
			context.unlockBuffer(this);
		}
	}

	@Override
	public void reset() {
		foundStart = false;
		start = -1;
		foundLength = 0;
	}
}
