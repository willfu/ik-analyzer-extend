package test;

import org.wltea.analyzer.IKSegmentation;
import org.wltea.analyzer.Lexeme;

import java.io.IOException;
import java.io.StringReader;

/**
 * Created by masonqwli on 15/12/30.
 */
public class Tests {
	public static void main(String[] args) {

		// ** 用于测试书名
		// String str = "论语《论语》论语是一部珍贵的历史遗产";
//		 String str = "论语《资治通鉴》论语是一部珍贵的历史遗产";
		String str = "通过ftp://bd.com学习english";

		// ** 用于测试人名 / 英文 / 书名分词
//		String str = "徐增寿、潘天寿合著的《我是传奇》，确确实实卖成了legend，值得我们第5小组学习，请发总结到我的邮箱naughty610@qq.com，thanks";

		IKSegmentation ik = new IKSegmentation(
				new StringReader(str), true);
		try {
			Lexeme lm;
			while ((lm = ik.next()) != null) {
				System.err.println("segment lm:" + lm.getLexemeText() + ", type:" + lm.getType());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
