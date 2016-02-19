package org.wltea.analyzer;

import java.util.HashMap;

/**
 * IK Analyzer v3.2
 * 语义单元（词元） *
 *
 * @author 林良益
 */
public final class Lexeme implements Comparable<Lexeme> {

	// type 一旦固定下来就不能改动只能新增
	public enum Type {
		TYPE_CJK_NORMAL, // 普通词元
		TYPE_CJK_SUR_NAME, // 姓氏
		TYPE_CJK_FULL_NAME, // 全名
		TYPE_CJK_SF, // 尾缀
		TYPE_CJK_UNKNOWN,  // 未知的
		TYPE_NUM, // 数词
		TYPE_NUMCOUNT, // 量词
		TYPE_LETTER, // 英文
		TYPE_BOOK_TITLE, // 书名
		TYPE_EMAIL, // 邮件地址
		TYPE_URL; // URL
		private static HashMap<Integer, Type> map = new HashMap<>();
		static {
			for (Type t : Type.values()) {
				map.put(t.ordinal(), t);
			}
		}
		public static Type of(int i) {
			return map.get(i);
		}
	}

	//词元的起始位移
	private int offset;
	//词元的相对起始位置
	private int begin;
	//词元的长度
	private int length;
	//词元文本
	private String lexemeText;
	//词元类型
	private Type type;

	//当前词元的前一个词元
	private Lexeme prev;
	//当前词元的后一个词元
	private Lexeme next;

	public Lexeme(int offset, int begin, int length, Type type) {
		this.offset = offset;
		this.begin = begin;
		if (length < 0) {
			throw new IllegalArgumentException("length < 0");
		}
		this.length = length;
		this.type = type;
	}

	/*
	 * 判断词元相等算法
	 * 起始位置偏移、起始位置、终止位置相同
	 * @see java.lang.Object#equals(Object o)
	 */
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		if (this == o) {
			return true;
		}

		if (o instanceof Lexeme) {
			Lexeme other = (Lexeme) o;
			return this.offset == other.getOffset()
					&& this.begin == other.getBegin()
					&& this.length == other.getLength();
		} else {
			return false;
		}
	}

	/*
	 * 词元哈希编码算法
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int absBegin = getBeginPosition();
		int absEnd = getEndPosition();
		return (absBegin * 37) + (absEnd * 31) + ((absBegin * absEnd) % getLength()) * 11;
	}

	/*
	 * 词元在排序集合中的比较算法
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Lexeme other) {
		if (other == null) {
			throw new IllegalArgumentException("other should not be null");
		}
		//起始位置优先
		if (this.begin < other.getBegin()) {
			return -1;
		} else if (this.begin == other.getBegin()) {
			//词元长度优先
			if (this.length > other.getLength()) {
				return -1;
			} else if (this.length == other.getLength()) {
				return 0;
			} else {
				return 1;
			}

		} else {
			return 1;
		}
	}

	/**
	 * 判断词元是否彼此包含
	 *
	 * @param other
	 * @return boolean true 完全包含 ， false 可能不相交 或者 相交但不包含
	 */
	public boolean isOverlap(Lexeme other) {
		return other != null
				&& (this.getBeginPosition() <= other.getBeginPosition() && this.getEndPosition() >= other.getEndPosition()
				|| this.getBeginPosition() >= other.getBeginPosition() && this.getEndPosition() <= other.getEndPosition());
	}

	/**
	 * <pre>
	 *     分词结果，相同长度的普通词和书名，选择留下书名，放弃普通词
	 * </pre>
	 * <pre>
	 *     目前只有书名用到,简单处理
	 * </pre>
	 */
	public boolean isImportantThan(Lexeme other) {
		return other != null && other.getType() != Type.TYPE_BOOK_TITLE;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getBegin() {
		return begin;
	}

	/**
	 * 获取词元在文本中的起始位置
	 */
	public int getBeginPosition() {
		return offset + begin;
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}

	/**
	 * 获取词元在文本中的结束位置
	 */
	public int getEndPosition() {
		return offset + begin + length;
	}

	/**
	 * 获取词元的字符长度
	 *
	 * @return int
	 */
	public int getLength() {
		return this.length;
	}

	public void setLength(int length) {
		if (this.length < 0) {
			throw new IllegalArgumentException("length < 0");
		}
		this.length = length;
	}

	/**
	 * 获取词元的文本内容
	 *
	 * @return String
	 */
	public String getLexemeText() {
		if (lexemeText == null) {
			return "";
		}
		return lexemeText;
	}

	public void setLexemeText(String lexemeText) {
		if (lexemeText == null) {
			this.lexemeText = "";
			this.length = 0;
		} else {
			this.lexemeText = lexemeText;
			this.length = lexemeText.length();
		}
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String toString() {
		StringBuilder strbuf = new StringBuilder();
		strbuf.append(this.getBeginPosition()).append("-").append(this.getEndPosition());
		strbuf.append(" : ").append(this.lexemeText).append(" : \t");
		switch (type) {
			case TYPE_CJK_NORMAL:
				strbuf.append("CJK_NORMAL");
				break;
			case TYPE_CJK_SF:
				strbuf.append("CJK_SUFFIX");
				break;
			case TYPE_CJK_SUR_NAME:
				strbuf.append("CJK_NAME");
				break;
			case TYPE_CJK_UNKNOWN:
				strbuf.append("UNKNOWN");
				break;
			case TYPE_NUM:
				strbuf.append("NUMBER");
				break;
			case TYPE_NUMCOUNT:
				strbuf.append("COUNT");
				break;
			case TYPE_LETTER:
				strbuf.append("LETTER");
				break;
			case TYPE_CJK_FULL_NAME:
				strbuf.append("CJK_FULL_NAME");
				break;
			case TYPE_BOOK_TITLE:
				strbuf.append("CJK_BOOK_TITLE");
				break;
			case TYPE_URL:
				strbuf.append("URL");
				break;
			case TYPE_EMAIL:
				strbuf.append("EMAIL");
				break;
			default:
				throw new IllegalStateException("invalid type:" + type);
		}
		return strbuf.toString();
	}

	Lexeme getPrev() {
		return prev;
	}

	void setPrev(Lexeme prev) {
		this.prev = prev;
	}

	Lexeme getNext() {
		return next;
	}

	void setNext(Lexeme next) {
		this.next = next;
	}
}
