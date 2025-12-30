package com.anonchat.backend.controller;

import com.anonchat.backend.model.ChatMessage;
import com.anonchat.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
public class RoomController {

    private final ChatService chatService;
    private final SimpMessageSendingOperations messagingTemplate;

    // Claim Ownership (Called automatically when joining)
    @PostMapping("/{roomId}/claim")
    public ResponseEntity<Map<String, String>> claimRoom(@PathVariable String roomId) {
        String token = chatService.attemptToClaimRoom(roomId);

        if (token != null) {
            return ResponseEntity.ok(Map.of("role", "OWNER", "token", token));
        } else {
            return ResponseEntity.ok(Map.of("role", "GUEST"));
        }
    }

    // Delete Room (Requires Token)
    @DeleteMapping("/{roomId}")
    public ResponseEntity<String> deleteRoom(@PathVariable String roomId,
                                             @RequestHeader("X-Owner-Token") String token) {
        // Security Check
        if (!chatService.verifyOwner(roomId, token))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("🚫 Access Denied: You are not the owner.");

        chatService.deleteRoom(roomId);

        ChatMessage systemMsg = ChatMessage.builder()
                .type(ChatMessage.MessageType.LEAVE)
                .sender("System")
                .content("🚫 Room has been deleted by the owner.")
                .build();

        messagingTemplate.convertAndSend("/topic/" + roomId, systemMsg);


        chatService.deleteRoom(roomId);
        return ResponseEntity.ok("✅ Room deleted successfully.");
    }
}