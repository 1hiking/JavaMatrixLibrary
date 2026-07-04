package org.hik.services.userdata;

import org.hik.api.UserData;
import org.hik.context.ClientContext;
import org.hik.services.utils.ConfiguratedMapper;
import org.hik.services.utils.HttpTransport;
import tools.jackson.databind.ObjectMapper;

import java.util.Objects;

public class UserDataService implements UserData {

    private final ObjectMapper objectMapper = ConfiguratedMapper.getInstance();
    private final HttpTransport httpTransport = new HttpTransport();
    private final ClientContext client;

    public UserDataService(ClientContext context) {
        this.client = context;
    }

    @Override
    public void searchUsersByTerm(Integer limit, String searchTerm) throws InterruptedException {
        String searchTermToUse = Objects.requireNonNull(searchTerm, "A search term is required.");
        int limitToUse = Objects.requireNonNullElse(limit, 10);
        String rawTextPayload = """
                {"limit": "%d","search_term":"%s"}
                """.formatted(limitToUse, searchTerm);

    }

    @Override
    public void getUserProfile() throws InterruptedException {

    }

    @Override
    public void getUserProfileByProperty() throws InterruptedException {

    }

    @Override
    public void setUserProfileProperty() throws InterruptedException {

    }

    @Override
    public void deleteUserProfileProperty() throws InterruptedException {

    }
}
