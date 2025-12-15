package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {

    public MaxArrayDeque(Comparator<T> comparator) {
        super();
    }

    public T max(){

    }

    public T max(Comparator<T> comparator){
        if (isEmpty()){
            return null;
        }

    }
}
