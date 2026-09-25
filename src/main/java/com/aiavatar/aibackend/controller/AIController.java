package com.aiavatar.aibackend.controller;

import com.aiavatar.aibackend.service.ScriptGenerator;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final ScriptGenerator scriptGenerator;

    public AIController(ScriptGenerator scriptGenerator) {
        this.scriptGenerator = scriptGenerator;
    }

    @PostMapping("/test-script")
    public ResponseEntity<String> testScript(
            @RequestBody String prompt) {

        String generatedScript =
                scriptGenerator.generateScript(prompt);

        return ResponseEntity.ok(generatedScript);
    }
}