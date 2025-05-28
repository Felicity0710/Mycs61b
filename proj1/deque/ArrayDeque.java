package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T> {

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

    @Override
    public void addFirst(T t) {
        extend();
        array[pre--] = t;
        pre = (pre + array.length) % array.length;
        size++;
    }

    @Override
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

    @Override
    public void addLast(T t) {
        extend();
        array[last++] = t;
        last %= array.length;
        size++;
    }

    @Override
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

    @Override
    public T get(int idx) {
        return array[(pre + 1 + idx) % array.length];
    }

    @Override
    public void printDeque() {
        int start = (pre + 1) % array.length;
        int end = (last - 1 + array.length) % array.length;
        for (int i = start; i != end; i = (i + 1) % array.length) {
            System.out.print(array[i] + " ");
        }
        System.out.print(array[end]);
    }

    @Override
    public int size() {
        return size;
    }

    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
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
            for (int i = 0; i < size(); i++) {
                if (get(i) != tmp.get(i)) {
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
