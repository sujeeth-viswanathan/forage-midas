# 💰 Midas Core Simulation – J.P. Morgan Forage Program

This repository contains my completed implementation of the **J.P. Morgan Chase Software Engineering Virtual Experience** on [Forage](https://www.theforage.com/).  
The simulation models a backend financial system that processes transactions, calculates incentives, and provides user balance lookups.

---

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot**
- **Spring Kafka**
- **Spring Data JPA**
- **H2 In-Memory Database**
- **Maven Wrapper**
- **REST API / JSON**
- **Kafka Listener**

---

## 🚀 Features

✅ Transaction consumer using **Apache Kafka**  
✅ Calls an **external Incentive API** to calculate transaction rewards  
✅ **Updates user balances** in an in-memory H2 database  
✅ Exposes a **REST API** (`/balance`) to retrieve user balances  
✅ Rejects invalid or duplicate transactions  
✅ Runs on **custom port 33400**

---

## 📦 Setup & Usage

### 📁 Folder Structure
forage-midas/
├── src/
├── services/
│ └── incentives-api.jar
├── pom.xml
├── application.yaml
└── README.md
### 🧱 Step 1: Run the Incentive API

Start the provided Incentive API JAR (runs on port `8080`):

```bash
java -jar services/incentives-api.jar
Make sure this is running before starting Midas Core.

🚀 Step 2: Run the Spring Boot App

Use the Maven wrapper to start the app:

./mvnw spring-boot:run

Or on Windows:

mvnw.cmd spring-boot:run

App will start on:
📍 http://localhost:33400
🔄 Step 3: Kafka Topic

The application listens for Transaction messages on this Kafka topic:

midas-task

Transactions are validated, processed, and stored, with incentives added for recipients.

🌐 API – Get User Balance
Endpoint:
GET /balance?userId={userId}

Example:
GET http://localhost:33400/balance?userId=2

Response (JSON):
{
  "amount": 3089.0
}

If the user does not exist, returns:

{
  "amount": 0.0
}

🧪 Running Tests
Run the final validation test with:

./mvnw test -Dtest=TaskFiveTests

Expected output (example):

---begin output---
Balance {amount=0.0}
Balance {amount=1326.98}
...
---end output---
✅ Submit the full block (including the begin/end) for verification.

🧠 Logic Summary
1 Transactions are consumed via Kafka.
2 Each transaction is validated and processed.
3 Incentives are fetched via POST http://localhost:8080/incentive.
4 User balances are updated accordingly.
5 A REST controller provides a /balance endpoint.

📚 License
This project was completed as part of the J.P. Morgan Software Engineering Virtual Experience through Forage.
This is a personal educational submission and is not affiliated with or endorsed by JPMorgan Chase & Co.

🙋‍♂️ Author
Sujeeth Viswanathan– aspiring backend engineer
