# Category API Guide

This document outlines the API endpoints available for managing Categories.

Base URL: `http://localhost:8080` (or as configured in `app.base-url`)

## Data Models

### CategoryDTO
The standard response format for Categories:
```json
{
  "id": 1,
  "name": "Supplements",
  "imageUrl": "http://localhost:8080/api/images/cat_12345-abcde.jpg"
}
```

---

## Public Endpoints

### 1. Get All Categories
Retrieves a list of all available categories. This is accessible without authentication.

- **URL:** `/api/categories`
- **Method:** `GET`
- **Auth Required:** No
- **Success Response:**
  - **Code:** 200 OK
  - **Content:** List of `CategoryDTO` objects.

---

## Admin Endpoints

All admin endpoints require a valid JWT token with the `ROLE_ADMIN` authority and should be passed in the `Authorization` header as `Bearer <token>`.

### 2. Get All Categories (Admin)
Retrieves a list of all available categories.

- **URL:** `/api/admin/categories`
- **Method:** `GET`
- **Auth Required:** Yes (`ROLE_ADMIN`)
- **Success Response:**
  - **Code:** 200 OK
  - **Content:** List of `CategoryDTO` objects.

### 3. Create a Category
Creates a new category. Allows uploading an image that is associated with that category.

- **URL:** `/api/admin/categories`
- **Method:** `POST`
- **Auth Required:** Yes (`ROLE_ADMIN`)
- **Content-Type:** `multipart/form-data`
- **Request Parameters:**
  - `name` (String, Required): The name of the category.
  - `image` (File, Optional): The category feature image.
- **Success Response:**
  - **Code:** 200 OK
  - **Content:** Created `CategoryDTO` object.

### 4. Update a Category
Updates an existing category's details. You can update the name, the image, or both. If a new image is provided, the previous image file is deleted from the server.

- **URL:** `/api/admin/categories/{id}`
- **Method:** `PUT`
- **Auth Required:** Yes (`ROLE_ADMIN`)
- **Content-Type:** `multipart/form-data`
- **URL Path Variables:**
  - `id` (Long, Required): The ID of the category you want to update.
- **Request Parameters:**
  - `name` (String, Optional): The new name of the category.
  - `image` (File, Optional): The new category image. Replaces the old one.
- **Success Response:**
  - **Code:** 200 OK
  - **Content:** Updated `CategoryDTO` object.
