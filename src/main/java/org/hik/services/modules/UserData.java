package org.hik.services.modules;

import org.hik.context.ClientContext;
import org.hik.services.networking.HttpTransport;
import tools.jackson.databind.ObjectMapper;

import java.util.Objects;

public class UserData {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpTransport httpTransport = new HttpTransport();
    private final ClientContext client;

    public UserData(ClientContext context) {
        this.client = context;
    }

    public void searchUsersByTerm(Integer limit, String searchTerm) throws InterruptedException {
        String searchTermToUse = Objects.requireNonNull(searchTerm, "A search term is required.");
        int limitToUse = Objects.requireNonNullElse(limit, 10);
        String rawTextPayload = "{\"limit\": \"%d\",\"search_term\":\"%s\"}\n".formatted(limitToUse, searchTerm);

    }
}
