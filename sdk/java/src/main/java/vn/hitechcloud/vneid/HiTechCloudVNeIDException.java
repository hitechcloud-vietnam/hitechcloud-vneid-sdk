package vn.hitechcloud.vneid;

import java.util.Map;

public class HiTechCloudVNeIDException extends RuntimeException {
    private final int statusCode;
    private final String responseCode;
    private final Map<String, Object> data;

    public HiTechCloudVNeIDException(String message, int statusCode, String responseCode, Map<String, Object> data) {
        super(message);
        this.statusCode = statusCode;
        this.responseCode = responseCode;
        this.data = data;
    }

    public HiTechCloudVNeIDException(String message) {
        this(message, 0, null, null);
    }

    public int getStatusCode() { return statusCode; }
    public String getResponseCode() { return responseCode; }
    public Map<String, Object> getData() { return data; }
}
