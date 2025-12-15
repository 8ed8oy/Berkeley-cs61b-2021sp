package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T> {
    private int size, nextFirst, nextLast;
    private T[] items;


    public ArrayDeque() {
        size = 0;
        items = (T[]) new Object[8];
        nextFirst = 0;
        nextLast = 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public T get(int index){
        return items[(index + nextFirst + 1) % items.length];
    }

    private void resize(int capacity, int nextfirst, int nextlast) {
        T[] newItems = (T[]) new Object[capacity];
        if (nextfirst < nextlast) {
            System.arraycopy(items, nextfirst, newItems, 0, size);
            items = newItems;
            this.nextFirst = 0;
            this.nextLast = size;
        } else {
            System.arraycopy(items, nextfirst, newItems, 0, items.length - nextfirst);
            System.arraycopy(items, 0, newItems, items.length - nextfirst, nextlast);
            items = newItems;
            this.nextFirst = 0;
            this.nextLast = size;
        }
    }

    @Override
    public void addLast(T item){
        if (size == items.length) {
            resize(size * 2, nextFirst, nextLast);
        }
        items[nextLast] = item;
        nextLast = (nextLast + 1) % items.length;
        size++;
    }

    @Override
    public void addFirst(T item){
        if (size == items.length) {
            resize(size * 2, nextFirst, nextLast);
        }
        nextFirst = (nextFirst - 1 + items.length) % items.length;
        items[nextFirst] = item;
        size++;
    }

    @Override
    public T removeFirst(){
        if (isEmpty()) {
            return null;
        }
        T item = items[nextFirst];
        items[nextFirst] = null;
        nextFirst = (nextFirst + 1) % items.length;
        size--;
        if (size < items.length / 4 && items.length > 8) {
            resize(items.length / 2, nextFirst, nextLast);
        }
        return item;
    }

    @Override
    public T removeLast(){
        if (isEmpty()) {
            return null;
        }
        nextLast = (nextLast - 1 + items.length) % items.length;
        T item = items[nextLast];
        items[nextLast] = null;
        size--;
        if (size < items.length / 4 && items.length > 8) {
            resize(items.length / 2, nextFirst, nextLast);
        }
        return item;
    }

    @Override
    public void printDeque(){
        for (int i = 0; i < size; i++) {
            System.out.print(get(i) + " ");
        }
        System.out.println();
    }

    private class DequeIterator implements Iterator<T>{
        private int index;

        public DequeIterator() {
            index = 0;
        }

        @Override
        public boolean hasNext() {
            return index < size;
        }

        @Override
        public T next() {
            T item = items[(nextFirst + 1 + index) % items.length];
            index++;
            return item;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new DequeIterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArrayDeque)) {
            return false;
        }

        ArrayDeque<?> that = (ArrayDeque<?>) o;
        if (this.size != that.size) {
            return false;
        }
        for (int i = 0; i < this.size; i++) {
            if(this.get(i) == null){
                if(that.get(i) != null){
                    return false;
                }
            }
            if (!this.get(i).equals(that.get(i))) {
                return false;
            }
        }
        return true;
    }
}
