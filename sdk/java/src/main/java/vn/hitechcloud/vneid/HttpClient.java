package vn.hitechcloud.vneid;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Low-level HTTP client with retry logic. */
public class HttpClient {
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

    private final String baseUrl;
    private final int timeout;
    private final int retries;

    public HttpClient(String baseUrl, int timeout, int retries) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.timeout = timeout;
        this.retries = retries;
    }

    public Map<String, Object> postJson(String path, Object body, Map<String, String> headers) {
        return request("POST", path, GSON.toJson(body), "application/json; charset=utf-8", headers);
    }

    public Map<String, Object> postForm(String path, Map<String, String> form, Map<String, String> headers) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : form.entrySet()) {
            if (sb.length() > 0) sb.append('&');
            sb.append(enc(e.getKey())).append('=').append(enc(e.getValue()));
        }
        return request("POST", path, sb.toString(), "application/x-www-form-urlencoded", headers);
    }

    public Map<String, Object> get(String path, Map<String, String> headers) {
        return request("GET", path, null, null, headers);
    }

    private Map<String, Object> request(String method, String path, String body, String contentType, Map<String, String> headers) {
        Exception lastError = null;

        for (int attempt = 0; attempt <= retries; attempt++) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(baseUrl + "/" + path.replaceAll("^/+", ""));
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);
                conn.setConnectTimeout(timeout * 1000);
                conn.setReadTimeout(timeout * 1000);
                conn.setRequestProperty("User-Agent", "HiTechCloud-VNeID-Java-SDK/" + VNeIDClient.VERSION);
                conn.setRequestProperty("Accept", "application/json");
                if (contentType != null) {
                    conn.setRequestProperty("Content-Type", contentType);
                }
                if (headers != null) {
                    for (Map.Entry<String, String> h : headers.entrySet()) {
                        conn.setRequestProperty(h.getKey(), h.getValue());
                    }
                }
                if (body != null && ("POST".equals(method) || "PUT".equals(method))) {
                    conn.setDoOutput(true);
                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(body.getBytes(StandardCharsets.UTF_8));
                    }
                }

                int status = conn.getResponseCode();

                if (status == 429) {
                    int retryAfter = parseIntOr(conn.getHeaderField("Retry-After"), 60);
                    if (attempt < retries) {
                        sleep(retryAfter * 1000L);
                        continue;
                    }
                    throw new RateLimitException("Rate limit exceeded", null, null, retryAfter);
                }
                if (status >= 500 && attempt < retries) {
                    sleep((long) Math.pow(2, attempt) * 1000);
                    continue;
                }

                InputStream is = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
                String responseBody = readStream(is);
                Map<String, Object> result = responseBody.isEmpty() ? null : GSON.fromJson(responseBody, MAP_TYPE);

                if (status >= 400) {
                    throw mapError(status, result);
                }
                return result;
            } catch (HiTechCloudVNeIDException e) {
                throw e;
            } catch (Exception e) {
                lastError = e;
                if (attempt < retries) {
                    sleep((long) Math.pow(2, attempt) * 1000);
                    continue;
                }
            } finally {
                if (conn != null) conn.disconnect();
            }
        }
        throw new HiTechCloudVNeIDException("Request failed: " + (lastError != null ? lastError.getMessage() : "unknown"));
    }

    private HiTechCloudVNeIDException mapError(int status, Map<String, Object> data) {
        String message = "API error";
        String responseCode = null;
        if (data != null) {
            for (String k : new String[]{"description", "message", "error"}) {
                Object v = data.get(k);
                if (v != null) { message = v.toString(); break; }
            }
            Object rc = data.get("responseCode");
            if (rc != null) responseCode = rc.toString();
        }
        switch (status) {
            case 400: return new ValidationException(message, status, responseCode, data);
            case 401: return new AuthenticationException(message, status, responseCode, data);
            case 403: return new AuthorizationException(message, status, responseCode, data);
            case 404: return new NotFoundException(message, status, responseCode, data);
            case 501: return new BiometricException(message, status, responseCode, data);
            default:
                if (status >= 500) return new ServerException(message, status, responseCode, data);
                return new HiTechCloudVNeIDException(message, status, responseCode, data);
        }
    }

    private static String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private static String enc(String s) {
        try {
            return URLEncoder.encode(s, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return s;
        }
    }

    private static int parseIntOr(String s, int fallback) {
        try {
            return s == null ? fallback : Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
