package com.frytes.cinemaPlus.notification.service;

import com.frytes.cinemaPlus.booking.event.BookingPaidEvent;
import com.frytes.cinemaPlus.booking.event.TicketDetail;
import com.frytes.cinemaPlus.users.event.UserRegisteredEvent;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class HtmlTemplateService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public String createBookingConfirmation(BookingPaidEvent event, String qrBase64) {
        String seatsFormatted = formatSeats(event.tickets());
        String seatsCount = event.tickets().size() + " " + getSeatWord(event.tickets().size());

        LocalDateTime mskTime = event.eventTime().plusHours(3);

        return String.format("""
    <!DOCTYPE html>
    <html lang="ru">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
    </head>
    <body style="
        margin:0;
        padding:0;
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
        background: linear-gradient(135deg, #0a0a0a 0%%, #1a0a0a 100%%);
        color: #ffffff;
        min-height: 100vh;
    ">
       \s
        <table width="100%%" cellpadding="0" cellspacing="0" align="center">
            <tr>
                <td align="center" style="padding: 30px 20px;">
                   \s
                    <!-- Header -->
                    <table width="100%%" cellpadding="0" cellspacing="0"\s
                           style="max-width: 680px; margin-bottom: 30px;">
                        <tr>
                            <td align="center">
                                <h1 style="
                                    color:#ff0000;\s
                                    margin:0 0 15px 0;
                                    font-size:42px;
                                    font-weight:900;
                                    letter-spacing:2px;
                                    text-transform: uppercase;
                                ">
                                    CINEMA<span style="color:#ffffff;">PLUS</span>
                                </h1>
                               \s
                                <div style="
                                    display: inline-block;
                                    background: linear-gradient(90deg, #ff0000, #d00000, #9d0208);
                                    padding: 12px 32px;
                                    border-radius: 8px;
                                    margin: 10px 0 25px 0;
                                    box-shadow: 0 4px 20px rgba(255, 0, 0, 0.5);
                                ">
                                    <span style="
                                        color: #ffffff;
                                        font-size: 18px;
                                        font-weight: 700;
                                        letter-spacing: 1px;
                                    ">
                                        🎬 ВАШ ЗАКАЗ ГОТОВ
                                    </span>
                                </div>
                               \s
                                <p style="
                                    color:#b0b0b0;\s
                                    margin:0;
                                    font-size:16px;
                                    max-width:500px;
                                    line-height:1.5;
                                ">
                                    Покажите этот билет на входе. Приятного просмотра!
                                </p>
                            </td>
                        </tr>
                    </table>
                   \s
                    <!-- Horizontal Ticket Container -->
                    <table width="100%%" cellpadding="0" cellspacing="0"\s
                           style="
                               max-width: 680px;
                               background: linear-gradient(135deg, #1a1a1a 0%%, #0d0d0d 100%%);
                               border-radius: 20px;
                               box-shadow: 0 20px 60px rgba(255, 0, 0, 0.15);
                               border: 2px solid rgba(255, 0, 0, 0.3);
                               overflow: hidden;
                           ">
                       \s
                        <!-- Красная полоса слева -->
                        <tr>
                            <td style="
                                width: 8px;
                                background: linear-gradient(to bottom, #ff0000, #d00000, #9d0208);
                            "></td>
                           \s
                            <!-- Основное содержимое -->
                            <td style="padding: 30px 25px;">
                               \s
                                <table width="100%%" cellpadding="0" cellspacing="0">
                                    <tr>
                                        <!-- Левая часть - информация -->
                                        <td style="padding-right: 25px; vertical-align: top;">
                                           \s
                                            <!-- Название фильма -->
                                            <div style="margin-bottom: 25px;">
                                                <h2 style="
                                                    color: #ffffff;
                                                    margin: 0 0 5px 0;
                                                    font-size: 26px;
                                                    font-weight: 800;
                                                    line-height: 1.2;
                                                ">
                                                    🎥 %s
                                                </h2>
                                               \s
                                                <div style="
                                                    display: inline-block;
                                                    background: rgba(255, 0, 0, 0.1);
                                                    padding: 6px 14px;
                                                    border-radius: 8px;
                                                    border: 1px solid rgba(255, 0, 0, 0.3);
                                                ">
                                                    <span style="color: #ff6b6b; font-size: 13px;">📅</span>
                                                    <span style="
                                                        color: #ffffff;
                                                        margin-left: 6px;
                                                        font-size: 15px;
                                                        font-weight: 600;
                                                    ">
                                                         %s | %s <span style="color: #888; margin: 0 5px;">|</span> %s
                                                    </span>
                                                </div>
                                            </div>
                                           \s
                                            <!-- Информационные блоки в ряд -->
                                            <table width="100%%" cellpadding="0" cellspacing="0"\s
                                                   style="margin-bottom: 20px;">
                                                <tr>
                                                    <td style="padding: 0 10px 0 0; vertical-align: top;">
                                                        <div style="
                                                            background: rgba(40, 40, 40, 0.8);
                                                            padding: 12px;
                                                            border-radius: 10px;
                                                            border: 1px solid rgba(255, 0, 0, 0.2);
                                                            width: 180px;
                                                            box-sizing: border-box;
                                                        ">
                                                            <div style="
                                                                color: #ff6b6b;
                                                                font-size: 13px;
                                                                font-weight: 600;
                                                                margin-bottom: 6px;
                                                            ">
                                                                🎫 БИЛЕТЫ
                                                            </div>
                                                            <div style="
                                                                color: #ffffff;
                                                                font-size: 22px;
                                                                font-weight: 800;
                                                                line-height: 1;
                                                            ">
                                                                %s
                                                            </div>
                                                        </div>
                                                    </td>
                                                   \s
                                                    <td style="padding: 0; vertical-align: top;">
                                                        <div style="
                                                            background: rgba(40, 40, 40, 0.8);
                                                            padding: 12px;
                                                            border-radius: 10px;
                                                            border: 1px solid rgba(255, 0, 0, 0.2);
                                                            width: 180px;
                                                            box-sizing: border-box;
                                                        ">
                                                            <div style="
                                                                color: #ff6b6b;
                                                                font-size: 13px;
                                                                font-weight: 600;
                                                                margin-bottom: 6px;
                                                            ">
                                                                💰 СТОИМОСТЬ
                                                            </div>
                                                            <div style="
                                                                color: #ffffff;
                                                                font-size: 22px;
                                                                font-weight: 800;
                                                                line-height: 1;
                                                            ">
                                                                %s ₽
                                                            </div>
                                                            <div style="
                                                                color: #888888;
                                                                font-size: 10px;
                                                                margin-top: 5px;
                                                            ">
                                                                оплачено • %s
                                                            </div>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </table>
                                           \s
                                            <!-- Места -->
                                            <div style="margin-bottom: 20px;">
                                                <div style="
                                                    display: inline-block;
                                                    background: rgba(255, 0, 0, 0.1);
                                                    padding: 5px 12px;
                                                    border-radius: 6px;
                                                    margin-bottom: 12px;
                                                ">
                                                    <span style="
                                                        color: #ff6b6b;
                                                        font-size: 13px;
                                                        font-weight: 600;
                                                    ">
                                                        🎯 ВЫБРАННЫЕ МЕСТА
                                                    </span>
                                                </div>
                                                <div>
                                                    %s
                                                </div>
                                            </div>
                                           \s
                                            <!-- Дополнительная информация -->
                                            <div style="
                                                background: rgba(20, 20, 20, 0.9);
                                                padding: 15px;
                                                border-radius: 12px;
                                                border: 1px solid rgba(255, 0, 0, 0.2);
                                            ">
                                                <p style="
                                                    color: #ff6b6b;
                                                    font-size: 12px;
                                                    font-weight: 600;
                                                    margin: 0 0 8px 0;
                                                ">
                                                    ⚡ ИНСТРУКЦИЯ:
                                                </p>
                                                <ul style="
                                                    color: #b0b0b0;
                                                    font-size: 12px;
                                                    margin: 0;
                                                    padding-left: 18px;
                                                    line-height: 1.5;
                                                ">
                                                    <li>Приходите <strong>за 15 минут</strong> до начала сеанса</li>
                                                    <li>Покажите <strong>QR-код</strong> на входе в зал</li>
                                                    <li>Сохраняйте этот билет до конца сеанса</li>
                                                </ul>
                                            </div>
                                           \s
                                        </td>
                                       \s
                                        <!-- Правая часть - QR код -->
                                            <td style="
                                                width: 180px;
                                                vertical-align: middle;
                                                border-left: 2px dashed rgba(255, 0, 0, 0.3);
                                                padding-left: 20px;
                                            ">
                                               \s
                                                <div style="
                                                    background: #ffffff;
                                                    padding: 15px;
                                                    border-radius: 12px;
                                                    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
                                                    border: 2px solid #ff0000;
                                                    text-align: center;
                                                ">
                                                    <!-- ВАЖНО: Вставляем Base64 -->
                                                    <img src="data:image/png;base64,%s"
                                                         width="140"
                                                         height="140"
                                                         alt="QR-код билета"
                                                         style="
                                                             display: block;
                                                             border-radius: 6px;
                                                             margin: 0 auto;
                                                         "
                                                    />
                                                   \s
                                                    <div style="
                                                        margin-top: 15px;
                                                        padding: 10px;
                                                        background: linear-gradient(90deg, #ff0000, #d00000, #9d0208);
                                                        border-radius: 8px;
                                                    ">
                                                        <p style="
                                                            color: #ffffff;
                                                            font-size: 13px;
                                                            margin: 0;
                                                            font-weight: 700;
                                                        ">
                                                            📱 ОТСКАНИРУЙТЕ
                                                        </p>
                                                        <p style="
                                                            color: rgba(255, 255, 255, 0.9);
                                                            font-size: 10px;
                                                            margin: 3px 0 0 0;
                                                        ">
                                                            на входе в кинотеатр
                                                        </p>
                                                    </div>
                                                </div>
                                               \s
                                            </td>
                                    </tr>
                                </table>
                               \s
                            </td>
                        </tr>
                       \s
                        <!-- Нижняя красная полоса -->
                        <tr>
                            <td colspan="2">
                                <div style="
                                    height: 6px;
                                    background: linear-gradient(90deg,\s
                                        #ff0000 0%%,\s
                                        #d00000 20%%,\s
                                        #9d0208 40%%,\s
                                        #9d0208 60%%,\s
                                        #d00000 80%%,\s
                                        #ff0000 100%%
                                    );
                                "></div>
                            </td>
                        </tr>
                       \s
                    </table>
                   \s
                    <!-- Footer -->
                    <table width="100%%" cellpadding="0" cellspacing="0"\s
                           style="max-width: 680px; margin-top: 40px;">
                        <tr>
                            <td align="center">
                                <div style="
                                    padding: 25px;
                                    background: rgba(20, 20, 20, 0.8);
                                    border-radius: 12px;
                                    border: 1px solid rgba(255, 0, 0, 0.2);
                                ">
                                    <table width="100%%" cellpadding="0" cellspacing="0">
                                        <tr>
                                            <td align="center" style="padding: 0 15px;">
                                                <div style="
                                                    display: inline-block;
                                                    text-align: center;
                                                    padding: 0 20px;
                                                ">
                                                    <div style="
                                                        color: #ff0000;
                                                        font-size: 14px;
                                                        font-weight: 600;
                                                        margin-bottom: 8px;
                                                    ">
                                                        📍 АДРЕС
                                                    </div>
                                                    <div style="
                                                        color: #cccccc;
                                                        font-size: 14px;
                                                    ">
                                                        г. Краснодар, ул. Кинематографическая, 7
                                                    </div>
                                                </div>
                                               \s
                                                <div style="
                                                    display: inline-block;
                                                    width: 1px;
                                                    height: 40px;
                                                    background: rgba(255, 0, 0, 0.3);
                                                    margin: 0 30px;
                                                "></div>
                                               \s
                                                <div style="
                                                    display: inline-block;
                                                    text-align: center;
                                                    padding: 0 20px;
                                                ">
                                                    <div style="
                                                        color: #ff0000;
                                                        font-size: 14px;
                                                        font-weight: 600;
                                                        margin-bottom: 8px;
                                                    ">
                                                        📞 КОНТАКТЫ
                                                    </div>
                                                    <div style="
                                                        color: #cccccc;
                                                        font-size: 14px;
                                                    ">
                                                        +7 (999) 123-45-67<br>
                                                        support@cinema-plus.ru
                                                    </div>
                                                </div>
                                            </td>
                                        </tr>
                                    </table>
                                   \s
                                    <div style="
                                        height: 1px;
                                        background: linear-gradient(90deg,\s
                                            transparent,\s
                                            rgba(255, 0, 0, 0.3),\s
                                            transparent
                                        );
                                        margin: 25px 0;
                                    "></div>
                                   \s
                                    <p style="
                                        margin:0;
                                        color:#888888;
                                        font-size:12px;
                                        text-align: center;
                                    ">
                                        © 2025 CINEMAPLUS. Электронный билет.
                                    </p>
                                </div>
                            </td>
                        </tr>
                    </table>
                   \s
                </td>
            </tr>
        </table>
       \s
    </body>
    </html>
   \s""",
                escape(event.movieTitle()),
                mskTime.format(DATE_FORMATTER),
                mskTime.format(TIME_FORMATTER),
                escape(event.hall()),
                seatsCount,
                event.amount(),
                mskTime.format(TIME_FORMATTER),
                seatsFormatted,
                qrBase64
        );
    }

    private String formatSeats(List<TicketDetail> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            return "<p style='color:#888888; font-size:14px; margin:0;'>Места не указаны</p>";
        }

        StringBuilder seatsHtml = new StringBuilder();

        for (TicketDetail ticket : tickets) {
            boolean isVip = ticket.type().toString().equalsIgnoreCase("VIP");

            String seatHtml = String.format("""
            <div style="
                display: inline-block;
                margin: 0 8px 8px 0;
                padding: 8px 14px;
                border-radius: 8px;
                background: %s;
                border: 1px solid %s;
                box-shadow: 0 3px 8px rgba(0, 0, 0, 0.3);
                vertical-align: middle;
            ">
                <span style="
                    color: %s;
                    font-size: 13px;
                    font-weight: 700;
                ">
                    🪑 РЯД <span style="color:#ffffff;">%d</span> • МЕСТО <span style="color:#ffffff;">%s</span>
                </span>
                <span style="
                    display: inline-block;
                    margin-left: 8px;
                    padding: 2px 8px;
                    background: %s;
                    border-radius: 5px;
                    font-size: 10px;
                    font-weight: 900;
                    color: #ffffff;
                    text-transform: uppercase;
                ">
                    %s
                </span>
            </div>
            """,
                    isVip ?
                            "linear-gradient(135deg, #9d0208, #d00000)" :
                            "linear-gradient(135deg, #2d2d2d, #1a1a1a)",
                    isVip ? "#ff6b6b" : "#444444",
                    isVip ? "#ffffff" : "#cccccc",
                    ticket.rowIndex() + 1,
                    ticket.seatNumber(),
                    isVip ? "#ff0000" : "#666666",
                    isVip ? "VIP" : "ОБЫЧНЫЙ"
            );

            seatsHtml.append(seatHtml);
        }

        return seatsHtml.toString();
    }

    private String getSeatWord(int count) {
        if (count % 10 == 1 && count % 100 != 11) return "билет";
        if (count % 10 >= 2 && count % 10 <= 4 &&
                (count % 100 < 10 || count % 100 >= 20)) return "билета";
        return "билетов";
    }

    public String createWelcomeEmail(UserRegisteredEvent event) {
        return String.format("""
        <!DOCTYPE html>
        <html lang="ru">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    margin: 0;
                    padding: 0;
                    font-family: 'Segoe UI', -apple-system, BlinkMacSystemFont, sans-serif;
                    background: linear-gradient(135deg, #0a0a0a 0%%, #1a0a0a 100%%);
                    color: #ffffff;
                }
                .container {
                    max-width: 600px;
                    margin: 0 auto;
                    background: rgba(30, 30, 30, 0.95);
                    border-radius: 20px;
                    overflow: hidden;
                    box-shadow: 0 20px 60px rgba(255, 0, 0, 0.15);
                    border: 2px solid rgba(255, 0, 0, 0.3);
                }
                .header {
                    background: linear-gradient(90deg, #1a1a1a, #0d0d0d);
                    padding: 40px;
                    text-align: center;
                    border-bottom: 3px solid #e50914;
                }
                .logo {
                    font-size: 42px;
                    font-weight: 900;
                    letter-spacing: 2px;
                    text-transform: uppercase;
                    margin-bottom: 15px;
                }
                .logo-red {
                    color: #e50914;
                    text-shadow: 0 0 15px rgba(229, 9, 20, 0.7);
                }
                .logo-white {
                    color: #ffffff;
                    text-shadow: 0 0 10px rgba(255, 255, 255, 0.3);
                }
                .welcome-title {
                    margin: 25px 0 10px 0;
                    color: #ffffff;
                    font-size: 32px;
                    font-weight: 800;
                }
                .content {
                    padding: 40px;
                }
                .greeting {
                    font-size: 24px;
                    color: #ffffff;
                    margin-bottom: 20px;
                }
                .username {
                    color: #e50914;
                    font-weight: bold;
                    text-shadow: 0 0 8px rgba(229, 9, 20, 0.5);
                }
                .features {
                    margin: 30px 0;
                    padding: 25px;
                    background: rgba(40, 40, 40, 0.8);
                    border-radius: 12px;
                    border: 1px solid rgba(229, 9, 20, 0.2);
                }
                .feature-item {
                    display: flex;
                    align-items: center;
                    margin-bottom: 15px;
                    color: #cccccc;
                }
                .feature-icon {
                    font-size: 20px;
                    margin-right: 15px;
                    color: #e50914;
                }
                .cta-button {
                    display: inline-block;
                    background: linear-gradient(90deg, #e50914, #b2070f, #8a0309);
                    color: #ffffff;
                    padding: 16px 40px;
                    border-radius: 12px;
                    text-decoration: none;
                    font-weight: 700;
                    font-size: 18px;
                    margin: 30px 0;
                    transition: all 0.3s;
                    box-shadow: 0 8px 25px rgba(229, 9, 20, 0.3);
                    border: none;
                    cursor: pointer;
                }
                .cta-button:hover {
                    transform: translateY(-3px);
                    box-shadow: 0 12px 35px rgba(229, 9, 20, 0.4);
                }
                .footer {
                    padding: 30px 40px;
                    background: rgba(20, 20, 20, 0.9);
                    border-top: 1px solid rgba(229, 9, 20, 0.2);
                    text-align: center;
                    color: #888888;
                    font-size: 14px;
                }
                .highlight-box {
                    background: linear-gradient(135deg, rgba(229, 9, 20, 0.08), rgba(157, 2, 8, 0.08));
                    border: 1px solid rgba(229, 9, 20, 0.3);
                    padding: 20px;
                    border-radius: 12px;
                    margin: 25px 0;
                }
                .email-highlight {
                    background: rgba(229, 9, 20, 0.1);
                    padding: 10px 15px;
                    border-radius: 8px;
                    border-left: 4px solid #e50914;
                    margin: 15px 0;
                    font-family: monospace;
                    color: #ff6b6b;
                }
                .get-started {
                    text-align: center;
                    margin: 40px 0;
                }
                .get-started h3 {
                    color: #e50914;
                    margin-bottom: 20px;
                }
                .steps {
                    display: flex;
                    justify-content: center;
                    gap: 20px;
                    margin: 30px 0;
                    flex-wrap: wrap;
                }
                .step {
                    background: rgba(40, 40, 40, 0.8);
                    padding: 20px;
                    border-radius: 10px;
                    width: 150px;
                    text-align: center;
                    border: 1px solid rgba(229, 9, 20, 0.2);
                }
                .step-number {
                    display: block;
                    width: 40px;
                    height: 40px;
                    line-height: 40px;
                    background: #e50914;
                    color: white;
                    border-radius: 50%%;
                    margin: 0 auto 15px;
                    font-weight: bold;
                    font-size: 18px;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <!-- Шапка -->
                <div class="header">
                    <div class="logo">
                        <span class="logo-red">CINEMA</span><span class="logo-white">PLUS</span>
                    </div>
                    <div class="welcome-title">
                        🎬 ДОБРО ПОЖАЛОВАТЬ!
                    </div>
                </div>
        
                <!-- Основной контент -->
                <div class="content">
                    <h1 class="greeting">
                        Приветствуем вас, <span class="username">%s</span>! 👋
                    </h1>
        
                    <p style="color: #cccccc; font-size: 16px; line-height: 1.6;">
                        Спасибо за регистрацию в <strong style="color:#e50914;">CINEMA<span style="color:#ffffff;">PLUS</span></strong> —
                        ваш портал в мир кино! Теперь вы стали частью нашего сообщества любителей кино.
                    </p>
        
                    <!-- Ваш email -->
                    <div class="email-highlight">
                        📧 Ваш email для входа: <strong>%s</strong>
                    </div>
        
                    <!-- Как начать -->
                    <div class="get-started">
                        <h3>🚀 Как начать пользоваться сервисом:</h3>
                        <div class="steps">
                            <div class="step">
                                <span class="step-number">1</span>
                                <strong>Войдите</strong> в ваш аккаунт
                            </div>
                            <div class="step">
                                <span class="step-number">2</span>
                                <strong>Выберите</strong> фильм и сеанс
                            </div>
                            <div class="step">
                                <span class="step-number">3</span>
                                <strong>Забронируйте</strong> места онлайн
                            </div>
                        </div>
                    </div>
        
                    <!-- Блок с преимуществами -->
                    <div class="highlight-box">
                        <h3 style="color: #e50914; margin-top: 0; text-align: center;">✨ Ваши новые возможности:</h3>
        
                        <div class="features">
                            <div class="feature-item">
                                <span class="feature-icon">🎟️</span>
                                <span><strong>Бронирование билетов онлайн</strong> — выбирайте лучшие места одним кликом</span>
                            </div>
                            <div class="feature-item">
                                <span class="feature-icon">⚡</span>
                                <span><strong>Мгновенное подтверждение</strong> — билеты сразу на вашу почту</span>
                            </div>
                            <div class="feature-item">
                                <span class="feature-icon">📱</span>
                                <span><strong>Электронные билеты с QR-кодом</strong> — покажите на входе в зал</span>
                            </div>
                            <div class="feature-item">
                                <span class="feature-icon">🔒</span>
                                <span><strong>Безопасная оплата</strong> — все платежи защищены</span>
                            </div>
                        </div>
                    </div>
        
                    <!-- Призыв к действию (без конкретной ссылки) -->
                    <div style="text-align: center; margin-top: 40px;">
                        <p style="color: #aaaaaa; margin-bottom: 20px;">
                            Чтобы начать бронирование, просто войдите в ваш аккаунт
                        </p>
                        <div class="cta-button">
                            🎫 НАЧАТЬ БРОНИРОВАНИЕ
                        </div>
                </div>
        
                <!-- Подвал -->
                <div class="footer">
                    <p style="margin: 0 0 10px 0;">
                        <strong style="color:#e50914;">CINEMA<span style="color:#ffffff;">PLUS</span></strong> — современный кинотеатр
                    </p>
                    <p style="margin: 0; font-size: 11px; color: #666;">
                        © 2025 CinemaPlus Demo Project. Это автоматическое сообщение.
                    </p>
                </div>
            </div>
        </body>
        </html>
        """,
                escape(event.username()),
                escape(event.email())
        );
    }
    private String escape(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

}