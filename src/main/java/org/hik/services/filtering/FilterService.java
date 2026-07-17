package org.hik.services.filtering;

import org.hik.api.Filter;
import org.hik.api.filtering.FilterDefinition;
import org.hik.context.ClientContext;
import org.hik.services.utils.HttpTransport;
import org.hik.services.utils.Mapper;
import org.hik.services.utils.Validator;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;

public class FilterService implements Filter {
    private static final String USER_FILTER_ENDPOINT = "/_matrix/client/v3/user/";

    private final ObjectMapper objectMapper = Mapper.getInstance();
    private final HttpTransport httpTransport = new HttpTransport(10);
    private final ClientContext context;

    public FilterService(ClientContext context) {
        this.context = context;
    }

    @Override
    public String publishFilter(String userId, FilterDefinition filter) {
        userId = Validator.userId(userId);
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
    public FilterDefinition getFilter(String userId, String filterId) {
        userId = Validator.userId(userId);
        filterId = Validator.notNull(filterId, "The filter ID");
        URI uri = httpTransport.generateEncodedURI(context.discoveryResponse().homeserver().baseUrl(),
                USER_FILTER_ENDPOINT + userId + "/filter/" + filterId, null);
        return Mapper.getObjectFromString(httpTransport.getEvent(uri, context.token()),
                FilterDefinition.class);
    }
}
