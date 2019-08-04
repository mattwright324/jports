package io.mattw.jports;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Represents an IPv4 address for easy conversion between decimal and String variants.
 * <p>
 * Designed to be flexible to incorrect String representations by default,
 * such as when a segment overflows the 256 maximum value. It would be
 * converted to it's decimal representation and back to it's String equivalent.
 */
public class IPv4Address implements Serializable {

    private static final long UNSIGNED_MAX_INT = 4294967296L;
    private static final Pattern PATTERN_IPV4 = Pattern.compile("(\\d{1,3}\\.){3}\\d{1,3}");
    /**
     * Ordered in the position they would be used in an x.x.x.x String.split()
     */
    private static final double[] SEGMENT_MULTIPLIER = {Math.pow(256, 3), Math.pow(256, 2), 256, 1};

    private final String address;
    private final long decimal;

    /**
     * Interprets a standard 4-segment IPv4 address.
     * <p>
     * This constructor is the slowest of the two due to
     * String parsing in {@link #convertIPv4ToDecimal(String)}
     *
     * @param address x.x.x.x
     */
    public IPv4Address(final String address) {
        if (matchesIPv4Pattern(address)) {
            this.decimal = convertIPv4ToDecimal(address);
            this.address = convertDecimalToIPv4(this.decimal);
        } else {
            throw new IllegalArgumentException("Value did not follow a valid IPv4 format.");
        }
    }

    /**
     * Interprets a decimal value to IPv4 address.
     * <p>
     * This constructor is the significantly faster of the two.
     *
     * @param decimal any number
     */
    public IPv4Address(final long decimal) {
        this.decimal = decimal % UNSIGNED_MAX_INT;
        this.address = convertDecimalToIPv4(this.decimal);
    }

    /**
     * Determines if a string matches the expected 4-segmented digit IPv4 address.
     *
     * @return string matches pattern
     */
    public static boolean matchesIPv4Pattern(final String string) {
        return PATTERN_IPV4.matcher(string).matches();
    }

    /**
     * @param address an IPv4 address of pattern x.x.x.x
     * @return decimal equivalent of the address
     */
    public static long convertIPv4ToDecimal(final String address) {
        if (matchesIPv4Pattern(address)) {
            int i = 0;
            double decimal = 0L;
            for (final String segment : address.split("\\.")) {
                decimal += Integer.parseInt(segment) * SEGMENT_MULTIPLIER[i++];
            }

            return (long) decimal;
        } else {
            throw new IllegalArgumentException("Value did not follow a valid IPv4 format.");
        }
    }

    /**
     * @param decimal number representation of IPv4 address
     * @return IPv4 address of pattern x.x.x.x
     */
    public static String convertDecimalToIPv4(long decimal) {
        final int[] segments = new int[4];

        int i = 0;
        while (decimal > 0 && i < 4) {
            segments[i++] = (int) (decimal % 256);
            decimal /= 256;
        }

        return segments[3] + "." + segments[2] + "." + segments[1] + "." + segments[0];
    }

    public long getDecimal() {
        return this.decimal;
    }

    public String getAddress() {
        return this.address;
    }

    public IPv4Address traverse(final long distance) {
        return new IPv4Address(this.decimal + distance);
    }

    public IPv4Address nextAddress() {
        return traverse(1);
    }

    public IPv4Address prevAddress() {
        return traverse(-1);
    }

}
