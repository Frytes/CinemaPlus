package com.frytes.cinemaPlus.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookingLockService {

    private final StringRedisTemplate redisTemplate;

    private static final String LOCK_KEY_PATTERN = "booking:session:%d:seat:%d";
    private static final Duration LOCK_DURATION = Duration.ofMinutes(10);

    public boolean acquireLock(Long sessionId, Long seatId, Long userId) {
        String key = String.format(LOCK_KEY_PATTERN, sessionId, seatId);
        String value = String.valueOf(userId);
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(key, value, LOCK_DURATION);
        return Boolean.TRUE.equals(locked);
    }

    public void releaseLock(Long sessionId, Long seatId, Long userId) {
        String key = String.format(LOCK_KEY_PATTERN, sessionId, seatId);
        String value = redisTemplate.opsForValue().get(key);
        if (value != null && value.equals(String.valueOf(userId))) {
            redisTemplate.delete(key);
        }
    }
    public Set<Long> getLockedSeats(Long sessionId) {
        String pattern = String.format("booking:session:%d:seat:*", sessionId);
        Set<Long> lockedSeatIds = new HashSet<>();

        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(100)
                .build();


        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                try {
                    String seatIdStr = key.substring(key.lastIndexOf(":") + 1);
                    lockedSeatIds.add(Long.parseLong(seatIdStr));
                } catch (NumberFormatException e) {
                    // Игнорируем битые ключи
                }
            }
        }
        return lockedSeatIds;
    }
}
