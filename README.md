# File Upload Backend

A multi-tenant file upload and download backend built with **Spring Boot 3** and **Java 21**. It is designed for large files: the client uploads the file in chunks (S3 multipart upload) straight to **AWS S3** using pre-signed URLs, so the file never passes through the application's memory. File metadata, users, tenants and audit logs are stored in **MySQL** (split across two shards), and **Redis** is used for rate limiting.

I built this project to learn how real file-storage platforms (like Drive or Dropbox) work behind the scenes: large uploads, security, multi-tenancy and scaling.

---

## Features

**File handling**
- Chunk-based (multipart) upload for large files. The chunk size is calculated automatically (minimum 5 MB, and never more than 10,000 parts)
- Pre-signed URLs for each chunk, so the client uploads directly to S3
- Pre-signed download URLs, so the backend does not stream files either
- Storage quota check before an upload starts (it also counts uploads that are still in progress)
- File list, download count and recent activity for each tenant

**Multi-tenancy and users**
- Each organisation is a *tenant* with its own subdomain, users and storage quota
- Roles: `ADMIN`, `TENANT_ADMIN` and `MEMBER`
- Tenant admins can invite members by email using a registration link
- Plans: `FREE`, `BASIC`, `PREMIUM`, `ENTERPRISE`, each with its own storage and user limit
- Subscription expiry and suspended tenants are checked on every request using an interceptor

**Sharing**
- Share a file with another user in the same tenant with a permission (`VIEW`, `DOWNLOAD`, `EDIT`)
- Shares have a status (`ACTIVE`, `REVOKED`, `EXPIRED`) and an optional expiry time

**Security and reliability**
- Login with JWT (Spring Security) and Google OAuth2 sign-in
- Role-based access using `@PreAuthorize`
- Per-user and per-IP rate limiting with Bucket4j + Redis (HTTP 429 when the limit is crossed)
- Audit log for important actions (upload started/completed/failed, download, share, login, plan changes and more)
- Global exception handling with custom exceptions

**Scaling**
- Database sharding using Apache ShardingSphere, sharded by `tenant_id` across 2 MySQL databases
- Dockerfile and Docker Compose setup, plus basic Kubernetes YAML files

---

## Tech Stack

| Area | Technology |
|------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.6 |
| Security | Spring Security, JWT (jjwt 0.12.6), Google OAuth2 |
| Database | MySQL 8 with Spring Data JPA |
| Sharding | Apache ShardingSphere JDBC 5.5.2 |
| Storage | AWS S3 (AWS SDK v1) |
| Rate limiting | Bucket4j + Redis (Lettuce) |
| Build tool | Maven |
| Testing | JUnit, Mockito, Spring Security Test |
| Deployment | Docker, Docker Compose, Kubernetes YAML |

---

## How the Upload Works

The backend only hands out permissions and keeps track of metadata. The actual bytes go from the browser straight to S3.

```
Client                      Backend                          AWS S3
  |                            |                                |
  |-- 1. POST /files/initiate->|                                |
  |                            |-- check quota, create record ->|
  |                            |-- start multipart upload ----->|
  |<-- uploadId, chunkSize,    |                                |
  |    totalChunk, s3Key ------|                                |
  |                            |                                |
  |-- 2. POST /files/presigned-url (per chunk) ->               |
  |<-- pre-signed URL ---------|                                |
  |                            |                                |
  |-- 3. PUT chunk directly to the pre-signed URL ------------->|
  |<-- ETag --------------------------------------------------- |
  |                            |                                |
  |-- 4. POST /files/complete (list of ETags) ->|               |
  |                            |-- complete multipart upload -->|
  |<-- success ----------------|                                |
```

Download is simpler: the client calls `GET /api/private/download/{fileId}`, the backend checks that the file belongs to the user's tenant and returns a time-limited pre-signed URL.

---

## Project Structure

```
src/main/java/com/fileupload/fileproject
├── Config/          # Security, S3, CORS, sharding, Redis rate limit, interceptor
├── Filter/          # JWTFilter, RateLimitFilter
├── controller/      # REST controllers
├── service/         # Business logic (FileService, TenantService, ...)
├── repository/      # Spring Data JPA repositories
├── entity/          # JPA entities (Tenant, Users, FileMetadata, FileShare, ...)
├── enums/           # Roles, plans, upload status, rate limit rules, audit actions
├── requestDto/      # Request bodies
├── responseDto/     # Response bodies
├── context/         # Thread-local Tenant / User / Lookup context
├── Exception/       # Custom exceptions + global handler
└── util/            # JwtUtil, CustomUserDetails

src/main/resources
├── application.properties
└── sharding.yaml    # ShardingSphere config (2 MySQL shards)
```

---

## API Endpoints

All `/api/private/**` endpoints need a JWT in the `Authorization: Bearer <token>` header.

