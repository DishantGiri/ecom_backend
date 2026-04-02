# 🛍️ Product CRUD API Documentation

This guide explains how to use the Product Management API, including how to handle bundles (offers) and per-offer feature images.

---

## 🚀 Base URL
`http://localhost:8080`

## 🔐 Authentication
Admin endpoints require a **JWT Token** in the header:
`Authorization: Bearer <your_jwt_token>`

---

## 🛠️ Admin Endpoints

### 1. Create Product
*   **Method:** `POST`
*   **URL:** `/api/admin/products`
*   **Content-Type:** `multipart/form-data`

| Part Name | Type | Description |
| :--- | :--- | :--- |
| `data` | **JSON String** | The `ProductRequestDTO` containing product details and offers. |
| `featureImage` | File | Main hero image for the product (Global). |
| `galleryImages` | File(s) | Multiple images for the general product gallery. |
| `promotionalImages` | File(s) | Multiple images representing the product's long description/manufacturer promotional graphics. |
| `offerImage_0` | File | Hero image specific to the **1st offer** in the list. |
| `offerImage_1` | File | Hero image specific to the **2nd offer** in the list. |
| `offerImage_2` | File | Hero image specific to the **3rd offer** in the list. |

#### **`data` JSON Example:**
```json
{
  "title": "Nerve Freedom Pro",
  "slug": "nerve-freedom-pro",
  "ribbon": "#1 Best Seller",
  "numberOfReviews": 115,
  "starRating": 4.5,
  "originalPrice": 59.95,
  "discountedPrice": 24.95,
  "categoryId": 1,
  "productLink": "https://example.com/item",
  "description": "Enjoy the natural support of our Nerve Freedom Pro...",
  "highlights": "🧠 Supports healthy nerve function\n⚕️ Promotes improved blood circulation\n⚡ Helps maintain consistent energy levels",
  "details": "Weight: 1kg\nDimensions: 10x10x10",
  "customFields": [
    { "fieldName": "How to Use", "fieldValue": "Take 2 pills with water.", "displayOrder": 1 },
    { "fieldName": "Allergens", "fieldValue": "Contains soy and dairy.", "displayOrder": 2 }
  ],
  "directions": "For optimal results, take one (1) capsule per day with food...",
  "benefits": "- Supports healthy nerve function and promotes optimal blood circulation...",
  "guarantee": "30-Day Money Back Guarantee\nAt Supplements Fast, we stand behind...",
  "shippingInfo": "Standard Free Shipping USA = 3 - 4 Business Days Delivery...",
  "offers": [
    { "label": "Buy 1 Bottle", "quantity": 1, "originalPrice": 59.95, "discountedPrice": 24.95, "displayOrder": 1 },
    { "label": "Buy 2 Bottles Get 1 Free", "quantity": 3, "originalPrice": 179.85, "discountedPrice": 49.95, "displayOrder": 2 },
    { "label": "Buy 3 Bottles Get 2 Free", "quantity": 5, "originalPrice": 299.75, "discountedPrice": 69.95, "displayOrder": 3 }
  ]
}
```

### 2. Update Product
*   **Method:** `PUT`
*   **URL:** `/api/admin/products/{id}`
*   **Content-Type:** `multipart/form-data`
*   **Behavior:** Replace images by sending a new file. To keep an existing offer image, simply don't send that `offerImage_X` part.

### 3. Delete Product
*   **Method:** `DELETE`
*   **URL:** `/api/admin/products/{id}`
*   **Result:** Deletes product, all offers, and removes all associated files from the server.

### 4. Remove Single Image (Gallery or Promotional)
*   **Method:** `DELETE`
*   **URLs:** 
    *   `/api/admin/products/{id}/gallery/{filename}`
    *   `/api/admin/products/{id}/promotional/{filename}`
*   **Result:** Deletes the specific image file from the database array and the server disk.

### 5. Bulk Upload Products
*   **Method:** `POST`
*   **URL:** `/api/admin/products/bulk-upload`
*   **Content-Type:** `multipart/form-data`

| Part Name | Type | Description |
| :--- | :--- | :--- |
| `file` | File | A CSV file containing product data. |

**Expected CSV Headers (case-insensitive):**
* `title` (Required)
* `category`, `categoryId`, or `category_id` (Matches by ID or exact category name)
* `originalPrice` or `original_price`
* `discountedPrice` or `discounted_price`
* `numberOfReviews` or `number_of_reviews`
* `starRating` or `star_rating`
* `productLink` or `product_link`
* `description`
* `highlights`
* `details`
* `image` (Comma-separated filenames. 1st = Feature Image, others = Gallery)
* `slug` (Unique SEO-friendly URL string. If empty, auto-generated from title)

> **Manual Image Uploads**: To use specific images, first upload your `.png/.jpg` files to the server's `uploads/` folder. Then, in the `image` column of your CSV, put the exact filenames. The API will link them automatically. If no `image` is provided, it will fallback to a random feature image from existing products.

---

