package com.anonchat.backend.controller;

import com.anonchat.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HistoryController {

    private final ChatService chatService;

    @GetMapping("/api/history/{roomId}")
    public ResponseEntity<List<Object>> getChatHistory(@PathVariable String roomId) {
        return ResponseEntity.ok(chatService.getHistory(roomId));
    }
}