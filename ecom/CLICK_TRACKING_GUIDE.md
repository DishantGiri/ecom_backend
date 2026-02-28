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
