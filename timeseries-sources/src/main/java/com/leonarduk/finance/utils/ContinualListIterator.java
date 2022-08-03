package com.leonarduk.finance.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ContinualListIterator<T> implements Iterator<T> {

    private final LinkedList<T> underlyingCollection;

    public ContinualListIterator(List<T> list) {
        this.underlyingCollection = new LinkedList<>(list);
    }

    @Override
    public boolean hasNext() {
        return !this.underlyingCollection.isEmpty();
    }

    @Override
    public T next() {
        T first = this.underlyingCollection.removeFirst();
        this.underlyingCollection.addLast(first);
        return first;
    }

}
