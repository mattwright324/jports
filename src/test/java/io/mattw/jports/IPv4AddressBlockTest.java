package io.mattw.jports;

import io.mattw.jports.IPv4Address;
import io.mattw.jports.IPv4AddressBlock;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.*;

public class IPv4AddressBlockTest {

    private IPv4Address firstAddress = new IPv4Address("0.0.0.0");
    private int cidrValue = 16;
    private IPv4Address lastAddressCidr = firstAddress.traverse((long) Math.pow(2, 32 - cidrValue));
    private IPv4Address lastAddress = firstAddress.traverse(100);
    private String cidrForwardSlash = lastAddress.getAddress() + "/" + cidrValue;
    private String cidrBackwardsSlash = lastAddress.getAddress() + "\\" + cidrValue;

    private IPv4AddressBlock block;

    @Test
    public void testBlock_sameValue() {
        block = new IPv4AddressBlock(lastAddress, lastAddress);

        assertFalse(block.isValidCIDR());
        assertEquals(0, block.getSize());
        assertEquals(-1, block.getCidrLength());
        assertNull(block.getCidrNotation());
    }

    @Test
    public void testBlock_rangeNotCIDR() {
        block = new IPv4AddressBlock(firstAddress, lastAddress);

        assertFalse(block.isValidCIDR());
        assertThat(block.getSize(), greaterThan(0L));
    }

    @Test
    public void testBlock_rangeValidCIDR() {
        block = new IPv4AddressBlock(firstAddress, lastAddressCidr);

        assertTrue(block.isValidCIDR());
        assertNotNull(block.getCidrNotation());
        assertEquals(cidrValue, block.getCidrLength());
    }

    @Test
    public void testBlock_cidrForwardSlash() {
        block = new IPv4AddressBlock(cidrForwardSlash);

        assertTrue(block.isValidCIDR());
        assertEquals(cidrForwardSlash, block.getCidrNotation());
    }

    @Test
    public void testBlock_cidrBackwardSlash() {
        block = new IPv4AddressBlock(cidrBackwardsSlash);

        assertTrue(block.isValidCIDR());
        assertNotNull(block.getCidrNotation());
        assertNotEquals(cidrBackwardsSlash, block.getCidrNotation());
    }

    @Test
    @Ignore
    public void testBlock_traversal() {
        block = new IPv4AddressBlock("0.0.0.0", 1);

        IPv4Address address = block.getFirstAddress();
        do {
            address = address.nextAddress();
        } while (address.getDecimal() < block.getLastAddress().getDecimal());
    }

}
