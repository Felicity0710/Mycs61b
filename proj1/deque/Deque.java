package deque;

public interface Deque<T> {
    void addFirst(T t);
    T removeFirst();
    void addLast(T t);
    T removeLast();
    default boolean isEmpty() {
        return size() == 0;
    }
    int size();
    T get(int idx);
    void printDeque();
}
