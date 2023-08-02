import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class FOCSetTest {
    int exists(int[] data, int el) {
        int low = 0;
        int high = data.length - 1;
        int mid;

        while (low <= high) {
            mid = (low + high) / 2;

            while (data[mid] == -1 && mid < high)
                mid = mid + 1;

            while (data[mid] == -1 && mid > low)
                mid = mid - 1;

            if (data[mid] == -1)
                return -1;

            if (data[mid] > el) {
                high = mid - 1;
            } else if (data[mid] < el) {
                low = mid + 1;
            } else {
                return mid;
            }
        }

        return -1;
    }

    @Test
    void checkCheckCheck() {
        int[] data = {1, 2, 3, 4, 5, 6};

        assertEquals(0, exists(data, 1));
        assertEquals(1, exists(data, 2));
        assertEquals(2, exists(data, 3));
        assertEquals(3, exists(data, 4));
        assertEquals(4, exists(data, 5));
        assertEquals(5, exists(data, 6));

        data = new int[]{1, 2, -1, 4, 5, 6};
        assertEquals(0, exists(data, 1));
        assertEquals(1, exists(data, 2));
        assertEquals(3, exists(data, 4));
        assertEquals(4, exists(data, 5));
        assertEquals(5, exists(data, 6));

        data = new int[]{1, 2, -1, 4, -1, 6};
        assertEquals(0, exists(data, 1));
        assertEquals(1, exists(data, 2));
        assertEquals(3, exists(data, 4));
        assertEquals(5, exists(data, 6));

        data = new int[]{1, 2, -1, 4, 5, -1};
        assertEquals(0, exists(data, 1));
        assertEquals(1, exists(data, 2));
        assertEquals(3, exists(data, 4));
        assertEquals(4, exists(data, 5));

        data = new int[]{1, 2, -1, -1, -1, -1};
        assertEquals(0, exists(data, 1));
        assertEquals(1, exists(data, 2));

        data = new int[]{1, -1, -1, -1, -1, -1};
        assertEquals(0, exists(data, 1));

        data = new int[]{1, -1, -1, -1, -1, 10};
        assertEquals(0, exists(data, 1));
        assertEquals(5, exists(data, 10));

        data = new int[]{-1, -1, -1, -1, -1, 10};
        assertEquals(5, exists(data, 10));

        data = new int[]{-1, -1, -1, -1, -1, -1};
        assertEquals(-1, exists(data, 10));

        data = new int[]{-1, -1, -1, -1, -1, 10};
        assertEquals(5, exists(data, 10));
    }

    @Test
    void shouldBeAbleToAddANewInteger() {
        FOCSet set = new FOCSet(10);

        for (int i = 1; i < 11; i++) {
            set.insert(i);
            assertEquals(true, set.member(i));
        }

        for (int i = 1; i < 11; i++) {
            set.delete(i);
            assertNotEquals(true, set.member(i));
        }

        set = new FOCSet(10);
        for (int i = 10; i > 0; i--) {
            set.insert(i);
            assertEquals(true, set.member(i));
        }

        for (int i = 10; i > 0; i--) {
            set.delete(i);
            assertNotEquals(true, set.member(i));
        }

        set = new FOCSet(10);
        for (int i = 1; i < 11; i++) {
            set.insert(i);
            assertEquals(true, set.member(i));
            set.delete(i);
            assertNotEquals(true, set.member(i));
        }
    }

    private int cleanup2(int[] data, int maxIndex) {
        int nextPosition = 0;
        int nextElIndex = 0;

        while (nextElIndex < maxIndex) {
            if (data[nextElIndex] == -1)
                nextElIndex++;
            else {
                data[nextPosition] = data[nextElIndex];
                nextPosition++;
                nextElIndex++;
            }
            System.out.println(Arrays.toString(data));
        }
        return maxIndex - (nextElIndex - nextPosition);
    }

    @Test
    void testestest() {
        int[] d = new int[] {1, 2, -1, 3, -1, 4, -1, 5, -1};

        System.out.println(cleanup2(d, d.length));
    }
}