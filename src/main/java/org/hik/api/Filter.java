package org.hik.api;

import org.hik.api.filters.FilterDefinition;
import org.hik.api.identifiers.UserID;

/// Core interface for executing protocol operations for filtering.
///
/// All operations in this interface are blocking. Implementations must ensure
/// thread safety and avoid synchronization blocks that cause carrier thread pinning
/// during network I/O.
///
/// @see <a href="https://spec.matrix.org/v1.19/client-server-api/#filtering>Matrix Client-Server API Specification for Filters</a>
public interface Filter {

    /// @param userId
    /// @param filter
    /// @return
    String publishFilter(UserID userId, FilterDefinition filter);

    /// @param userId
    /// @param filterId
    /// @return
    FilterDefinition getFilter(UserID userId, String filterId);
}