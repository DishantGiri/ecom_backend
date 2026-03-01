# 📝 Blog Management API Documentation

This guide explains how to use the Blog API in the eCommerce system. The Blog module is designed to be highly SEO-friendly and supports rich text content.

---

## 🚀 Base URL
`http://localhost:8080`

## 🔐 Authentication
Admin endpoints require a **JWT Token** in the header:
`Authorization: Bearer <your_jwt_token>`

---

## 🛠️ Admin Endpoints

### 1. Create a Blog Post
*   **Method:** `POST`
*   **URL:** `/api/admin/blogs`
*   **Content-Type:** `multipart/form-data`

| Part Name | Type | Description |
| :--- | :--- | :--- |
| `data` | **JSON String** | The `BlogRequestDTO` containing the blog data. |
| `featureImage` | File | Main hero image for the blog post (Optional). |

#### **`data` JSON Example:**
```json
{
  "title": "5 Natural Ways to Improve Circulation",
  "content": "<p>Blood circulation is critical for tissue health...</p><br/><p>Here are 5 native herbal components...</p>",
  "author": "Dr. Sarah Mitchell",
  "metaTitle": "Top 5 Herbal Ways to Boost Circulation",
  "metaDescription": "Learn how natural herbal extracts can enhance your cardiovascular system efficiently.",
  "metaKeywords": "health, circulation, herbs, cardiovascular"
}
```
**Notes:** 
* `content` should contain raw HTML data directly from your Rich Text Editor (e.g. Quill.js, TinyMCE, TipTap).
* If `meta*` fields are left empty/null, the backend will auto-generate them for you by safely stripping your HTML content to extract the first 150 characters to act as the `metaDescription`. 

### 2. Update a Blog Post
*   **Method:** `PUT`
*   **URL:** `/api/admin/blogs/{id}`
*   **Content-Type:** `multipart/form-data`
*   **Behavior:** Similar to Create. Replaces the feature image only if the `featureImage` part is provided. If you alter the `title`, the SLUG URL path will automatically regenerate on the backend.

### 3. Delete a Blog Post
*   **Method:** `DELETE`
*   **URL:** `/api/admin/blogs/{id}`
*   **Result:** Deletes the blog entirely and securely wipes tracking and images from the server disc.

### 4. Get All Blogs (Admin)
*   **Method:** `GET`
*   **URL:** `/api/admin/blogs`

### 5. Get Single Blog By ID (Admin)
*   **Method:** `GET`
*   **URL:** `/api/admin/blogs/{id}`

---

## 🌍 Public Endpoints (No Auth)
These are endpoints your frontend client uses to read and display the blogs to normal users cleanly, fully utilizing the SEO friendly Slugs (URL).

### 1. List All Blogs
*   **Method:** `GET`
*   **URL:** `/api/blogs`
*   **Description:** Returns an array of `BlogResponseDTO` structured exactly like the admin responses, sorted by newest first.

### 2. Get Blog By Slug (For rendering individual blog pages securely)
*   **Method:** `GET`
*   **URL:** `/api/blogs/{slug}`
*   **Description:** Use this to fetch the full rich HTML content to feed into your `<div dangerouslySetInnerHTML={{__html: blog.content}} />` type containers.

### Response Data Structure Example:
```json
{
  "id": 1,
  "title": "5 Natural Ways to Improve Circulation",
  "slug": "5-natural-ways-to-improve-circulation",
  "content": "<p>Blood circulation is critical...</p>",
  "featureImageUrl": "http://localhost:8080/api/images/a83jd-blog-hero.jpg",
  "author": "Dr. Sarah Mitchell",
  "metaTitle": "Top 5 Herbal Ways to Boost Circulation",
  "metaDescription": "Learn how natural herbal extracts can enhance your cardiovascular system efficiently.",
  "metaKeywords": "health, circulation, herbs, cardiovascular",
  "createdAt": "2023-11-20T10:15:30",
  "updatedAt": "2023-11-20T10:15:30"
}
```
