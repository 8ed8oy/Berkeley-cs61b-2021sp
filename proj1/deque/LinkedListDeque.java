package deque;

import java.util.Iterator;

public class LinkedListDeque <T>{
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

    public void addFirst(T i) {
        Node newNode = new Node(i, sentinel, sentinel.next);
        sentinel.next.prev = newNode;
        sentinel.next = newNode;
        size++;
    }

    public void addLast(T i) {
        Node newNode = new Node(i, sentinel.prev, sentinel);
        sentinel.prev.next = newNode;
        sentinel.prev = newNode;
        size++;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        Node current = sentinel.next;
        while (current != sentinel) {
            System.out.print(current.t + " ");
            current = current.next;
        }
        System.out.println();
    }

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


    //public Iterable<T> iterator() {}

    //public T get() {} /** Use iterator. */

    //public boolean equals(Object o) {}
}
