package org.hik.services.utils;

import org.hik.exceptions.ErrorResponse;
import org.hik.exceptions.MatrixIOException;
import org.hik.exceptions.MatrixNetworkException;
import tools.jackson.core.exc.StreamReadException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/// A [HttpTransport] is responsible for the construction of asynchronous [requests][HttpRequest], this class is
/// transparent
/// such that all methods require providing required datatypes for the payloads, such as with [`URI`][URI] and with
///  [HttpRequest.BodyPublisher]
public class HttpTransport {
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    private void validateResponse(int code, String body) {
        if (code >= 200 && code < 300) {
            return;
        }

        if (body.isBlank()) {
            throw new MatrixNetworkException("Server returned with unknown error");
        }

        ErrorResponse errorResponse;
        try {
            errorResponse = ConfiguratedMapper.getInstance().readValue(body, ErrorResponse.class);
        } catch (StreamReadException e) {
            throw new MatrixIOException("Server returned with malformed response", e);
        }

        throw new MatrixNetworkException("Server returned with error: " + errorResponse.error() + ", and code: " + errorResponse.errCode());
    }

    /// Sends a `GET` request to the given endpoint.
    ///
    /// @param path      the [URI] of the selected endpoint to query.
    /// @param authToken if supplied, the `Bearer` token.
    /// @return an unparsed JSON [String] when the operation is successful.
    /// @throws IllegalArgumentException if the path was not supplied
    public String getEvent(URI path, String authToken) {
        var builderRequest = HttpRequest.newBuilder()
                .uri(path)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .GET();

        if (authToken != null) {
            builderRequest.header(AUTHORIZATION, BEARER + authToken);
        }
        var request = builderRequest.build();


        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new MatrixIOException("There has been an I/O error attempting to process this request", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MatrixNetworkException("This request has been interrupted", e);
        }
        this.validateResponse(response.statusCode(), response.body());
        return response.body();

    }


    /// Sends a `POST` request to the given endpoint.
    ///
    /// @param path      the [URI] of the selected endpoint to query.
    /// @param body      the parsed JSON payload as a String
    /// @param authToken if supplied, the `Bearer` token.
    /// @return an unparsed JSON [String] when the operation is successful.
    /// @throws MatrixIOException      if an I/O error has occurred while sending the request
    /// @throws MatrixNetworkException if the path was not supplied
    public String postEvent(URI path, String body, String authToken) {
        var builderRequest = HttpRequest.newBuilder()
                .uri(path);

        if (body != null) {
            builderRequest.header(CONTENT_TYPE, APPLICATION_JSON);
        }

        builderRequest.POST(body != null ? HttpRequest.BodyPublishers.ofString(body) :
                HttpRequest.BodyPublishers.noBody());

        if (authToken != null) {
            builderRequest.header(AUTHORIZATION, BEARER + authToken);
        }
        var request = builderRequest.build();


        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new MatrixIOException("There has been an I/O error attempting to process this request", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MatrixNetworkException("This request has been interrupted", e);
        }
        this.validateResponse(response.statusCode(), response.body());
        return response.body();

    }


    /// Sends a `PUT` request to the given endpoint.
    ///
    /// @param path      the [URI] of the selected endpoint to query.
    /// @param body      the parsed JSON payload as a String
    /// @param authToken if supplied, the `Bearer` token.
    /// @return an unparsed JSON [String] when the operation is successful.
    /// @throws MatrixIOException        if an I/O error has occurred while sending the request
    /// @throws MatrixNetworkException   if the operation has been interrupted
    /// @throws IllegalArgumentException if the path was not supplied
    public String putEvent(URI path, String body, String authToken) {


        var builderRequest = HttpRequest.newBuilder()
                .uri(path)
                .header(AUTHORIZATION, BEARER + authToken)
                .header(CONTENT_TYPE, APPLICATION_JSON);

        builderRequest.PUT(body != null ? HttpRequest.BodyPublishers.ofString(body) :
                HttpRequest.BodyPublishers.noBody());
        var request = builderRequest.build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new MatrixIOException("There has been an I/O error attempting to process this request", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MatrixNetworkException("This request has been interrupted", e);
        }
        this.validateResponse(response.statusCode(), response.body());
        return response.body();
    }


    /// Sends a `PUT` request to the given endpoint.
    ///
    /// @param path      the [URI] of the selected endpoint to query.
    /// @param resource  a [Path] pointing to the resource to be uploaded.
    /// @param authToken if supplied, the `Bearer` token.
    /// @return an unparsed JSON [String] when the operation is successful.
    /// @throws MatrixIOException        if an I/O error has occurred while sending the request
    /// @throws MatrixNetworkException   if the operation has been interrupted
    /// @throws IllegalArgumentException if the path was not supplied
    public String putResource(URI path, Path resource, String authToken) {
        HttpRequest uploadRequest = null;
        try {
            uploadRequest = HttpRequest.newBuilder()
                    .uri(path)
                    .header(AUTHORIZATION, BEARER + authToken)
                    .header(CONTENT_TYPE, Files.probeContentType(resource))
                    .PUT(HttpRequest.BodyPublishers.ofFile(resource))
                    .build();
        } catch (IOException e) {
            throw new MatrixIOException("There has been an I/O error attempting to process this request", e);
        }

        HttpResponse<String> response = null;
        try {
            response = client.send(uploadRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new MatrixIOException("There has been an I/O error attempting to process this request", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MatrixNetworkException("This request has been interrupted", e);
        }
        this.validateResponse(response.statusCode(), response.body());
        return response.body();

    }


}
