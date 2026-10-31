// Basic usage of the HiTechCloud VNeID SDK (Java).
// Set VNEID_API_KEY and VNEID_TARGET_CODE in the environment.
import vn.hitechcloud.vneid.VNeIDClient;
import vn.hitechcloud.vneid.HiTechCloudVNeIDException;
import vn.hitechcloud.vneid.RateLimitException;

import java.util.Map;

public class BasicUsage {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        VNeIDClient client = VNeIDClient.builder()
                .apiKey(getenv("VNEID_API_KEY", "YOUR_API_KEY"))
                .targetCode(getenv("VNEID_TARGET_CODE", "YOUR_TARGET_CODE"))
                .sandbox(true)
                .build();

        try {
            // 1) Start a data-share transaction
            Map<String, Object> init = client.dataShare().init(Map.of("citizenPid", "036188001958"));
            Map<String, Object> data = (Map<String, Object>) init.get("data");
            String txnId = (String) data.get("txnId");
            System.out.println("txnId: " + txnId);

            // 2) Redirect the citizen to VNeID (App-to-App)
            System.out.println("Open on VNeID: " + client.consent().buildUniversalLink(txnId));

            // 3) After the citizen consents, fetch the data
            Map<String, Object> tx = client.dataShare().getTransaction(txnId);

            // 4) Verify the JWS and read the payload
            String c06PublicKey = System.getenv("C06_PUBLIC_KEY");
            if (c06PublicKey != null && tx.get("userInfo") != null) {
                Map<String, Object> userInfo = client.signature().verifyJws((String) tx.get("userInfo"), c06PublicKey);
                System.out.println("fullName: " + userInfo.get("fullName"));
            }
        } catch (RateLimitException e) {
            System.out.println("Rate limited, retry after " + e.getRetryAfter() + " seconds");
        } catch (HiTechCloudVNeIDException e) {
            System.out.println("API error: " + e.getMessage() + " (HTTP " + e.getStatusCode() + ")");
        }
    }

    private static String getenv(String key, String fallback) {
        String v = System.getenv(key);
        return v != null ? v : fallback;
    }
}
