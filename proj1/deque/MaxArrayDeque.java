package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> c) {
        super();
        this.comparator = c;
    }

    public T max(){
        return max(this.comparator);
    }

    public T max(Comparator<T> comparator){
        if (isEmpty()){
            return null;
        }
        T maxItem = get(0);
        for (int i = 1; i < size(); i++){
            T currentItem = get(i);
            if (comparator.compare(currentItem, maxItem) > 0){
                maxItem = currentItem;
            }
        }
        return maxItem;
    }
}
