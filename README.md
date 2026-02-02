# 🎬 CinemaPlus — Enterprise High-Load Ticketing Platform with Real-Time Booking & Observability

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-green?style=for-the-badge&logo=spring-boot)
![React](https://img.shields.io/badge/React-18-blue?style=for-the-badge&logo=react)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?style=for-the-badge&logo=postgresql)
![Kafka](https://img.shields.io/badge/Apache_Kafka-Event_Driven-231f20?style=for-the-badge&logo=apachekafka)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Orchestration-326ce5?style=for-the-badge&logo=kubernetes)
![Grafana](https://img.shields.io/badge/Grafana-Observability-F46800?style=for-the-badge&logo=grafana)

**CinemaPlus** — это Enterprise-ready система бронирования билетов, построенная по архитектуре **Modular Monolith**. Проект имитирует работу реального высоконагруженного сервиса с акцентом на надежность транзакций, масштабируемость и наблюдаемость (Observability).

🔗 **Live Demo:** [https://cinema-plus.ru]
![Краткое демо](docs/gif/gif.gif)
---
[![Видео-демо CinemaPlus](docs/screenshots/MainPage.png)](https://dzen.ru/video/watch/697fdba971f0967fd909b119)

**▶️ [Смотреть полное видео на Dzen](https://dzen.ru/video/watch/697fdba971f0967fd909b119)**  
*Показаны: бронирование, админ-панель, WebSocket-синхронизация, Grafana*

## 🏛️ Архитектура и Ключевые Решения

Проект спроектирован не как простой CRUD, а как сложная распределенная система, способная выдерживать конкурентную нагрузку.
### Архитектурная схема

### 1. Modular Monolith & Clean Architecture
Приложение разделено на независимые модули (`booking`, `content`, `notification`, `users`), что позволяет в будущем легко выносить их в микросервисы.
*   **ArchUnit Tests:** Автоматический контроль архитектуры. Тесты падают, если нарушаются границы модулей (например, Контроллер лезет в чужой Репозиторий).
*   **DTO Projection:** Сущности БД никогда не покидают слой сервисов.

### 2. Надежность данных (Data Consistency)
*   **Transactional Outbox Pattern:** Для гарантии доставки событий в Kafka используется таблица `outbox_events`. Событие "Оплата прошла" сохраняется в БД в той же транзакции, что и обновление статуса заказа. Отдельный `@Scheduled` процесс надежно отправляет их в брокер.
*   **Defense in Depth (Тройная валидация):**
    1.  **Frontend:** Блокировка занятых мест в UI.
    2.  **Backend:** Проверка бизнес-правил и статусов.
    3.  **Database:** `Pessimistic Locking` при бронировании и `UNIQUE Constraints` как последняя линия обороны от овербукинга.

### 3. Real-Time & Scaling (WebSockets)
Реализовано обновление схемы зала в реальном времени.
*   Если пользователь А выбрал место, пользователь Б сразу видит его как "Занятое".
*   **Масштабируемость:** WebSocket-сообщения синхронизируются между подами в Kubernetes через **Kafka**, что позволяет пользователям, подключенным к разным экземплярам сервиса, видеть одинаковое состояние зала.

### 4. Динамическое ценообразование (Strategy Pattern)
Система скидок реализована через паттерн **Strategy**, что позволяет включать/выключать бизнес-правила (скидка на утренние сеансы, наценка за VIP) **без перезагрузки приложения** и изменения кода, просто меняя флаги в БД через админку.

```mermaid
graph TB
    %% ========== КЛИЕНТЫ ==========
    CLIENT_USER["🎫 **Пользователь**<br/>Бронирование билетов"] 
    CLIENT_ADMIN["👔 **Администратор**<br/>Управление контентом"]
    
    %% ========== ФРОНТЕНД ==========
    FRONTEND["⚛️ **React Frontend**<br/>Vite + WebSocket"]
    ADMIN_PANEL["📊 **React Admin Panel**<br/>Dashboard + Analytics"]
    
    %% ========== БЭКЕНД ==========
    subgraph "**Spring Boot 3.2 (Modular Monolith)**"
        API_GATEWAY["🚪 **API Gateway**<br/>REST + WebSocket"]
        
        MODULE_BOOKING["🎫 **Booking Module**<br/>- Бронирование<br/>- Платежи<br/>- Outbox Pattern"]
        MODULE_CONTENT["🎬 **Content Module**<br/>- Фильмы/Залы<br/>- Сеансы"]
        MODULE_USERS["👥 **Users Module**<br/>- JWT Auth<br/>- Refresh Tokens"]
        MODULE_NOTIFY["🔔 **Notification Module**<br/>- Email (QR)<br/>- WebSocket"]
        MODULE_SIMULATE["⚡ **Simulation Module**<br/>- Нагрузочное тестирование"]
    end
    
    %% ========== ХРАНИЛИЩА ==========
    DB_MAIN[("**PostgreSQL**<br/>Основная БД + Liquibase")]
    DB_CACHE[("**Redis**<br/>Кэш + Pessimistic Locking")]
    DB_OUTBOX["**Outbox Table**<br/>Transactional Pattern"]
    
    %% ========== МЕССЕНДЖИНГ ==========
    MQ_KAFKA["⚫ **Apache Kafka**<br/>Event Streaming (KRaft)"]
    MQ_CONSUMER_EMAIL["📧 **Email Consumer**<br/>HTML Templates + QR"]
    MQ_CONSUMER_WS["🔄 **WebSocket Sync**<br/>Межсерверная синхронизация"]
    
    %% ========== МОНИТОРИНГ ==========
    MON_PROMETHEUS["📈 **Prometheus**<br/>Spring Actuator Metrics"]
    MON_LOKI["📝 **Loki**<br/>Centralized Logging"]
    MON_GRAFANA["📊 **Grafana**<br/>Real-time Dashboards"]
    MON_HEALTH["❤️ **Health Checks**<br/>K8s Readiness/Liveness"]
    
    %% ========== ДЕПЛОЙ ==========
    DEPLOY_K8S["☸️ **Kubernetes**<br/>Deployments + Services"]
    DEPLOY_CI["🔄 **GitHub Actions**<br/>CI/CD Pipeline"]
    
    %% ========== СВЯЗИ ==========
    CLIENT_USER --> FRONTEND
    CLIENT_ADMIN --> ADMIN_PANEL
    
    FRONTEND --> API_GATEWAY
    ADMIN_PANEL --> API_GATEWAY
    
    API_GATEWAY --> MODULE_BOOKING
    API_GATEWAY --> MODULE_CONTENT
    API_GATEWAY --> MODULE_USERS
    API_GATEWAY --> MODULE_NOTIFY
    
    MODULE_BOOKING --> DB_MAIN
    MODULE_CONTENT --> DB_MAIN
    MODULE_USERS --> DB_MAIN
    
    MODULE_BOOKING --> DB_CACHE
    MODULE_CONTENT --> DB_CACHE
    
    MODULE_BOOKING --> DB_OUTBOX
    DB_OUTBOX --> MQ_KAFKA
    
    MQ_KAFKA --> MQ_CONSUMER_EMAIL
    MQ_KAFKA --> MQ_CONSUMER_WS
    MQ_CONSUMER_WS --> FRONTEND
    
    MODULE_BOOKING --> MON_PROMETHEUS
    MODULE_CONTENT --> MON_PROMETHEUS
    API_GATEWAY --> MON_PROMETHEUS
    
    MON_PROMETHEUS --> MON_GRAFANA
    MON_LOKI --> MON_GRAFANA
    
    MODULE_SIMULATE --> MODULE_BOOKING
    MODULE_SIMULATE --> MODULE_CONTENT
    
    DEPLOY_K8S --> MODULE_BOOKING
    DEPLOY_K8S --> MODULE_CONTENT
    DEPLOY_CI --> DEPLOY_K8S
    
    %% ========== СТИЛИ ==========
    style CLIENT_USER fill:#f39c12,color:#000
    style CLIENT_ADMIN fill:#3498db,color:#fff
    style FRONTEND fill:#61dafb,color:#000
    style ADMIN_PANEL fill:#9b59b6,color:#fff
    style API_GATEWAY fill:#2ecc71,color:#fff
    style MODULE_BOOKING fill:#e74c3c,color:#fff
    style MODULE_CONTENT fill:#e50914,color:#fff
    style MODULE_USERS fill:#1abc9c,color:#000
    style MODULE_NOTIFY fill:#f1c40f,color:#000
    style MODULE_SIMULATE fill:#e67e22,color:#fff
    style DB_MAIN fill:#336791,color:#fff
    style DB_CACHE fill:#c0392b,color:#fff
    style DB_OUTBOX fill:#16a085,color:#fff
    style MQ_KAFKA fill:#000,color:#fff
    style MQ_CONSUMER_EMAIL fill:#8e44ad,color:#fff
    style MQ_CONSUMER_WS fill:#27ae60,color:#fff
    style MON_PROMETHEUS fill:#e74c3c,color:#fff
    style MON_LOKI fill:#00b894,color:#fff
    style MON_GRAFANA fill:#f46800,color:#fff
    style MON_HEALTH fill:#e84393,color:#fff
    style DEPLOY_K8S fill:#326ce5,color:#fff
    style DEPLOY_CI fill:#6e5494,color:#fff
    
    linkStyle 0 stroke:#f39c12,stroke-width:2px
    linkStyle 1 stroke:#3498db,stroke-width:2px
    linkStyle 2 stroke:#61dafb,stroke-width:2px
    linkStyle 3 stroke:#9b59b6,stroke-width:2px
    linkStyle 16 stroke:#e50914,stroke-width:3px,stroke-dasharray:5,5
```
---

## 🛠️ Технологический Стек

| Категория      | Технологии                                                |
|----------------|-----------------------------------------------------------|
| **Core**       | Java 21, Spring Boot 3.2, Spring Security                 |
| **Data**       | PostgreSQL, Liquibase, Redis (Caching & Locking)          |
| **Messaging**  | Apache Kafka (KRaft mode), Transactional Outbox           |
| **Frontend**   | React, Vite, Axios, StompJS (WebSockets)                  |
| **DevOps**     | Docker Compose, Kubernetes, GitHub Actions                |
| **Monitoring** | Prometheus, Grafana, Loki (PLG Stack)                     |
| **Testing**    | JUnit 5, Testcontainers, ArchUnit, Stress Simulation Tool |

---

## 📸 Демонстрация

### 1. Мониторинг и Нагрузка (Grafana + Stress Test)
В проект встроен **Simulation Service**, который генерирует тысячи ботов. Они регистрируются, ищут сеансы и конкурируют за билеты.
*Скриншоты Grafana под нагрузкой: HikariCP (пул соединений), Response Time и Kafka Throughput.* (3000 ботов)

![Grafana HikariCP](docs/screenshots/HikariCP.png)
![Grafana RPS](docs/screenshots/GrafanaResponseTime.png)
![Grafana Kafka](docs/screenshots/Kafka.png)

### 2. Инфраструктура в Kubernetes (K9s)
Проект полностью адаптирован для запуска в K8s. Используются `ConfigMaps`, `Secrets` и `HealthChecks`.

![K9s Pods](docs/screenshots/K9S.png)

### 3. Уведомления и Билеты
Асинхронная генерация красивых HTML-писем с QR-кодом (эмуляция MailHog).

![Email Ticket](docs/screenshots/Ticket.png)

---

## 🔐 Безопасность (Security)

*   **JWT + Refresh Token:** Реализована безопасная схема с ротацией Refresh токенов. Токены хранятся в БД (с привязкой к устройству/сессии).
*   **Smart Auto-Refresh:** Frontend (Axios Interceptor) автоматически перехватывает 401 ошибку, обновляет токен и повторяет исходный запрос прозрачно для пользователя.

---

## 🚀 Запуск проекта

### Локально (Docker Compose)
Самый простой способ поднять всё окружение (БД, Кафка, Бэкенд, Фронт, Мониторинг):

# 1. Сборка и запуск
```bash
git clone https://github.com/Frytes/CinemaPlus
cd CinemaPlus
APP_PROFILE=default docker compose --profile full up -d --build
```

# 2. Доступ к сервисам:

# Frontend: http://localhost:80  (admin/admin)
# Grafana: http://localhost:3000 (admin/admin)
# MailHog: http://localhost:8025 


## ‍💻 Автор
**Антон Рубель** — Java Backend Developer
Связь: Anton.Rubel.94@gmail.com | Telegram - **@Frytes94** 