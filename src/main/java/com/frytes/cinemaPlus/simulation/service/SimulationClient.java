package com.frytes.cinemaPlus.simulation.service;

import com.frytes.cinemaPlus.booking.dto.BookingRequest;
import com.frytes.cinemaPlus.booking.dto.BookingResponse;
import com.frytes.cinemaPlus.content.dto.SeatStatusDto;
import com.frytes.cinemaPlus.content.dto.SessionDto;
import com.frytes.cinemaPlus.simulation.config.StressProperties;
import com.frytes.cinemaPlus.users.dto.AuthResponse;
import com.frytes.cinemaPlus.users.dto.LoginRequest;
import com.frytes.cinemaPlus.users.dto.RegisterRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

@Service
public class SimulationClient {

    private final RestClient restClient;


    public SimulationClient(RestClient.Builder builder, StressProperties props) {
        this.restClient = builder
                .baseUrl(props.targetUrl())
                .build();
    }

    public boolean checkHealth() {
        try {
            restClient.get()
                    .uri("/api/movies")
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String login(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);

        try {
            AuthResponse response = restClient.post()
                    .uri("/api/auth/login")
                    .body(loginRequest)
                    .retrieve()
                    .body(AuthResponse.class);
            if (response == null || response.accessToken() == null) {
                throw new RuntimeException("Пустой ответ от сервера");
            }

            return response.accessToken();

        } catch (Exception e) {
            throw new RuntimeException("Неудачный вход для " + email, e);
        }
    }

    public String register(String name, String email, String password) {
        RegisterRequest registerRequest = new RegisterRequest(name, email, password);

        try {
            AuthResponse response = restClient.post()
                    .uri("/api/auth/register")
                    .body(registerRequest)
                    .retrieve()
                    .body(AuthResponse.class);
            if (response == null || response.accessToken() == null) {
                throw new RuntimeException("Пустой ответ от сервера");
            }

            return response.accessToken();

        } catch (Exception e) {
            throw new RuntimeException("Неудачный вход для " + email, e);
        }
    }

    public List<SessionDto> getSessions(LocalDate date) {

        try {

            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/sessions")
                            .queryParam("date", date.toString())
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

        } catch (Exception e) {
            throw new RuntimeException("Неудачный попытка получить список сесcий", e);
        }
    }

    public List<SeatStatusDto> getSeats(Long sessionId) {

        try {

            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/bookings/session/")
                            .path(String.valueOf(sessionId))
                            .pathSegment("seats")
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

        } catch (Exception e) {
            throw new RuntimeException("Неудачный попытка получить список мест", e);
        }
    }

    public BookingResponse bookSeats(BookingRequest bookingRequest, String token) {

        try {

            BookingResponse bookingResponse = restClient.post()
                    .uri("/api/bookings")
                    .header("Authorization", "Bearer " + token)
                    .body(bookingRequest)
                    .retrieve()
                    .body(BookingResponse.class);


            if (!"PENDING".equals(bookingResponse.status())) {
                throw new RuntimeException("Ошибка бронирования: статус " + bookingResponse.status());
            }
            return bookingResponse;

        } catch (Exception e) {
            throw new RuntimeException("Неудачная попытка зарезервировать место", e);
        }

    }

    public void payOrder(Long orderId, String token) {
        try {
            restClient.post()
                    .uri("/api/bookings/" + orderId + "/pay")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {
            throw new RuntimeException("Ошибка оплаты заказа " + orderId, e);
        }
    }

}
