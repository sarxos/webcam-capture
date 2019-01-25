package com.github.sarxos.webcam.ds.raspistill;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Queue;

/**
 * ClassName: CircularListCache , cache bases on circular list, it is array
 * implementation, FIFO <br/>
 * <font color="red"> Please be noted, remove is extremely low inefficient.
 * Typical usage of this API is creating one fixed length cache, and keep adding
 * element, and then cache will auto remove oldest one. </font> date: Jan 23,
 * 2019 9:00:03 AM <br/>
 * 
 * @author maoanapex88@163.com (alexmao86)
 */
public class CircularListCache<T> extends AbstractList<T> implements Queue<T> {
	private transient Object[] elementData;
	private int size = 0;
	private final int capacity;

	private int headPointer = 0;

	public CircularListCache(int capacity) {
		super();
		this.capacity = capacity;
		elementData = new Object[capacity];
	}

	public int getCapacity() {
		return capacity;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get(int index) {
		if (index > size - 1)
			throw new IndexOutOfBoundsException();
		if (size < capacity) {
			return (T) elementData[index];
		} else {// capacity is full
			return (T) elementData[(index + headPointer) % capacity];
		}
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void clear() {
		for (int i = 0; i < elementData.length; i++) {
			elementData[i] = null;
		}
		headPointer = 0;
		size = 0;
	}

	public boolean add(T e) {// add at tail
		if (size < capacity) {
			elementData[headPointer] = e;
			headPointer++;
			headPointer %= capacity;
			size++;
			return true;
		}

		elementData[headPointer] = e;// replace
		// reset headPointer
		headPointer = (headPointer + 1) % capacity;

		return true;
	}

	@Override
	public void add(int index, T element) {
		if (index > size - 1)
			throw new IndexOutOfBoundsException();
		if (size < capacity) {
			System.arraycopy(elementData, index, elementData, index + 1, size - index);
			elementData[index] = element;
			size++;
			headPointer++;
			headPointer %= capacity;
		} else {
			for (int i = index + 1; i < capacity; i++) {
				elementData[(i + headPointer) % capacity] = elementData[(i - 1 + headPointer) % capacity];
			}
			// reset headPointer
			headPointer = (headPointer + 1) % capacity;// point to next
		}
	}

	public T set(int index, T element) {
		if (index > size - 1)
			throw new IndexOutOfBoundsException();

		int realIndex = index;

		if (size >= capacity) {
			realIndex = (index + headPointer) % capacity;
		}
		elementData[realIndex] = element;

		return element;
	}

	@SuppressWarnings("unchecked")
	public T remove(int index) {
		if (index > size - 1)
			throw new IndexOutOfBoundsException();
		if (size < capacity) {
			System.arraycopy(elementData, index + 1, elementData, index, size - index);
			size--;
			headPointer--;
			return (T) elementData[index];
		} else {
			T target = (T) elementData[(index + headPointer) % capacity];
			// order it
			for (int i = 0; i < headPointer; i++) {
				Object tmp = elementData[0];
				for (int j = 1; j < capacity; j++) {
					elementData[j - 1] = elementData[j];
				}
				elementData[capacity - 1] = tmp;
			}

			System.arraycopy(elementData, index + 1, elementData, index, size - index - 1);

			size--;
			headPointer--;

			return target;
		}
	}

	@Override
	public String toString() {
		return Arrays.toString(elementData);
	}

	@Override
	public boolean offer(T e) {
		return this.add(e);
	}

	@Override
	public T remove() {
		return this.remove(this.size - 1);
	}

	@Override
	public T poll() {
		return this.remove(0);
	}

	@Override
	public T element() {
		return this.get(0);
	}

	@Override
	public T peek() {
		return this.get(0);
	}
}
