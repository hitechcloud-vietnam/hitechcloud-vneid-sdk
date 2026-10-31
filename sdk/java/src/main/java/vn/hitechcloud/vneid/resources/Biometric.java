package vn.hitechcloud.vneid.resources;

import vn.hitechcloud.vneid.Config;
import vn.hitechcloud.vneid.HttpClient;
import vn.hitechcloud.vneid.Signature;

import java.util.HashMap;
import java.util.Map;

/** Biometric resource — face matching / liveness (v2/get-transaction). */
public class Biometric extends BaseResource {

    public Biometric(HttpClient http, Config config, Signature signature) {
        super(http, config, signature);
    }

    /**
     * POST /api/tcdn/{serviceCode}/v2/api/get-transaction.
     * requestType is "sdk-liveness-match" or "sdk-match".
     */
    public Map<String, Object> getTransactionV2(String serviceCode, String transactionId,
                                                String faceImage, String requestType) {
        if (requestType == null) requestType = "sdk-liveness-match";
        Map<String, Object> body = new HashMap<>();
        body.put("transactionId", transactionId);
        if (faceImage != null) body.put("faceImage", faceImage);

        HeaderOpts opts = new HeaderOpts();
        opts.requestType = requestType;
        Map<String, String> headers = vneidHeaders(opts);
        return http.postJson(tcdnPath(serviceCode, "/v2/api/get-transaction"), body, headers);
    }

    /** Convenience for service-code face-matching. */
    public Map<String, Object> faceMatching(String transactionId, String faceImage, String requestType) {
        return getTransactionV2("face-matching", transactionId, faceImage, requestType);
    }

    /** Convenience for service-code biometric-share-info. */
    public Map<String, Object> shareWithBiometric(String transactionId, String faceImage, String requestType) {
        return getTransactionV2("biometric-share-info", transactionId, faceImage, requestType);
    }
}
