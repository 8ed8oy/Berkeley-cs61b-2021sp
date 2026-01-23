package bstmap;

import java.util.Iterator;
import java.util.Stack;

public class BSTMap<K extends Comparable<? super K>, V> implements Map61B<K, V> {
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
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        BSTNode curr = root;
        while (curr != null) {
            int cmp = curr.key.compareTo(key);
            if (cmp > 0) {
                curr = curr.left;
            } else if (cmp < 0) {
                curr = curr.right;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(K key) {
        return getHelper(root, key);
    }

    private V getHelper(BSTNode node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = node.key.compareTo(key);
        if (cmp > 0) {
            return getHelper(node.left, key);
        } else if (cmp < 0) {
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
        if (!containsKey(key)) {
            size += 1;
        }
        root = putHelper(root, key, value);
    }

    private BSTNode putHelper(BSTNode node, K key, V value) {
        if (node == null) {
            return new BSTNode(key, value);
        }
        int cmp = node.key.compareTo(key);
        if (cmp > 0) {
            node.left = putHelper(node.left, key, value);
        } else if (cmp < 0) {
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
