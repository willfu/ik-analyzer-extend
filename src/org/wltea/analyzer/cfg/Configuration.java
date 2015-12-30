package org.wltea.analyzer.cfg;

import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.seg.BookTitleSegmenter;
import org.wltea.analyzer.seg.CJKSegmenter;
import org.wltea.analyzer.seg.ISegmenter;
import org.wltea.analyzer.seg.LetterSegmenter;
import org.wltea.analyzer.seg.QuantifierSegmenter;

import java.util.ArrayList;
import java.util.List;

/**
 * IK Analyzer v3.2
 * 简单的配置管理类,单子模式
 *
 * @author 林良益
 */
public class Configuration {

	private Configuration() {
	}

	/**
	 * 初始化子分词器实现
	 * （目前暂时不考虑配置扩展）
	 *
	 * @return List<ISegmenter>
	 */
	public static List<ISegmenter> loadSegmenter() {
		//初始化词典单例
		Dictionary.getInstance();
		List<ISegmenter> segmenters = new ArrayList<>(4);
		//处理数量词的子分词器
		segmenters.add(new QuantifierSegmenter());
		//处理中文词的子分词器
		segmenters.add(new CJKSegmenter());
		//处理字母的子分词器
		segmenters.add(new LetterSegmenter());
		//书名分词器
		segmenters.add(new BookTitleSegmenter());
		return segmenters;
	}
}
