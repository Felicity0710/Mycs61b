package hashmap;

import java.util.*;

/**
 * A hash table-backed Map implementation. Provides amortized constant time
 * access to elements via get(), remove(), and put() in the best case.
 * <p>
 * Assumes null keys will never be inserted, and does not resize down upon remove().
 *
 * @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }

        @Override
        public boolean equals(Object obj) {
            Node u = (Node) obj;
            return u.key.equals(key) && u.value.equals(value);
        }
    }

    protected class HashMapIterator implements Iterator<K> {
        private Iterator<K> it;

        HashMapIterator() {
            it = keySet().iterator();
        }

        public boolean hasNext() {
            return it.hasNext();
        }

        public K next() {
            return it.next();
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!
    private int initialLength = 16;
    private double loadFactor = 0.75;
    private int size = 0;
    private int expandRate = 2;

    public int size() {
        return size;
    }

    /**
     * Constructors
     */
    public MyHashMap() {
        buckets = createTable(initialLength);
    }

    public MyHashMap(int initialSize) {
        initialLength = initialSize;
        buckets = createTable(initialLength);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad     maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        initialLength = initialSize;
        loadFactor = maxLoad;
        buckets = createTable(initialLength);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    public void clear() {
        buckets = createTable(initialLength);
        size = 0;
    }

    private int getHashCode(K key, int length) {
        return ((key.hashCode() % length) + length) % length;
    }

    public boolean containsKey(K key) {
        int hashCode = getHashCode(key, buckets.length);
        return buckets[hashCode] != null;
    }

    public V get(K key) {
        int hashCode = getHashCode(key, buckets.length);
        Collection<Node> tmp = buckets[hashCode];
        if (tmp != null) {
            for (Node now : tmp) {
                if (now.key.equals(key)) {
                    return now.value;
                }
            }
        }
        return null;
    }

    public V remove(K key) {
        V result = get(key);
        if (result == null) {
            return null;
        }
        int hashCode = getHashCode(key, buckets.length);
        Collection<Node> ll = buckets[hashCode];
        ll.remove(createNode(key, result));
        size--;
        if (ll.isEmpty()) {
            buckets[hashCode] = null;
        }
        return result;
    }

    public V remove(K key, V value) {
        V result = get(key);
        if (result == null || !result.equals(value)) {
            return null;
        }
        return remove(key);
    }

    private void resize() {
        Collection<Node>[] tmp = createTable(buckets.length * expandRate);
        for (Collection<Node> now : buckets) {
            if (now != null) {
                for (Node i : now) {
                    put(tmp, i);
                }
            }
        }
        buckets = tmp;
    }

    private boolean put(Collection<Node>[] array, Node u) {
        int hashCode = getHashCode(u.key, array.length);
        if (array[hashCode] == null) {
            array[hashCode] = createBucket();
        }
        for (Node now : array[hashCode]) {
            if (now.key.equals(u.key)) {
                now.value = u.value;
                return false;
            }
        }
        array[hashCode].add(u);
        return true;
    }

    public void put(K key, V value) {
        if (put(buckets, createNode(key, value))) {
            size++;
        }
        if (((double) size) / buckets.length >= loadFactor) {
            resize();
        }
    }

    public Set<K> keySet() {
        Set<K> result = new HashSet<>();
        for (Collection<Node> tmp : buckets) {
            if (tmp != null) {
                for (Node now : tmp) {
                    result.add(now.key);
                }
            }
        }
        return result;
    }

    /**
     * Returns a data structure to be a hash table bucket
     * <p>
     * The only requirements of a hash table bucket are that we can:
     * 1. Insert items (`add` method)
     * 2. Remove items (`remove` method)
     * 3. Iterate through items (`iterator` method)
     * <p>
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     * <p>
     * Override this method to use different data structures as
     * the underlying bucket type
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new ArrayList<>();
    }


    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }

    // Your code won't compile until you do so!

    public Iterator<K> iterator() {
        return new HashMapIterator();
    }

}
