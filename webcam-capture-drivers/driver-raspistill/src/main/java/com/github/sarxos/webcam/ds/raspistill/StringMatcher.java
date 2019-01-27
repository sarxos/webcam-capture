package com.github.sarxos.webcam.ds.raspistill;

/**
 * fast circular queue used for string match
 * @author maoanapex88@163.com (alexmao86)
 *
 */
class StringMatcher {
	char[] cache;
	int start;
	String target;

	public StringMatcher(String s) {
		this.target = s;
		int length = s.length();
		cache = new char[length];
		start = 0;
		for (int i = 0; i < cache.length; i++)
			cache[i] = 128;
	}

	public void push(char c) {
		cache[start++] = c;
		start %= cache.length;
	}

	public boolean isTarget() {
		if (cache[(start + 1) % cache.length] == 128)
			return false;
		int index = 0;
		for (int i = start; i < cache.length; i++, index++)
			if (cache[i] != target.charAt(index))
				return false;
		for (int i = 0; i < start; i++, index++)
			if (cache[i] != target.charAt(index))
				return false;
		return true;
	}

	public boolean isTarget(boolean ignoreCase) {
		if (cache[(start + 1) % cache.length] == 128)
			return false;
		int index = 0;
		for (int i = start; i < cache.length; i++, index++) {
			if (ignoreCase && cache[i] != target.charAt(index)
					&& cache[i] - target.charAt(index) != 32
					&& cache[i] - target.charAt(index) != -32)
				return false;
		}
		for (int i = 0; i < start; i++, index++) {
			if (ignoreCase && cache[i] != target.charAt(index)
					&& cache[i] - target.charAt(index) != 32
					&& cache[i] - target.charAt(index) != -32)
				return false;
		}
		return true;
	}

	/**
	 * clear
	 */
	public void clear() {
		start = 0;
		for (int i = 0; i < cache.length; i++)
			cache[i] = 128;
	}

	/**
	 * @return
	 */
	public int length() {
		return target.length();
	}
}