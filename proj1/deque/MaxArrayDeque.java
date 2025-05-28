package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> compar;

    public MaxArrayDeque(Comparator<T> c) {
        compar = c;
    }

    public T max() {
        return max(compar);
    }

    public T max(Comparator<T> c) {
        if (super.isEmpty()) {
            return null;
        }
        T res = super.get(0);
        for (int i = 1; i < super.size(); i++) {
            T tmp = super.get(i);
            if (c.compare(tmp, res) > 0) {
                res = tmp;
            }
        }
        return res;
    }

}
