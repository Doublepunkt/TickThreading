package me.nallar.collections;

import me.nallar.tickthreading.Log;

public class SynchronizedList<T> extends ConcurrentUnsafeIterableArrayList<T> {
	@Override
	public synchronized boolean add(final T t) {
		if (t == null) {
			Log.severe("Tried to add null to SynchronizedList", new Throwable());
		}
		return super.add(t);
	}

	@Override
	public synchronized T remove(final int index) {
		return super.remove(index);
	}

	@Override
	public synchronized boolean remove(final Object o) {
		return super.remove(o);
	}

	@Override
	public synchronized <T1> T1[] toArray(final T1[] a) {
		return super.toArray(a);
	}

	@Override
	public synchronized Object[] toArray() {
		return super.toArray();
	}
}