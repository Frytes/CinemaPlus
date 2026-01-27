package com.frytes.cinemaPlus.booking.service;

import com.frytes.cinemaPlus.booking.dto.BookingRequest;
import com.frytes.cinemaPlus.booking.entity.Order;
import com.frytes.cinemaPlus.booking.entity.Ticket;
import com.frytes.cinemaPlus.booking.repository.OrderRepository;
import com.frytes.cinemaPlus.booking.repository.TicketRepository;
import com.frytes.cinemaPlus.booking.service.pricing.PriceCalculator;
import com.frytes.cinemaPlus.booking.service.pricing.PricingRulesService;
import com.frytes.cinemaPlus.common.exception.SeatAlreadySoldException;
import com.frytes.cinemaPlus.common.exception.UserAlreadyExistsException;
import com.frytes.cinemaPlus.content.entity.Hall;
import com.frytes.cinemaPlus.content.entity.Movie;
import com.frytes.cinemaPlus.content.entity.Seat;
import com.frytes.cinemaPlus.content.entity.Session;
import com.frytes.cinemaPlus.content.entity.enumps.SeatType;
import com.frytes.cinemaPlus.notification.service.SocketNotificationService;
import com.frytes.cinemaPlus.content.repository.SeatRepository;
import com.frytes.cinemaPlus.content.repository.SessionRepository;
import com.frytes.cinemaPlus.users.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("🧪 Unit: Логика бронирования (BookingService)")
class BookingServiceUnitTest {

    @Mock private SessionRepository sessionRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private BookingLockService bookingLockService;
    @Mock private PriceCalculator priceCalculator;
    @Mock private PricingRulesService pricingRulesService;
    @Mock private SocketNotificationService socketService;

    @InjectMocks
    private BookingService bookingService;

    private User user;
    private Session session;
    private Hall hall;
    private Seat seat;

    @BeforeEach
    void setUp() {
        user = new User(1L, "user", "email@test.com", "pass", null);
        hall = new Hall(1L, "Hall 1", 10, 10, null);
        session = new Session(100L, new Movie(), hall, null, null, BigDecimal.valueOf(100));
        seat = new Seat(50L, hall, 1, 1, SeatType.STANDARD, "A1");
    }

    @Test
    @DisplayName("✅ Успешное создание заказа")
    void shouldCreateBooking_WhenAllChecksPass() {
        // Given
        BookingRequest request = new BookingRequest(session.getId(), List.of(seat.getId()));

        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(ticketRepository.findAllBySessionIdAndSeatIdIn(any(), any())).thenReturn(List.of());
        when(seatRepository.findAllById(request.seatIds())).thenReturn(List.of(seat));
        when(bookingLockService.acquireLock(any(), any(), any())).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(priceCalculator.getActiveRules()).thenReturn(Map.of());
        when(priceCalculator.calculatePrice(any(), any(), any())).thenReturn(BigDecimal.valueOf(100));

        // When
        Order order = bookingService.createBooking(request, user);

        // Then
        verify(bookingLockService).acquireLock(session.getId(), seat.getId(), user.getId());
        verify(orderRepository).save(any(Order.class));

        assertThat(order.getTotalPrice()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(order.getTickets().getFirst().getPrice()).isEqualTo(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("❌ Ошибка: Место уже куплено (Проверка БД)")
    void shouldThrowException_WhenSeatIsSoldInDb() {
        BookingRequest request = new BookingRequest(session.getId(), List.of(seat.getId()));
        Ticket existingTicket = new Ticket();

        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(ticketRepository.findAllBySessionIdAndSeatIdIn(any(), any())).thenReturn(List.of(existingTicket));

        assertThrows(SeatAlreadySoldException.class, () -> bookingService.createBooking(request, user));

        verify(bookingLockService, never()).acquireLock(any(), any(), any());
    }

    @Test
    @DisplayName("❌ Ошибка: Место заблокировано в Redis (другим юзером)")
    void shouldThrowException_WhenRedisLockFails() {
        BookingRequest request = new BookingRequest(session.getId(), List.of(seat.getId()));

        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(ticketRepository.findAllBySessionIdAndSeatIdIn(any(), any())).thenReturn(List.of());
        when(seatRepository.findAllById(request.seatIds())).thenReturn(List.of(seat));

        when(bookingLockService.acquireLock(any(), any(), any())).thenReturn(false);

        assertThrows(UserAlreadyExistsException.class, () -> bookingService.createBooking(request, user));
    }

    @Test
    @DisplayName("🔄 Компенсация: Если упала база, нужно снять лок в Redis")
    void shouldReleaseLock_WhenDbSaveFails() {
        BookingRequest request = new BookingRequest(session.getId(), List.of(seat.getId()));

        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(seatRepository.findAllById(request.seatIds())).thenReturn(List.of(seat));
        when(bookingLockService.acquireLock(any(), any(), any())).thenReturn(true);
        when(priceCalculator.getActiveRules()).thenReturn(Map.of());
        when(priceCalculator.calculatePrice(any(), any(), any())).thenReturn(BigDecimal.valueOf(100));


        when(orderRepository.save(any())).thenThrow(new RuntimeException("DB Error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> bookingService.createBooking(request, user));
        verify(bookingLockService).releaseLock(session.getId(), seat.getId(), user.getId());
    }
}