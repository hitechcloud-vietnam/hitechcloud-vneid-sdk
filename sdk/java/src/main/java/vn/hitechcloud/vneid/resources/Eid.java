package vn.hitechcloud.vneid.resources;

import vn.hitechcloud.vneid.Config;
import vn.hitechcloud.vneid.HttpClient;
import vn.hitechcloud.vneid.Signature;
import vn.hitechcloud.vneid.SignatureException;

import java.util.HashMap;
import java.util.Map;

/** eID resource — chip CCCD verification (RSA-SHA256 signed). */
public class Eid extends BaseResource {

    public Eid(HttpClient http, Config config, Signature signature) {
        super(http, config, signature);
    }

    /** POST /eid/api/verify/request */
    public Map<String, Object> verifyChip(String idCard, String dscert, String diaChi) {
        if (config.licenseId == null || config.deviceType == null || config.cskPrivateKey == null) {
            throw new SignatureException("eID verification requires licenseId, deviceType and cskPrivateKey");
        }
        String requestId = newRequestId();
        String requestTime = eidRequestTime();
        String sig = signature.signEid(requestId, requestTime, config.licenseId, config.deviceType,
                diaChi, idCard, dscert, config.cskPrivateKey);

        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json; charset=utf-8");
        headers.put("request_id", requestId);
        headers.put("request_time", requestTime);
        headers.put("license_id", config.licenseId);
        headers.put("device_type", config.deviceType);
        headers.put("dia_chi", diaChi);
        headers.put("signature", sig);

        Map<String, Object> body = new HashMap<>();
        body.put("id_card", idCard);
        body.put("dscert", dscert);
        return http.postJson("/eid/api/verify/request", body, headers);
    }
}
