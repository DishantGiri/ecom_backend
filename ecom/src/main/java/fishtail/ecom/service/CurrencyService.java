package fishtail.ecom.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CurrencyService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyService.class);
    private final RestTemplate restTemplate;

    // Cache to store exchange rates with USD as the base currency
    private final Map<String, BigDecimal> exchangeRates = new ConcurrentHashMap<>();
    private LocalDateTime lastFetchTime = LocalDateTime.MIN;

    public CurrencyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Converts a USD amount to the target currency.
     * Uses a cached exchange rate to keep endpoints fast and avoid rate limits.
     * Cache is refreshed every 12 hours.
     */
    public BigDecimal convertFromUsd(BigDecimal amountUsd, String targetCurrency) {
        if (amountUsd == null)
            return null;
        if (targetCurrency == null || targetCurrency.isBlank() || targetCurrency.equalsIgnoreCase("USD")) {
            return amountUsd.setScale(2, RoundingMode.HALF_UP);
        }

        targetCurrency = targetCurrency.toUpperCase();
        refreshRatesIfNeeded();

        BigDecimal rate = exchangeRates.get(targetCurrency);
        if (rate == null) {
            // Fallback to USD if the requested currency rate isn't found
            log.warn("Currency rate for {} not found, falling back to USD", targetCurrency);
            return amountUsd.setScale(2, RoundingMode.HALF_UP);
        }

        return amountUsd.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private synchronized void refreshRatesIfNeeded() {
        // Refresh cache every 12 hours to avoid spamming the free API
        if (lastFetchTime.isBefore(LocalDateTime.now().minusHours(12))) {
            try {
                String url = "https://api.frankfurter.dev/v1/latest?base=USD";
                @SuppressWarnings("unchecked")
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);

                if (response != null && response.containsKey("rates")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Number> ratesMap = (Map<String, Number>) response.get("rates");

                    ratesMap.forEach((currency, rate) -> {
                        exchangeRates.put(currency, BigDecimal.valueOf(rate.doubleValue()));
                    });

                    lastFetchTime = LocalDateTime.now();
                    log.info("Successfully refreshed currency exchange rates from Frankfurter API.");
                }
            } catch (Exception e) {
                log.error("Failed to fetch exchange rates: {}", e.getMessage());
                // If it fails, keep using old rates and try again next time.
                // We'll advance the fetch time by 5 minutes to prevent continuous failures from
                // stalling threads
                lastFetchTime = LocalDateTime.now().minusHours(11).minusMinutes(55);
            }
        }
    }
}
