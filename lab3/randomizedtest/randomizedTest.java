package randomizedtest;

import edu.princeton.cs.introcs.StdRandom;

public class randomizedTest {
    public static void main(String[] args) {
        AListNoResizing<Integer> L = new AListNoResizing<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 3);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // getLast
                int Val = L.getLast();
                System.out.println("getLast(" + Val + ")");
            }
              else if (operationNumber == 2) {
                // removelast
                int Val = L.removeLast();
                System.out.println("removelast(" + Val + ")");
            }
        }
    }
}
