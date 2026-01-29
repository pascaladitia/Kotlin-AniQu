# Authentication API

This documentation provides comprehensive details for the Authentication API endpoints. The API supports user registration, login, password management, and email verification functionality with support for multiple user types (admin & user).

**Base URL:** `http://localhost:8080`

### Auth Endpoints

| Method | Endpoint | Description | Authentication Required |
|--------|----------|-------------|------------------------|
| `POST` | `/auth/login` | Authenticate user and receive access token | No |
| `POST` | `/auth/register` | Register a new user account | No |

---

## Endpoint Details

### 1. User Login

**`POST /auth/login`**

Authenticate a user and receive an access token for subsequent API calls.

#### Request Parameters

| Parameter | Type | Required | Description                   |
|-----------|------|----------|-------------------------------|
| `email` | string | Yes | User's email address          |
| `password` | string | Yes | User's password               |
| `userType` | string | Yes | Type of user (`admin`, `user`) |

#### Example Request

```bash
curl -X 'POST' \
  'http://localhost:8080/auth/login' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "customer@gmail.com",
    "password": "p1234",
    "userType": "user"
  }'
```

#### Example Response

```json
{
  "isSuccess": true,
  "statusCode": {
    "value": 200,
    "description": "OK"
  },
  "data": {
    "user": {
      "id": "ce563774-d3d5-442e-ad1a-b884bb0a53f0",
      "email": "customer@gmail.com",
      "userType": "customer"
    },
    "accessToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9..."
  }
}
```

#### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `isSuccess` | boolean | Indicates if the request was successful |
| `statusCode` | object | HTTP status code and description |
| `data.user` | object | User information |
| `data.accessToken` | string | JWT token for authentication |

---

### 2. User Registration

**`POST /auth/register`**

#### Request Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `email` | string | Yes | User's email address |
| `password` | string | Yes | User's password |
| `userType` | string | Yes | Type of user (`customer`, `seller`, `admin`) |

#### Example Request

```bash
curl -X 'POST' \
  'http://localhost:8080/auth/register' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "test@gmail.com",
    "password": "p12345678",
    "userType": "admin"
  }'
```

#### Example Response

```json
{
  "isSuccess": true,
  "statusCode": {
    "value": 200,
    "description": "OK"
  },
  "data": {
    "id": "f48ec4f9-5482-4a23-9e49-e69f97bd20a6",
    "email": "piash@gmail.com"
  }
}
```

### 3. OTP Verification

**`POST /auth/otp-verification`**

#### Request Parameters

| Parameter  | Type | Required | Description |
|------------|------|----------|-------------|
| `userId`   | string | Yes | User's Id   |
| `otp`      | string | Yes | otp Input   |

#### Example Request

```bash
curl -X 'GET' \
  'http://localhost:8080/auth/otp-verification?userId=5994b239-67b3-4004-ad2b-f01de650e5d8&otp=355498' \
  -H 'accept: application/json'
```

#### Example Response

```json
{
  "isSuccess": true,
  "statusCode": {
    "value": 200,
    "description": "OK"
  },
  "data": true
}
```