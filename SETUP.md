# College Marketplace & Auction House — Setup Instructions

## Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- A modern browser (Chrome/Firefox)
- (Optional) Node.js for serving the frontend via `live-server`

---

## 1. Database Setup

```sql
CREATE DATABASE clgmarketplace;
```

MySQL will auto-create tables via Spring JPA (`ddl-auto=update`).

---

## 2. Backend Setup

### Configure
Edit `backend/src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
app.jwt.secret=YOUR_LONG_SECRET_KEY_MIN_32_CHARS
stripe.secret.key=sk_test_YOUR_STRIPE_KEY   # optional
```

### Build & Run
```bash
cd backend
mvn clean install -DskipTests
mvn spring-boot:run
```

Server starts at: `http://localhost:8080`

The `uploads/` directory is auto-created in the backend root for file storage.

---

## 3. Frontend Setup

### Option A — VS Code Live Server (recommended)
1. Install the **Live Server** extension in VS Code
2. Open `frontend/index.html`
3. Click **Go Live** (bottom right)
4. Frontend runs at `http://127.0.0.1:5500`

### Option B — Node.js http-server
```bash
cd frontend
npx http-server -p 5500
```

### Option C — Python
```bash
cd frontend
python -m http.server 5500
```

> Make sure `app.cors.allowed-origins` in `application.properties` includes your frontend URL.

---

## 4. Create Admin User

After registering normally, run this SQL to promote a user to admin:
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'your@email.com';
```

Then access the admin panel at `/pages/admin.html`.

---

## 5. Stripe (Optional)

1. Create a free account at https://stripe.com
2. Get your test secret key from the Stripe Dashboard
3. Set `stripe.secret.key=sk_test_...` in `application.properties`
4. Use test card `4242 4242 4242 4242` with any future expiry

---

## Project Structure

```
clgmarketplace/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/clgmarket/app/
│       ├── ClgMarketplaceApplication.java
│       ├── config/          # Security, WebSocket, WebMvc
│       ├── controller/      # REST controllers
│       ├── dto/             # Request/Response DTOs
│       ├── entity/          # JPA entities
│       ├── exception/       # Global error handler
│       ├── repository/      # Spring Data JPA repos
│       ├── security/        # JWT util + filter
│       └── service/         # Business logic + scheduler
└── frontend/
    ├── index.html           # Browse listings
    ├── css/style.css
    ├── js/app.js            # Shared utilities
    └── pages/
        ├── login.html
        ├── register.html
        ├── item.html        # Item detail + live bidding
        ├── auctions.html    # Live auctions list
        ├── sell.html        # Create/edit listing
        ├── dashboard.html   # User dashboard
        ├── profile.html     # Public profile + reviews
        ├── chat.html        # Buyer-seller chat
        ├── admin.html       # Admin panel
        └── payment-success.html
```
