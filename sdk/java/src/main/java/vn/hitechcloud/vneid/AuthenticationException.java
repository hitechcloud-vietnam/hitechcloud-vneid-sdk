package vn.hitechcloud.vneid;

import java.util.Map;

public class AuthenticationException extends HiTechCloudVNeIDException {
    public AuthenticationException(String message, int statusCode, String responseCode, Map<String, Object> data) {
        super(message, statusCode, responseCode, data);
    }
}
