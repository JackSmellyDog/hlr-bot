package me.shaposhnik.hlrbot.integration.bsg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.integration.bsg.dto.*;
import me.shaposhnik.hlrbot.integration.bsg.exception.BsgException;
import me.shaposhnik.hlrbot.integration.bsg.properties.IntegrationUrlsProperties;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.Objects.requireNonNull;


@Slf4j
@Component
@RequiredArgsConstructor
public class BsgApiClient {

    private static final MediaType APPLICATION_JSON = MediaType.get("application/json");
    private static final String X_API_KEY = "X-API-KEY";

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final IntegrationUrlsProperties integrationUrlsProperties;


    public <T extends Collection<HrlRequest>> MultipleHlrResponse sendHlrs(T hrlRequests, ApiKey apiKey) {
        final var request = createHlrOkHttpRequest(hrlRequests, apiKey);

        try (var response = client.newCall(request).execute()) {
            return mapOkHttpResponseBodyToMultipleHlrResponse(response.body());

        } catch (BsgException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to send HLR request!", e);
            throw new BsgException(e);
        }
    }

    // TODO: 4/13/21 rewrite with multipart data
    public HlrResponse sendHlr(HrlRequest hrlRequest, ApiKey apiKey) {
        final var multipleHlrResponse = sendHlrs(List.of(hrlRequest), apiKey);

        return Optional.ofNullable(multipleHlrResponse.getResult())
                .orElseGet(Collections::emptyList)
                .stream()
                .findFirst()
                .orElseThrow(BsgException::new);
    }

    public HlrInfo getHlrInfo(String id, ApiKey apiKey) {
        final var request = new Request.Builder()
                .url(integrationUrlsProperties.getRequestHlrInfoUrl() + id)
                .header(X_API_KEY, apiKey.getKey())
                .get()
                .build();

        try (var response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                final String message = String.format("Response was unsuccessful or body was null! Code: %d", response.code());
                log.error(message);

                throw new BsgException(message);
            }

            return mapOkHttpResponseBodyToHlrInfo(response.body());

        } catch (BsgException e) {
            throw e;
        } catch (Exception e) {
            log.error("Something unexpected happened! Message: {}", e.getMessage());
            throw new BsgException(e);
        }
    }

    public BalanceResponse checkBalance(ApiKey apiKey) {
        final var request = new Request.Builder()
                .url(integrationUrlsProperties.getBalanceUrl())
                .header(X_API_KEY, apiKey.getKey())
                .get()
                .build();

        try (var response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                final String message = String.format("Response was unsuccessful or body was null! Code: %d", response.code());
                log.error(message);

                throw new BsgException(message);
            }

            return mapOkHttpResponseBodyToBalanceResponse(response.body());

        } catch (BsgException e) {
            throw e;
        } catch (Exception e) {
            log.error("Something unexpected happened! Message: {}", e.getMessage());
            throw new BsgException(e);
        }
    }

    private <T extends Collection<HrlRequest>> Request createHlrOkHttpRequest(T hrlRequests, ApiKey apiKey) {
        try {
            final String payload = objectMapper.writeValueAsString(hrlRequests);
            log.info("Request payload is: '{}'", payload);

            final var requestBody = RequestBody.create(payload, APPLICATION_JSON);

            return new Request.Builder()
                    .url(integrationUrlsProperties.getCreateHlrUrl())
                    .header(X_API_KEY, apiKey.getKey())
                    .post(requestBody)
                    .build();

        } catch (Exception e) {
            log.error("Failed to create request for HLR!", e);
            throw new BsgException(e);
        }
    }

    private MultipleHlrResponse mapOkHttpResponseBodyToMultipleHlrResponse(ResponseBody responseBody) {
        try {
            requireNonNull(responseBody, "Response Body must not be null!");
            final String body = responseBody.string();
            log.info("HLR Request response received with body: {}", body);

            return objectMapper.readValue(body, MultipleHlrResponse.class);
        } catch (Exception e) {
            log.error("Failed to read MultipleHlrResponse from string!", e);
            throw new BsgException(e);
        }
    }

    private HlrInfo mapOkHttpResponseBodyToHlrInfo(ResponseBody responseBody) {
        try {
            final String body = responseBody.string();
            log.info("HLR Info response received with body: {}", body);

            return objectMapper.readValue(body, HlrInfo.class);
        } catch (JsonProcessingException e) {
            final String originalResponseBody = (String) e.getLocation().getSourceRef();
            log.error("Can't deserialize to HlrInfo: ({})", originalResponseBody);

            throw new BsgException(e);
        } catch (Exception e) {
            log.error("Something went wrong when deserialize to HlrInfo!", e);
            throw new BsgException(e);
        }
    }

    private BalanceResponse mapOkHttpResponseBodyToBalanceResponse(ResponseBody responseBody) {
        try {
            return objectMapper.readValue(responseBody.string(), BalanceResponse.class);
        } catch (JsonProcessingException e) {
            final String originalResponseBody = (String) e.getLocation().getSourceRef();
            log.error("Can't deserialize to BalanceResponse: ({})", originalResponseBody);

            throw new BsgException(e);
        } catch (Exception e) {
            log.error("Something went wrong when deserialize to BalanceResponse!", e);
            throw new BsgException(e);
        }
    }
}
