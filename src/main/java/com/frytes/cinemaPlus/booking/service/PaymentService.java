package com.frytes.cinemaPlus.booking.service;

import com.frytes.cinemaPlus.booking.entity.Order;
import com.frytes.cinemaPlus.booking.entity.Ticket;
import com.frytes.cinemaPlus.booking.entity.enumps.OrderStatus;
import com.frytes.cinemaPlus.booking.event.BookingPaidEvent;
import com.frytes.cinemaPlus.booking.event.TicketDetail;
import com.frytes.cinemaPlus.booking.repository.OrderRepository;
import com.frytes.cinemaPlus.booking.repository.TicketRepository;
import com.frytes.cinemaPlus.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final BookingLockService bookingLockService;
    private final TicketRepository ticketRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final Random random = new Random();


    @Value("${cinema.payment.delay-ms:1000}")
    private int delayMs;

    @Value("${cinema.payment.fail-probability:10}")
    private int failProbability;

    @SneakyThrows
    public boolean processPayment(Long orderId) {

        if (delayMs > 0) {
            Thread.sleep(delayMs + random.nextInt(200));
        }

        boolean success = random.nextInt(100) >= failProbability;

        return finalizeOrder(orderId, success);
    }

    @Transactional
    protected boolean finalizeOrder(Long orderId, boolean paymentSuccess) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Заказ не найден"));

        if (order.getStatus() != OrderStatus.PENDING) {
            return false;
        }

        if (paymentSuccess) {
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);

            String movieTitle = "Unknown";
            if (!order.getTickets().isEmpty()) {
                movieTitle = order.getTickets().getFirst().getSession().getMovie().getTitle();
            }

            Long sessionId = null;
            if (!order.getTickets().isEmpty()) {
                sessionId = order.getTickets().getFirst().getSession().getId();
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
                    ticketDetails,
                    order.getTotalPrice(),
                    LocalDateTime.now()
            );

            kafkaTemplate.send("booking-events-topic", String.valueOf(orderId), event);

            return true;
        } else {
            for (Ticket ticket : order.getTickets()) {
                bookingLockService.releaseLock(
                        ticket.getSession().getId(),
                        ticket.getSeat().getId(),
                        order.getUser().getId()
                );
            }
            List<Ticket> ticketsToDelete = new ArrayList<>(order.getTickets());
            order.getTickets().clear();
            ticketRepository.deleteAll(ticketsToDelete);

            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            return false;
        }
    }
}