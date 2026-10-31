package vn.hitechcloud.vneid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VNeIDClientTest {

    @Test
    void baseUrlDefaults() {
        assertEquals(VNeIDClient.PRODUCTION_URL,
                VNeIDClient.builder().apiKey("k").build().getBaseUrl());
        assertEquals(VNeIDClient.SANDBOX_URL,
                VNeIDClient.builder().apiKey("k").sandbox(true).build().getBaseUrl());
    }

    @Test
    void resourcesPresent() {
        VNeIDClient c = VNeIDClient.builder().apiKey("k").targetCode("t").build();
        assertNotNull(c.eid);
        assertNotNull(c.dataShare);
        assertNotNull(c.biometric);
        assertNotNull(c.verifyInfo);
        assertNotNull(c.organization);
        assertNotNull(c.sso);
        assertNotNull(c.cts);
        assertNotNull(c.consent);
    }

    @Test
    void universalLink() {
        VNeIDClient c = VNeIDClient.builder().apiKey("k").build();
        assertEquals("https://universal.dancuquocgia.com/share/abc",
                c.consent.buildUniversalLink("abc"));
    }

    @Test
    void eidSigningStringOrder() {
        String s = Signature.buildEidSigningString("rid", "20261101093000", "LIC", "WEB", "hanoi", "001", "dscert");
        assertEquals("rid|20261101093000|LIC|WEB|hanoi|001|dscert", s);
    }

    @Test
    void authorizeUrl() {
        VNeIDClient c = VNeIDClient.builder().apiKey("k").build();
        String url = c.sso.buildAuthorizeUrl("https://sso.vneid.gov.vn", "cid", "https://app/cb", null);
        assertTrue(url.startsWith("https://sso.vneid.gov.vn/auth?"));
        assertTrue(url.contains("client_id=cid"));
    }
}
