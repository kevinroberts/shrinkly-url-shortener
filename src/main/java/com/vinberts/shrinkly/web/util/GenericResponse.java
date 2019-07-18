package com.vinberts.shrinkly.web.util;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */

public class GenericResponse {
    private String message;
    private String error;
    private String field;

    public GenericResponse(final String message) {
        super();
        this.message = message;
    }

    public GenericResponse(final String message, final String error) {
        super();
        this.message = message;
        this.error = error;
    }

    public GenericResponse(final String message, final String error, final String field) {
        this.message = message;
        this.error = error;
        this.field = field;
    }

    public GenericResponse(List<ObjectError> allErrors, String error) {
        this.error = error;
        String temp = allErrors.stream().map(e -> {
            if (e instanceof FieldError) {
                String message = StringEscapeUtils.escapeJson(e.getDefaultMessage());
                return "{\"field\":\"" + ((FieldError) e).getField() + "\",\"defaultMessage\":\"" + message + "\"}";
            } else {
                String message = StringEscapeUtils.escapeJson(e.getDefaultMessage());
                return "{\"object\":\"" + e.getObjectName() + "\",\"defaultMessage\":\"" + message + "\"}";
            }
        }).collect(Collectors.joining(","));
        this.message = "[" + temp + "]";
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(final String error) {
        this.error = error;
    }

    public String getField() {
        return field;
    }

    public void setField(final String field) {
        this.field = field;
    }
}
