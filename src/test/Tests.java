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

		IKSegmentation ik = new IKSegmentation(
				new StringReader("徐增寿、潘天寿合著的《我是传奇》，确确实实卖成了legend，值得我们第5小组学习，请发总结到我的邮箱naughty610@qq.com，thanks"), true);
		try {
			Lexeme lm;
			while ((lm = ik.next()) != null) {
				System.err.println("segment lm:" + lm.getLexemeText());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
