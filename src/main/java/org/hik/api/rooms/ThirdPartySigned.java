package org.hik.api.rooms;

import java.util.Map;

public record ThirdPartySigned(String mxid,
                               String sender,
                               Map<String, Map<String, String>> signatures,
                               String token) {}
