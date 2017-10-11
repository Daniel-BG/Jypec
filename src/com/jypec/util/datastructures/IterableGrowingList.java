package com.jypec.util.datastructures;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * List which iterator supports the addition of new elements on the fly.
 * Most methods unimplemented
 * @author Daniel
 *
 * @param <T>
 */
public class IterableGrowingList<T> implements List<T> {

	private int size;
	private Node<T> head;
	private Node<T> tail;
	
	
	/**
	 * Create a growing iterable list
	 */
	public IterableGrowingList() {
		this.size = 0;
		this.head = null;
		this.tail = null;
	}
	
	@Override
	public int size() {
		return this.size;
	}

	@Override
	public boolean isEmpty() {
		return this.size != 0;
	}

	@Override
	public boolean contains(Object o) {
		for (T t: this) {
			if (t.equals(o)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<T> iterator() {
		return new IGLIterator<>(head);
	}

	@Override
	public Object[] toArray() {
		Object[] array = new Object[this.size];
		int index = 0;
		for (T t: this) {
			array[index] = t;
			index++;
		}
		return array;
	}

	@Override
	public boolean add(T e) {
		Node<T> n = new Node<T>(e);
		if (this.size == 0) {
			this.head = n;
			this.tail = this.head;
		} else {
			n.previous = this.tail;
			this.tail.next = n;
			this.tail = n;
		}
		this.size++;
		return true;
	}

	@Override
	public boolean remove(Object o) {
		Iterator<T> it = this.iterator();
		while (it.hasNext()) {
			T t = it.next();
			if (t.equals(o)) {
				it.remove();
				this.size--;
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void clear() {
		this.head = null;
		this.tail = null;
		this.size = 0;
	}
	
	@Override
	public ListIterator<T> listIterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T2> T2[] toArray(T2[] a) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public ListIterator<T> listIterator(int index) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T get(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T set(int index, T element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, T element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int lastIndexOf(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	
	private class Node<T2> {
		public Node<T2> previous;
		public Node<T2> next;
		public T2 content;
		
		public Node(T2 content) {
			this.content = content;
		}
	}
	
	
	private class IGLIterator<T2> implements Iterator<T2> {
		protected Node<T2> pointer;
		protected Node<T2> previous;
		
		protected IGLIterator(Node<T2> pointer) {
			this.pointer = pointer;
			this.previous = null;
		}

		@Override
		public boolean hasNext() {
			if (this.pointer != null) {
				return true;
			} else {
				if (this.previous != null && this.previous.next != null) {
					this.pointer = this.previous.next;
					return true;
				}
			}
			return false;
		}

		@Override
		public T2 next() {
			T2 object = this.pointer.content;
			this.previous = this.pointer;
			this.pointer = this.pointer.next;
			return object;
		}
		
		@Override
		public void remove() {
			if (this.pointer == head) {
				head = head.next;
				if (head.next == null) {
					tail = head;
				}
			} else if (this.pointer == tail) {
				tail = tail.previous;
				if (tail.previous == null) {
					head = tail;
				}
			} else {
				this.pointer.previous.next = this.pointer.next;
				this.pointer.next.previous = this.pointer.previous;
			}
		}
		
	}


	

}
