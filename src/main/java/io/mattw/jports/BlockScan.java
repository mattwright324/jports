package io.mattw.jports;

import java.util.Collection;

public abstract class BlockScan<T extends BlockScan> {

    ScanMethod scanMethod;
    IPv4Address startAddress;
    Collection<IPv4Address> addresses;
    IPv4AddressBlock addressBlock;
    int threadCount = 1;

    ExecutorGroup producer = new ExecutorGroup(1);
    ExecutorGroup consumers;
    boolean shutdown = false;

    /**
     * Scan a block of addresses
     */
    public BlockScan(final IPv4AddressBlock addressBlock) {
        this.addressBlock = addressBlock;

        this.scanMethod = ScanMethod.RANGE_ADDRESS;
    }

    /**
     * Endlessly scan in either direction (or single addresses, though this doesn't make much sense to do so).
     */
    public BlockScan(final IPv4Address address, final ScanMethod scanMethod) {
        if (scanMethod != ScanMethod.ENDLESS_DECREASE && scanMethod != ScanMethod.ENDLESS_INCREASE && scanMethod != ScanMethod.SINGLE_ADDRESS) {
            throw new IllegalArgumentException("Invalid scan method. Must be ENDLESS_DECREASE, ENDLESS_INCREASE, or SINGLE_ADDRESS");
        }

        this.startAddress = address;
        this.scanMethod = scanMethod;
    }

    /**
     * Scan a specific list of addresses.
     */
    public BlockScan(final Collection<IPv4Address> addresses) {
        this.addresses = addresses;

        this.scanMethod = ScanMethod.MULTI_ADDRESS;
    }

    /**
     * Signals a shutdown of the producing and consuming threads to end the process early.
     */
    public void shutdown() {
        this.shutdown = true;
    }

    private T getThis() {
        return (T) this;
    }

    public T setAddressBlock(final IPv4AddressBlock addressBlock) {
        this.addressBlock = addressBlock;
        return getThis();
    }

    public T setThreadCount(final int threadCount) {
        this.threadCount = Math.abs(threadCount);
        return getThis();
    }

    /**
     * Executes and shuts down the ExecutorServices.
     */
    public abstract T execute();

    abstract void producer();

    abstract void consumer();

    /**
     * Await the ExecutorServices to finish working
     */
    public T await() throws InterruptedException {
        producer.await();
        consumers.await();
        return (T) this;
    }

    /**
     * Execute, shutdown, and await the ExecutorServices.
     */
    public T executeAndAwait() throws InterruptedException {
        return (T) execute().await();
    }

    void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {}
    }

}
