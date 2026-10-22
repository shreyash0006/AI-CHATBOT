package com.example.CHATBOT.AITools;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class CurrencyTools {

    private final RestClient restClient;

    public CurrencyTools(
            RestClient.Builder builder,
            @Value("${currency.api.base-url:https://open.er-api.com/v6/latest}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    /**
     * Clean result DTO passed back to the LLM.
     */
    public record ConversionResult(
            String baseCurrency,
            String targetCurrency,
            double originalAmount,
            double exchangeRate,
            double convertedAmount
    ) {}

    @Tool(description = "Converts an amount from one fiat currency to another using real-time foreign exchange rates")
    public ConversionResult convertCurrency(
            @ToolParam(description = "The 3-letter ISO code of the source currency (e.g., USD, INR, EUR, GBP)", required = true)
            String from,

            @ToolParam(description = "The 3-letter ISO code of the target currency (e.g., USD, INR, EUR, GBP)", required = true)
            String to,

            @ToolParam(description = "The amount of source currency to convert", required = true)
            double amount
    ) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Both 'from' and 'to' currency codes must be provided.");
        }

        String sourceCode = from.trim().toUpperCase();
        String targetCode = to.trim().toUpperCase();

        // Fetches exchange rates relative to the source currency
        ExchangeRateApiResponse response = restClient.get()
                .uri("/{base}", sourceCode)
                .retrieve()
                .body(ExchangeRateApiResponse.class);

        if (response == null || !"success".equalsIgnoreCase(response.result()) || response.rates() == null) {
            throw new IllegalStateException("Failed to fetch exchange rates for currency: " + sourceCode);
        }

        Double rate = response.rates().get(targetCode);
        if (rate == null) {
            throw new IllegalArgumentException("Unsupported target currency code: " + targetCode);
        }

        double converted = Math.round((amount * rate) * 100.0) / 100.0;

        return new ConversionResult(
                sourceCode,
                targetCode,
                amount,
                rate,
                converted
        );
    }

    // --- Internal DTO mapping the API's JSON response ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ExchangeRateApiResponse(
            String result,
            String base_code,
            Map<String, Double> rates
    ) {}
}