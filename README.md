> [!CAUTION]
> This project is in early development. APIs may change without notice — use at your own risk.

# A Matrix Client Library for Java

A client-side Java library for the [Matrix](https://matrix.org) protocol. Use it to build bots,
user-facing clients, scripts, or any application that needs to talk to a Matrix homeserver.

## Project goals

- This library aims to maintain a low amount of external dependencies.
- Leverage modern Java language features to improve developer experience.
- The library returns immutable, serialized data and callers decide how to store, cache, or process it.

## Quick start

```java

MatrixClient client = MatrixClient.create("https://matrix.example.org", userId, accessToken);


// Now you can do any operation!, as an example:
ResolvedAlias roomId = client.room().resolveAlias("#general:example.org");
```

## Feature support

| Service   | Capabilities                                   | Status      |
|-----------|------------------------------------------------|-------------|
| Rooms     | Banning, kicking, room summary, room search    | ✅ Supported |
| Events    | Sending messages, reading room events, `/sync` | 🟡 Partial  |
| User Data | Profile search, profile modification           | ✅ Supported |
| Filtering | Creating and retrieving filters                | 🟡 Partial  |

## Installation

(None currently!)

### Requirements

- Java 23+

## Testing

- Tests use [WireMock](https://wiremock.org/) for HTTP stubbing and JUnit 5 as the test framework.
- Each module has its own test file; all tests must pass before merging.

To run the test suite:

```bash
./mvnw test
```

## Dependencies

- [Jackson](https://github.com/FasterXML/jackson) 3.x — JSON serialization/deserialization

## License

None for now