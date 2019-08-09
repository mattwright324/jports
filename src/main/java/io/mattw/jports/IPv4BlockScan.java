package io.mattw.jports;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Configure and start a multithreaded cycle through a block of addresses.
 * <p>
 * A single thread will produce to a queue and the consuming threads will
 * use the provided consuming method to process them to.
 */
public class IPv4BlockScan extends BlockScan<IPv4BlockScan> {

    private Consumer<IPv4Address> consumingMethod;
    private Queue<IPv4Address> objectQueue = new LinkedBlockingQueue<>();

    /**
     * Scan a block of addresses
     */
    public IPv4BlockScan(final IPv4AddressBlock addressBlock) {
        super(addressBlock);
    }

    /**
     * Endlessly scan in either direction (or a single address though this doesn't make much sense to do so).
     */
    public IPv4BlockScan(final String address, final ScanMethod scanMethod) {
        super(address, scanMethod);
    }

    /**
     * Endlessly scan in either direction (or a single address though this doesn't make much sense to do so).
     */
    public IPv4BlockScan(final IPv4Address address, final ScanMethod scanMethod) {
        super(address, scanMethod);
    }

    /**
     * Scan a specific list of addresses.
     */
    public IPv4BlockScan(final List<String> addresses) {
        super(addresses);
    }

    /**
     * Scan a specific list of addresses.
     */
    public IPv4BlockScan(final Collection<IPv4Address> addresses) {
        super(addresses);
    }

    public IPv4BlockScan setConsumingMethod(final Consumer<IPv4Address> consumingMethod) {
        this.consumingMethod = consumingMethod;
        return this;
    }

    @Override
    public IPv4BlockScan execute() {
        Objects.requireNonNull(consumingMethod);

        producer.submitAndShutdown(this::producer);

        consumers = new ExecutorGroup(threadCount);
        consumers.submitAndShutdown(this::consumer);

        return this;
    }

    @Override
    void producer() {
        switch (scanMethod) {
            case SINGLE_ADDRESS:
                objectQueue.offer(startAddress);
                break;

            case RANGE_ADDRESS:
                IPv4Address address1 = addressBlock.getFirstAddress();
                do {
                    objectQueue.offer(address1);

                    address1 = address1.nextAddress();
                } while (address1.getDecimal() < addressBlock.getLastAddress().getDecimal() && !shutdown);
                break;

            case MULTI_ADDRESS:
                for (IPv4Address address2 : addresses) {
                    objectQueue.offer(address2);

                    if (shutdown) {
                        break;
                    }
                }
                break;

            case ENDLESS_DECREASE:
            case ENDLESS_INCREASE:
                IPv4Address address3 = startAddress;
                do {
                    objectQueue.offer(address3);

                    if (scanMethod == ScanMethod.ENDLESS_INCREASE) {
                        address3 = address3.nextAddress();
                    } else {
                        address3 = address3.prevAddress();
                    }
                } while (!shutdown);

            default:
                throw new IllegalStateException("Could not determine which scan method to perform.");
        }
    }

    @Override
    void consumer() {
        while (producer.isStillWorking() || !objectQueue.isEmpty()) {
            final IPv4Address address = objectQueue.poll();

            if (address != null) {

                consumingMethod.accept(address);
            }

            if (shutdown) {
                break;
            }

            // Without delay, would freeze JavaFX UI despite being on a separate thread.
            sleep(2);
        }
    }

}