### 6. Content Snippets (Reusable Text Blocks)
This feature allows an admin to save reusable blocks of text (like "Standard Shipping Policy" or "Summer Guarantee") to instantly insert them into a product's fields when adding or editing a product via the frontend context.

*   `GET /api/admin/snippets` - List all snippets
*   `POST /api/admin/snippets` - Create a new snippet
    *   **Payload:** `{"name": "Standard Shipping", "content": "Delivery in 3-4 days..."}`
*   `PUT /api/admin/snippets/{id}` - Update a snippet
*   `DELETE /api/admin/snippets/{id}` - Delete a snippet

---

### 7. Product Reviews (Admin Added)
This feature allows the admin to attach highly customized user reviews directly to a product, complete with the reviewer's name, star rating, and a review image (like a photo of the product in use).

*   **Create a Review:** `POST /api/admin/products/{productId}/reviews`
    *   **Content-Type:** `multipart/form-data`
    *   **Payload Parts:**
        *   `reviewerName` (Text)
        *   `reviewText` (Text)
        *   `starRating` (Text/Double, Optional)
        *   `image` (File, Optional) - Stores as `imageUrl`

*   **Update a Review:** `PUT /api/admin/products/reviews/{reviewId}`
    *   **Content-Type:** `multipart/form-data`
    *   **Payload Parts:** (Same as Create)
        *   `reviewerName` (Text)
        *   `reviewText` (Text)
        *   `starRating` (Text/Double, Optional)
        *   `image` (File, Optional) - Replace existing image

*   **Delete a Review:** `DELETE /api/admin/products/reviews/{reviewId}`

> **Note**: Fetching the product via the global `GET /api/products/{id}` automatically includes an ordered array of these `reviews` alongside the product data.

---

## 🌍 Public Endpoints (No Auth)

| Method | URL | Description |
| :--- | :--- | :--- |
| `GET` | `/api/products` | Get list of all products. |
| `GET` | `/api/products/popular` | Get list of all products **sorted by most clicks**. |
| `GET` | `/api/products/{id}` | **Single Product Details** (Legacy ID-based). |
| `GET` | `/api/products/slug/{slug}` | **Single Product Details (SEO Friendly)**. |
| `GET` | `/api/products/search?keyword=...` | Search products by title (**Supports typos/fuzzy match**). |
| `GET` | `/api/products/category?name=...` | Filter products by category. |
| `GET` | `/api/images/{filename}` | Serve an image file. |
| `POST` | `/api/track/click` | Track a product click. Send `{"productId": 1, "ipAddress": "..."}`. |
| `GET` | `/api/track/country?ipAddress=...` | Resolve an IP address to a country name. |

> 🌍 **Multi-Currency Support:** All product GET endpoints accept an optional `?currency=EUR` (or GBP, AUD, CAD etc) query parameter. If provided, the API will convert all product and offer prices into the requested currency using a cached live exchange rate from Frankfurter API. It defaults to USD.

---

## 📈 Analytics & Click Tracking
The `/api/track/click` endpoint allows the frontend to send click events.
1. When a user clicks a product or button, call this endpoint.
2. Provide the `productId` and the user's `ipAddress`.
3. The backend resolves the IP to a **country** via a geolocation API.
4. It increments the counter for that `(Product, Country)` pair.

When you fetch a product (`GET /api/products/{id}`), it now includes an array of `clickStats`:
```json
"clickStats": [
  { "country": "United States", "clickCount": 15 },
  { "country": "Canada", "clickCount": 3 }
]
```

---

## 🎨 Frontend Implementation Logic

### **You May Also Like (Similar Products)**
When you call `/api/products/{id}`, the response includes a `similarProducts` array.
*   **Logic**: Finds other products in the **same category** where the price is between **0.5x** and **1.5x** of the current product.
*   **Usage**: Scroll down to the bottom of your Product Details page and map through `similarProducts` to show related items.

### **Image Switching Logic**
The backend returns a `featureImageUrl` for the product and a `featureImageUrl` for each **offer**.

1.  **Initial Load:**
    *   Display `product.featureImageUrl` (or `product.offers[0].featureImageUrl`).
    *   The gallery should be `[activeImage, ...product.galleryImageUrls]`.

2.  **When User Clicks an Offer:**
    *   Check if `selectedOffer.featureImageUrl` exists.
    *   Update the **Main Display Image** to the offer image.
    *   Prepend the offer image to the gallery so it is at **Index 0**.

### **Star Ratings**
*   Range: `0.5` to `5.0`.
*   Increments: `0.5` only (e.g., 3.0, 3.5, 4.0).
*   Backend will return a `400 Bad Request` if increments are invalid.

### **Price Display**
*   Admin can set `originalPrice` and `discountedPrice` (price after discount) per offer.
*   The UI should calculate "You Save" using: `originalPrice - discountedPrice`.

---

## 📁 Storage
*   Images are stored on the server in the `uploads/` directory.
*   Filenames are obfuscated using UUIDs for security.
