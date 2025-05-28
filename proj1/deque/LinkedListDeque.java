package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T> {

    private class Node {
        private T item;
        private Node pre;
        private Node next;

        Node(T val, Node pref, Node nxt) {
            this.item = val;
            this.pre = pref;
            this.next = nxt;
        }

        public Node(Node pref, Node nxt) {
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

    @Override
    public void addFirst(T t) {
        Node tmp = new Node(t, preSentinel, preSentinel.next);
        preSentinel.next.pre = tmp;
        preSentinel.next = tmp;
        size++;
    }

    @Override
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

    @Override
    public void addLast(T t) {
        Node tmp = new Node(t, lastSentinel.pre, lastSentinel);
        lastSentinel.pre.next = tmp;
        lastSentinel.pre = tmp;
        size++;
    }

    @Override
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

    @Override
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

    @Override
    public void printDeque() {
        Node now = preSentinel.next;
        while (now.next != null) {
            System.out.print(now.item + " ");
            now = now.next;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    @Override
    public boolean equals(Object t) {
        if (t == null) {
            return false;
        } else if (t instanceof ArrayDeque) {
            ArrayDeque<T> tmp = (ArrayDeque<T>) t;
            if (tmp.size() != size()) {
                return false;
            }
            if (isEmpty()) {
                return true;
            }
            T element1 = get(0), element2 = tmp.get(0);
            if (!(element1 != null && element2 != null)) {
                return false;
            }
            Iterator<T> it = iterator();
            for (int i = 0; i < size(); i++) {
                if (it.next() != tmp.get(i)) {
                    return false;
                }
            }
            return true;
        } else if (t instanceof LinkedListDeque) {
            LinkedListDeque<T> tmp = (LinkedListDeque<T>) t;
            if (tmp.size() != size()) {
                return false;
            }
            if (isEmpty()) {
                return true;
            }
            T element1 = get(0), element2 = tmp.get(0);
            if (!(element1 != null && element2 != null)) {
                return false;
            }
            Iterator<T> it1 = iterator(), it2 = tmp.iterator();
            while (it1.hasNext()) {
                if (it1.next() != it2.next()) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
