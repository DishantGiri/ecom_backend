# Admin Login Implementation Guide

This documentation outlines how to integrate the Admin Login and "Change Password on First Login" flow into your Next.js frontend.

## 1. Authentication Overview

The backend uses **JWT (JSON Web Token)** for authentication.
- **Base URL:** `http://localhost:8080/api`
- **Authentication Method:** Bearer Token (send in `Authorization` header as `Bearer <token>`)

---

## 2. Default Credentials

On application startup, a default admin account is initialized:
- **Email:** `admin@ecom.com`
- **Password:** `admin123`
- **Initial State:** `requirePasswordChange = true`

---

## 3. Implementation Flow

### Step A: Initial Login
Send the admin credentials to the login endpoint.

**Request:**
- **URL:** `POST /api/login`
- **Body:**
```json
{
  "email": "admin@ecom.com",
  "password": "admin123"
}
```

**Response (Success):**
```json
{
  "token": "eyJhbG...",
  "mustChangePassword": true,
  "email": "admin@ecom.com",
  "role": "ROLE_ADMIN"
}
```

### Step B: Check "mustChangePassword"
1. Save the `token` in a cookie or localStorage.
2. Check the `mustChangePassword` boolean.
3. **If `true`:** Force redirect the user to a `/change-password` page.
4. **If `false`:** Redirect to the Admin Dashboard.

### Step C: Change Password (Mandatory for first login)
If the user was redirected, they must set a new password. **This endpoint is protected and requires the JWT.**

**Request:**
- **URL:** `POST /api/change-password`
- **Headers:** `Authorization: Bearer <token>`
- **Body:**
```json
{
  "newPassword": "your-secure-password"
}
```

---

## 4. Next.js Code Snippets

### Login Component
```javascript
const handleLogin = async (email, password) => {
  const response = await fetch('http://localhost:8080/api/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  });

  if (response.ok) {
    const data = await response.json();
    localStorage.setItem('token', data.token);
    
    if (data.mustChangePassword) {
      router.push('/change-password');
    } else {
      router.push('/admin/dashboard');
    }
  }
};
```

### Change Password Component
```javascript
const handleChangePassword = async (newPassword) => {
  const token = localStorage.getItem('token');
  const response = await fetch('http://localhost:8080/api/change-password', {
    method: 'POST',
    headers: { 
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ newPassword }), 
  });

  if (response.ok) {
    alert('Password updated! Redirecting...');
    router.push('/admin/dashboard');
  }
};
```

---

## 5. Security Rules

- `/api/login`: Public
- `/api/register`: Public
- `/api/change-password`: **Authenticated (Requires JWT)**
- `/api/admin/forgot-password`: **Public**
- `/api/admin/reset-password`: **Public**
- `/api/admin/**`: **Admin Only (Requires ROLE_ADMIN)**

---

## 6. Forgot Password Flow
If an admin loses their password, they can reset it via a 6-digit OTP sent to their email. 

### Step A: Request OTP
*   **Method:** `POST`
*   **URL:** `/api/admin/forgot-password`
*   **Access:** Public
*   **Body:**
```json
{
  "email": "admin@ecom.com"
}
```
*   **Behavior:** Checks if an Admin user exists with that email. If so, a randomized 6-digit PIN is injected into their database row and sent to the SMTP email address. The OTP expires automatically in **15 minutes**.

### Step B: Validate & Reset Password
*   **Method:** `POST`
*   **URL:** `/api/admin/reset-password`
*   **Access:** Public
*   **Body:**
```json
{
  "email": "admin@ecom.com",
  "otp": "654321",
  "newPassword": "MyNewSecurePassword123!"
}
```
*   **Behavior:** 
    1. Checks if the `otp` explicitly matches the one stored in the DB.
    2. Validates it hasn't been over 15 minutes since it was generated.
    3. Hashes the `newPassword` and securely overwrites the old forgotten one.
    4. Wipes the OTP columns to prevent replay attacks.
    5. Clears the `requirePasswordChange` flag if it was stranded.
    6. Upon success, the user can now login normally through `/api/login`!

---

## 7. Change Admin Email
If the business undergoes a handover or rebranding, the admin can securely change their email address. To prevent unauthorized takeovers, they must confirm their current password during the swap.

*   **Method:** `POST`
*   **URL:** `/api/admin/change-email`
*   **Headers:** `Authorization: Bearer <token>`
*   **Body:**
```json
{
  "newEmail": "new-admin@yourdomain.com",
  "password": "TheirCurrentPassword123"
}
```
*   **Behavior:** 
    1. Grabs the currently authenticated user's email from the JWT token.
    2. Validates that the provided `password` matches the one in the database.
    3. Checks to ensure `newEmail` is not already taken by another account.
    4. Replaces the email and saves.
    5. **IMPORTANT:** Upon success, the frontend should immediately force the user to re-login, as their previous JWT Token is bound to an email that no longer exists!

---

## 8. Secure Admin Password Change
Aside from the mandatory first-time password reset, an admin can change their password at any time from their settings panel. This requires validating their current password for security.

*   **Method:** `POST`
*   **URL:** `/api/admin/change-password`
*   **Headers:** `Authorization: Bearer <token>`
*   **Body:**
```json
{
  "currentPassword": "OldPassword123",
  "newPassword": "BrandNewSecurePassword456"
}
```
*   **Behavior:** 
    1. Grabs the currently authenticated user's email from the JWT token.
    2. Validates that `currentPassword` matches the one in the database.
    3. Hashes and updates to `newPassword`.
    4. Clears the `requirePasswordChange` flag if it was still active.
    5. Returns a success message upon completion.

