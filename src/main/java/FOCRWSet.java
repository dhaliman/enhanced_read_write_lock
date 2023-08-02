public class FOCRWSet {

    private final int[] data;
    // Always one greater than the index of the last element.
    private int maxIndex;

    public FOCRWSet(int n) {
        this.data = new int[n];
        this.maxIndex = 0;
    }

    public void insert(int el) {
        // check if the element already exists in the array.
        if (exists(el) >= 0)
            return;

        // remove all the -1s.
        cleanup();

        // check if there is space in the array.
        if (maxIndex == data.length)
            return;

        // find index to insert the new el at.
        int newElIndex = 0;
        for (int i = 0; i <= maxIndex; i++) {
            if (data[i] > el) {
                newElIndex = i;
                break;
            }
            if (i == maxIndex)
                newElIndex = maxIndex;
        }

        // right shift all elements to create space.
        for (int i = maxIndex; i > newElIndex; i--) {
            data[i] = data[i - 1];
        }
        data[newElIndex] = el; // insert the new index.
        maxIndex++; // increase the maxIndex because we have added a new element.

    }

    public int cleanup() {
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
        }
        return maxIndex - (nextElIndex - nextPosition);
    }

    public void delete(int el) {
        int index = exists(el);
        if (index >= 0)
            data[index] = -1;
    }

    public Boolean member(int el) {
        return exists(el) >= 0;
    }

    private int exists(int el) {
        int low = 0;
        int high = maxIndex - 1;
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

    public void printSorted() {
        for (int i = 0; i < maxIndex; i++) {
            if (data[i] != -1)
                System.out.print(data[i]);
        }
    }
}

