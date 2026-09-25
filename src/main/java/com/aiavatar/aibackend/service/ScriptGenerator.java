package com.aiavatar.aibackend.service;

import com.aiavatar.aibackend.dto.VideoScriptResponse;

public interface ScriptGenerator {

    String generateScript(String prompt);
    VideoScriptResponse generateVideoScript(String prompt);
}