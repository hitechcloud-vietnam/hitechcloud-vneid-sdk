package vn.hitechcloud.vneid;

import java.util.Map;

public class NotFoundException extends HiTechCloudVNeIDException {
    public NotFoundException(String message, int statusCode, String responseCode, Map<String, Object> data) {
        super(message, statusCode, responseCode, data);
    }
}
