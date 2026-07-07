package org.hik.api;

import org.hik.api.filtering.FilterDefinition;

public interface Filter {

    String publishFilter(String userId, FilterDefinition filter);

    FilterDefinition getFilter(String userId, String filterId);
}