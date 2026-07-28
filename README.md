> [!CAUTION]
> This project is in early development. APIs may change without notice — use at your own risk.

# A Matrix Client SDK for Java

A client-side Java library for the [Matrix](https://matrix.org) protocol.

## Purpose

This library aims to allow authors to build any kind of Matrix program such as a bot or a desktop client, the library
handles all matters of serialization, validation and authentication.

## Quick start

```java

import io.github.hikingc.matrixsdk.api.MatrixAuth;
import io.github.hikingc.matrixsdk.api.MatrixClient;
import io.github.hikingc.matrixsdk.api.auth.TokenMetadata;
import io.github.hikingc.matrixsdk.context.DiscoveryResponse;

MatrixAuth auth = new MatrixAuth(URI.create("https://example.org")); // Set the URI of your Matrix server.
DiscoveryResponse response = auth.fetchWellKnown(); // Fetch well known for future operations

TokenMetadata token = auth.login(CLIENT_NAME, PORT, DEVICE_ID); // Set configuration parameters
// After doing the login with the server in your browser, you will be returned the tokens...
MatrixClient client = MatrixClient.create(response, token.accessToken()); // Use any service you want!

```

## Feature support

| Service   | Capabilities                                   | Status    |
|-----------|------------------------------------------------|-----------|
| Rooms     | Banning, kicking, room summary, room search    | Supported |
| Events    | Sending messages, reading room events, `/sync` | Partial   |
| User Data | Profile search, profile modification           | Supported |
| Filtering | Creating and retrieving filters                | Supported |
| Auth      | OpenIDConnect handshake                        | Supported |

## Installation

(None currently!)

### Requirements

- Java 23+

## Testing

- Tests use [WireMock](https://wiremock.org/) for HTTP stubbing and JUnit 5 as the test framework.
- Each service component has its own test file; all tests must pass to ensure the library works as intended.

To run the test suite:

```bash
./mvnw test
```
> [!NOTE]
> When using IntelliJ, JUnit might throw `IllegalAccessError`, 
> fix this by either checking "Do not use --module-path option" or configuring Maven as the
> test runner.

## Dependencies

- [Jackson](https://github.com/FasterXML/jackson) required for JSON handling.
- [java-http](https://github.com/FusionAuth/java-http) required for creating a small server for callbacks.

## License

See [LICENSE](/LICENSE)