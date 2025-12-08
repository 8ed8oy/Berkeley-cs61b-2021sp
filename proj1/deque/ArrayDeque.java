package deque;

public class ArrayDeque<T> {
    private int size, nextFirst, nextLast;
    private T[] items;


    public ArrayDeque() {
        size = 0;
        items = (T[]) new Object[8];
        nextFirst = 0;
        nextLast = 0;
    }

    public int size() {
        return size;
    }

    public T get(int index){
        return items[(index + nextFirst + 1) % items.length];
    }

    public boolean isEmpty() {
        return size == 0;
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

    public void addLast(T item){
        if (size == items.length) {
            resize(size * 2, nextFirst, nextLast);
        }
        items[nextLast] = item;
        nextLast = (nextLast + 1) % items.length;
        size++;
    }

    public void addFirst(T item){
        if (size == items.length) {
            resize(size * 2, nextFirst, nextLast);
        }
        nextFirst = (nextFirst - 1 + items.length) % items.length;
        items[nextFirst] = item;
        size++;
    }

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

    public void printDeque(){
        for (int i = 0; i < size; i++) {
            System.out.print(get(i) + " ");
        }
        System.out.println();
    }

    //public Iterable<T> iterator() {}

    //public boolean equals(Object o) {}
}
