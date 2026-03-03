# 📈 Click Tracking Implementation Guide

This guide explains how to implement the product click tracking feature on the frontend using the new API endpoints. We track which country a click originated from by analyzing the user's IP address.

---

## 🚀 1. The Tracking Endpoint

Whenever a user clicks a product to view its details (or clicks a "Buy" button), you should send a request to the backend.

*   **Endpoint:** `POST /api/track/click`
*   **Auth Required:** No (Public endpoint)
*   **Body:** JSON
    ```json
    {
      "productId": 1,
      "ipAddress": "192.168.1.1" // The user's actual IP
    }
    ```

---

## 🌐 2. Getting the User's IP Address (Frontend)

Because the frontend runs in the user's browser, you need to fetch their public IP address before sending the tracking request. You can use a free service like [ipify](https://www.ipify.org/) for this.

Here is an example using `fetch` or `axios` in a React/Vanilla JS app:

```javascript
// Step 1: Fetch the user's IP
async function getUserIP() {
    try {
        const response = await fetch('https://api.ipify.org?format=json');
        const data = await response.json();
        return data.ip;
    } catch (error) {
        console.error("Could not fetch IP", error);
        return null; // Fallback
    }
}

// Step 2: Send tracking request to your Spring Boot backend
async function trackProductClick(productId) {
    const ip = await getUserIP();
    
    if (!ip) return; // Don't track if IP failed

    try {
        await fetch('http://localhost:8080/api/track/click', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                productId: productId,
                ipAddress: ip
            })
        });
        console.log("Click tracked successfully!");
    } catch (error) {
        console.error("Failed to track click", error);
    }
}
```

### 🖱️ 3. Where to call this function?
Call `trackProductClick(product.id)` inside your `onClick` handler when a user clicks on a product card to view it.

```javascript
<button onClick={() => {
    trackProductClick(product.id);
    // Navigate to product page...
}}>
    View Details
</button>
```

---

## 📊 4. Viewing the Statistics

When you fetch a single product's details using `GET /api/products/{id}`, the backend will automatically return the aggregated click counts for that product, grouped by country.

**Example Response:**
```json
{
  "id": 1,
  "title": "Nerve Freedom Pro",
  "clickStats": [
    {
      "country": "United States",
      "clickCount": 142
    },
    {
      "country": "Canada",
      "clickCount": 35
    },
    {
      "country": "Unknown",
      "clickCount": 2
    }
  ],
  ... // other product details
}
```

You can use the `clickStats` array to build an admin dashboard that visualizes where your traffic is coming from!
---

## 📊 5. Admin Analytics Overview

For managers who want to see a full birds-eye view of all product activity across the entire store, we have a specialized admin-only endpoint. This endpoint is designed to be **non-developer friendly**, returning actual product titles instead of IDs.

*   **Endpoint:** `GET /api/admin/analytics/clicks`
*   **Auth Required:** Yes (Requires Admin JWT)
*   **Description:** Returns a list of all products that have received clicks, their total click count, and a breakdown by country.

**Example Response:**
```json
[
  {
    "productTitle": "Nerve Freedom Pro",
    "totalClicks": 179,
    "clicksByCountry": {
      "United States": 142,
      "Canada": 35,
      "Unknown": 2
    }
  },
  {
    "productTitle": "Daily Multi-Vitamin",
    "totalClicks": 85,
    "clicksByCountry": {
      "Australia": 50,
      "United Kingdom": 35
    }
  }
]
```

### 📈 How to use this for the dashboard?
This endpoint is perfect for building a **Top Products** table or a **Global Heatmap** for the admin dashboard. Since the response is already aggregated and sorted by total clicks (highest first), you can simply map over the list to render your charts!
