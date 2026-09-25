package com.aiavatar.aibackend.storage;

public interface FileStorageService {

    String saveAudio(byte[] audioBytes, String fileName);

}