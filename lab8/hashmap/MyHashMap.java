package hashmap;

import java.util.Collection;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
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
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    private static final int INITIAL_SIZE = 16;
    private static final double MAX_LOAD = 0.75;

    private int N;
    private int M;
    private int initialSize;
    private double maxLoad;
    // You should probably define some more!

    /** Constructors */
    public MyHashMap() {
        this(INITIAL_SIZE);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, MAX_LOAD);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        buckets = createTable(initialSize);
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = createBucket();
        }
        this.N = 0;
        this.M = initialSize;
        this.initialSize = initialSize;
        this.maxLoad = maxLoad;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new java.util.LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] buckets = new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
            buckets[i] = createBucket();
        }
        return buckets;
    }

    private double loadFactor() {
        return (double) N / M;
    }

    @Override
    public void clear() {
        buckets = createTable(this.initialSize);
        this.N = 0;
        this.M = initialSize;
    }

    @Override
    public boolean containsKey(K key) {return get(key) != null;}

    @Override
    public V get(K key) {
        int index = Math.floorMod(key.hashCode(), M);
        for (Node node : buckets[index]) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    @Override
    public int size() {return N;}

    @Override
    public void put(K key, V value) {
        int index = Math.floorMod(key.hashCode(), M);
        if (!containsKey(key)) {
            buckets[index].add(createNode(key, value));
            N++;
            if (loadFactor() > maxLoad) {
                largerSize();
            }
        }else{
            for (Node node : buckets[index]) {
                if (node.key.equals(key)) {
                    node.value = value;
                    return;
                }
            }
        }
    }

    private void largerSize(){
        int newM = M * 2;
        Collection<Node>[] newBuckets = createTable(newM);
        for (Collection<Node> bucket : buckets){
            for (Node node : bucket){
                int newIndex = Math.floorMod(node.key.hashCode(), newM);
                newBuckets[newIndex].add(node);
            }
        }
        this.buckets = newBuckets;
        this.M = newM;
    }

    @Override
    public java.util.Set<K> keySet() {
        java.util.Set<K> set = new java.util.HashSet<>();
        for (Collection<Node> bucket : buckets) {
            for (Node node : bucket) {
                set.add(node.key);
            }
        }
        return set;
    }

    @Override
    public java.util.Iterator<K> iterator() {
        return keySet().iterator();
    }

    @Override
    public V remove(K key) {throw new UnsupportedOperationException();}

    @Override
    public V remove(K key, V value) {throw new UnsupportedOperationException();}
}
