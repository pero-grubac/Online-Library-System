<div align="center">

# 📚 Online Library System

![Java](https://img.shields.io/badge/Java-Used-red?logo=java&logoColor=white)
![WebSockets](https://img.shields.io/badge/WebSockets-Enabled-blue?logo=websocket)
![REST API](https://img.shields.io/badge/REST%20API-Available-brightgreen?logo=apachespark)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Used-orange?logo=rabbitmq)
![RMI](https://img.shields.io/badge/RMI-Supported-yellow?logo=protocols)
![Redis](https://img.shields.io/badge/Redis-Database-red?logo=redis&logoColor=white)
![Tomcat](https://img.shields.io/badge/Tomcat-9-yellow?logo=apachetomcat&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Infrastructure-2496ED?logo=docker&logoColor=white)

</div>

## 📌 Project Overview

A multi-component Java system that simulates an online library platform with real-time communication and integration. The system consists of three independent applications — Library, Member, and Supplier — each communicating through different protocols and technologies.

---

## ✨ Key Features

- **Library Application** — GUI for staff to manage members, books, and reservations
- **Member Application** — GUI for members to browse, reserve, and download books
- **Supplier Application** — GUI for suppliers to handle inventory and book orders

---

## 📋 Library Application

![Library App](readme%20assets/library.gif)

- Manage members: approve, reject, add, update, or delete member data
- Book inventory: CRUD operations using Redis as the database
- Handle book reservations with review workflow
- Order books from suppliers using socket-based communication
- Test data is generated at startup for demonstration purposes

---

## 👤 Member Application

![Member App](readme%20assets/member.gif)

- **Registration** — Members register via REST endpoints with personal details
- **Login** — Simple authentication system using username/password
- **Book browsing** — Table view with filtering and search options
- **Book details** — View cover and preview (first 100 lines)
- **Download books** — Select books to receive via email as ZIP
- **Chat** — Secure socket-based chat system between members
- **Multicast** — Suggest books for purchase visible to other users

---

## 📦 Supplier Application

![Supplier App](readme%20assets/supplier.gif)

- Manage own inventory and book offerings to the library
- Connects to Project Gutenberg for automatic book fetching
- Process orders received from library via message queue (FIFO)
- Generate invoices and send them to the accounting system using RMI
- Applies 17% VAT in invoice totals

---

## 📂 Technical Details

| Technology | Usage |
|------------|-------|
| Java | Core language for all applications |
| Redis | Book inventory storage |
| REST API | Member registration and data exchange |
| WebSockets | Real-time chat between members |
| Multicast | Book purchase suggestions |
| RabbitMQ | Order queue between library and supplier |
| RMI | Invoice transmission to accounting system |
| XML | Data serialization |

- **Logging** — Built-in logger handles exception recording
- **Configuration** — Properties files used for paths and ports
- **Serialization** — Custom serialization used for invoice data
- **Concurrency** — Supports one library app, multiple member and supplier apps running concurrently

---

## 🚀 How to Run

> ⚠️ Services must be started in the exact order listed below.

### Prerequisites
- Java 11 JDK installed
- Docker and Docker Compose installed
- Apache Tomcat 9 installed
- RabbitMQ management plugin enabled

---

### Step 1 — Start infrastructure (Docker)

Start Redis and RabbitMQ using Docker Compose:

```bash
docker compose up -d
```

RabbitMQ Management UI will be available at `http://localhost:15672` (guest/guest).

---

### Step 2 — Build and deploy LibraryServer to Tomcat

LibraryServer is a WAR project that must be compiled and deployed to Tomcat manually.

**Compile sources** (run from `LibraryServer/` folder in cmd.exe):

```cmd
javac -encoding UTF-8 -source 8 -target 8 -cp "src\main\webapp\WEB-INF\lib\*" -d bin ^
  src\main\java\org\unibl\etf\mdp\model\*.java ^
  src\main\java\org\unibl\etf\mdp\libraryserver\properties\AppConfig.java ^
  src\main\java\org\unibl\etf\mdp\libraryserver\logger\FileLogger.java ^
  src\main\java\org\unibl\etf\mdp\libraryserver\mock\MockUsers.java ^
  src\main\java\org\unibl\etf\mdp\libraryserver\repository\*.java ^
  src\main\java\org\unibl\etf\mdp\libraryserver\service\*.java ^
  src\main\java\org\unibl\etf\mdp\libraryserver\api\*.java ^
  src\main\java\org\unibl\etf\mdp\libraryserver\app\App.java
```

**Build WAR file:**

```cmd
rmdir /s /q war
mkdir war\WEB-INF\classes
xcopy /s bin\* war\WEB-INF\classes\
xcopy /s src\main\webapp\WEB-INF\lib\* war\WEB-INF\lib\
copy src\main\webapp\WEB-INF\properties\app.properties war\WEB-INF\classes\org\unibl\etf\mdp\libraryserver\properties\
copy src\main\webapp\WEB-INF\web.xml war\WEB-INF\
cd war
jar -cvf ..\LibraryServer.war .
cd ..
```

**Deploy to Tomcat:**

```cmd
copy /y LibraryServer.war <TOMCAT_HOME>\webapps\
```

**Start Tomcat:**

```cmd
<TOMCAT_HOME>\bin\startup.bat
```

Verify deployment at: `http://localhost:8080/LibraryServer/api/users/`

---

### Step 3 — Start DiscoveryServer

Open in IntelliJ and run `App.java` with VM options:

```
-Djava.security.manager -Djava.security.policy="<path>\DiscoveryServer\security\server_policyfile.txt"
```

---

### Step 4 — Start AccountingServer

Open in IntelliJ and run `App.java` with VM options:

```
-Djava.security.manager -Djava.security.policy="<path>\AccountingServer\security\server_policyfile.txt"
```

---

### Step 5 — Start SupplierServer

Open in IntelliJ and run `App.java` with VM options:

```
-Djava.security.manager -Djava.security.policy="<path>\SupplierServer\security\server_policyfile.txt"
```

---

### Step 6 — Start Supplier GUI

Open in IntelliJ and run `App.java` with VM options:

```
-Djava.security.manager -Djava.security.policy="<path>\Supplier\security\client_policyfile.txt"
```

---

### Step 7 — Start Library GUI

Open in IntelliJ and run `App.java`. No VM options required.

---

### Step 8 — Start User/Member GUI

Open in IntelliJ and run `App.java` with VM options:

```
-Djavax.net.ssl.trustStore="<path>\User\keystore.jks"
-Djavax.net.ssl.trustStorePassword=password123
-Djavax.net.ssl.keyStore="<path>\User\keystore.jks"
-Djavax.net.ssl.keyStorePassword=password123
```

> Multiple instances of User GUI can be run simultaneously to test chat functionality.
> Enable **Allow multiple instances** in IntelliJ Run Configuration.

---

## 🏗️ Architecture

```
Library GUI ──────────────────────────────────────────┐
     │                                                 │
     ├── REST API ──────────── Member GUI              │
     │                              │                  │
     ├── WebSocket ──────────── Chat between members   │
     │                                                 │
     ├── Multicast ──────────── Book suggestions       │
     │                                                 │
     └── Socket ─────────────── Supplier GUI           │
                                      │                │
                                 RabbitMQ (orders)     │
                                      │                │
                              AccountingServer ◄── RMI ┘
                                      │
                                   Redis (inventory)

LibraryServer (REST API) ──── Tomcat 9 ──── localhost:8080
Redis ────────────────────── Docker ──────── localhost:6379
RabbitMQ ─────────────────── Docker ──────── localhost:5672
```