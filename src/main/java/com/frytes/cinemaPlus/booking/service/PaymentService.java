package com.frytes.cinemaPlus.booking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frytes.cinemaPlus.booking.entity.Order;
import com.frytes.cinemaPlus.booking.entity.OutboxEvent;
import com.frytes.cinemaPlus.booking.entity.Ticket;
import com.frytes.cinemaPlus.booking.entity.enumps.OrderStatus;
import com.frytes.cinemaPlus.booking.entity.enumps.OutboxStatus;
import com.frytes.cinemaPlus.booking.event.BookingPaidEvent;
import com.frytes.cinemaPlus.booking.event.TicketDetail;
import com.frytes.cinemaPlus.booking.repository.OrderRepository;
import com.frytes.cinemaPlus.booking.repository.OutboxRepository;
import com.frytes.cinemaPlus.booking.repository.TicketRepository;
import com.frytes.cinemaPlus.common.exception.ResourceNotFoundException;
import com.frytes.cinemaPlus.content.dto.enums.SocketStatus;
import com.frytes.cinemaPlus.content.entity.Session;
import com.frytes.cinemaPlus.notification.service.SocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final BookingLockService bookingLockService;
    private final TicketRepository ticketRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final SocketNotificationService socketService;


    private final Random random = new Random();


    @Value("${cinema.mock.payment.delay-ms:1000}")
    private int delayMs;

    @Value("${cinema.mock.payment.fail-probability:10}")
    private int failProbability;

    @SneakyThrows
    public boolean processPayment(Long orderId) {

        if (delayMs > 0) {
            Thread.sleep(delayMs + random.nextInt(200));
        }

        boolean success = random.nextInt(100) >= failProbability;

        return finalizeOrder(orderId, success);
    }
    @SneakyThrows
    @Transactional
    protected boolean finalizeOrder(Long orderId, boolean paymentSuccess)  {
        Order order = orderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Заказ не найден"));

        if (order.getStatus() != OrderStatus.PENDING) {
            return false;
        }

        if (paymentSuccess) {
            order.setStatus(OrderStatus.PAID);

            String movieTitle = "Unknown";
            String hallName = "Unknown Hall";
            Long sessionId = null;

            if (!order.getTickets().isEmpty()) {
                Session session = order.getTickets().getFirst().getSession();
                movieTitle = session.getMovie().getTitle();
                hallName = session.getHall().getName();
                sessionId = session.getId();
            }

            List<TicketDetail> ticketDetails = order.getTickets().stream()
                    .map(t -> new TicketDetail(
                            t.getSeat().getSeatNumber(),
                            t.getSeat().getRowIndex(),
                            t.getSeat().getColIndex(),
                            t.getSeat().getType()
                    ))
                    .toList();



            BookingPaidEvent event = new BookingPaidEvent(
                    order.getId(),
                    order.getUser().getId(),
                    order.getUser().getEmail(),
                    movieTitle,
                    sessionId,
                    hallName,
                    ticketDetails,
                    order.getTotalPrice(),
                    LocalDateTime.now()
            );
            String json = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .topic("booking-events-topic")
                    .payload(json)
                    .status(OutboxStatus.NEW)
                    .build();
            outboxRepository.save(outboxEvent);
            for (Ticket ticket : order.getTickets()) {
                socketService.sendSeatUpdate(
                        ticket.getSession().getId(),
                        ticket.getSeat(),
                        SocketStatus.SOLD
                );
            }
            log.info("✅ Payment success! Outbox event saved. Order ID: {}", order.getId());
            return true;
        } else {
            for (Ticket ticket : order.getTickets()) {
                bookingLockService.releaseLock(
                        ticket.getSession().getId(),
                        ticket.getSeat().getId(),
                        order.getUser().getId()

                );
                socketService.sendSeatUpdate(
                        ticket.getSession().getId(),
                        ticket.getSeat(),
                        SocketStatus.AVAILABLE
                );
            }

            ticketRepository.deleteAll(order.getTickets());
            order.getTickets().clear();
            order.setStatus(OrderStatus.CANCELLED);

            return false;
        }
    }
}