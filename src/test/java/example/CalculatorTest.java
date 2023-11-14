package example;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CalculatorTest {

    @Test
    public void testCase0() {
        doTestCase();
    }

    @Test
    public void testCase1() throws InterruptedException {
        int count = 1000;
        CountDownLatch latch = new CountDownLatch(count);
        ThreadPoolExecutor pool = new ThreadPoolExecutor(5, 5, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(count));
        Set<BigDecimal> results = new HashSet<>();
        for (int i=0; i<count; i++) {
            pool.submit(() -> {
               try {
                   results.add(doTestCase());
               } finally {
                   latch.countDown();
               }
            });
        }
        latch.await();

        Assert.assertTrue(results.size() == 1);
        pool.shutdownNow();
    }

    private BigDecimal doTestCase() {
        try {
            BigDecimal result = Calculator.add(BigDecimal.valueOf(1), BigDecimal.valueOf(2));
            Assert.assertTrue(result.equals(BigDecimal.valueOf(3)));

            result = Calculator.subtract(result, BigDecimal.valueOf(1));
            Assert.assertTrue(result.equals(BigDecimal.valueOf(2)));

            result = Calculator.multiply(result, BigDecimal.valueOf(5));
            Assert.assertTrue(result.equals(BigDecimal.valueOf(10)));

            result = Calculator.divide(result, BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP);
            Assert.assertTrue(result.equals(BigDecimal.valueOf(5)));

            result = Calculator.undo();
            Assert.assertTrue(result.equals(BigDecimal.valueOf(10)));

            result = Calculator.redo();
            Assert.assertTrue(result.equals(BigDecimal.valueOf(5)));

            result = Calculator.undo();
            Assert.assertTrue(result.equals(BigDecimal.valueOf(10)));

            result = Calculator.undo();
            Assert.assertTrue(result.equals(BigDecimal.valueOf(2)));

            result = Calculator.undo();
            Assert.assertTrue(result.equals(BigDecimal.valueOf(3)));

            result = Calculator.undo();
            Assert.assertTrue(result.equals(BigDecimal.valueOf(1)));

            result = Calculator.redo();
            Assert.assertTrue(result.equals(BigDecimal.valueOf(3)));

            result = Calculator.redo();
            Assert.assertTrue(result.equals(BigDecimal.valueOf(2)));

            result = Calculator.redo();
            Assert.assertTrue(result.equals(BigDecimal.valueOf(10)));

            result = Calculator.redo();
            Assert.assertTrue(result.equals(BigDecimal.valueOf(5)));

            result = Calculator.redo();
            Assert.assertTrue(result.equals(BigDecimal.valueOf(5)));
            return result;
        } finally {
            Calculator.reset();
        }
    }

    @Test
    public void testCase2() {
        try {
            BigDecimal result = Calculator.undo();
            Assert.assertTrue(result.compareTo(BigDecimal.ZERO) == 0);
        } finally {
            Calculator.reset();
        }
    }

    @Test
    public void testCase3() {
        try {
            BigDecimal result = Calculator.redo();
            Assert.assertTrue(result.compareTo(BigDecimal.ZERO) == 0);
        } finally {
            Calculator.reset();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCase4() {
        try {
            Calculator.add(null, BigDecimal.ONE);
        } finally {
            Calculator.reset();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCase5() {
        try {
            Calculator.add(BigDecimal.ONE, null);
        } finally {
            Calculator.reset();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCase6() {
        try {
            Calculator.divide(BigDecimal.ONE, BigDecimal.ZERO, 0, RoundingMode.HALF_UP);
        } finally {
            Calculator.reset();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCase7() {
        try {
            Calculator.divide(BigDecimal.ONE, BigDecimal.ONE, -1, RoundingMode.HALF_UP);
        } finally {
            Calculator.reset();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCase8() {
        try {
            Calculator.divide(BigDecimal.ONE, BigDecimal.ONE, null, RoundingMode.HALF_UP);
        } finally {
            Calculator.reset();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCase9() {
        try {
            Calculator.divide(BigDecimal.ONE, BigDecimal.ONE, 0, null);
        } finally {
            Calculator.reset();
        }
    }

    @Test
    public void testCase10() {
        try {
            Calculator.reset();
        } finally {
            Calculator.reset();
        }
    }
}
