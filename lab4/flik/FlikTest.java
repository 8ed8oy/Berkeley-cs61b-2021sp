package flik;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FlikTest {
    @Test
    public void test1() {
        Integer a = 129;
        Integer b = 129;
        assertTrue(Flik.isSameNumber(a, b));
    }
}

