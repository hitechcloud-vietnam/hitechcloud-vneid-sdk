package vn.hitechcloud.vneid;

import java.util.Map;

public class RateLimitException extends HiTechCloudVNeIDException {
    private final int retryAfter;

    public RateLimitException(String message, String responseCode, Map<String, Object> data, int retryAfter) {
        super(message, 429, responseCode, data);
        this.retryAfter = retryAfter;
    }

    public int getRetryAfter() { return retryAfter; }
}
