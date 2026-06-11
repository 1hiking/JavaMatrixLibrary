# Matrix client library for Java

This is a Java client-side library to interact with the Matrix protocol.

Some of the aims of this project are:

- Maintain low quantity of dependencies
- Leverage modern Java features to decrease code complexity
- Maintain the client asynchronous

Current features:

- Post text messages (unformatted, no rich text yet)
- Post images, files, audio content
- Human readable error messages
- Basic presence with /messages payload

### Usage:

```java
MatrixClient matrixClient = new MatrixClient("https://matrix.org", "example", "authTokenGoesHere");
```

Now you are able to make use of all the features, the following above is an example in which we leverage the
asynchronous api:

```java
        var image = matrixClient.publishRoomMessage(Path.of("/path/to/image.jpg"), roomId, EventType.IMAGE);
var file = matrixClient.publishRoomMessage(Path.of("/path/to/file.txt"), roomId, EventType.FILE);
var audio = matrixClient.publishRoomMessage(Path.of("/path/to/song.mp3"), roomId, EventType.AUDIO);

CompletableFuture<List<String>> posts = CompletableFuture.allOf(image, file, audio)
        .thenApply(v -> {
            List<String> list = new ArrayList<>();
            list.add(image.join());
            list.add(file.join());
            list.add(audio.join());
            return list;
        });
        logger.

info(String.valueOf(posts.join()));
```