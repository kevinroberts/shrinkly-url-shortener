package com.vinberts.shrinkly.security.captcha;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Response body of the Cloudflare Turnstile siteverify endpoint.
 * https://developers.cloudflare.com/turnstile/get-started/server-side-validation/
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "success", "challenge_ts", "hostname", "error-codes", "action", "cdata" })
public class TurnstileResponse {

    @JsonProperty("success")
    private boolean success;
    @JsonProperty("challenge_ts")
    private String challengeTs;
    @JsonProperty("hostname")
    private String hostname;
    @JsonProperty("error-codes")
    private ErrorCode[] errorCodes;
    @JsonProperty("action")
    private String action;
    @JsonProperty("cdata")
    private String cdata;


    static enum ErrorCode {
        MissingSecret, InvalidSecret, MissingResponse, InvalidResponse,
        BadRequest, TimeoutOrDuplicate, InternalError, Unknown;

        private static Map<String, ErrorCode> errorsMap = new HashMap<>(7);

        static {
            errorsMap.put("missing-input-secret", MissingSecret);
            errorsMap.put("invalid-input-secret", InvalidSecret);
            errorsMap.put("missing-input-response", MissingResponse);
            errorsMap.put("invalid-input-response", InvalidResponse);
            errorsMap.put("bad-request", BadRequest);
            errorsMap.put("timeout-or-duplicate", TimeoutOrDuplicate);
            errorsMap.put("internal-error", InternalError);
        }

        @JsonCreator
        public static ErrorCode forValue(final String value) {
            return errorsMap.getOrDefault(value.toLowerCase(), Unknown);
        }
    }

    @JsonProperty("success")
    public boolean isSuccess() {
        return success;
    }

    @JsonProperty("success")
    public void setSuccess(boolean success) {
        this.success = success;
    }

    @JsonProperty("challenge_ts")
    public String getChallengeTs() {
        return challengeTs;
    }

    @JsonProperty("challenge_ts")
    public void setChallengeTs(String challengeTs) {
        this.challengeTs = challengeTs;
    }

    @JsonProperty("hostname")
    public String getHostname() {
        return hostname;
    }

    @JsonProperty("hostname")
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @JsonProperty("error-codes")
    public void setErrorCodes(ErrorCode[] errorCodes) {
        this.errorCodes = errorCodes;
    }

    @JsonProperty("error-codes")
    public ErrorCode[] getErrorCodes() {
        return errorCodes;
    }

    @JsonProperty("action")
    public String getAction() {
        return action;
    }

    @JsonProperty("action")
    public void setAction(String action) {
        this.action = action;
    }

    @JsonProperty("cdata")
    public String getCdata() {
        return cdata;
    }

    @JsonProperty("cdata")
    public void setCdata(String cdata) {
        this.cdata = cdata;
    }

    /**
     * True when the failure was caused by the submitted token (bad, missing,
     * expired or replayed) rather than by configuration or Cloudflare itself,
     * so the attempt should count against the client.
     */
    @JsonIgnore
    public boolean hasClientError() {
        final ErrorCode[] errors = getErrorCodes();
        if (errors == null) {
            return false;
        }
        for (final ErrorCode error : errors) {
            switch (error) {
                case InvalidResponse:
                case MissingResponse:
                case TimeoutOrDuplicate:
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "TurnstileResponse{" + "success=" + success + ", challengeTs='" + challengeTs + '\'' + ", hostname='" + hostname + '\'' + ", errorCodes=" + Arrays.toString(errorCodes) + '}';
    }

}
