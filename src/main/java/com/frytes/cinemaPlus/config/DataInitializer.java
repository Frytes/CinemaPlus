package com.frytes.cinemaPlus.config;


import com.frytes.cinemaPlus.content.entity.Hall;
import com.frytes.cinemaPlus.content.entity.Movie;
import com.frytes.cinemaPlus.content.entity.Seat;
import com.frytes.cinemaPlus.content.entity.Session;
import com.frytes.cinemaPlus.content.entity.enumps.SeatType;
import com.frytes.cinemaPlus.repository.HallRepository;
import com.frytes.cinemaPlus.repository.MovieRepository;
import com.frytes.cinemaPlus.repository.SessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final HallRepository hallRepository;
    private final MovieRepository movieRepository;
    private final SessionRepository sessionRepository;

    private final BigDecimal basePriceMorning;
    private final BigDecimal basePriceEvening;
    private final BigDecimal surchargeRedHall;
    private final BigDecimal surchargeBlueHall;
    private final BigDecimal surchargeGreenHall;

    public DataInitializer(
            HallRepository hallRepository,
            MovieRepository movieRepository,
            SessionRepository sessionRepository,
            @Value("${cinema.pricing.base-price-morning}") BigDecimal basePriceMorning,
            @Value("${cinema.pricing.base-price-evening}") BigDecimal basePriceEvening,
            @Value("${cinema.pricing.surcharge_red_hall}") BigDecimal surchargeRedHall,
            @Value("${cinema.pricing.surcharge_blue_hall}") BigDecimal surchargeBlueHall,
            @Value("${cinema.pricing.surcharge_green_hall}") BigDecimal surchargeGreenHall
    ) {
        this.hallRepository = hallRepository;
        this.movieRepository = movieRepository;
        this.sessionRepository = sessionRepository;
        this.basePriceMorning = basePriceMorning;
        this.basePriceEvening = basePriceEvening;
        this.surchargeRedHall = surchargeRedHall;
        this.surchargeBlueHall = surchargeBlueHall;
        this.surchargeGreenHall = surchargeGreenHall;
    }
    @Override
    @Transactional
    public void run(String... args) {
        if (hallRepository.count() == 0) {
            log.info("🚀 Starting Data Seeding...");
            initHalls();
            initSessions();
            log.info("✅ Data Seeding Completed!");
        }
    }
    private void initHalls() {
        // --- 1. Зеленый зал (Средний) ---
        Hall green = new Hall();
        green.setName("Зеленый зал");
        green.setWidth(40);
        green.setHeight(5);

        // Ряд 1: 28
        addSimpleRow(green, 0, 28);
        // Ряд 2: 10 + 7 VIP + 10
        addMixedRow(green, 1, 10, 7, 10);
        // Ряд 3: 12 + 7 VIP + 12
        addMixedRow(green, 2, 12, 7, 12);
        // Ряд 4: 38
        addSimpleRow(green, 3, 38);
        // Ряд 5: 34
        addSimpleRow(green, 4, 34);

        hallRepository.save(green);


        // --- 2. Красный зал (Большой с проходом) ---
        Hall red = new Hall();
        red.setName("Красный зал");
        red.setWidth(44);
        red.setHeight(8);

        int aisleGap = 2;

        // Ряд 1: 11 + проход + 11
        addSplitRow(red, 0, 11, 11, SeatType.STANDARD, SeatType.STANDARD, aisleGap);
        // Ряд 2: 14 + проход + 14
        addSplitRow(red, 1, 14, 14, SeatType.STANDARD, SeatType.STANDARD, aisleGap);
        // Ряд 3: 16 + проход + 16
        addSplitRow(red, 2, 16, 16, SeatType.STANDARD, SeatType.STANDARD, aisleGap);
        // Ряд 4: 18 + проход + 18
        addSplitRow(red, 3, 18, 18, SeatType.STANDARD, SeatType.STANDARD, aisleGap);
        // Ряд 5: (15  + 4 VIP) + проход + (4 VIP + 15)
        addComplexRow(red, 4, 15, 4, 4, 15, aisleGap);
        // Ряд 6: (17  + 4 VIP) + проход + (4 VIP + 17)
        addComplexRow(red, 5, 17, 4, 4, 17, aisleGap);
        // Ряд 7: (12  + 4 VIP) + проход + (4 VIP + 12)
        addComplexRow(red, 6, 12, 4, 4, 12, aisleGap);
        // Ряд 8: (5  + 8 VIP) + проход + (8 VIP + 5)
        addComplexRow(red, 7, 5, 8, 8, 5, aisleGap);

        hallRepository.save(red);


        // --- 3. Синий зал (Малый) ---
        Hall blue = new Hall();
        blue.setName("Синий зал");
        blue.setWidth(23);
        blue.setHeight(3);

        // Ряд 1: 23 синих
        addSimpleRow(blue, 0, 23);
        // Ряд 2: 8 синих + 6 VIP + 8 синих
        addMixedRow(blue, 1, 8, 6, 8);
        // Ряд 3: 8 синих + 6 VIP + 8 синих
        addMixedRow(blue, 2, 8, 6, 8);

        hallRepository.save(blue);
    }

    // Простой ряд одного типа (по центру)
    private void addSimpleRow(Hall hall, int rowIdx, int count) {
        int offset = (hall.getWidth() - count) / 2;
        for (int i = 0; i < count; i++) {
            createSeat(hall, rowIdx, offset + i, i + 1, SeatType.STANDARD);
        }
    }

    // Ряд: [Standard] [VIP] [Standard] (Без прохода)
    private void addMixedRow(Hall hall, int rowIdx, int leftStd, int centerVip, int rightStd) {
        int totalSeats = leftStd + centerVip + rightStd;
        int offset = (hall.getWidth() - totalSeats) / 2;
        int seatNum = 1;

        // Левые
        for (int i = 0; i < leftStd; i++) createSeat(hall, rowIdx, offset++, seatNum++, SeatType.STANDARD);
        // VIP
        for (int i = 0; i < centerVip; i++) createSeat(hall, rowIdx, offset++, seatNum++, SeatType.VIP);
        // Правые
        for (int i = 0; i < rightStd; i++) createSeat(hall, rowIdx, offset++, seatNum++, SeatType.STANDARD);
    }

    // Ряд с проходом: [LeftBlock] |GAP| [RightBlock] (Одного типа)
    private void addSplitRow(Hall hall, int rowIdx, int leftCount, int rightCount, SeatType leftType, SeatType rightType, int gap) {
        int totalWidth = leftCount + gap + rightCount;
        int offset = (hall.getWidth() - totalWidth) / 2;
        int seatNum = 1;

        // Левая часть
        for (int i = 0; i < leftCount; i++) createSeat(hall, rowIdx, offset++, seatNum++, leftType);

        offset += gap;

        // Правая часть
        for (int i = 0; i < rightCount; i++) createSeat(hall, rowIdx, offset++, seatNum++, rightType);
    }

    // Ряд с проходом и смешанными типами: [Std][Vip] |GAP| [Vip][Std]
    private void addComplexRow(Hall hall, int rowIdx, int lStd, int lVip, int rVip, int rStd, int gap) {
        int totalWidth = (lStd + lVip) + gap + (rVip + rStd);
        int offset = (hall.getWidth() - totalWidth) / 2;
        int seatNum = 1;

        // Левый блок (Std -> Vip)
        for (int i = 0; i < lStd; i++) createSeat(hall, rowIdx, offset++, seatNum++, SeatType.STANDARD);
        for (int i = 0; i < lVip; i++) createSeat(hall, rowIdx, offset++, seatNum++, SeatType.VIP);

        offset += gap;

        // Правый блок (Vip -> Std)
        for (int i = 0; i < rVip; i++) createSeat(hall, rowIdx, offset++, seatNum++, SeatType.VIP);
        for (int i = 0; i < rStd; i++) createSeat(hall, rowIdx, offset++, seatNum++, SeatType.STANDARD);
    }

    private void createSeat(Hall hall, int row, int col, int number, SeatType type) {
        Seat seat = new Seat();
        seat.setHall(hall);
        seat.setRowIndex(row);
        seat.setColIndex(col);
        seat.setSeatNumber(String.valueOf(number));
        seat.setType(type);
        hall.addSeat(seat);
    }
    private void initSessions() {
        List<Movie> movies = movieRepository.findAll();
        List<Hall> halls = hallRepository.findAll();

        if (movies.isEmpty() || halls.isEmpty()) return;

        Random random = new Random();
        LocalDate today = LocalDate.now();

        // Генерируем расписание на 4 дня
        for (int day = 0; day < 4; day++) {
            LocalDate currentDate = today.plusDays(day);

            for (Hall hall : halls) {
                LocalTime time = LocalTime.of(10, 0);
                while (time.isBefore(LocalTime.of(23, 0))) {
                    Movie movie = movies.get(random.nextInt(movies.size()));

                    int minutes = time.getMinute();
                    int remainder = minutes % 15;
                    if (remainder != 0) {
                        time = time.plusMinutes(15 - remainder);
                    }
                    time = time.withSecond(0).withNano(0);

                    if (time.equals(LocalTime.MIDNIGHT) || time.isBefore(LocalTime.of(10, 0))) {
                        break;
                    }

                    LocalDateTime startDateTime = LocalDateTime.of(currentDate, time);

                    // Расчет конца: Фильм + 30 мин уборка
                    int totalDuration = movie.getDurationMinutes() + 30;
                    LocalDateTime endDateTime = startDateTime.plusMinutes(movie.getDurationMinutes());

                    BigDecimal price = (time.getHour() < 14) ? basePriceMorning : basePriceEvening;
                    if (hall.getName().contains("Зеленый зал")) price = price.add(surchargeGreenHall);
                    if (hall.getName().contains("Красный зал")) price = price.add(surchargeRedHall);
                    if (hall.getName().contains("Синий зал")) price = price.add(surchargeBlueHall);

                    Session session = new Session();
                    session.setMovie(movie);
                    session.setHall(hall);
                    session.setStartTime(startDateTime);
                    session.setEndTime(endDateTime);
                    session.setBasePrice(price);

                    sessionRepository.save(session);

                    time = time.plusMinutes(totalDuration);
                }
            }
        }
    }
}