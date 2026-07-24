package org.hik.api.filters;

import java.util.List;

public record EventFilter(Integer limit,
                          List<String> notSenders,
                          List<String> notTypes,
                          List<String> senders,
                          List<String> types) {
}
