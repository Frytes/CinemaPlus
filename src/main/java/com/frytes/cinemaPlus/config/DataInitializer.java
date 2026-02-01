package com.frytes.cinemaPlus.config;


import com.frytes.cinemaPlus.content.entity.Hall;
import com.frytes.cinemaPlus.content.entity.Movie;
import com.frytes.cinemaPlus.content.entity.Seat;
import com.frytes.cinemaPlus.content.entity.Session;
import com.frytes.cinemaPlus.content.entity.enumps.SeatType;
import com.frytes.cinemaPlus.content.repository.HallRepository;
import com.frytes.cinemaPlus.content.repository.MovieRepository;
import com.frytes.cinemaPlus.content.repository.SessionRepository;
import com.frytes.cinemaPlus.users.entity.Role;
import com.frytes.cinemaPlus.users.entity.User;
import com.frytes.cinemaPlus.users.repository.UserRepository;
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
    private final UserRepository userRepository;

    private final BigDecimal basePriceMorning;
    private final BigDecimal basePriceEvening;
    private final BigDecimal surchargeRedHall;
    private final BigDecimal surchargeBlueHall;
    private final BigDecimal surchargeGreenHall;

    public DataInitializer(
            HallRepository hallRepository,
            MovieRepository movieRepository,
            SessionRepository sessionRepository, UserRepository userRepository,
            @Value("${cinema.demo-seeding.price-generation.base-morning}") BigDecimal basePriceMorning,
            @Value("${cinema.demo-seeding.price-generation.base-evening}") BigDecimal basePriceEvening,
            @Value("${cinema.demo-seeding.price-generation.hall-surcharges.red}") BigDecimal surchargeRedHall,
            @Value("${cinema.demo-seeding.price-generation.hall-surcharges.blue}") BigDecimal surchargeBlueHall,
            @Value("${cinema.demo-seeding.price-generation.hall-surcharges.green}") BigDecimal surchargeGreenHall
    ) {
        this.hallRepository = hallRepository;
        this.movieRepository = movieRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
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
            initMovies();
            initHalls();
            initSessions();
            initAdminUser();
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
        addSimpleRow(green, 0, 27);
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
        addSimpleRow(blue, 0, 22);
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
        Seat seat = Seat.builder()
                .hall(hall)
                .rowIndex(row)
                .colIndex(col)
                .seatNumber(String.valueOf(number))
                .type(type)
                .build();
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

                    Session session = Session.builder()
                            .movie(movie)
                            .hall(hall)
                            .startTime(startDateTime)
                            .endTime(endDateTime)
                            .basePrice(price)
                            .build();

                    sessionRepository.save(session);

                    time = time.plusMinutes(totalDuration);
                }
            }
        }
    }

    private void initAdminUser(){
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("$2a$10$tBjOyW3NHCMH8I2erLbqQ..c9bjW5q99jOmHhpiKD.1KMCCqdtpWm");
        adminUser.setEmail("admin");
        adminUser.setRole(Role.ADMIN);
        userRepository.save(adminUser);
    }
    private void initMovies() {
        log.info("🎬 Seeding Movies...");

        createMovie("Начало",
                "Кобб — талантливый вор, лучший из лучших в опасном искусстве извлечения: он крадет ценные секреты из глубин подсознания во время сна.",
                148, "https://images-s.kinorium.com/movie/poster/472809/w1500_52479049.jpg", 2010, 8.8, 12, "Фантастика, Боевик");

        createMovie("Интерстеллар",
                "Наше время на Земле подошло к концу, команда исследователей берет на себя самую важную миссию в истории человечества; путешествуя за пределы нашей галактики.",
                169, "https://ir.ozone.ru/s3/multimedia-1-u/6955807206.jpg", 2014, 8.6, 12, "Фантастика, Приключения");

        createMovie("Темный рыцарь",
                "Бэтмен поднимает ставки в войне с криминалом. С помощью лейтенанта Джима Гордона и прокурора Харви Дента он намерен очистить улицы от преступности.",
                152, "https://avatars.mds.yandex.net/get-mpic/4408567/2a000001919dc17b6bfee339046e8a264aa8/orig", 2008, 9.0, 16, "Боевик, Криминал");

        createMovie("Джокер",
                "Готэм, начало 1980-х годов. Комик Артур Флек живет с больной матерью. Пытаясь нести в мир хорошее, Артур сталкивается с человеческой жестокостью.",
                122, "https://avatars.mds.yandex.net/i?id=93821fa4249c5c8565dca5b2995f3988_l-2479991-images-thumbs&n=13", 2019, 8.4, 18, "Криминал, Драма");

        createMovie("Человек-паук: Паутина вселенных",
                "Майлз Моралес отправляется в приключение по мультивселенной вместе с Гвен Стейси и новой командой Людей-Пауков.",
                140, "https://avatars.dzeninfra.ru/get-zen_doc/271828/pub_6731df02a693237a9ffe5607_6731e8ef1c69ef1b2975acec/scale_1200", 2023, 8.7, 6, "Мультфильм, Боевик");

        createMovie("Кунг-фу Панда 4",
                "По, Воин Дракона, призван судьбой... чтобы уже наконец отдохнуть. Точнее, он призван стать Духовным Лидером Долины Мира.",
                94, "https://images-s.kinorium.com/movie/fanart/9805987/w1500_52652825.jpg", 2024, 7.1, 0, "Мультфильм, Комедия");

        createMovie("Гадкий я 4",
                "Грю, Люси и их девочки приветствуют нового члена семьи, Грю-младшего, который намерен мучить своего отца.",
                95, "https://cdn.premierzal.ru/files/image/fjldvc1wswai-zln.jpg", 2024, 7.3, 0, "Мультфильм, Семейный");

        createMovie("Головоломка 2",
                "Головоломка 2 возвращается в сознание новоиспеченного подростка Райли как раз в тот момент, когда штаб-квартира подвергается сносу.",
                96, "https://avatars.mds.yandex.net/i?id=68f98c9ceeddfe6fcc52b29b40070975_l-4351135-images-thumbs&n=13", 2024, 7.7, 6, "Мультфильм, Семейный");
    }

    private void createMovie(String title, String desc, int duration, String poster, int year, double rating, int age, String genre) {
        Movie movie = Movie.builder()
                .title(title)
                .description(desc)
                .durationMinutes(duration)
                .posterUrl(poster)
                .releaseYear(year)
                .rating(rating)
                .ageLimit(age)
                .genre(genre)
                .build();
        movieRepository.save(movie);
    }
}
