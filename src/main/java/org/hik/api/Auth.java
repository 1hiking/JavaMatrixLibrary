package org.hik.api;

import org.hik.api.auth.AuthMetadata;
import org.hik.api.auth.WhoAmI;

public interface Auth {

    AuthMetadata getAuthMetadata();

    WhoAmI getCurrentAccountInformation();

}
