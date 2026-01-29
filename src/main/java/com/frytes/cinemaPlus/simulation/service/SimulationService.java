package com.frytes.cinemaPlus.simulation.service;

import com.frytes.cinemaPlus.booking.dto.BookingRequest;
import com.frytes.cinemaPlus.booking.dto.BookingResponse;
import com.frytes.cinemaPlus.content.dto.SeatStatusDto;
import com.frytes.cinemaPlus.content.dto.SessionDto;
import com.frytes.cinemaPlus.simulation.config.StressProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationService {

    private final SimulationClient simulationClient;
    private final StressProperties props;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public void start() {
        if (!props.enabled()) {
            log.warn("⛔ Симуляция выключена в настройках.");
            return;
        }

        if (isRunning.get()) {
            log.warn("⚠️ Симуляция уже запущена!");
            return;
        }
        isRunning.set(true);

        log.info("🔥 ЗАПУСК АТАКИ: {} ботов...", props.botCount());

        long delayBetweenStarts = props.rampUpSeconds() * 1000L / props.botCount();
        executor.submit(() -> {
            for (int i = 0; i < props.botCount(); i++) {
                if (!isRunning.get()) {
                    log.info("🛑 Атака прервана пользователем.");
                    break;
                }

                executor.submit(this::runBotScenario);

                if (delayBetweenStarts > 0) {
                    try {
                        Thread.sleep(delayBetweenStarts);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
    }

    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            log.info("🛑 Получен сигнал остановки! Новые боты создаваться не будут.");
        } else {
            log.info("ℹ️ Симуляция не была запущена.");
        }
    }

    private void runBotScenario() {
        String botName = "Bot-" + ThreadLocalRandom.current().nextInt(10000, 99999);

        try {
            think();

            String uuid = UUID.randomUUID().toString();
            String email = "bot-" + uuid.substring(0, 8) + "@stress.test";
            String token = simulationClient.register(botName, email, "password123");

            log.info("🤖 {} зарегистрирован", botName);


            LocalDate targetDate = LocalDate.now().plusDays(
                    ThreadLocalRandom.current().nextInt(props.daysToScan())
            );
            List<SessionDto> sessions = simulationClient.getSessions(targetDate);

            List<SessionDto> validSessions = sessions.stream()
                    .filter(s -> s.startTime().isAfter(LocalDateTime.now()))
                    .toList();

            if (validSessions.isEmpty()) {
                 log.debug("⛔ {} не нашел сеансов на {}", botName, targetDate);
                return;
            }

            think();

            SessionDto session = validSessions.get(ThreadLocalRandom.current().nextInt(validSessions.size()));

            List<SeatStatusDto> allSeats = simulationClient.getSeats(session.id());

            List<Long> availableSeatIds = new ArrayList<>(allSeats.stream()
                    .filter(s -> !s.isBooked())
                    .map(SeatStatusDto::id)
                    .toList());

            if (availableSeatIds.isEmpty()){
                 log.debug("⛔ {} хотел на '{}', но мест нет", botName, session.movieTitle());
                return;
            }

            int seatsToBuyCount = Math.min(availableSeatIds.size(), ThreadLocalRandom.current().nextInt(1, 4));
            Collections.shuffle(availableSeatIds);
            List<Long> selectedSeats = availableSeatIds.subList(0, seatsToBuyCount);

            think();

            BookingRequest bookingRequest = new BookingRequest(session.id(), selectedSeats);
            BookingResponse bookingResponse = simulationClient.bookSeats(bookingRequest, token);

            log.info("🎫 {} забронировал {} мест(а) на '{}'", botName, seatsToBuyCount, session.movieTitle());

            if (ThreadLocalRandom.current().nextInt(100) < props.buyProbability()) {
                think();

                simulationClient.payOrder(bookingResponse.orderId(), token);
                log.info("💰 {} ОПЛАТИЛ заказ #{}", botName, bookingResponse.orderId());
            } else {
                log.info("👋 {} передумал платить (брошенная корзина)", botName);
            }

        } catch (Exception e) {
            log.error("💀 {} ошибка: {}", botName, e.getMessage());
        }
    }

    private void think() {
        if (props.thinkTimeMs() > 0) {
            try {
                int baseTime = props.thinkTimeMs();
                int deviation = baseTime / 2;
                int sleepTime = baseTime - deviation + ThreadLocalRandom.current().nextInt(deviation * 2);
                Thread.sleep(Math.max(10, sleepTime));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}