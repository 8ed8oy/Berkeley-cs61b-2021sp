package randomizedtest;

import edu.princeton.cs.introcs.StdRandom;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    public static void main(String[] args) {
        BuggyAList<Integer> L = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = edu.princeton.cs.introcs.StdRandom.uniform(0, 3);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // getLast
                if (L.size() > 0) {
                    int Val = L.getLast();
                    System.out.println("getLast(" + Val + ")");
                }
            } else if (operationNumber == 2) {
                // removelast
                if (L.size() > 0) {
                    int Val = L.removeLast();
                    System.out.println("removelast(" + Val + ")");
                }
            }
        }
    }
}
