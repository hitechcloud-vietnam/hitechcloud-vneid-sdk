package vn.hitechcloud.vneid;

import java.util.Map;

public class ServerException extends HiTechCloudVNeIDException {
    public ServerException(String message, int statusCode, String responseCode, Map<String, Object> data) {
        super(message, statusCode, responseCode, data);
    }
}
