package bstmap;

import java.util.Iterator;
import java.util.Stack;

public class BSTMap<K, V> implements Map61B<K, V> {
    int size = 0;
    private BSTNode root = null;

    private class BSTNode {
        BSTNode(K k, V v) {
            key = k;
            value = v;
        }

        K key;
        V value;
        BSTNode left;
        BSTNode right;
        int size;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    @Override
    public V get(K key) {
        BSTNode currentNode = root;
        return getHelper(currentNode, key);
    }

    private V getHelper(BSTNode node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = ((Comparable<K>) key).compareTo(node.key);
        if (cmp < 0) {
            return getHelper(node.left, key);
        } else if (cmp > 0) {
            return getHelper(node.right, key);
        } else {
            return node.value;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        BSTNode currentNode = root;
        putHelper(currentNode, key, value);
        size += 1;
    }

    private BSTNode putHelper(BSTNode node, K key, V value) {
        if (node == null) {
            return new BSTNode(key, value);
        }
        int cmp = ((Comparable<K>) key).compareTo(node.key);
        if (cmp < 0) {
            node.left = putHelper(node.left, key, value);
        } else if (cmp > 0) {
            node.right = putHelper(node.right, key, value);
        } else {
            node.value = value;
        }
        return node;
    }

    @Override
    public java.util.Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public java.util.Iterator<K> iterator() {
        return new BSTMapIterator();
    }

    private class BSTMapIterator implements Iterator<K> {
        private Stack<BSTNode> stack;

        public BSTMapIterator() {
            stack = new Stack<>();
            pushLeft(root);
        }

        private void pushLeft(BSTNode node) {
            while (node != null) {
                stack.push(node);
                node = node.left;
            }
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public K next() {
            BSTNode currentNode = stack.pop();
            if (currentNode.right != null) {
                pushLeft(currentNode.right);
            }
            return currentNode.key;
        }
    }
}
