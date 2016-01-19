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
			if (this.tail.compareTo(lexeme) < 0) { // 词元接入链表尾部
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
				Lexeme currentTail = this.tail;
				while (currentTail != null && currentTail.compareTo(lexeme) > 0) {
					currentTail = currentTail.getPrev();
				}
				if (currentTail != null) {
					lexeme.setPrev(currentTail);
					lexeme.setNext(currentTail.getNext());
					if (currentTail.getNext() != null) {
						currentTail.getNext().setPrev(lexeme);
					}
					currentTail.setNext(lexeme);
					if (currentTail == this.tail) {
						this.tail = lexeme;
					}
					this.size++;
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
					if (one.isImportantThan(another)) {
						another = another.getNext(); // 删掉another
						one.setNext(another);
						if (another != null) {
							another.setPrev(one);
						}
					} else {
						// 删掉one
						Lexeme previousOne = one.getPrev();
						if (previousOne != null) {
							previousOne.setNext(another);
							another.setPrev(previousOne);
							one = another;
							another = another.getNext();
						} else {
							another.setPrev(null);
							one.setNext(null);
							if (one == this.head) { // 如果要删除的前面的节点刚刚好是head,要把head重新指向新节点
								this.head = another;
							}
							one = another;
							another = another.getNext();
						}
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