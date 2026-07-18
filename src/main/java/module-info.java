/// Base Client Module
module JavaMatrixClient {

    // Required for all our networking code
    requires java.net.http;

    // Required for JSON manipulation
    requires tools.jackson.core;
    requires tools.jackson.databind;
    requires java.desktop;
    requires jdk.httpserver;
    requires org.slf4j;

    // Interfaces and facade
    exports org.hik.api;
    exports org.hik.context;

    // Records and Interfaces
    exports org.hik.api.rooms;
    exports org.hik.api.events;
    exports org.hik.api.userdata;
    exports org.hik.api.auth;
    exports org.hik.api.filtering;

    // Common exceptions
    exports org.hik.exceptions;


}