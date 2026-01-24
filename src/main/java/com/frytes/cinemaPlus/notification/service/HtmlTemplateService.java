package com.frytes.cinemaPlus.notification.service;

import com.frytes.cinemaPlus.booking.event.BookingPaidEvent;
import com.frytes.cinemaPlus.booking.event.TicketDetail;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
public class HtmlTemplateService {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public String createBookingConfirmation(BookingPaidEvent event) {
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
            <!--[if mso]>
            <style>
                .ticket { width: 580px !important; }
                .gradient-red { background: #d00000 !important; }
            </style>
            <![endif]-->
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
                        <!-- Main Horizontal Ticket -->
                        %s
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
                                                            г. Москва, ул. Кинематографическая, 7
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
                                                            support@cinemaplus.ru
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
       \s""", buildHtmlTicket(event));
    }

    private String buildHtmlTicket(BookingPaidEvent event) {
        String qrUrl = String.format(
                "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=CINEMA-%s&color=000000&bgcolor=ffffff",
                URLEncoder.encode(event.orderId().toString(), StandardCharsets.UTF_8)
        );

        String seatsFormatted = formatSeats(event.tickets());
        String seatsCount = event.tickets().size() + " " + getSeatWord(event.tickets().size());

        return """
        <!-- Horizontal Ticket Container -->
        <table width="100%%" cellpadding="0" cellspacing="0" 
               style="
                   max-width: 680px;
                   background: linear-gradient(135deg, #1a1a1a 0%%, #0d0d0d 100%%);
                   border-radius: 20px;
                   box-shadow: 0 20px 60px rgba(255, 0, 0, 0.15);
                   border: 2px solid rgba(255, 0, 0, 0.3);
                   overflow: hidden;
               ">
            
            <!-- Красная полоса слева -->
            <tr>
                <td style="
                    width: 8px;
                    background: linear-gradient(to bottom, #ff0000, #d00000, #9d0208);
                "></td>
                
                <!-- Основное содержимое -->
                <td style="padding: 30px 25px;">
                    
                    <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr>
                            <!-- Левая часть - информация -->
                            <td style="padding-right: 25px; vertical-align: top;">
                                
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
                                            %s | %s
                                        </span>
                                    </div>
                                </div>
                                
                                <!-- Информационные блоки в ряд -->
                                <table width="100%%" cellpadding="0" cellspacing="0" 
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
                                                    🎫 КОЛИЧЕСТВО БИЛЕТОВ
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
                                
                            </td>
                            
                            <!-- Правая часть - QR код -->
                            <td style="
                                width: 180px;
                                vertical-align: middle;
                                text-align: center;
                                border-left: 2px dashed rgba(255, 0, 0, 0.3);
                                padding-left: 20px;
                            ">
                                
                                <div style="
                                    background: #ffffff;
                                    padding: 15px;
                                    border-radius: 12px;
                                    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
                                    border: 2px solid #ff0000;
                                ">
                                    <img src="%s"
                                         width="140"
                                         height="140"
                                         alt="QR-код билета"
                                         style="
                                             display: block;
                                             border-radius: 6px;
                                         "
                                    />
                                    
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
                                
                            </td>
                        </tr>
                    </table>
                    
                </td>
            </tr>
            
            <!-- Нижняя красная полоса -->
            <tr>
                <td colspan="2">
                    <div style="
                        height: 6px;
                        background: linear-gradient(90deg, 
                            #ff0000 0%%, 
                            #d00000 20%%, 
                            #9d0208 40%%, 
                            #9d0208 60%%, 
                            #d00000 80%%, 
                            #ff0000 100%%
                        );
                    "></div>
                </td>
            </tr>
            
        </table>
        """.formatted(
                escape(event.movieTitle()),
                event.eventTime().format(DATE_FORMATTER),
                event.eventTime().format(TIME_FORMATTER),
                seatsCount,
                event.amount(),
                event.eventTime().format(TIME_FORMATTER),
                seatsFormatted,
                qrUrl
        );
    }

    private String formatSeats(java.util.List<TicketDetail> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            return "<p style='color:#888888; font-size:14px; margin:0;'>Места не указаны</p>";
        }

        StringBuilder seatsHtml = new StringBuilder();

        for (TicketDetail ticket : tickets) {
            boolean isVip = ticket.type().toString().equalsIgnoreCase("VIP");
            int rowNumber = ticket.rowIndex() + 1;

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
                    rowNumber,
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