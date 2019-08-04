package io.mattw.jports;

import io.mattw.jports.IPv4Address;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class IPv4AddressTest {

    private String zeroAddress = "0.0.0.0";
    private String address100 = "0.0.0.100";
    private String full255Address = "255.255.255.255";
    private String address256 = "10.0.0.256";
    private String full256Address = "256.256.256.256";
    private String overflowAddress = "10.999.999.999";
    private String invalidAddress4digit = "10.1234.123.123";
    private String invalidAddressAlpha = "Hello World";

    private long zero = 0;

    private IPv4Address address;

    @Test
    public void test_zeroAddress() {
        address = new IPv4Address(zeroAddress);

        assertEquals(zeroAddress, address.getAddress());
        assertEquals(0, address.getDecimal());
    }

    @Test
    public void test_address100() {
        address = new IPv4Address(address100);

        assertEquals(address100, address.getAddress());
        assertEquals(100, address.getDecimal());
    }

    @Test
    public void test_full255Address() {
        address = new IPv4Address(full255Address);

        assertEquals(full255Address, address.getAddress());
        assertEquals(Integer.MAX_VALUE*2L+1, address.getDecimal());
    }

    @Test
    public void test_overflowAddress() {
        address = new IPv4Address(overflowAddress);

        assertNotEquals(overflowAddress, address.getAddress());
    }

    @Test
    public void test_address256() {
        address = new IPv4Address(address256);

        assertNotEquals(address256, address.getAddress());
    }

    @Test
    public void test_full256Address() {
        address = new IPv4Address(full256Address);

        assertNotEquals(full256Address, address.getAddress());
    }

    @Test
    public void testDecimal_0() {
        address = new IPv4Address(zero);

        assertEquals(zero, address.getDecimal());
        assertEquals(zeroAddress, address.getAddress());
    }

    @Test
    public void testDecimals_cidr20() {
        for(long decimal = 0; decimal < Math.pow(2, 32 - 22); decimal++) {
            address = new IPv4Address(decimal);

            assertEquals(decimal, address.getDecimal());

            System.out.printf("%1$-24s%2$-24s\n", address.getAddress(), address.getDecimal());
        }
    }

    @Test
    public void testDecimal_255() {
        address = new IPv4Address(255L);

        assertEquals(255L, address.getDecimal());
    }

    @Test
    public void testDecimal_maxInt() {
        address = new IPv4Address(Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, address.getDecimal());
    }

    @Test
    public void testDecimal_maxIntx2() {
        address = new IPv4Address(Integer.MAX_VALUE*2L);

        assertEquals(Integer.MAX_VALUE*2L, address.getDecimal());

        System.out.println(address.getAddress());
    }

    @Test
    public void testDecimal_maxIntx2plus1() {
        address = new IPv4Address(Integer.MAX_VALUE*2L+1);

        assertEquals(Integer.MAX_VALUE*2L+1, address.getDecimal());

        System.out.println(address.getAddress());
    }

    @Test
    public void testDecimal_maxIntx2plus2() {
        address = new IPv4Address(Integer.MAX_VALUE*2L+2);

        assertEquals(0, address.getDecimal());

        System.out.println(address.getAddress());
    }

    @Test (expected = IllegalArgumentException.class)
    public void test_invalidAddress_4digitSegment() {
        address = new IPv4Address(invalidAddress4digit);
    }

    @Test (expected = IllegalArgumentException.class)
    public void test_invalidAddress_alpha() {
        address = new IPv4Address(invalidAddressAlpha);
    }

}