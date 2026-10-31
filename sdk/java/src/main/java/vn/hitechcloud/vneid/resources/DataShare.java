package vn.hitechcloud.vneid.resources;

import vn.hitechcloud.vneid.Config;
import vn.hitechcloud.vneid.HttpClient;
import vn.hitechcloud.vneid.Signature;

import java.util.HashMap;
import java.util.Map;

/** Data share resource — share-info service. */
public class DataShare extends BaseResource {
    public static final String SERVICE_CODE = "share-info";

    public DataShare(HttpClient http, Config config, Signature signature) {
        super(http, config, signature);
    }

    /** POST /api/tcdn/share-info/api/agent/init. body must include "citizenPid". */
    public Map<String, Object> init(Map<String, Object> body) {
        Map<String, String> headers = vneidHeaders(new HeaderOpts());
        return http.postJson(tcdnPath(SERVICE_CODE, "/api/agent/init"), body, headers);
    }

    /** POST /api/tcdn/share-info/api/get-transaction */
    public Map<String, Object> getTransaction(String transactionId) {
        Map<String, Object> body = new HashMap<>();
        body.put("transactionId", transactionId);
        Map<String, String> headers = vneidHeaders(new HeaderOpts());
        return http.postJson(tcdnPath(SERVICE_CODE, "/api/get-transaction"), body, headers);
    }
}
