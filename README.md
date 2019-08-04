# jports
Flexible, fast, multithreaded address and port scanning library for Java.

## Features
- Java 8
- Flexible inputs to accept a variety of formats
  - Single address
  - Multiple addresses
  - Address range with start and end
  - Address range with CIDR notation
- Endless scanning
  - With a given address, continuously increase or decrease, stopping when you want it to.
- Check for open port(s)
- Multithreaded, specify thread count

## Sample(s)

#### Constructors
Examples of creating addresses and address blocks
```java
new IPv4Address(3232235521L); // 192.168.0.1
new IPv4Address("192.168.0.1");
new IPv4Address("192.168.0.260"); // 192.168.1.4
new IPv4Address("999.999.999.999"); // 234.234.234.231

new IPv4AddressBlock("192.168.0.0", "192.168.1.255");
new IPv4AddressBlock(new IPv4Address("192.168.0.0"), new IPv4Address("192.168.1.255"));
new IPv4AddressBlock("192.168.0.0/24");
new IPv4AddressBlock("192.168.0.0", 24);
new IPv4AddressBlock(new IPv4Address("192.168.0.0"), 24);

// Scan through just IP addresses
new IPv4BlockScan(new IPv4AddressBlock("192.168.0.0/24"));
new IPv4BlockScan(new IPv4Address("192.168.0.0"), ScanMethod.SINGLE_ADDRESS);
new IPv4BlockScan(new IPv4Address("192.168.0.0"), ScanMethod.ENDLESS_INCREASE);
new IPv4BlockScan(new IPv4Address("192.168.0.0"), ScanMethod.ENDLESS_DECREASE);
new IPv4BlockScan(Arrays.asList(new IPv4Address("192.168.1.1"), new IPv4Address("192.168.1.145"))));

// Scan through IP addresses and ports
new IPv4BlockPortScan(new IPv4AddressBlock("192.168.0.0/24"));
new IPv4BlockPortScan(new IPv4Address("192.168.0.0"), ScanMethod.SINGLE_ADDRESS);
new IPv4BlockPortScan(new IPv4Address("192.168.0.0"), ScanMethod.ENDLESS_INCREASE);
new IPv4BlockPortScan(new IPv4Address("192.168.0.0"), ScanMethod.ENDLESS_DECREASE);
new IPv4BlockPortScan(Arrays.asList(new IPv4Address("192.168.1.1"), new IPv4Address("192.168.1.145"))));
```

#### Traversing IP blocks

Can manually cycle through a block of addresses.
```java
IPv4AddressBlock block = new IPv4AddressBlock("192.168.0.0", "192.168.1.255");
IPv4Address address = block.getFirstAddress();

do {
    System.out.println(address.getAddress());

    address = address.nextAddress();
} while (address.getDecimal() < block.getLastAddress().getDecimal());
```

Or use the address scanner to do so in a faster, multithreaded context.
```java
IPv4AddressBlock addressBlock = new IPv4AddressBlock("192.168.0.0", "192.168.1.255");

IPv4BlockScan blockScan = new IPv4BlockScan(addressBlock)
        .setThreadCount(64)
        .setConsumingMethod(YourApplication::consumingMethod)
        .executeAndAwait();
```

#### Port scanner example
In this example, we are creating a range of the most commonly expected local address range
and have a list of the most common ports used for web pages.

We initiate the scan and consume the found results, expecting these to be web pages.
Using the Jsoup library, we attempt to grab the page and print out the URL and page title.

```java
public class LocalScanner {
    
    private IPv4AddressBlock localBlock = new IPv4AddressBlock("192.168.0.0", "192.168.1.255");
    private List<Integer> ports = Arrays.asList(80, 81, 443, 8080, 8000);

    public static void main(String[] args) {
        LocalScanner scanner = new LocalScanner();
        scanner.startScan();
    }   

    public void startScan() {
        IPv4BlockPortScan portScan = new IPv4BlockPortScan(localBlock)
                .setPorts(ports)
                .setThreadCount(64)
                .setCheckPorts(true) // default true, false to consume all address:port combinations
                .setCheckTimeout(150)
                .setConsumingMethod(this::consumingMethod)
                .executeAndAwait();
    }

    public void consumingMethod(IPv4AddressPort addressPort) {
        // further process, store in database, display on screen, etc.
        String expectedWebpage;
        if(addressPort.getPort() == 80) {
            expectedWebpage = "http://" + addressPort.getAddress().getAddress();
        } else if(addressPort.getPort() == 443) {
            expectedWebpage = "https://" + addressPort.getAddress().getAddress();
        } else {
            expectedWebpage = "http://" + addressPort.getFullAddress();
        }

        try {
            Document document = Jsoup.connect(expectedWebpage).get();

            System.out.printf("%s    size=%s    title=%s\n", expectedWebpage, document.html().length(), document.title());
        } catch (IOException e) {
            System.err.printf("%s    %s\n", expectedWebpage, e.getClass().getSimpleName()+": "+e.getLocalizedMessage());
        }
    }

}
```
    
