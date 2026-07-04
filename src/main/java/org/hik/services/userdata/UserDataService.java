package org.hik.services.userdata;

import org.hik.api.UserData;
import org.hik.api.userdata.UsersFound;
import org.hik.context.ClientContext;
import org.hik.services.utils.ConfigurationMapper;
import org.hik.services.utils.HttpTransport;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.Objects;

public class UserDataService implements UserData {

    public static final String USER_DIR = "/_matrix/client/v3/user_directory/search";
    private final ObjectMapper objectMapper = ConfigurationMapper.getInstance();
    private final HttpTransport httpTransport = new HttpTransport(10);
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

        httpTransport.postEvent(URI.create(USER_DIR),rawTextPayload,context.credentials().token());
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
