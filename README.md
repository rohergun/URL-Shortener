## URL Shortener

A URL shortening service built with Spring Boot and DynamoDB. 

Converts long URLs into short, shareable links with automatic expiry.

### How it works:
1. `POST/shorten` accepts a long URL and returns 7 character short code
2. `GET/{shorCode}` returns a code with **http 302** redirect
3. Short codes randomly generated (using Base62 **(a-z,A-Z,0-9)**)
4. Mappings expire automatically after 30 days via DynamoDB native TTL

### Running Locally:
No AWS account needed. DynamoDB runs locally via Docker.

**Prerequisites: Docker, Java 21, Maven**

```bash
# Clone the repo
git clone https://github.com/roh/url-shortener
cd url-shortener

# Start the app and DynamoDB Local
docker compose up --build
```

Local DynamoDB Admin (Optional)

To inspect the DynamoDB table locally, use dynamodb-admin:
```basb
npx dynamodb-admin
# opens UI at http://localhost:8001
```

## API
**Shorten a URL:**
```bash
POST /shorten
Content-Type: application/json

{ "url": "https://some url" }
```
**Response:**
```json
{ "shortUrl": "http://localhost:8080/xK3mPqR" }
```

**Redirect:**

`GET /{shortCode}`

Response: 302 Found with Location header pointing to the original URL.

```bash
curl -L http://localhost:8080/xK3mPqR
# follows redirect to https://url-that-you-entered
```

## URL Validation
**The /shorten endpoint rejects invalid input before saving:**

| Case | Response |
|---|---|
| Empty or null URL | `400 Bad Request` |
| Exceeds 2048 characters | `400 Bad Request` |
| Malformed URL | `400 Bad Request` |
| Non http/https scheme (`ftp://`, `javascript://`) | `400 Bad Request` |
| Missing host | `400 Bad Request` |
| Localhost or private IP ranges | `400 Bad Request` |

Private IP blocking prevents SSRF (Server-Side Request Forgery) attacks, where an attacker could use the shortener to probe internal network addresses.

## Planned Improvements
**User accounts** — PostgreSQL for user domain (ownership, plans, history). Polyglot persistence: DynamoDB for shortener throughput, Postgres for relational user data
- **Redis caching** — cache hot short codes in memory to reduce DynamoDB reads at scale
- **Click analytics** — track redirects per short code, expose `GET /{shortCode}/stats`
- **Custom aliases** — let users define their own short code instead of random generation
- **Rate limiting** — prevent abuse on `/shorten` via bucket4j


