package io.mattw.jports;

import java.io.Serializable;

public class IPv4AddressPort implements Serializable {

    private IPv4Address address;
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
     * @param address x.x.x.x
     * @param port    0-65535
     */
    public IPv4AddressPort(final String address, final int port) {
        this(new IPv4Address(address), port);
    }

    /**
     * @param address arbitrary address
     * @param port    0-65535
     */
    public IPv4AddressPort(final IPv4Address address, final int port) {
        this.address = address;
        this.port = Math.abs(port) % 65536;

        this.fullAddress = this.address.getAddress() + ":" + this.port;
    }

    public IPv4Address getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getFullAddress() {
        return fullAddress;
    }

}
