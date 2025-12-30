package com.anonchat.backend.service;

import com.anonchat.backend.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final RedisTemplate<String, Object> redisTemplate;

    // CONSTANT: Maximum messages to keep in memory per room
    private static final int HISTORY_LIMIT = 1000;

    public void saveMessage(String roomId, ChatMessage message) {
        String key = "room:" + roomId;
        redisTemplate.opsForList().rightPush(key, message);

        // If size > 1000, remove the 'left' (oldest) item.
        redisTemplate.opsForList().trim(key, -HISTORY_LIMIT, -1);

        // TTL: 1 hour
        redisTemplate.expire(key, 1, TimeUnit.HOURS);
    }

    public List<Object> getHistory(String roomId) {
        String key = "room:" + roomId;

        return redisTemplate.opsForList().range(key, 0, -1);
    }

    public void deleteRoom(String roomId) {
        // Delete the Message History
        redisTemplate.delete("room:" + roomId);

        // Delete the Owner Key too!
        // This allows the next person who joins to claim ownership.
        redisTemplate.delete("room:" + roomId + ":owner");
    }

    public String attemptToClaimRoom(String roomId) {
        String key = "room:" + roomId + ":owner";
        String token = java.util.UUID.randomUUID().toString();

        Boolean isNewOwner = redisTemplate.opsForValue()
                .setIfAbsent(key, token, java.time.Duration.ofHours(1));

        if (Boolean.TRUE.equals(isNewOwner))
            return token;
        else
            return null;
    }

    public boolean verifyOwner(String roomId, String token) {
        String key = "room:" + roomId + ":owner";
        String storedToken = (String) redisTemplate.opsForValue().get(key);
        return storedToken != null && storedToken.equals(token);
    }

    public void userJoined(String roomId) {
        redisTemplate.opsForValue().increment("room:" + roomId + ":count", 1);
    }

    public void userLeft(String roomId) {
        Long count = redisTemplate.opsForValue().decrement("room:" + roomId + ":count", 1);
        if (count != null && count <= 0) {
            // Room is empty! Reset ownership.
            redisTemplate.delete("room:" + roomId + ":count");
            redisTemplate.delete("room:" + roomId + ":owner");
        }
    }
}
