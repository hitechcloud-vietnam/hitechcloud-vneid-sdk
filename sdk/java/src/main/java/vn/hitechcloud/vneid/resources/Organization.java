package vn.hitechcloud.vneid.resources;

import vn.hitechcloud.vneid.Config;
import vn.hitechcloud.vneid.HttpClient;
import vn.hitechcloud.vneid.Signature;

import java.util.HashMap;
import java.util.Map;

/** Organization resource — organization identity (type=ORG). */
public class Organization extends BaseResource {
    public static final String SERVICE_CODE = "share-info";

    public Organization(HttpClient http, Config config, Signature signature) {
        super(http, config, signature);
    }

    /** POST /api/tcdn/share-info/api/agent/init with type=ORG. */
    public Map<String, Object> init(String shareOrgNumber, String citizenPid, Map<String, Object> extra) {
        Map<String, Object> body = new HashMap<>();
        body.put("citizenPid", citizenPid == null ? "" : citizenPid);
        body.put("type", "ORG");
        body.put("shareOrgNumber", shareOrgNumber);
        if (extra != null) body.putAll(extra);
        Map<String, String> headers = vneidHeaders(new HeaderOpts());
        return http.postJson(tcdnPath(SERVICE_CODE, "/api/agent/init"), body, headers);
    }

    /** POST /api/tcdn/share-info/api/get-transaction — org data (orgPid, taxCode, ...). */
    public Map<String, Object> getTransaction(String transactionId) {
        Map<String, Object> body = new HashMap<>();
        body.put("transactionId", transactionId);
        Map<String, String> headers = vneidHeaders(new HeaderOpts());
        return http.postJson(tcdnPath(SERVICE_CODE, "/api/get-transaction"), body, headers);
    }
}
