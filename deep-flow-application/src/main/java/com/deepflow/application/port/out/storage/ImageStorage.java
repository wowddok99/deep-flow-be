package com.deepflow.application.port.out.storage;

public interface ImageStorage {
    String upload(String key, byte[] data, String contentType);
    void delete(String key);
}