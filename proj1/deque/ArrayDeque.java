package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Iterable<T>, Deque<T> {

    private class ArrayDequeIterator implements Iterator<T> {
        private int now;

        ArrayDequeIterator() {
            now = (pre + 1) % array.length;
        }

        @Override
        public boolean hasNext() {
            return now != last;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                return null;
            }
            T res = array[now];
            now = (now + 1) % array.length;
            return res;
        }

    }

    private T[] array;
    private int pre;
    private int last;
    private int size;

    public ArrayDeque() {
        array = (T[]) new Object[8];
        pre = size = 0;
        last = 1;
    }

    private void resize(int s) {
        T[] tmp = (T[]) new Object[s];
        pre = (pre + 1) % array.length;
        last = (last - 1 + array.length) % array.length;
        if (pre < last) {
            System.arraycopy(array, pre, tmp, 1, last - pre + 1);
            last = last - pre + 2;
        } else {
            System.arraycopy(array, pre, tmp, 1, array.length - pre);
            System.arraycopy(array, 0, tmp, 1 + array.length - pre, last + 1);
        }
        pre = 0;
        last = size + 1;
        array = tmp;
    }

    private void extend() {
        if (size == array.length) {
            resize((int) (array.length * 1.2) + 1);
        }
    }

    private void shrink() {
        if (array.length >= 16 && ((double) size / array.length) < 0.25) {
            resize(array.length / 2);
        }
    }

    public void addFirst(T t) {
        extend();
        array[pre--] = t;
        pre = (pre + array.length) % array.length;
        size++;
    }

    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        pre = (pre + 1) % array.length;
        size--;
        T res = array[pre];
        shrink();
        return res;
    }

    public void addLast(T t) {
        extend();
        array[last++] = t;
        last %= array.length;
        size++;
    }

    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        last = (last - 1 + array.length) % array.length;
        size--;
        T res = array[last];
        shrink();
        return res;
    }

    public T get(int idx) {
        return array[(pre + 1 + idx) % array.length];
    }

    public void printDeque() {
        int start = (pre + 1) % array.length;
        int end = (last - 1 + array.length) % array.length;
        for (int i = start; i != end; i = (i + 1) % array.length) {
            System.out.print(array[i] + " ");
        }
        System.out.print(array[end]);
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
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
