package deque;

import java.util.Iterator;

public class LinkedListDeque <T> implements Deque<T> {
    private class Node {
        public T t;
        public Node next;
        public Node prev;

        public Node(T i, Node p, Node n) {
            t = i;
            prev = p;
            next = n;
        }
    }

    private Node sentinel;
    private int size;

    public LinkedListDeque() {
        sentinel = new Node(null, null, null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        size = 0;
    }

    @Override
    public void addFirst(T i) {
        Node newNode = new Node(i, sentinel, sentinel.next);
        sentinel.next.prev = newNode;
        sentinel.next = newNode;
        size++;
    }

    @Override
    public void addLast(T i) {
        Node newNode = new Node(i, sentinel.prev, sentinel);
        sentinel.prev.next = newNode;
        sentinel.prev = newNode;
        size++;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        Node current = sentinel.next;
        while (current != sentinel) {
            System.out.print(current.t + " ");
            current = current.next;
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        Node firstNode = sentinel.next;
        T item = firstNode.t;
        sentinel.next = firstNode.next;
        firstNode.next.prev = sentinel;
        size--;
        return item;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        Node lastNode = sentinel.prev;
        T item = lastNode.t;
        sentinel.prev = lastNode.prev;
        lastNode.prev.next = sentinel;
        size--;
        return item;
    }

    public T getRecursive(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        return getRecursiveHelper(sentinel.next, index);
    }

    private T getRecursiveHelper(Node node, int index) {
        if (index == 0) {
            return node.t;
        }
        return getRecursiveHelper(node.next, index - 1);
    }

    @Override
    public Iterator<T> iterator() {
        return new DequeIterator();
    }

    private class DequeIterator implements Iterator<T> {
        private Node current = sentinel.next;

        @Override
        public boolean hasNext() {
            return current != sentinel;
        }

        @Override
        public T next() {
            T item = current.t;
            current = current.next;
            return item;
        }
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        Iterator<T> iter = iterator();
        int i = 0;
        while (iter.hasNext()) {
            T item = iter.next();
            if (i == index) {
                return item;
            }
            i++;
        }
        return null;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Deque)) {
            return false;
        }
        Deque<?> that = (Deque<?>) o;
        if (this.size() != that.size()) {
            return false;
        }
        Iterator<T> thisIter = this.iterator();
        Iterator<?> thatIter = that.iterator();
        while (thisIter.hasNext() && thatIter.hasNext()) {
            T thisItem = thisIter.next();
            Object thatItem = thatIter.next();
            if (thisItem == null) {
                if (thatItem != null) {
                    return false;
                }
            } else {
                if (!thisItem.equals(thatItem)) {
                    return false;
                }
            }
        }
        return true;
    }
}
