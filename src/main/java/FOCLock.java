public class FOCLock {
    private int readers = 0;
    private int writers = 0;
    private int compromiseReaders = 0;
    private int writeRequests = 0;
    private int readWriteRequests = 0;
    private int lockLow;
    private int lockHigh;
    private final int[] array;

    public FOCLock(int[] array) {
        this.array = array;
        this.lockLow = 0;
        this.lockHigh = array.length - 1;
    }

    public synchronized void enterExclusiveRead() throws InterruptedException {
        while (writers > 0 || writeRequests > 0 || readWriteRequests > 0)
            wait();
        readers++;
        notifyAll();
    }

    public synchronized void leaveExclusiveRead() {
        readers--;
        notifyAll();
    }

    public synchronized void enterExclusiveWrite(Boolean isReader, int low, int high) throws InterruptedException {
        if (isReader) {
            readWriteRequests++;
            readers--;
            while (readers > 0 || writers > 0)
                wait();
        } else {
            writeRequests++;
            while (readers > 0 || writers > 0 || readWriteRequests > 0 || compromiseReaders > 0)
                wait();
        }

        lockLow = low;
        lockHigh = high;
        if (isReader)
            readWriteRequests--;
        else
            writeRequests--;
        writers++;
    }

    public synchronized void leaveExclusiveWrite() {
        writers--;
        lockLow = 0;
        lockHigh = array.length - 1;
        notifyAll();
    }

    public synchronized void enterCompromiseRead() {
        compromiseReaders++;
    }

    public synchronized void leaveCompromiseRead() {
        compromiseReaders--;
        notifyAll();
    }

    public synchronized void updateLockRange(int low, int high) {
        lockLow = low;
        lockHigh = high;
        notifyAll();
    }

    public synchronized int getElement(int index, Boolean isWriter) throws InterruptedException {
        if (!isWriter) {
            while (writers > 0 && index >= lockLow && index <= lockHigh)
                wait();
        }
        return array[index];
    }

    public synchronized void writeElement(int index, int element) {
        array[index] = element;
    }

    public int[] getArray() {

        return this.array;
    }
}
