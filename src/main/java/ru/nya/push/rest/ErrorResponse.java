package ru.nya.push.rest;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author by lesin-sa on 20.07.2016.
 */
@Data
@NoArgsConstructor
public class ErrorResponse {
    private String errorMessage;
    private String detailMessage;
    private String exceptionClass;
    private Map<String, String> errorFields;

    public ErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
