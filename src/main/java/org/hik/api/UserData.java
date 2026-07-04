package org.hik.api;

/// Core interface for executing Matrix protocol operations against user data.
///
/// All operations in this interface are blocking. Implementations must ensure
/// thread safety and avoid synchronization blocks that cause carrier thread pinning
/// during network I/O.
///
/// @see
/// <a href="https://spec.matrix.org/v1.18/client-server-api/#user-data">Matrix Client-Server API Specification for User Data</a>
public interface UserData {

    void searchUsersByTerm(Integer limit, String searchTerm) throws InterruptedException;

    void getUserProfile() throws InterruptedException;

    void getUserProfileByProperty() throws InterruptedException;

    void setUserProfileProperty() throws InterruptedException;

    void deleteUserProfileProperty() throws InterruptedException;

}
