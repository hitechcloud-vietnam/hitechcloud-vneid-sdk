package vn.hitechcloud.vneid.resources;

import vn.hitechcloud.vneid.Config;
import vn.hitechcloud.vneid.HttpClient;
import vn.hitechcloud.vneid.Signature;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BaseResource {
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter VNEID_FMT = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");
    private static final DateTimeFormatter EID_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    protected final HttpClient http;
    protected final Config config;
    public final Signature signature;

    public BaseResource(HttpClient http, Config config, Signature signature) {
        this.http = http;
        this.config = config;
        this.signature = signature;
    }

    protected static String newRequestId() {
        return UUID.randomUUID().toString();
    }

    /** ddMMyyyyHHmmss — VNeID services. */
    protected static String vneidRequestTime() {
        return ZonedDateTime.now(VN_ZONE).format(VNEID_FMT);
    }

    /** yyyyMMddHHmmss — eID services. */
    protected static String eidRequestTime() {
        return ZonedDateTime.now(VN_ZONE).format(EID_FMT);
    }

    protected static class HeaderOpts {
        String targetCode;
        String requestType;
        String requestId;
        String contentType = "application/json; charset=utf-8";
        String bearer;
        boolean includeTargetCode = true;
    }

    protected Map<String, String> vneidHeaders(HeaderOpts opts) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", opts.contentType);
        headers.put("request_id", opts.requestId != null ? opts.requestId : newRequestId());
        headers.put("request_time", vneidRequestTime());
        if (config.apiKey != null) {
            headers.put("api_key", config.apiKey);
        }
        if (opts.includeTargetCode) {
            String tc = opts.targetCode != null ? opts.targetCode : config.targetCode;
            if (tc != null) {
                headers.put("target_code", tc);
            }
        }
        if (opts.requestType != null) {
            headers.put("request_type", opts.requestType);
        }
        if (opts.bearer != null) {
            headers.put("Authorization", "Bearer " + opts.bearer);
        }
        return headers;
    }

    protected static String tcdnPath(String serviceCode, String suffix) {
        return "/api/tcdn/" + serviceCode + suffix;
    }
}
