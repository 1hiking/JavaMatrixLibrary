package io.github.hikingc.matrixsdk.api.rooms;

/// Utility record to create a filter
///
/// @param genericSearchTerm
/// @param roomTypes
public record RoomFilter(String genericSearchTerm, String roomTypes) {}
