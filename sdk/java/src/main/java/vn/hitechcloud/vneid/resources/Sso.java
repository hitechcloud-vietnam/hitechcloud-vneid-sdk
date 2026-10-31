package vn.hitechcloud.vneid.resources;

import vn.hitechcloud.vneid.Config;
import vn.hitechcloud.vneid.HttpClient;
import vn.hitechcloud.vneid.Signature;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** SSO resource — VNeID SSO (OAuth2/OIDC). */
public class Sso extends BaseResource {

    public Sso(HttpClient http, Config config, Signature signature) {
        super(http, config, signature);
    }

    /** Build the VNeID authorize redirect URL (WEB flow). */
    public String buildAuthorizeUrl(String authorizeBase, String clientId, String redirectUri, String scope) {
        if (scope == null || scope.isEmpty()) scope = "openid";
        String base = authorizeBase.replaceAll("/+$", "");
        return base + "/auth?response_type=code"
                + "&client_id=" + enc(clientId)
                + "&scope=" + enc(scope)
                + "&redirect_uri=" + enc(redirectUri);
    }

    /** POST /api/tcdn/sso-token/api/vneid/form/sso (x-www-form-urlencoded). */
    public Map<String, Object> exchangeToken(String code, String clientId, String clientSecret, String redirectUri) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("grant_type", "authorization_code");
        form.put("client_id", clientId);
        form.put("client_secret", clientSecret);
        form.put("code", code);
        form.put("redirect_uri", redirectUri);

        HeaderOpts opts = new HeaderOpts();
        opts.contentType = "application/x-www-form-urlencoded";
        opts.includeTargetCode = false;
        Map<String, String> headers = vneidHeaders(opts);
        return http.postForm(tcdnPath("sso-token", "/api/vneid/form/sso"), form, headers);
    }

    /** GET /api/tcdn/sso-userinfo/api/vneid/sso */
    public Map<String, Object> getUserInfo(String accessToken) {
        HeaderOpts opts = new HeaderOpts();
        opts.bearer = accessToken;
        opts.includeTargetCode = false;
        Map<String, String> headers = vneidHeaders(opts);
        return http.get(tcdnPath("sso-userinfo", "/api/vneid/sso"), headers);
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
