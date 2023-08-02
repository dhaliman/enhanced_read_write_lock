public class FOCConcurrentSet {

    private FOCLock lock;
    // Always one greater than the index of the last element.
    private int maxIndex;
    private int length;

    public FOCConcurrentSet(int n) {
        this.maxIndex = 0;
        this.length = n;
        this.lock = new FOCLock(new int[n]);
    }

    public void insert(int el) throws InterruptedException {
        lock.enterExclusiveWrite(true, 0, length - 1);
        // check if the element already exists in the array.
        if (exists(el, true) >= 0) {
            lock.leaveExclusiveWrite();
            return;
        }

        // remove all the -1s.
        cleanup();

        // check if there is space in the array.
        if (maxIndex == length)
            return;

        // find index to insert the new el at.
        int newElIndex = 0;
        for (int i = 0; i <= maxIndex; i++) {
            if (lock.getElement(i, true) > el) {
                newElIndex = i;
                break;
            }
            if (i == maxIndex)
                newElIndex = maxIndex;
        }
        lock.updateLockRange(newElIndex, length - 1);
        // right shift all elements to create space.
        for (int i = maxIndex; i > newElIndex; i--) {
            lock.writeElement(i, lock.getElement(i-1, true));
        }
        // insert the new index.
        lock.writeElement(newElIndex, el);
        maxIndex++; // increase the maxIndex because we have added a new element.
        lock.leaveExclusiveWrite();
    }

    public void cleanup() throws InterruptedException {
        int nextPosition = 0;
        int nextElIndex = 0;

        while (nextElIndex < maxIndex) {
            if (lock.getElement(nextElIndex, true) == -1)
                nextElIndex++;
            else {
                lock.writeElement(nextPosition, lock.getElement(nextElIndex, true));
                nextPosition++;
                nextElIndex++;
            }
        }
        maxIndex = maxIndex - (nextElIndex - nextPosition);
    }

    public void delete(int el) throws InterruptedException {
        lock.enterExclusiveRead();
        int index = exists(el, false);
        lock.enterExclusiveWrite(true, index, index);
        if (index >= 0 && lock.getElement(index, true) != -1) {
            lock.writeElement(index, -1);
        }
        lock.leaveExclusiveWrite();
    }

    public Boolean member(int el) throws InterruptedException {
        lock.enterExclusiveRead();
        Boolean result = exists(el, false) >= 0;
        lock.leaveExclusiveRead();
        return result;
    }

    private int exists(int el, Boolean isWriter) throws InterruptedException {
        int low = 0;
        int high = maxIndex - 1;
        int mid;

        while (low <= high) {
            mid = (low + high) / 2;

            while (lock.getElement(mid, isWriter) == -1 && mid < high)
                mid = mid + 1;

            while (lock.getElement(mid, isWriter) == -1 && mid > low)
                mid = mid - 1;

            if (lock.getElement(mid, isWriter) == -1)
                return -1;


            if (lock.getElement(mid, isWriter) > el) {
                high = mid - 1;
            } else if (lock.getElement(mid, isWriter) < el) {
                low = mid + 1;
            } else {
                return mid;
            }
        }

        return -1;
    }

    public void printSorted() throws InterruptedException {
        lock.enterCompromiseRead();
        for (int i = 0; i < maxIndex; i++) {
            int currEl = lock.getElement(i, false);
            if (currEl != -1)
                System.out.print(currEl + ", ");
        }
        System.out.println();
        lock.leaveCompromiseRead();
    }

    public int[] getArray() {
        int[] arr = new int[maxIndex];
        int j = 0;
        for (int i = 0; i < maxIndex; i++) {
            if (lock.getArray()[i] != -1) {
                arr[j] = lock.getArray()[i];
                j++;
            }
        }

        int[] finalArr = new int[j];
        for (int i = 0; i < j; i++) {
            finalArr[i] = arr[i];
        }

        return finalArr;
    }
}
