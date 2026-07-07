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
        String responseBody = httpTransport.postEvent(
                URI.create(context.discoveryResponse().homeserver().baseUrl() + USER_FILTER_ENDPOINT + userId +
                        "/filter"),
                serializedInputData,
                context.credentials().token());

        return Mapper.getStringFromSingleObject(responseBody, "filter_id");
    }

    @Override
    public FilterDefinition getFilter(String userId, String filterId) {
        return null;
    }
}
