package org.hik.services.userdata;

import org.hik.api.UserData;
import org.hik.api.identifiers.UserID;
import org.hik.api.userdata.UserProfile;
import org.hik.api.userdata.UsersFound;
import org.hik.context.ClientContext;
import org.hik.services.utils.HttpTransport;
import org.hik.services.utils.Mapper;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

/// Main service implementation class of the UserData interface, providing all the required endpoints and records to
/// perform activities such as the creation, modification, deletion and retrieval of profile data, and also for the
/// query and search of users.
public class UserDataService implements UserData {

    /// Common endpoint for directory operations.
    public static final String USER_DIR = "/_matrix/client/v3/user_directory/search";
    private static final String PROFILE_DIR = "/_matrix/client/v3/profile/";
    private final HttpTransport httpTransport = new HttpTransport(10);
    private final ClientContext context;

    /// Service constructor to operate
    ///
    /// @param context the [ClientContext] of the facade
    public UserDataService(ClientContext context) {
        this.context = context;
    }

    @Override
    public UsersFound searchUsersByTerm(Integer limit, String searchTerm) {
        Objects.requireNonNull(searchTerm, "The search term must no be null");
        int limitToUse = Objects.requireNonNullElse(limit, 10);
        String rawTextPayload = """
                {"limit": "%d","search_term":"%s"}
                """.formatted(limitToUse, searchTerm);

        String responseBody =
                httpTransport.postEvent(URI.create(context.discoveryResponse().homeserver().baseUrl() + USER_DIR),
                        rawTextPayload, context.token());
        return Mapper.getObjectFromString(responseBody, UsersFound.class);
    }

    @Override
    public UserProfile getUserProfile(UserID userId) {
        String responseBody = httpTransport.getEvent(
                URI.create(context.discoveryResponse().homeserver().baseUrl() + PROFILE_DIR + userId),
                context.token());
        return Mapper.getObjectFromString(responseBody, UserProfile.class);


    }

    @Override
    public String getUserProfileByProperty(UserID userId, String keyName) {
        Objects.requireNonNull(keyName, "The key name must no be null");
        String responseBody = httpTransport.getEvent(
                URI.create(context.discoveryResponse().homeserver().baseUrl() + PROFILE_DIR + userId + "/" + keyName),
                context.token());

        return Mapper.getStringFromSingleObject(responseBody, keyName);
    }

    @Override
    public void setUserProfileProperty(UserID userId, String keyName, String valueName) {
        Objects.requireNonNull(keyName, "The key name must no be null");
        Objects.requireNonNull(valueName, "The value name must no be null");
        var serializedJson = Mapper.createObjectFromMap(Map.ofEntries(Map.entry(keyName, valueName)));

        httpTransport.putEvent(
                URI.create(context.discoveryResponse().homeserver().baseUrl() + PROFILE_DIR + userId + "/" + keyName),
                serializedJson,
                context.token());
    }

    @Override
    public void deleteUserProfileProperty(UserID userId, String keyName) {
        Objects.requireNonNull(keyName, "The key name must no be null");

        httpTransport.deleteEvent(
                URI.create(context.discoveryResponse().homeserver().baseUrl() + PROFILE_DIR + userId + "/" + keyName),
                context.token());
    }
}
