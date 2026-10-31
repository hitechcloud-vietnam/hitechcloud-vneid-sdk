package vn.hitechcloud.vneid;

import java.util.Map;

public class ValidationException extends HiTechCloudVNeIDException {
    public ValidationException(String message, int statusCode, String responseCode, Map<String, Object> data) {
        super(message, statusCode, responseCode, data);
    }
}
