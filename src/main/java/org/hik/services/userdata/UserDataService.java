package org.hik.services.userdata;

import org.hik.api.UserData;
import org.hik.api.userdata.UserProfile;
import org.hik.api.userdata.UsersFound;
import org.hik.context.ClientContext;
import org.hik.exceptions.MatrixIOException;
import org.hik.services.utils.HttpTransport;
import org.hik.services.utils.Mapper;
import org.hik.services.utils.Validator;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

/// Main service implementation class of the UserData interface, providing all the required endpoints and records to
/// perform activities such as the creation, modification, deletion and retrieval of profile data, and also for the
/// query and search of users.
public class UserDataService implements UserData {

    public static final String USER_DIR = "/_matrix/client/v3/user_directory/search";
    private static final String PROFILE_DIR = "/_matrix/client/v3/profile/";
    private final ObjectMapper objectMapper = Mapper.getInstance();
    private final HttpTransport httpTransport = new HttpTransport(10);
    private final ClientContext context;

    public UserDataService(ClientContext context) {
        this.context = context;
    }

    @Override
    public UsersFound searchUsersByTerm(Integer limit, String searchTerm) {
        String searchTermToUse = Validator.notNull(searchTerm, "Search term");
        int limitToUse = Objects.requireNonNullElse(limit, 10);
        String rawTextPayload = """
                {"limit": "%d","search_term":"%s"}
                """.formatted(limitToUse, searchTermToUse);

        String responseBody = httpTransport.postEvent(URI.create(context.discoveryResponse().homeserver().baseUrl() + USER_DIR),
                rawTextPayload, context.credentials().token());
        try {
            return objectMapper.readValue(responseBody, UsersFound.class);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public UserProfile getUserProfile(String userId) {
        userId = Validator.userId(userId);

        String responseBody = httpTransport.getEvent(
                URI.create(context.discoveryResponse().homeserver().baseUrl() + PROFILE_DIR + userId),
                context.credentials().token());
        try {
            return objectMapper.readValue(responseBody, UserProfile.class);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }

    }

    @Override
    public String getUserProfileByProperty(String userId, String keyName) {
        userId = Validator.roomId(userId);
        keyName = Validator.notNull(keyName, "The key name");
        String responseBody = httpTransport.getEvent(
                URI.create(context.discoveryResponse().homeserver().baseUrl() + PROFILE_DIR + userId + "/" + keyName),
                context.credentials().token());

        return Mapper.getStringFromSingleObject(responseBody, keyName);
    }

    @Override
    public void setUserProfileProperty(String userId, String keyName, String valueName) {
        userId = Validator.roomId(userId);
        keyName = Validator.notNull(keyName, "The key name");
        valueName = Validator.notNull(valueName, "The value name");
        var serializedJson = Mapper.createObjectFromMap(Map.ofEntries(Map.entry(keyName, valueName)));

        httpTransport.putEvent(
                URI.create(context.discoveryResponse().homeserver().baseUrl() + PROFILE_DIR + userId + "/" + keyName),
                serializedJson,
                context.credentials().token());
    }

    @Override
    public void deleteUserProfileProperty(String userId, String keyName) {
        userId = Validator.roomId(userId);
        keyName = Validator.notNull(keyName, "The key name");

        httpTransport.deleteEvent(
                URI.create(context.discoveryResponse().homeserver().baseUrl() + PROFILE_DIR + userId + "/" + keyName),
                context.credentials().token());
    }
}
