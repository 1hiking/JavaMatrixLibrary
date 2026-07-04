> [!CAUTION]
> This project is in very early stages, use at your own risk.

# A Matrix client library for Java

This is a Java client-side library to interact with the Matrix protocol.

Some of the aims of this project are:

- Maintain low quantity of external dependencies.
- Leverage modern Java features to improve developer experience.
- Maintain the client simple, allowing consumers to process their own data, in return the library safely returns
  serialized immutable data.

## Feature table:

| Service                                             | Supported   |
|-----------------------------------------------------|-------------|
| Rooms (banning, kicking, room summary, room search) | Yes         |
| Events (sending posts, reading from rooms)          | Partially   |
| User Data                                           | In progress |

### Installation:

Declare the library in your pom

### Testing

- Testing is done via the library `Wiremocks` and `JUnit 5`.
- Each module has their own test file. All tests must pass.