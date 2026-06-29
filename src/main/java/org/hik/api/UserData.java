package org.hik.api;

public interface UserData {
    /// @param limit
    /// @param searchTerm
    /// @throws InterruptedException
    void searchUsersByTerm(Integer limit, String searchTerm) throws InterruptedException;
}
