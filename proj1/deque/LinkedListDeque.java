package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Iterable<T>, Deque<T> {

    private class Node {
        private T item;
        private Node pre;
        private Node next;

        Node(T val, Node pref, Node nxt) {
            this.item = val;
            this.pre = pref;
            this.next = nxt;
        }

        Node(Node pref, Node nxt) {
            this.pre = pref;
            this.next = nxt;
        }
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private Node now;

        LinkedListDequeIterator() {
            now = preSentinel;
        }

        @Override
        public boolean hasNext() {
            return now.next != lastSentinel;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                return null;
            }
            now = now.next;
            return now.item;
        }
    }

    private Node preSentinel;
    private Node lastSentinel;
    private int size;

    public LinkedListDeque() {
        preSentinel = new Node(null, null);
        lastSentinel = new Node(null, null);
        preSentinel.next = lastSentinel;
        lastSentinel.pre = preSentinel;
        size = 0;
    }

    public void addFirst(T t) {
        Node tmp = new Node(t, preSentinel, preSentinel.next);
        preSentinel.next.pre = tmp;
        preSentinel.next = tmp;
        size++;
    }

    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        Node tmp = preSentinel.next;
        preSentinel.next = tmp.next;
        tmp.next.pre = preSentinel;
        size--;
        return tmp.item;
    }

    public void addLast(T t) {
        Node tmp = new Node(t, lastSentinel.pre, lastSentinel);
        lastSentinel.pre.next = tmp;
        lastSentinel.pre = tmp;
        size++;
    }

    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        Node tmp = lastSentinel.pre;
        lastSentinel.pre = tmp.pre;
        tmp.pre.next = lastSentinel;
        size--;
        return tmp.item;
    }

    public T get(int idx) {
        if (idx >= size || idx < 0) {
            return null;
        }
        Node now = preSentinel;
        while (idx >= 0) {
            now = now.next;
            idx--;
        }
        return now.item;
    }

    private T getRecursiveHelper(Node now, int idx) {
        if (now.next == null) {
            return null;
        }
        if (idx == 0) {
            return now.item;
        }
        return getRecursiveHelper(now.next, idx - 1);
    }

    public T getRecursive(int idx) {
        if (idx < 0) {
            return null;
        }
        return getRecursiveHelper(preSentinel.next, idx);
    }

    public void printDeque() {
        Node now = preSentinel.next;
        while (now.next != null) {
            System.out.print(now.item + " ");
            now = now.next;
        }
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    @Override
    public boolean equals(Object t) {
        if (!(t instanceof ArrayDeque || t instanceof LinkedListDeque)) {
            return false;
        } else {
            Deque<?> tmp;
            if (t instanceof ArrayDeque) {
                tmp = (ArrayDeque<?>) t;
            } else {
                tmp = (LinkedListDeque<?>) t;
            }
            if (size() != tmp.size()) {
                return false;
            }
            if (isEmpty()) {
                return true;
            }
            Iterator<T> it1 = iterator();
            Iterator<?> it2 = tmp.iterator();
            while (it1.hasNext()) {
                if (!it1.next().equals(it2.next())) {
                    return false;
                }
            }
            return true;
        }
    }
}
