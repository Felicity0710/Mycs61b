package deque;

import java.util.Iterator;

public interface Deque<T> {
    void addFirst(T t);

    T removeFirst();

    void addLast(T t);

    T removeLast();

    boolean isEmpty();

    int size();

    T get(int idx);

    void printDeque();

    Iterator<T> iterator();
}
