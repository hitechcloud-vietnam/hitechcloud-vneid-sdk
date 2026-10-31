package vn.hitechcloud.vneid;

import vn.hitechcloud.vneid.resources.Biometric;
import vn.hitechcloud.vneid.resources.Consent;
import vn.hitechcloud.vneid.resources.Cts;
import vn.hitechcloud.vneid.resources.DataShare;
import vn.hitechcloud.vneid.resources.Eid;
import vn.hitechcloud.vneid.resources.Organization;
import vn.hitechcloud.vneid.resources.Sso;
import vn.hitechcloud.vneid.resources.VerifyInfo;

/**
 * HiTechCloud eKYC — VNeID client.
 *
 * <pre>
 * VNeIDClient client = VNeIDClient.builder()
 *     .apiKey("...").targetCode("...").sandbox(true).build();
 * </pre>
 */
public class VNeIDClient {
    public static final String PRODUCTION_URL = "https://ekyc-c06.hitechcloud.vn";
    public static final String SANDBOX_URL = "https://ekyc-c06-sandbox.hitechcloud.vn";
    public static final String VERSION = "1.0.0";

    private final String baseUrl;
    public final Signature signature;
    public final Eid eid;
    public final DataShare dataShare;
    public final Biometric biometric;
    public final VerifyInfo verifyInfo;
    public final Organization organization;
    public final Sso sso;
    public final Cts cts;
    public final Consent consent;

    private VNeIDClient(Builder b) {
        this.baseUrl = b.baseUrl != null ? b.baseUrl : (b.sandbox ? SANDBOX_URL : PRODUCTION_URL);
        Config config = new Config(b.apiKey, b.targetCode, b.licenseId, b.deviceType, b.cskPrivateKey);
        HttpClient http = new HttpClient(this.baseUrl, b.timeout, b.retries);
        this.signature = new Signature();

        this.eid = new Eid(http, config, signature);
        this.dataShare = new DataShare(http, config, signature);
        this.biometric = new Biometric(http, config, signature);
        this.verifyInfo = new VerifyInfo(http, config, signature);
        this.organization = new Organization(http, config, signature);
        this.sso = new Sso(http, config, signature);
        this.cts = new Cts(http, config, signature);
        this.consent = new Consent(http, config, signature);
    }

    public String getBaseUrl() { return baseUrl; }

    public Eid eid() { return eid; }
    public DataShare dataShare() { return dataShare; }
    public Biometric biometric() { return biometric; }
    public VerifyInfo verifyInfo() { return verifyInfo; }
    public Organization organization() { return organization; }
    public Sso sso() { return sso; }
    public Cts cts() { return cts; }
    public Consent consent() { return consent; }
    public Signature signature() { return signature; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String apiKey;
        private String targetCode;
        private String baseUrl;
        private boolean sandbox = false;
        private int timeout = 30;
        private int retries = 3;
        private String licenseId;
        private String deviceType;
        private String cskPrivateKey;

        public Builder apiKey(String v) { this.apiKey = v; return this; }
        public Builder targetCode(String v) { this.targetCode = v; return this; }
        public Builder baseUrl(String v) { this.baseUrl = v; return this; }
        public Builder sandbox(boolean v) { this.sandbox = v; return this; }
        public Builder timeout(int v) { this.timeout = v; return this; }
        public Builder retries(int v) { this.retries = v; return this; }
        public Builder licenseId(String v) { this.licenseId = v; return this; }
        public Builder deviceType(String v) { this.deviceType = v; return this; }
        public Builder cskPrivateKey(String v) { this.cskPrivateKey = v; return this; }

        public VNeIDClient build() { return new VNeIDClient(this); }
    }
}
