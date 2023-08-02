import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class FOCConcurrentSetTest {

    @Test
    void ConcurrentInsertsShouldAddElementsCorrectly() throws InterruptedException {
        FOCConcurrentSet set = new FOCConcurrentSet(10);
        Thread thread1 = new Thread() {
            public void run() {
                for (int i = 0; i < 5; i++) {
                    try {
                        set.insert(i);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        Thread thread2 = new Thread() {
            public void run() {
                for (int i = 5; i < 10; i++) {
                    try {
                        set.insert(i);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        for (int i = 0; i < 10; i++)
            assertEquals(true, set.member(i));
    }

    @Test
    void ConcurrentDeletesInserts() throws InterruptedException {
        FOCConcurrentSet set = new FOCConcurrentSet(10);
        set.insert(1);
        set.insert(2);
        set.insert(3);
        set.insert(4);

        Thread thread1 = new Thread() {
            public void run() {
                try {
                    set.delete(1);
                    set.delete(2);
                    set.insert(5);
                    set.delete(3);
                    set.delete(4);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        Thread thread2 = new Thread() {
            public void run() {
                try {
                    set.delete(1);
                    set.delete(2);
                    set.insert(6);
                    set.delete(3);
                    set.delete(4);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        assertEquals(false, set.member(1));
        assertEquals(false, set.member(2));
        assertEquals(false, set.member(3));
        assertEquals(false, set.member(4));
        assertEquals(true, set.member(5));
        assertEquals(true, set.member(6));
    }

    @Test
    void ConcurrentInsertDeleteAndPrintShouldWorkCorrectly() throws InterruptedException {
        FOCConcurrentSet set = new FOCConcurrentSet(10);
        Thread thread1 = new Thread() {
            public void run() {
                try {
                    set.insert(8);
                    set.insert(9);
                    set.insert(2);
                    set.delete(9);
                    set.delete(2);
                    set.insert(10);
                    set.insert(3);
                    set.insert(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        Thread thread2 = new Thread() {
            public void run() {
                try {
                    set.insert(4);
                    set.insert(5);
                    set.printSorted();
                    set.insert(6);
                    set.insert(7);
                    set.printSorted();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        assertEquals(true, set.member(1));
        assertEquals(false, set.member(2));
        assertEquals(true, set.member(3));
        assertEquals(true, set.member(4));
        assertEquals(true, set.member(5));
        assertEquals(true, set.member(6));
        assertEquals(true, set.member(7));
        assertEquals(true, set.member(8));
        assertEquals(false, set.member(9));
        assertEquals(true, set.member(10));
    }

    @Test
    void ConcurrentExistsAndDeleteShouldWorkCorrectly() throws InterruptedException {
        FOCConcurrentSet set = new FOCConcurrentSet(10);
        set.insert(1);
        set.insert(2);
        set.insert(3);
        set.insert(4);

        Thread thread1 = new Thread() {
            public void run() {
                try {
                    set.delete(1);
                    set.delete(2);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        Thread thread2 = new Thread() {
            public void run() {
                try {
                    assertEquals(true, set.member(3));
                    assertEquals(true, set.member(4));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        assertEquals(false, set.member(1));
        assertEquals(false, set.member(2));
    }

    private void runARandomOperation(FOCConcurrentSet set) throws InterruptedException {
        Random rand = new Random();
        int upperbound = 4;
        int int_random = rand.nextInt(upperbound);

        Random element = new Random();
        upperbound = 100;
        int random_element = element.nextInt(upperbound);

        if (int_random == 0) {
            set.delete(random_element);
        }
        if (int_random == 1) {
            set.printSorted();
        }
        if (int_random == 2) {
            set.insert(random_element);
        }
        if (int_random == 3) {
            set.member(random_element);
        }
    }

    private Boolean checkSorted(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] > a[i + 1]) {
                return false; // It is proven that the array is not sorted.
            }
        }

        return true;
    }

    @Test
    void largeNumberOfInterleavings() throws InterruptedException {
        FOCConcurrentSet set = new FOCConcurrentSet(4000);

        Thread thread1 = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    try {
                        runARandomOperation(set);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        Thread thread2 = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    try {
                        runARandomOperation(set);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        Thread thread3 = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    try {
                        runARandomOperation(set);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        Thread thread4 = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    try {
                        runARandomOperation(set);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();

        int[] output = set.getArray();

        assertEquals(true, checkSorted(output));
    }
}