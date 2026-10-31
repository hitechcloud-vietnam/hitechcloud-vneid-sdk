package vn.hitechcloud.vneid.resources;

import vn.hitechcloud.vneid.Config;
import vn.hitechcloud.vneid.HttpClient;
import vn.hitechcloud.vneid.Signature;

import java.util.HashMap;
import java.util.Map;

/** Verify-info resource — True/False verification against the population DB. */
public class VerifyInfo extends BaseResource {
    public static final String SERVICE_CODE = "verify-info";

    public VerifyInfo(HttpClient http, Config config, Signature signature) {
        super(http, config, signature);
    }

    /** POST /api/tcdn/verify-info/api/agent/init. Fields in body are verified (True/False). */
    public Map<String, Object> init(Map<String, Object> body) {
        Map<String, String> headers = vneidHeaders(new HeaderOpts());
        return http.postJson(tcdnPath(SERVICE_CODE, "/api/agent/init"), body, headers);
    }

    /** POST /api/tcdn/verify-info/api/get-transaction */
    public Map<String, Object> getTransaction(String transactionId) {
        Map<String, Object> body = new HashMap<>();
        body.put("transactionId", transactionId);
        Map<String, String> headers = vneidHeaders(new HeaderOpts());
        return http.postJson(tcdnPath(SERVICE_CODE, "/api/get-transaction"), body, headers);
    }
}
