package me.shaposhnik.hlrbot.integration.bsg;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.shaposhnik.hlrbot.integration.bsg.exception.BsgException;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Getter
@RequiredArgsConstructor
public enum BsgApiErrorCode {
    // Common
    NO_ERRORS(0, "No errors"),
    INVALID_API_KEY(1, "Invalid API key"),
    NOT_ENOUGH_PARAMS(2, "Not enough params"),
    ACCOUNT_BLOCKED(3, "Account blocked"),
    INVALID_REQUEST_OR_API_METHOD_DOES_NOT_EXIST(4, "Invalid request or API method does not exist"),
    UNKNOWN_ERROR(5, "Unknown error"),
    INVALID_PRICE_GRID_PARAMETER(6, "Invalid price grid parameter"),
    INVALID_OR_MISSING_PARAMETERS(7, "Invalid or missing parameters"),
    NOT_ENOUGH_MONEY(8, "Not enough money"),
    INACTIVE_PRICE_GRID(9, "Inactive price grid"),
    INVALID_TIME_SENT(10, "Invalid time sent"),
    EXCEEDED_LIMIT_FOR_MESSAGES_PROCESSED_SIMULTANEOUSLY(11, "Exceeded limit for messages processed simultaneously"),
    EXCEEDED_BATCH_SIZE_LIMIT(12, "Exceeded batch size limit"),

    // HLR
    HLR_NOT_FOUND(60, "HLR not found"),
    INVALID_MSISDN(61, "Invalid MSISDN"),
    EXTERNAL_ID_ABSENT(62, "External ID absent"),
    EXTERNAL_ID_ALREADY_EXISTS(63, "External ID already exists"),
    INVALID_REQUEST_PAYLOAD(64, "Invalid request payload"),
    INVALID_EXTERNAL_ID(65, "Invalid External ID"),
    MSISDN_ALREADY_PRESENT_IN_REQUEST(66, "MSISDN already present in request");

    static {
        BSG_API_ERROR_CODE_MAP = Stream.of(values()).collect(toMap(BsgApiErrorCode::getCode, identity()));
    }

    private final int code;
    private final String description;

    private static final Map<Integer, BsgApiErrorCode> BSG_API_ERROR_CODE_MAP;


    public static BsgApiErrorCode fromErrorCode(int errorCode) {
        return Optional.ofNullable(BSG_API_ERROR_CODE_MAP.get(errorCode)).orElseThrow(BsgException::new);
    }
}
