package com.example.ttaraga.ttaraga.controller;

import com.example.ttaraga.ttaraga.service.settingCustomModel.CustomModel;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CustomModelController {
    private final CustomModel customModel;

    @GetMapping("/custom-model")
    public ResponseEntity<ObjectNode> getCustomModel() {
        ObjectNode model = customModel.generateCustomModelJson();
        return ResponseEntity.ok(model);
    }
}
