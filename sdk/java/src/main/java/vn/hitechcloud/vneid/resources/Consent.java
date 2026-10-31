package vn.hitechcloud.vneid.resources;

import vn.hitechcloud.vneid.Config;
import vn.hitechcloud.vneid.HttpClient;
import vn.hitechcloud.vneid.Signature;

import java.util.HashMap;
import java.util.Map;

/** Consent resource — receive-result, withdraw-consent, universal link. */
public class Consent extends BaseResource {
    public static final String UNIVERSAL_LINK_BASE = "https://universal.dancuquocgia.com/share";

    public Consent(HttpClient http, Config config, Signature signature) {
        super(http, config, signature);
    }

    /** Return the VNeID universal link for a transaction (App-to-App). */
    public String buildUniversalLink(String transactionCode) {
        return UNIVERSAL_LINK_BASE + "/" + transactionCode;
    }

    /** POST /api/tcdn/{serviceCode}/agent/account/receive-result */
    public Map<String, Object> receiveResult(String serviceCode, Map<String, Object> body) {
        Map<String, String> headers = vneidHeaders(new HeaderOpts());
        return http.postJson(tcdnPath(serviceCode, "/agent/account/receive-result"), body, headers);
    }

    /**
     * POST /api/tcdn/withdraw-consent-result/agent/account/receive-result.
     * requestStatus is "DELETED" or "REJECT".
     */
    public Map<String, Object> withdrawConsentResult(String requestStatus, String reasonRequestCode, String reasonRequestText) {
        Map<String, Object> body = new HashMap<>();
        body.put("requestStatus", requestStatus);
        body.put("reasonRequestCode", reasonRequestCode);
        body.put("reasonRequestText", reasonRequestText);
        Map<String, String> headers = vneidHeaders(new HeaderOpts());
        return http.postJson(tcdnPath("withdraw-consent-result", "/agent/account/receive-result"), body, headers);
    }
}
