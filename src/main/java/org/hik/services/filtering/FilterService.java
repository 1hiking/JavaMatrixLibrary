package org.hik.services.filtering;

import org.hik.api.Filter;
import org.hik.api.filters.FilterDefinition;
import org.hik.api.identifiers.UserID;
import org.hik.context.ClientContext;
import org.hik.services.utils.HttpTransport;
import org.hik.services.utils.Mapper;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.Objects;

/// Main service implementation class of the Filter interface, providing the ability to create and query filters.
public class FilterService implements Filter {
    private static final String USER_FILTER_ENDPOINT = "/_matrix/client/v3/user/";

    private final ObjectMapper objectMapper = Mapper.getInstance();
    private final HttpTransport httpTransport = new HttpTransport(10);
    private final ClientContext context;

    /// Service constructor to operate.
    ///
    /// @param context the [ClientContext] of the facade
    public FilterService(ClientContext context) {
        this.context = context;
    }

    @Override
    public String publishFilter(UserID userId, FilterDefinition filter) {
        var serializedInputData = objectMapper.writeValueAsString(filter);
        URI uri = httpTransport.generateEncodedURI(context.discoveryResponse().homeserver().baseUrl(),
                USER_FILTER_ENDPOINT + userId + "/filter", null);
        String responseBody = httpTransport.postEvent(
                uri,
                serializedInputData,
                context.token());

        return Mapper.getStringFromSingleObject(responseBody, "filter_id");
    }

    @Override
    public FilterDefinition getFilter(UserID userId, String filterId) {
        Objects.requireNonNull(filterId, "Filter ID must not be null");
        URI uri = httpTransport.generateEncodedURI(context.discoveryResponse().homeserver().baseUrl(),
                USER_FILTER_ENDPOINT + userId + "/filter/" + filterId, null);
        return Mapper.getObjectFromString(httpTransport.getEvent(uri, context.token()),
                FilterDefinition.class);
    }
}
