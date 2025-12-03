package IntList;

import static org.junit.Assert.*;
import org.junit.Test;

public class SquarePrimesTest {

    /**
     * Here is a test for isPrime method. Try running it.
     * It passes, but the starter code implementation of isPrime
     * is broken. Write your own JUnit Test to try to uncover the bug!
     */
    @Test
    public void testSquarePrimesSimple1() {
        IntList lst = IntList.of(14, 15, 16, 17, 18);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("14 -> 15 -> 16 -> 289 -> 18", lst.toString());
        assertTrue(changed);
    }

    @Test
    public void testSquarePrimesSimple2_1() {
        IntList lst = IntList.of(1, 2, 3, 4, 5);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("1 -> 4 -> 9 -> 4 -> 25", lst.toString());
        assertTrue(changed);
    }

    @Test
    public void testSquarePrimesSimple2_2() {
        IntList lst = IntList.of(1, 2, 3, 4, 5);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals(true, changed);
        assertTrue(changed);
    }

    @Test
    public void testSquarePrimesSimple3_1() {
        IntList lst = IntList.of(1, 4, 6, 8, 10);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("1 -> 4 -> 6 -> 8 -> 10", lst.toString());

    }

    @Test
    public void testSquarePrimesSimple3_2() {
        IntList lst = IntList.of(1, 4, 6, 8, 10);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals(false, changed);

    }
}
