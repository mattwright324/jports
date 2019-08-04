package io.mattw.jports;

public enum ScanMethod {
    SINGLE_ADDRESS,    // x.x.x.x
    MULTI_ADDRESS,     // x.x.x.x,x.x.x.y
    RANGE_ADDRESS,     // x.x.x.x/16 or x.x.x.x-x.y.y.y
    ENDLESS_INCREASE,  // x.x.x.x
    ENDLESS_DECREASE   // x.x.x.x
}
