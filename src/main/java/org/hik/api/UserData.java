package org.hik.api;

import org.hik.api.userdata.UsersFound;

/// Core interface for executing Matrix protocol operations against user data.
///
/// All operations in this interface are blocking. Implementations must ensure
/// thread safety and avoid synchronization blocks that cause carrier thread pinning
/// during network I/O.
///
/// @see
/// <a href="https://spec.matrix.org/v1.18/client-server-api/#user-data">Matrix Client-Server API Specification for User Data</a>
public interface UserData {

    UsersFound searchUsersByTerm(Integer limit, String searchTerm);

    void getUserProfile();

    void getUserProfileByProperty();

    void setUserProfileProperty();

    void deleteUserProfileProperty();

}
