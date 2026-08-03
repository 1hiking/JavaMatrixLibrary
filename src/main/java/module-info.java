/// Base Client Module
module io.github.hikingc.matrixsdk {


    // Required for sending payloads
    requires java.net.http;

    // Required to create a callback server for OAuth
    requires io.fusionauth.http;
    requires java.desktop;

    // Required for JSON manipulation
    requires tools.jackson.core;
    requires tools.jackson.databind;

    // Logging
    requires org.slf4j;

    // Jspecify
    requires org.jspecify;

    // Exposed interfaces, facade and user classes
    exports io.github.hikingc.matrixsdk.api;
    exports io.github.hikingc.matrixsdk.api.identifiers;
    exports io.github.hikingc.matrixsdk.context;

    // Records and Interfaces
    exports io.github.hikingc.matrixsdk.api.rooms;
    exports io.github.hikingc.matrixsdk.api.events;
    exports io.github.hikingc.matrixsdk.api.events.types;
    exports io.github.hikingc.matrixsdk.api.userdata;
    exports io.github.hikingc.matrixsdk.api.auth;
    exports io.github.hikingc.matrixsdk.api.filters;

    // Exceptions
    exports io.github.hikingc.matrixsdk.exceptions;
}