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

### 4. Forget Password

**`GET /auth/forget-password`**

#### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `email` | string | Yes | User's email address |
| `userType` | string | No | Specify user type if multiple accounts exist with same email |

#### Example Request

```bash
curl -X 'GET' \
  'http://localhost:8080/auth/forget-password?email=piash@gmail.com&userType=customer' \
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
  "data": "verification code sent to piash@gmail.com"
}
```

---

### 5. Reset Password

**`GET /auth/reset-password`**

#### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `email` | string | Yes | User's email address |
| `otp` | string | Yes | Verification code from email |
| `newPassword` | string | Yes | New password |
| `userType` | string | No | Specify user type if multiple accounts exist |

#### Example Request

```bash
curl -X 'GET' \
  'http://localhost:8080/auth/reset-password?email=piash599%40gmail.com&otp=9889&newPassword=p1234&userType=customer' \
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
  "data": "Password change successful"
}
```

---

### 6. Change Password

**`PUT /auth/change-password`**

#### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `oldPassword` | string | Yes | Current password |
| `newPassword` | string | Yes | New password |

#### Headers

| Header | Value | Required |
|--------|-------|----------|
| `Authorization` | `Bearer <access_token>` | Yes |

#### Example Request

```bash
curl -X 'PUT' \
  'http://localhost:8080/auth/change-password?oldPassword=p1234&newPassword=newp1234' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...'
```

#### Example Response

```json
{
  "isSuccess": true,
  "statusCode": {
    "value": 200,
    "description": "OK"
  },
  "data": "Password has been changed"
}
```

---

### 7. Change User Type

**`PUT /auth/{userId}/change-user-type`**

Change an existing user's account type. This endpoint is available to administrators.

#### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | string | Yes | Unique identifier of the user to update |

#### Query Parameters

| Parameter | Type | Required | Description                                                       |
|-----------|------|----------|-------------------------------------------------------------------|
| `userType` | string | Yes | New user type to assign (`ADMIN`, `USER`) |

#### Headers

| Header | Value | Required |
|--------|-------|----------|
| `accept` | `application/json` | Yes |
| `Authorization` | `Bearer <access_token>` | Yes (Admin/Super Admin) |

#### Example Request

```bash
curl -X 'PUT' \
  'http://localhost:8080/auth/a67fd0cc-3d92-4259-bbd4-1e0ba49dece4/change-user-type?userType=ADMIN' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...'
```

#### Example Response

```json
{
  "isSuccess": true,
  "statusCode": {
    "value": 200,
    "description": "OK"
  },
  "data": "User type changed successfully to ADMIN"
}
```

---

### 8. Deactivate User

**`PUT /auth/{userId}/deactivate`**

#### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | string | Yes | Unique identifier of the user to deactivate |

#### Headers

| Header | Value | Required |
|--------|-------|----------|
| `accept` | `application/json` | Yes |
| `Authorization` | `Bearer <access_token>` | Yes (Admin/Super Admin) |

#### Example Request

```bash
curl -X 'PUT' \
  'http://localhost:8080/auth/a67fd0cc-3d92-4259-bbd4-1e0ba49dece4/deactivate' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...'
```

#### Example Response

```json
{
  "isSuccess": true,
  "statusCode": {
    "value": 200,
    "description": "OK"
  },
  "data": "User deactivated successfully"
}
```

---

### 9. Activate User

**`PUT /auth/{userId}/activate`**

#### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | string | Yes | Unique identifier of the user to activate |

#### Headers

| Header | Value | Required |
|--------|-------|----------|
| `accept` | `application/json` | Yes |
| `Authorization` | `Bearer <access_token>` | Yes (Admin/Super Admin) |

#### Example Request

```bash
curl -X 'PUT' \
  'http://localhost:8080/auth/a67fd0cc-3d92-4259-bbd4-1e0ba49dece4/activate' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...'
```

#### Example Response

```json
{
  "isSuccess": true,
  "statusCode": {
    "value": 200,
    "description": "OK"
  },
  "data": "User activated successfully"
}
```

---