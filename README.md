# Transaction Risk Assessment API

This controller provides an entry point for evaluating the fraud risk of financial transactions.
It leverages an asynchronous orchestration engine to run multiple heuristic rules (Velocity, Geo-Anomaly, and Amount Spike) in parallel.

---

## Endpoint Details

### **Process Transaction**
Evaluates an incoming transaction against established risk rules and persists the data to MySQL if the transaction is not blocked.

*   **URL:** `/api/v1/transactions/process`
*   **Method:** `POST`
*   **Content-Type:** `application/json`

### **Request Body**
```json
{
  "transactionId": "TXN-78901",
  "userId": 1,
  "amount": 250.00,
  "latitude": 28.6139,
  "longitude": 77.2090
}
```

### **Success Response (Decision: APPROVE, VERIFY, or HOLD)**
*   **Code:** `200 OK`
*   **Content:**
```json
{
  "transactionId": "TXN-78901",
  "decision": "APPROVE",
  "totalScore": 15,
  "ruleScores": [
    {"rule": "VELOCITY", "score": 15, "reason": "4 txns in 5 mins"},
    {"rule": "GEO_ANOMALY", "score": 0, "reason": "Same city"},
    {"rule": "AMOUNT_SPIKE", "score": 0, "reason": "1.2x average"}
  ],
  "processingTimeMs": 42
}
```

### **Error Response (Decision: BLOCK)**
*   **Code:** `403 FORBIDDEN`
*   **Reason:** Returned when the cumulative risk score exceeds the critical threshold (80+).
*   **Note:** Blocked transactions are **not** persisted to the `transactions` database.

---

## Logic & Orchestration

The controller delegates processing to the `RiskOrchestrator`, which performs the following steps:

1.  **Parallel Execution:** Rules are executed concurrently using a dedicated thread pool to minimize latency.
2.  **Scoring Heuristics:**
    *   **Velocity:** Checks frequency of transactions in a 5-minute window.
    *   **Geo-Anomaly:** Uses the Haversine formula to detect "impossible travel" based on commercial flight speeds.
    *   **Amount Spike:** Compares current amount against the user's historical average.
3.  **Persistence:** If the decision is `APPROVE`, `VERIFY`, or `HOLD`, the transaction is saved to MySQL with its associated risk metadata.

---

## Decision Table Reference

| Total Score | Decision | Action |
| :--- | :--- | :--- |
| **0 - 40** | `APPROVE` | Transaction processed normally. |
| **41 - 60** | `VERIFY` | Challenge user (OTP/Security Question). |
| **61 - 80** | `HOLD` | Flag for manual review by the fraud team. |
| **81+** | `BLOCK` | Transaction rejected; Account potentially frozen. |

---

## Dependencies
*   **Spring Boot Web**: REST Endpoints.
*   **JPA/Hibernate**: MySQL Persistence.

MYSQL Tables Created:
-- Disable foreign key checks to ensure clean drops
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Drop tables if they exist
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS users;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- 2. Create User Table
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    email VARCHAR(100) UNIQUE NOT NULL
) ENGINE=InnoDB;

-- 3. Create Transaction Table
-- Matches your provided Entity structure
CREATE TABLE transactions (
    transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DOUBLE NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    timestamp DATETIME NOT NULL,
    risk_level VARCHAR(20), -- Stores the Decision (APPROVE, VERIFY, etc.)
    total_score VARCHAR(10), -- Stores the numeric score as a String per your entity
    INDEX idx_user_time (user_id, timestamp),
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;


