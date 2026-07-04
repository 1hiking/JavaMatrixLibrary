package org.hik.services.userdata;

import org.hik.api.UserData;
import org.hik.api.userdata.UsersFound;
import org.hik.context.ClientContext;
import org.hik.services.utils.ConfigurationMapper;
import org.hik.services.utils.HttpTransport;
import tools.jackson.databind.ObjectMapper;

import java.util.Objects;

public class UserDataService implements UserData {

    private final ObjectMapper objectMapper = ConfigurationMapper.getInstance();
    private final HttpTransport httpTransport = new HttpTransport();
    private final ClientContext context;

    public UserDataService(ClientContext context) {
        this.context = context;
    }

    @Override
    public UsersFound searchUsersByTerm(Integer limit, String searchTerm) {
        String searchTermToUse = Objects.requireNonNull(searchTerm, "A search term is required.");
        int limitToUse = Objects.requireNonNullElse(limit, 10);
        String rawTextPayload = """
                {"limit": "%d","search_term":"%s"}
                """.formatted(limitToUse, searchTerm);
        return null;
    }

    @Override
    public void getUserProfile() {

    }

    @Override
    public void getUserProfileByProperty() {

    }

    @Override
    public void setUserProfileProperty() {

    }

    @Override
    public void deleteUserProfileProperty() {

    }
}
