package io.mattw.jports;

import io.mattw.jports.IPv4AddressBlock;
import io.mattw.jports.IPv4AddressPort;
import io.mattw.jports.IPv4BlockPortScan;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class IPv4BlockPortScanTest {

    private IPv4AddressBlock addressBlock = new IPv4AddressBlock("192.168.1.0/24");
    private List<Integer> ports = Arrays.asList(80, 443, 8080, 8000, 7878, 8989, 32400);
    private int threadCount = 64;

    private IPv4BlockPortScan blockScan;

    private void consumingMethod(IPv4AddressPort addressPort) {
        System.out.println(addressPort.getFullAddress());
    }

    @Test
    public void test_fullRun() throws InterruptedException {
        blockScan = new IPv4BlockPortScan(addressBlock)
                .setPorts(ports)
                .setThreadCount(threadCount)
                .setCheckPortOpen(false)
                .setConsumingMethod(this::consumingMethod)
                .executeAndAwait();
    }

}