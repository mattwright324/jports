package io.mattw.jports;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Represents an arbitrary range of IPv4 address in range and CIDR notations.
 * <p>
 * https://en.wikipedia.org/wiki/Classless_Inter-Domain_Routing
 */
public class IPv4AddressBlock implements Serializable {

    private static final Pattern PATTERN_CIDR = Pattern.compile("(\\d{1,3}\\.){3}\\d{1,3}[/\\\\]\\d{1,2}");

    private IPv4Address firstAddress, lastAddress;

    private boolean validCIDR = false;
    private int cidrLength = -1;
    private String cidrNotation;
    private long size = 0;
    private String rangeNotation;

    /**
     * Range notation x.x.x.x-y.y.y.y.
     * Addresses are accepted in any order.
     *
     * @param address1 x.x.x.x
     * @param address2 x.x.x.x
     */
    public IPv4AddressBlock(final String address1, final String address2) {
        this(new IPv4Address(address1), new IPv4Address(address2));
    }

    /**
     * Range notation x.x.x.x-y.y.y.y
     * Addresses are accepted in any order.
     *
     * @param address1 arbitrary address
     * @param address2 arbitrary address
     */
    public IPv4AddressBlock(final IPv4Address address1, final IPv4Address address2) {
        calculate(address1, address2);
    }

    /**
     * CIDR notation $address/$cidrValue
     *
     * @param address   x.x.x.x
     * @param cidrValue 0-32
     */
    public IPv4AddressBlock(final String address, final int cidrValue) {
        final IPv4Address iPv4Address = new IPv4Address(address);
        final long distance = (long) Math.pow(2, 32 - (cidrValue % 33));

        calculate(iPv4Address, iPv4Address.traverse(distance));
    }

    /**
     * CIDR notation $address/$cidrValue
     *
     * @param address   arbitrary address
     * @param cidrValue 0-32
     */
    public IPv4AddressBlock(final IPv4Address address, final int cidrValue) {
        final long distance = (long) Math.pow(2, 32 - cidrValue);

        calculate(address, address.traverse(distance));
    }

    /**
     * CIDR notation
     *
     * @param cidrNotation x.x.x.x/y
     */
    public IPv4AddressBlock(final String cidrNotation) {
        if (matchesCIDRNotation(cidrNotation)) {
            final String[] parts = cidrNotation.split("[/\\\\]");
            final IPv4Address address = new IPv4Address(parts[0]);
            final int length = Integer.parseInt(parts[1]) % 33;
            final long distance = (long) Math.pow(2, 32 - length);

            calculate(address, address.traverse(distance));
        } else {
            throw new IllegalArgumentException("Value did not follow a valid CIDR notation.");
        }
    }

    /**
     * Matches an IPv4 address x.x.x.x followed by a forward or backward slash and up to two digits (0-32)
     *
     * @return string matches pattern
     */
    public static boolean matchesCIDRNotation(final String string) {
        return PATTERN_CIDR.matcher(string).matches();
    }

    private void calculate(final IPv4Address address1, final IPv4Address address2) {
        final long decimal1 = address1.getDecimal(), decimal2 = address2.getDecimal();
        if (decimal1 < decimal2) {
            this.firstAddress = address1;
            this.lastAddress = address2;
        } else {
            this.firstAddress = address2;
            this.lastAddress = address1;
        }
        this.rangeNotation = firstAddress.getAddress() + "-" + lastAddress.getAddress();

        this.size = lastAddress.getDecimal() - firstAddress.getDecimal();
        this.validCIDR = size > 0 && ((size & (size - 1)) == 0); // distance is a power of two

        if (validCIDR) {
            this.cidrLength = 32 - (int) (Math.log10(size) / Math.log10(2)); // perform a log2() on size

            this.cidrNotation = firstAddress.getAddress() + "/" + cidrLength;
        }
    }

    public IPv4Address getFirstAddress() {
        return firstAddress;
    }

    public IPv4Address getLastAddress() {
        return lastAddress;
    }

    public boolean isValidCIDR() {
        return validCIDR;
    }

    public int getCidrLength() {
        return cidrLength;
    }

    public String getRangeNotation() {
        return rangeNotation;
    }

    public String getCidrNotation() {
        return cidrNotation;
    }

    public long getSize() {
        return size;
    }

    /**
     * @param address arbitrary address
     * @return whether address is in the block inclusive
     */
    public boolean contains(final IPv4Address address) {
        return address.getDecimal() >= firstAddress.getDecimal()
                && address.getDecimal() <= lastAddress.getDecimal();
    }


}
