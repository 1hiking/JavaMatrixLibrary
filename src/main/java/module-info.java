/// Base Client Module
module JavaMatrixClient {

    // Required for all our networking code
    requires java.net.http;

    // Required for JSON manipulation
    requires tools.jackson.core;
    requires tools.jackson.databind;
    requires java.sql;

    // Interfaces and facade
    exports org.hik.api;
    exports org.hik.context;

    // Records and Interfaces
    exports org.hik.payloads.roomstate;
    exports org.hik.payloads.roomevents;
    exports org.hik.responses;

    // Common exceptions
    exports org.hik.exceptions;

}