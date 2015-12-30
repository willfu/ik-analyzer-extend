package org.wltea.analyzer;

/**
 * Created by masonqwli on 15/12/21.
 */
public class IKSortedLinkSet {
	//链表头
	private Lexeme head;
	//链表尾
	private Lexeme tail;
	//链表的实际大小
	private int size;

	public IKSortedLinkSet() {
		this.size = 0;
	}

	/**
	 * 向链表集合添加词元
	 */
	public void addLexeme(Lexeme lexeme) {
		if (this.size == 0) {
			this.head = lexeme;
			this.tail = lexeme;
			this.size++;
		} else {
			if (this.tail.compareTo(lexeme) == 0) { // 词元与尾部词元相同，不放入集合

			} else if (this.tail.compareTo(lexeme) < 0) { // 词元接入链表尾部
				this.tail.setNext(lexeme);
				lexeme.setPrev(this.tail);
				this.tail = lexeme;
				this.size++;
			} else if (this.head.compareTo(lexeme) > 0) {//词元接入链表头部
				this.head.setPrev(lexeme);
				lexeme.setNext(this.head);
				this.head = lexeme;
				this.size++;
			} else {
				//从尾部上逆
				Lexeme l = this.tail;
				while (l != null && l.compareTo(lexeme) > 0) {
					l = l.getPrev();
				}
				if (l != null) {
					if (l.compareTo(lexeme) == 0) { // 词元与集合中的词元重复，不放入集合，=0表示词元与集合中的词元重复，不放入集合

					} else if (l.compareTo(lexeme) < 0) { // 词元插入链表中的某个位置
						lexeme.setPrev(l);
						lexeme.setNext(l.getNext());
						l.getNext().setPrev(lexeme);
						l.setNext(lexeme);
						this.size++;
					}
				}
			}
		}
	}

	public Lexeme pollFirst() {
		if (this.size == 1) {
			Lexeme first = this.head;
			this.head = null;
			this.tail = null;
			this.size--;
			return first;
		} else if (this.size > 1) {
			Lexeme first = this.head;
			this.head = first.getNext();
			first.setNext(null);
			this.size--;
			return first;
		} else {
			return null;
		}
	}

	public Lexeme peekFirst() {
		return this.head;
	}

	public Lexeme pollLast() {
		if (this.size == 1) {
			Lexeme last = this.head;
			this.head = null;
			this.tail = null;
			this.size--;
			return last;
		} else if (this.size > 1) {
			Lexeme last = this.tail;
			this.tail = last.getPrev();
			last.setPrev(null);
			this.size--;
			return last;
		} else {
			return null;
		}
	}

	/**
	 * 剔除集合汇总相邻的切完全包含的lexeme
	 * 进行最大切分的时候，过滤长度较小的交叠词元
	 */
	public void excludeOverlap() {
		if (this.size > 1) {
			Lexeme one = this.head;
			Lexeme another = one.getNext();
			do {
				if (one.isOverlap(another)) {
					//邻近的两个词元完全交叠
					another = another.getNext();
					//从链表中断开交叠的词元
					one.setNext(another);
					if (another != null) {
						another.setPrev(one);
					}
					this.size--;
				} else {//词元不完全交叠
					one = another;
					another = another.getNext();
				}
			} while (another != null);
		}
	}

	public int size() {
		return this.size;
	}
}