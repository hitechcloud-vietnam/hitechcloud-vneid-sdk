package vn.hitechcloud.vneid;

/** Client configuration shared with resources. */
public class Config {
    public final String apiKey;
    public final String targetCode;
    public final String licenseId;
    public final String deviceType;
    public final String cskPrivateKey;

    public Config(String apiKey, String targetCode, String licenseId, String deviceType, String cskPrivateKey) {
        this.apiKey = apiKey;
        this.targetCode = targetCode;
        this.licenseId = licenseId;
        this.deviceType = deviceType;
        this.cskPrivateKey = cskPrivateKey;
    }
}
