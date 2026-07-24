package org.hik.api;

import org.hik.api.identifiers.UserID;
import org.hik.api.userdata.UserProfile;
import org.hik.api.userdata.UsersFound;

/// Core interface for executing Matrix protocol operations against user data.
///
/// All operations in this interface are blocking. Implementations must ensure
/// thread safety and avoid synchronization blocks that cause carrier thread pinning
/// during network I/O.
///
/// @see <a href="https://spec.matrix.org/v1.18/client-server-api/#user-data">Matrix Client-Server API Specification for User Data</a>
public interface UserData {

    /// Perform a case-insensitive search of users based on a `search term`. Only users that are visible to the caller
    /// will be included in the search (are in `public` rooms with the caller, or `world_readable` for example).
    ///
    /// The search is **not collated to any language type**.
    ///
    /// @param limit the maximum number of results.
    /// @param searchTerm the term to search for.
    /// @return all the [UsersFound] by the server.
    UsersFound searchUsersByTerm(Integer limit, String searchTerm);

    /// Get the profile of a user
    ///
    /// @param userId the [UserID] to profile.
    /// @return the corresponding [UserProfile].
    UserProfile getUserProfile(UserID userId);

    /// Get the value of a profile field for a user
    ///
    /// @param userId the [UserID] to profile.
    /// @param keyName a property field
    /// @return the value of the key property.
    String getUserProfileByProperty(UserID userId, String keyName); // only 1 property allowed so no Map

    /// Set or update a profile field for a user.
    ///
    /// @param userId the [UserID] that'll receive the K-V.
    /// @param keyName the key to insert in the profile.
    /// @param valueName the value for the key.
    void setUserProfileProperty(UserID userId, String keyName, String valueName);

    /// Remove a specific field from a user’s profile.
    ///
    /// @param userId the [UserID] that'll have his K-V deleted.
    /// @param keyName the key to be deleted.
    void deleteUserProfileProperty(UserID userId, String keyName);

}
