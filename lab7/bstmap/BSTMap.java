package bstmap;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class BSTMap<Key extends Comparable<Key>, Value extends Comparable<Value>> implements Map61B<Key, Value> {

    private static final boolean RED = true;
    private static final boolean BLACK = false;

    private class node {
        private Key key;
        private Value val;
        private node left, right;
        private boolean color;
        private int size;

        private node(Key k, Value v, boolean c, int s) {
            key = k;
            val = v;
            color = c;
            size = s;
            left = right = null;
        }
    }

    private class BSTMapIterator implements Iterator<Key> {
        int idx;

        BSTMapIterator() {
            idx = 0;
        }

        public boolean hasNext() {
            return idx < size();
        }

        public Key next() {
            node result = getRank(root, idx);
            idx++;
            if (result == null) {
                return null;
            } else {
                return result.key;
            }
        }
    }

    private node root;

    public BSTMap() {
        root = null;
    }

    private int size(node u) {
        if (u == null) {
            return 0;
        } else {
            return u.size;
        }
    }

    public int size() {
        return size(root);
    }

    private node get(node u, Key k) {
        if (u == null) {
            return null;
        }
        int cmp = k.compareTo(u.key);
        if (cmp < 0) {
            return get(u.left, k);
        } else if (cmp > 0) {
            return get(u.right, k);
        } else {
            return u;
        }
    }

    public Value get(Key k) {
        node result = get(root, k);
        if (result == null) {
            return null;
        } else {
            return result.val;
        }
    }

    public boolean containsKey(Key k) {
        node result = get(root, k);
        return result != null;
    }

    public void clear() {
        root = null;
    }

    private boolean isRed(node u) {
        if (u == null) {
            return false;
        }
        return u.color == RED;
    }

    private node rotateLeft(node u) {
        node r = u.right;
        u.right = r.left;
        r.left = u;
        r.size = u.size;
        r.color = u.color;
        u.color = RED;
        u.size = size(u.left) + size(u.right) + 1;
        return r;
    }

    private node rotateRight(node u) {
        node l = u.left;
        u.left = l.right;
        l.right = u;
        l.size = u.size;
        l.color = u.color;
        u.color = RED;
        u.size = size(u.left) + size(u.right) + 1;
        return l;
    }

    private void flipColor(node u) {
        u.color = !u.color;
        u.left.color = !u.left.color;
        u.right.color = !u.right.color;
    }

    private node balance(node u) {
        if (isRed(u.right) && !isRed(u.left)) {
            u = rotateLeft(u);
        }
        if (isRed(u.left) && isRed(u.left.left)) {
            u = rotateRight(u);
        }
        if (isRed(u.left) && isRed(u.right)) {
            flipColor(u);
        }
        u.size = size(u.left) + size(u.right) + 1;
        return u;
    }

    private node insert(node u, Key k, Value v) {
        if (u == null) {
            return new node(k, v, RED, 1);
        }
        int cmp = k.compareTo(u.key);
        if (cmp < 0) {
            u.left = insert(u.left, k, v);
        } else if (cmp > 0) {
            u.right = insert(u.right, k, v);
        } else {
            u.val = v;
        }
        return balance(u);
    }

    public void put(Key k, Value v) {
        root = insert(root, k, v);
    }

    private node moveRedLeft(node u) {
        flipColor(u);
        if (isRed(u.right.left)) {
            u.right = rotateRight(u.right);
            u = rotateLeft(u);
            flipColor(u);
        }
        return u;
    }

    private node moveRedRight(node u) {
        flipColor(u);
        if (isRed(u.left.left)) {
            u = rotateRight(u);
            flipColor(u);
        }
        return u;
    }

    private node min(node u) {
        if (u.left == null) {
            return u;
        } else {
            return min(u.left);
        }
    }

    private node deleteMin(node u) {
        if (u.left == null) {
            return null;
        }
        if (!isRed(u.left) && !isRed(u.left.left)) {
            u = moveRedLeft(u);
        }
        u.left = deleteMin(u.left);
        return balance(u);
    }

    private node delete(node u, Key k) {
        if (k.compareTo(u.key) < 0) {
            if (!isRed(u.left) && !isRed(u.left.left)) {
                u = moveRedLeft(u);
            }
            u.left = delete(u.left, k);
        } else {
            if (isRed(u.left)) {
                u = rotateRight(u);
            }
            if (k.compareTo(u.key) == 0 && u.right == null) {
                return null;
            }
            if (!isRed(u.right) && !isRed(u.right.left)) {
                u = moveRedRight(u);
            }
            if (k.compareTo(u.key) == 0) {
                node tmp = min(u.right);
                u.key = tmp.key;
                u.val = tmp.val;
                u.right = deleteMin(u.right);
            } else {
                u.right = delete(u.right, k);
            }
        }
        return balance(u);
    }

    public Value remove(Key k) {
        node result = get(root, k);
        if (result == null) {
            return null;
        } else {
            Value ret = result.val;
            root = delete(root, k);
            return ret;
        }
    }

    public Value remove(Key k, Value v) {
        node result = get(root, k);
        if (result == null || result.val.compareTo(v) != 0) {
            return null;
        } else {
            root = delete(root, k);
            return v;
        }
    }

    private node getRank(node u, int r) {
        if (r < 0 || r >= size(u)) {
            return null;
        }
        int sizeLeft = size(u.left);
        if (r < sizeLeft) {
            return getRank(u.left, r);
        } else if (r == sizeLeft) {
            return u;
        } else {
            return getRank(u.right, r - sizeLeft - 1);
        }
    }

    public Iterator<Key> iterator() {
        return new BSTMapIterator();
    }

    private void keySet(Set<Key> s, node u) {
        if (u == null) {
            return;
        }
        s.add(u.key);
        keySet(s, u.left);
        keySet(s, u.right);
    }

    public Set<Key> keySet() {
        Set<Key> s = new TreeSet<>();
        keySet(s, root);
        return s;
    }
}
