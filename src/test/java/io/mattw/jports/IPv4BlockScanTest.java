package io.mattw.jports;

import io.mattw.jports.IPv4Address;
import io.mattw.jports.IPv4AddressBlock;
import io.mattw.jports.IPv4BlockScan;
import org.junit.Test;


public class IPv4BlockScanTest {

    private IPv4AddressBlock addressBlock = new IPv4AddressBlock("192.168.0.0/16");
    private int threadCount = 5;

    private IPv4BlockScan blockScan;

    private void consumingMethod(IPv4Address address) {
        System.out.println(address.getAddress());
    }

    @Test
    public void test_fullRun() throws InterruptedException {
        blockScan = new IPv4BlockScan(addressBlock)
                .setThreadCount(threadCount)
                .setConsumingMethod(this::consumingMethod)
                .executeAndAwait();
    }

}
