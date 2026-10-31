package vn.hitechcloud.vneid;

import java.util.Map;

public class AuthorizationException extends HiTechCloudVNeIDException {
    public AuthorizationException(String message, int statusCode, String responseCode, Map<String, Object> data) {
        super(message, statusCode, responseCode, data);
    }
}
