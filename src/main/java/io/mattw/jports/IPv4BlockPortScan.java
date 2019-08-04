package io.mattw.jports;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * Configure and start a multithreaded cycle through a block of addresses and ports.
 * <p>
 * Each address:port can checked being open with a {@link java.net.Socket} and sent to
 * the {@link #consumingMethod} for processing.
 */
public class IPv4BlockPortScan extends BlockScan<IPv4BlockPortScan> {

    private boolean checkPortOpen = true;
    private int checkTimeout = 300;
    private Collection<Integer> ports;
    private Consumer<IPv4AddressPort> consumingMethod;

    private Queue<IPv4AddressPort> objectQueue = new LinkedBlockingQueue<>();

    /**
     * Scan a block of addresses
     */
    public IPv4BlockPortScan(final IPv4AddressBlock addressBlock) {
        super(addressBlock);
    }

    /**
     * Endlessly scan in either direction or single addresses. Single addresses are more relevant here
     * than in {@link IPv4BlockScan} as you may have multiple ports to cycle through for a single address.
     */
    public IPv4BlockPortScan(final String address, final ScanMethod scanMethod) {
        super(address, scanMethod);
    }

    /**
     * Endlessly scan in either direction or single addresses. Single addresses are more relevant here
     * than in {@link IPv4BlockScan} as you may have multiple ports to cycle through for a single address.
     */
    public IPv4BlockPortScan(final IPv4Address address, final ScanMethod scanMethod) {
        super(address, scanMethod);
    }

    /**
     * Scan a specific list of addresses.
     * @param addresses
     */
    public IPv4BlockPortScan(final List<String> addresses) {
        super(addresses);
    }

    /**
     * Scan a specific list of addresses.
     */
    public IPv4BlockPortScan(final Collection<IPv4Address> addresses) {
        super(addresses);
    }

    public IPv4BlockPortScan setAddressBlock(final IPv4AddressBlock addressBlock) {
        this.addressBlock = addressBlock;
        return this;
    }

    public IPv4BlockPortScan setThreadCount(final int threadCount) {
        this.threadCount = Math.abs(threadCount);
        return this;
    }

    public IPv4BlockPortScan setConsumingMethod(final Consumer<IPv4AddressPort> consumingMethod) {
        this.consumingMethod = consumingMethod;
        return this;
    }

    public IPv4BlockPortScan setPorts(Collection<Integer> ports) {
        this.ports = ports;
        return this;
    }

    /**
     * Flag to check if the port is open on an address.
     *
     * @param checkPortOpen when true, if port is open, will push to consumer, otherwise ignored
     *                      when false, will push all generated values to consumer regardless if open
     */
    public IPv4BlockPortScan setCheckPortOpen(boolean checkPortOpen) {
        this.checkPortOpen = checkPortOpen;
        return this;
    }

    /**
     * limit in milliseconds to timeout when checking a port
     *
     * @param checkTimeout millis
     */
    public IPv4BlockPortScan setCheckTimeout(int checkTimeout) {
        this.checkTimeout = checkTimeout;
        return this;
    }

    @Override
    public IPv4BlockPortScan execute() {
        Objects.requireNonNull(consumingMethod);
        Objects.requireNonNull(addressBlock);
        Objects.requireNonNull(ports);

        if (ports.isEmpty()) {
            throw new IllegalStateException("Ports list should not be empty.");
        }

        producer.submitAndShutdown(this::producer);

        consumers = new ExecutorGroup(threadCount);
        consumers.submitAndShutdown(this::consumer);

        return this;
    }

    @Override
    void producer() {
        switch (scanMethod) {
            case SINGLE_ADDRESS:
                if (offerPorts(startAddress)) {
                    break;
                }
                break;

            case RANGE_ADDRESS:
                IPv4Address address1 = addressBlock.getFirstAddress();
                do {
                    if (offerPorts(address1)) {
                        break;
                    }

                    address1 = address1.nextAddress();
                } while (address1.getDecimal() < addressBlock.getLastAddress().getDecimal() && !shutdown);
                break;

            case MULTI_ADDRESS:
                for (IPv4Address address2 : addresses) {
                    if (offerPorts(address2)) {
                        break;
                    }

                    if (shutdown) {
                        break;
                    }
                }
                break;

            case ENDLESS_DECREASE:
            case ENDLESS_INCREASE:
                IPv4Address address3 = startAddress;
                do {
                    if (offerPorts(address3)) {
                        break;
                    }

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

    /**
     * Cycles through the list of ports for this address to offer to consumers
     *
     * @param address any address
     * @return ended from shutdown
     */
    private boolean offerPorts(final IPv4Address address) {
        for (Integer port : ports) {
            objectQueue.offer(new IPv4AddressPort(address, port));

            if (shutdown) {
                return true;
            }
        }

        return false;
    }

    @Override
    void consumer() {
        while (producer.isStillWorking() || !objectQueue.isEmpty()) {
            final IPv4AddressPort addressPort = objectQueue.poll();

            if (addressPort != null) {
                if (!checkPortOpen || checkPortOpen && isPortOpen(addressPort)) {
                    consumingMethod.accept(addressPort);
                }
            } else {
                sleep(25);
            }

            if (shutdown) {
                break;
            }
        }
    }

    private boolean isPortOpen(final IPv4AddressPort addressPort) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(addressPort.getAddress().getAddress(), addressPort.getPort()), checkTimeout);

            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