### Public

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/public/register/tenant` | Register a new organisation and its admin |
| POST | `/api/public/login` | Login (uses the `subdomain` query param) |
| POST | `/api/public/invitation/register/{token}` | Complete registration from an invitation |
| GET | `/api/public/plans` | List available plans |
| GET | `/oauth2/callback/google` | Google OAuth2 callback |

### Files (private)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/private/files/initiate` | Start a multipart upload |
| POST | `/api/private/files/presigned-url` | Get a pre-signed URL for one chunk |
| POST | `/api/private/files/complete` | Finish the upload with the list of ETags |
| GET | `/api/private/files` | List files of the tenant |
| GET | `/api/private/download/{fileId}` | Get a pre-signed download URL |
| GET | `/api/private/files/count` | Number of files |
| GET | `/api/private/users/count` | Number of users |
| GET | `/api/private/users/files/count` | Files uploaded per user |
| GET | `/api/private/recent-activity` | Recent activity (admin only) |
| GET | `/api/private/storageDetails` | Storage used vs. quota |

### Sharing, team and plans (private)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/private/shares` | Share a file with another user |
| GET | `/api/private/shares/me` | Files shared with me |
| GET | `/api/private/share` | Shares created by me |
| GET | `/api/private/members` | Members of my tenant |
| POST | `/api/private/invitations/send` | Invite a user by email (admin only) |
| PUT | `/api/private/tenant/upgrade` | Upgrade the plan (admin only) |

---

## Plans

| Plan | Storage | Max users |
|------|---------|-----------|
| Free | 10 GB | 5 |
| Basic | 50 GB | 20 |
| Premium | 100 GB | 50 |
| Enterprise | 500 GB | 200 |

---

## Getting Started

### Prerequisites

- Java 21
- Maven (or use the included `mvnw`)
- Docker and Docker Compose
- An AWS account with an S3 bucket and an IAM user that has access to it
- (Optional) A Google OAuth2 client ID and secret for Google login

### 1. Clone the repository

```bash
git clone <your-repo-url>
cd fileupload-backend-updateproject
```

### 2. Set the environment variables

Create a `.env` file in the project root (Docker Compose reads it automatically):

```env
# AWS S3
AWS_S3_BUCKET_NAME=your-bucket-name
AWS_S3_REGION=ap-south-1
AWS_S3_ACCESS_KEY=your-access-key
AWS_S3_SECRET_KEY=your-secret-key

# Redis (used for rate limiting)
SPRINGBOOT_REDIS_HOST=redis
SPRINGBOOT_REDIS_PORT=6379
SPRINGBOOT_REDIS_TIMEOUT=2000

# JPA
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.MySQLDialect

# Google OAuth2
CLIENT_ID=your-google-client-id
CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URL=http://localhost:8081/oauth2/callback/google
SPRING_TOKENENDPOINT=https://oauth2.googleapis.com/token
SPRING_USERINFOENDPOINT=https://www.googleapis.com/oauth2/v3/userinfo
SPRING_FROENTENDURL=http://localhost:3000

# CORS
CORS_ORIGIN=http://localhost:3000
```

> Never commit your `.env` file or real AWS keys to GitHub.

### 3. Build the Docker image

The `docker-compose.yaml` uses an image called `fileupload-backend`, so build it first:

```bash
docker build -t fileupload-backend .
```

### 4. Start everything

```bash
docker compose up -d
```

This starts:

| Service | Port |
|---------|------|
| MySQL shard 0 | 3307 |
| MySQL shard 1 | 3308 |
| Redis | 6379 |
| Backend | 8081 |
| Frontend | 3000 |

The backend is available at `http://localhost:8081`.

### Running without Docker for the backend

If you want to run only the backend from your IDE or terminal, start just the databases and Redis, then run the app:

```bash
docker compose up -d mysql-shard0 mysql-shard1 redis
./mvnw spring-boot:run
```

(You will need to export the same environment variables in your terminal or IDE first, and set `SPRINGBOOT_REDIS_HOST=localhost`.)

### Run the tests

```bash
./mvnw test
```

---

## Database Sharding

`src/main/resources/sharding.yaml` configures ShardingSphere with two MySQL databases (`shard0_db` and `shard1_db`). Tables like `tenants`, `users`, `file_metadata`, `file_shares`, `tenant_usage`, `tenant_invitations` and `audit_log` are split by `tenant_id` using the rule `tenant_id % 2`, so all the data of one organisation stays on the same shard. The `tenant_lookup` table (email to tenant/subdomain) is a *broadcast* table, which means it is copied to both shards.

## Rate Limiting

Every endpoint has its own limit defined in the `RateLimitRule` enum (for example 5 requests per minute for `initiate` and `login`). Logged-in users are limited by tenant and user ID, and public requests are limited by IP address. The buckets are stored in Redis, so the limit works even with more than one backend instance. When the limit is crossed the API returns `429 Too Many Requests`.

---

## What I Learned

- How S3 multipart upload and pre-signed URLs work, and why they are better than uploading through the server
- Designing a multi-tenant system and keeping every tenant's data separate
- Securing a REST API with JWT, roles and custom filters
- Sharding a database by tenant using ShardingSphere
- Distributed rate limiting with Redis and the token bucket algorithm
- Handling concurrency, for example locking the tenant usage row while checking quota
- Containerising a Spring Boot app with Docker and Docker Compose

## Future Improvements

- Actually send invitation emails (the email service is currently only a placeholder)
- Clean up abandoned multipart uploads automatically
- File deletion and a trash/restore feature
- Move secrets to AWS Secrets Manager or Kubernetes secrets
- More unit and integration tests
- API documentation with Swagger / OpenAPI

---

## Author

**Your Name**
GitHub: [@your-username](https://github.com/your-username)
