package io.mattw.jports;

import java.io.Serializable;

public class IPv4AddressPort implements Serializable {

    private IPv4Address iPv4Address;
    private int port;

    private String fullAddress;

    /**
     * @param decimal any number
     * @param port    0-65535
     */
    public IPv4AddressPort(final long decimal, final int port) {
        this(new IPv4Address(decimal), port);
    }

    /**
     * @param iPv4Address x.x.x.x
     * @param port    0-65535
     */
    public IPv4AddressPort(final String iPv4Address, final int port) {
        this(new IPv4Address(iPv4Address), port);
    }

    /**
     * @param iPv4Address arbitrary address
     * @param port    0-65535
     */
    public IPv4AddressPort(final IPv4Address iPv4Address, final int port) {
        this.iPv4Address = iPv4Address;
        this.port = Math.abs(port) % 65536;

        this.fullAddress = this.iPv4Address.getAddress() + ":" + this.port;
    }

    public IPv4Address getiPv4Address() {
        return iPv4Address;
    }

    public int getPort() {
        return port;
    }

    public String getFullAddress() {
        return fullAddress;
    }

}
