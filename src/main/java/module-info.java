/// Base Client Module
module JavaMatrixClient {

    // Required for all our http code
    requires java.net.http;

    // Required for serialization
    requires com.fasterxml.jackson.annotation;
    requires tools.jackson.core;
    requires tools.jackson.databind;

    // Required for MediaProcessor, might be deleted
    requires java.desktop;

    exports org.hik.api;
    exports org.hik.payloads.roomstate;
    exports org.hik.payloads.roomevents;
    exports org.hik.responses;
    exports org.hik.exceptions;
}