package vn.hitechcloud.vneid.resources;

import vn.hitechcloud.vneid.Config;
import vn.hitechcloud.vneid.HttpClient;
import vn.hitechcloud.vneid.Signature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** CTS resource — digital certificate (SmartCA) issuance. */
public class Cts extends BaseResource {
    public static final String RESULT_SERVICE_CODE = "create-smartca-account";

    public Cts(HttpClient http, Config config, Signature signature) {
        super(http, config, signature);
    }

    /** Parameters for a CTS init request. */
    public static class InitParams {
        public String citizenPid;
        public String certType = "PER"; // PER, ORG, PER-ORG
        public String shareOrgNumber;
        public String feeCode;
        public List<String> listOrgCode;
        public String keyData;
        public String serviceCode = "share-info";
        public String targetCode;
    }

    /** POST /api/tcdn/{serviceCode}/api/agent/init with CTS fields. */
    public Map<String, Object> init(InitParams p) {
        Map<String, Object> body = new HashMap<>();
        body.put("citizenPid", p.citizenPid);
        body.put("type", p.certType != null ? p.certType : "PER");
        if (p.shareOrgNumber != null) body.put("shareOrgNumber", p.shareOrgNumber);
        if (p.listOrgCode != null) body.put("listOrgCode", p.listOrgCode);
        if (p.feeCode != null) body.put("feeCode", p.feeCode);
        if (p.keyData != null) body.put("keyData", p.keyData);

        HeaderOpts opts = new HeaderOpts();
        opts.targetCode = p.targetCode;
        Map<String, String> headers = vneidHeaders(opts);
        String serviceCode = p.serviceCode != null ? p.serviceCode : "share-info";
        return http.postJson(tcdnPath(serviceCode, "/api/agent/init"), body, headers);
    }

    /** POST /api/tcdn/create-smartca-account/agent/account/receive-result */
    public Map<String, Object> reportResult(Map<String, Object> result) {
        Map<String, String> headers = vneidHeaders(new HeaderOpts());
        return http.postJson(tcdnPath(RESULT_SERVICE_CODE, "/agent/account/receive-result"), result, headers);
    }
}
