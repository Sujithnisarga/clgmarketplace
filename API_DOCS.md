# College Marketplace API Documentation

Base URL: `http://localhost:8080/api`

All protected endpoints require: `Authorization: Bearer <token>`

---

## Auth

### POST /auth/register
Register a new user.
```json
// Request
{ "name": "John Doe", "email": "john@mit.edu", "password": "secret123", "college": "MIT" }

// Response 201
{ "token": "eyJ...", "user": { "id": 1, "name": "John Doe", "email": "john@mit.edu" } }
```

### POST /auth/login
```json
// Request
{ "email": "john@mit.edu", "password": "secret123" }

// Response 200
{ "token": "eyJ...", "user": { "id": 1, "name": "John Doe", ... } }
```

---

## Users

### GET /users/me 🔒
Returns current user profile.

### PUT /users/me 🔒
Update profile. Multipart form: `name`, `bio`, `college`, `avatar` (file).

### GET /users/dashboard 🔒
Returns `{ listings, biddedItems, wonAuctions }`.

### GET /users/:id/profile
Returns `{ user, listings, reviews }`.

### POST /users/:id/review 🔒
Query params: `itemId`, `rating` (1-5), `comment`.

---

## Items

### GET /items
Browse items with optional filters:
- `search` — full-text search
- `category` — BOOKS | ELECTRONICS | CLOTHING | FURNITURE | SPORTS | OTHER
- `listingType` — FIXED | AUCTION
- `minPrice`, `maxPrice`
- `status` — ACTIVE | ENDED | SOLD (default: ACTIVE)
- `sort` — newest | ending_soon | price_asc | price_desc

### GET /items/:id
Get single item with seller and highest bidder info.

### POST /items 🔒
Multipart form:
- `data` (JSON blob):
```json
// Fixed price
{ "title": "Calculus Book", "description": "...", "category": "BOOKS", "listingType": "FIXED", "price": 25.00 }

// Auction
{
  "title": "MacBook Pro", "description": "...", "category": "ELECTRONICS",
  "listingType": "AUCTION", "startingBid": 500.00,
  "durationMinutes": 1440,
  "antiSnipe": true, "antiSnipeExtendMinutes": 5, "antiSnipeThresholdSeconds": 60
}
```
- `images` (files, up to 5)

### PUT /items/:id 🔒
Same as POST. Only seller can edit. Cannot edit auction with active bids.

### DELETE /items/:id 🔒
Only seller can delete.

---

## Bids

### POST /bids/:itemId 🔒
Place a bid on an auction.
```json
// Request
{ "amount": 550.00 }

// Response 201
{ "bid": { "id": 1, "bidder": {...}, "amount": 550.00 }, "currentBid": 550.00, "endTime": "..." }
```

Validations:
- Item must be AUCTION type
- Auction must be ACTIVE and not expired
- Amount must be > currentBid
- Seller cannot bid on own item

### GET /bids/:itemId
Get bid history for an item (newest first).

---

## Chat

### POST /chat 🔒
```json
{ "receiverId": 2, "itemId": 5, "content": "Is this still available?" }
```

### GET /chat/conversation/:partnerId 🔒
Get full conversation with a user.

### GET /chat/partners 🔒
Get list of users you've chatted with.

---

## Payments

### POST /payments/checkout 🔒
```json
{ "itemId": 5 }
// Response: { "url": "https://checkout.stripe.com/..." }
```

---

## Admin (ADMIN role only)

### GET /admin/stats
Returns `{ totalUsers, totalItems, activeAuctions }`.

### GET /admin/users
List all users.

### DELETE /admin/users/:id
Delete a user.

### PUT /admin/users/:id/role?role=ADMIN
Change user role.

### GET /admin/items
List all items.

### DELETE /admin/items/:id
Delete any item.

---

## WebSocket (STOMP over SockJS)

Connect to: `http://localhost:8080/ws`

### Subscribe to item updates:
`/topic/item/{itemId}` — receives bid updates and auction end events

**New Bid payload:**
```json
{
  "itemId": 5,
  "currentBid": 550.00,
  "highestBidder": { "id": 2, "name": "Jane" },
  "endTime": "2024-01-15T14:30:00",
  "latestBid": { "id": 10, "amount": 550.00, ... }
}
```

**Auction Ended payload:**
```json
{ "type": "AUCTION_ENDED", "itemId": 5, "status": "SOLD", "finalBid": 550.00, "winner": {...} }
```

### Subscribe to global auction changes:
`/topic/auctions` — receives `{ type: "STATUS_CHANGE", itemId, status }`

### Subscribe to personal notifications (authenticated):
`/user/{userId}/queue/outbid` — outbid notification
`/user/{userId}/queue/messages` — new chat message

---

## Database Schema (Key Auction Fields)

```sql
items:
  id, seller_id, title, description, category, listing_type,
  price,                    -- fixed price
  starting_bid,             -- auction starting bid
  current_bid,              -- current highest bid
  highest_bidder_id,        -- FK to users
  start_time,               -- auction start
  end_time,                 -- auction end (indexed)
  status,                   -- ACTIVE | ENDED | SOLD
  anti_snipe,               -- boolean
  anti_snipe_extend_minutes,
  anti_snipe_threshold_seconds,
  created_at
```
