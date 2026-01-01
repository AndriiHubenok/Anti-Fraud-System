# Anti-Fraud System

A RESTful API designed to detect and prevent fraudulent financial transactions. This system implements a rule-based engine that analyzes transactions in real-time based on specific heuristics, IP blacklists, and stolen card databases.

## 📋 Features

* **User Management:**
* Role-based access control (Administrator, Support, Merchant).
* User registration, locking/unlocking accounts, and role assignment.


* **Transaction Analysis:**
* Real-time transaction validation based on amount limits.
* Heuristic rules:
* **IP Correlation:** Detects if a card is used from multiple distinct IP addresses within a short timeframe.
* **Region Correlation:** Detects if a card is used in multiple distinct geographic regions within a short timeframe.


* Feedback loop: "Support" staff can manually adjust transaction results, which dynamically updates the fraud detection limits.


* **Blacklist Management:**
* **Suspicious IPs:** Add, remove, and list IPs blocked from transacting.
* **Stolen Cards:** Add, remove, and list card numbers flagged as stolen.


* **History & Auditing:**
* View full transaction history.
* Filter history by card number.



## 🛠️ Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 4
* **Security:** Spring Security (HTTP Basic Auth, Role-Based Access Control)
* **Database:** H2 (In-memory)
* **Build Tool:** Gradle

## 🔑 Roles & Permissions

| Role | Access Level | Responsibilities |
| --- | --- | --- |
| **ADMINISTRATOR** | User Management | Register/Delete users, Change roles, Lock/Unlock users. |
| **SUPPORT** | Fraud Management | Manage suspicious IPs, Stolen cards, View history, Provide feedback on transactions. |
| **MERCHANT** | Transaction Processing | Submit transactions for validation. |

> **Note:** The first registered user automatically receives the `ADMINISTRATOR` role. All subsequent users are registered as `MERCHANT` and are locked by default until unlocked by an Administrator.

## 📡 API Endpoints

### Authentication & Users

* `POST /api/auth/user` - Register a new user.
* `DELETE /api/auth/user/{username}` - Delete a user.
* `GET /api/auth/list` - List all users.
* `PUT /api/auth/role` - Change a user's role.
* `PUT /api/auth/access` - Lock or unlock a user.

### Anti-Fraud Services

* `POST /api/antifraud/transaction` - Validate a transaction. Returns `ALLOWED`, `MANUAL_PROCESSING`, or `PROHIBITED`.
* `PUT /api/antifraud/transaction` - Add feedback to a transaction (updates fraud limits).
* `GET /api/antifraud/history` - Get full transaction history.
* `GET /api/antifraud/history/{number}` - Get history for a specific card.

### Blacklists

* `POST /api/antifraud/suspicious-ip` - Add an IP to the blacklist.
* `DELETE /api/antifraud/suspicious-ip/{ip}` - Remove an IP from the blacklist.
* `GET /api/antifraud/suspicious-ip` - List all suspicious IPs.
* `POST /api/antifraud/stolencard` - Add a card to the stolen list.
* `DELETE /api/antifraud/stolencard/{number}` - Remove a card from the stolen list.
* `GET /api/antifraud/stolencard` - List all stolen cards.

## ⚙️ Configuration

The application uses an H2 database stored in a file relative to the project directory.
**`src/main/resources/application.properties`**:

```properties
server.port=28852
spring.datasource.url=jdbc:h2:file:../service_db

```
