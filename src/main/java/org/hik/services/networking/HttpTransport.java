package org.hik.services.networking;

import org.hik.exceptions.MatrixNetworkException;
import org.hik.responses.ErrorResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/// A [HttpTransport] is responsible for the construction of asynchronous [requests][HttpRequest], this class is transparent
/// such that all methods require providing required datatypes for the payloads, such as with [`URI`][URI] and with [HttpRequest.BodyPublisher]
public class HttpTransport {
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    private void validateHeaders(int code, String body) {
        if (code != 200) {
            ErrorResponse errorResponse = new ObjectMapper().readValue(body, ErrorResponse.class);
            throw new MatrixNetworkException("Error processing exception: " + errorResponse.error() + ", with code: " + errorResponse.errCode());

        }
    }

    /// Sends a `GET` request to the given endpoint.
    ///
    /// @param path      the [URI] of the selected endpoint to query.
    /// @param authToken if supplied, the `Bearer` token.
    /// @return an unparsed JSON [String] when the operation is successful.
    /// @throws IOException              if an I/O error has occurred while sending the request
    /// @throws InterruptedException     if the operation has been interrupted
    /// @throws IllegalArgumentException if the path was not supplied
    public String getJson(URI path, String authToken) throws IOException, InterruptedException {
        var builderRequest = HttpRequest.newBuilder()
                .uri(path)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .GET();

        if (authToken != null) {
            builderRequest.header(AUTHORIZATION, BEARER + authToken);
        }
        var request = builderRequest.build();


        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        this.validateHeaders(response.statusCode(), response.body());
        return response.body();

    }


    /// Sends a `POST` request to the given endpoint.
    ///
    /// @param path      the [URI] of the selected endpoint to query.
    /// @param body      the parsed JSON payload as a String
    /// @param authToken if supplied, the `Bearer` token.
    /// @return an unparsed JSON [String] when the operation is successful.
    /// @throws IOException              if an I/O error has occurred while sending the request
    /// @throws InterruptedException     if the operation has been interrupted
    /// @throws IllegalArgumentException if the path was not supplied
    public String postJson(URI path, String body, String authToken) throws IOException, InterruptedException {
        var builderRequest = HttpRequest.newBuilder()
                .uri(path);

        if (body != null) {
            builderRequest.header(CONTENT_TYPE, APPLICATION_JSON);
        }

        builderRequest.POST(body != null ? HttpRequest.BodyPublishers.ofString(body) : HttpRequest.BodyPublishers.noBody());

        if (authToken != null) {
            builderRequest.header(AUTHORIZATION, BEARER + authToken);
        }
        var request = builderRequest.build();


        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        this.validateHeaders(response.statusCode(), response.body());
        return response.body();

    }


    /// Sends a `PUT` request to the given endpoint.
    ///
    /// @param path      the [URI] of the selected endpoint to query.
    /// @param body      the parsed JSON payload as a String
    /// @param authToken if supplied, the `Bearer` token.
    /// @return an unparsed JSON [String] when the operation is successful.
    /// @throws IOException              if an I/O error has occurred while sending the request
    /// @throws InterruptedException     if the operation has been interrupted
    /// @throws IllegalArgumentException if the path was not supplied
    public String putJson(URI path, String body, String authToken) throws IOException, InterruptedException {


        var builderRequest = HttpRequest.newBuilder()
                .uri(path)
                .header(AUTHORIZATION, BEARER + authToken)
                .header(CONTENT_TYPE, APPLICATION_JSON);

        builderRequest.PUT(body != null ? HttpRequest.BodyPublishers.ofString(body) : HttpRequest.BodyPublishers.noBody());
        var request = builderRequest.build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        this.validateHeaders(response.statusCode(), response.body());
        return response.body();
    }


    /// Sends a `PUT` request to the given endpoint.
    ///
    /// @param path      the [URI] of the selected endpoint to query.
    /// @param resource  a [Path] pointing to the resource to be uploaded.
    /// @param authToken if supplied, the `Bearer` token.
    /// @return an unparsed JSON [String] when the operation is successful.
    /// @throws IOException              if an I/O error has occurred while sending the request
    /// @throws InterruptedException     if the operation has been interrupted
    /// @throws IllegalArgumentException if the path was not supplied
    public String putFile(URI path, Path resource, String authToken) throws IOException, InterruptedException {
        var uploadRequest = HttpRequest.newBuilder()
                .uri(path)
                .header(AUTHORIZATION, BEARER + authToken)
                .header(CONTENT_TYPE, Files.probeContentType(resource))
                .PUT(HttpRequest.BodyPublishers.ofFile(resource))
                .build();

        var response = client.send(uploadRequest, HttpResponse.BodyHandlers.ofString());
        this.validateHeaders(response.statusCode(), response.body());
        return response.body();


    }
}
