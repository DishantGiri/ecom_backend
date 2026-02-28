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
- `/api/admin/**`: **Admin Only (Requires ROLE_ADMIN)**
