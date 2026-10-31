package vn.hitechcloud.vneid;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

/**
 * Signature helper.
 *
 *  - eID (chip CCCD): RSA-SHA256 request signing with the CSK private key and
 *    RSA-SHA256 verification of the eID response signature.
 *  - VNeID data (userInfo / biometricResult): JWS ES256 verification + payload parse
 *    with the C06 public key.
 */
public class Signature {
    private static final Gson GSON = new Gson();

    /** Join the eID fields with '|' in the required order. */
    public static String buildEidSigningString(String requestId, String requestTime, String licenseId,
                                               String deviceType, String diaChi, String idCard, String dscert) {
        return String.join("|", requestId, requestTime, licenseId, deviceType, diaChi, idCard, dscert);
    }

    /** Return a Base64 RSA-SHA256 signature of the eID signing string. */
    public String signEid(String requestId, String requestTime, String licenseId, String deviceType,
                          String diaChi, String idCard, String dscert, String privateKeyPem) {
        return sign(buildEidSigningString(requestId, requestTime, licenseId, deviceType, diaChi, idCard, dscert), privateKeyPem);
    }

    /** Sign an arbitrary string with RSA-SHA256, return Base64. */
    public String sign(String signingString, String privateKeyPem) {
        try {
            PrivateKey key = loadPrivateKey(privateKeyPem);
            java.security.Signature signer = java.security.Signature.getInstance("SHA256withRSA");
            signer.initSign(key);
            signer.update(signingString.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signer.sign());
        } catch (Exception e) {
            throw new SignatureException("RSA-SHA256 signing failed: " + e.getMessage());
        }
    }

    /** Verify an RSA-SHA256 eID response signature. */
    public boolean verifyEidResponse(String signingString, String signatureB64, String publicKeyPem) {
        try {
            PublicKey key = loadPublicKey(publicKeyPem);
            java.security.Signature verifier = java.security.Signature.getInstance("SHA256withRSA");
            verifier.initVerify(key);
            verifier.update(signingString.getBytes(StandardCharsets.UTF_8));
            return verifier.verify(Base64.getDecoder().decode(signatureB64));
        } catch (Exception e) {
            throw new SignatureException("eID response verification failed: " + e.getMessage());
        }
    }

    /** Verify an ES256 JWS compact string and return the parsed payload. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> verifyJws(String jwsCompact, String publicKeyPem) {
        String[] parts = jwsCompact.split("\\.");
        if (parts.length != 3) {
            throw new SignatureException("Invalid JWS: expected 3 segments");
        }
        try {
            PublicKey key = loadPublicKey(publicKeyPem);
            String signingInput = parts[0] + "." + parts[1];
            byte[] rawSig = Base64.getUrlDecoder().decode(parts[2]);
            byte[] der = ecRawToDer(rawSig);

            java.security.Signature verifier = java.security.Signature.getInstance("SHA256withECDSA");
            verifier.initVerify(key);
            verifier.update(signingInput.getBytes(StandardCharsets.US_ASCII));
            if (!verifier.verify(der)) {
                throw new SignatureException("JWS signature verification failed");
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
            return (Map<String, Object>) GSON.fromJson(payload, mapType);
        } catch (SignatureException e) {
            throw e;
        } catch (Exception e) {
            throw new SignatureException("JWS verification error: " + e.getMessage());
        }
    }

    /** Parse the JWS payload WITHOUT verifying (sandbox/debug only). */
    @SuppressWarnings("unchecked")
    public Map<String, Object> decodeJwsUnverified(String jwsCompact) {
        String[] parts = jwsCompact.split("\\.");
        if (parts.length != 3) {
            throw new SignatureException("Invalid JWS: expected 3 segments");
        }
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
        return (Map<String, Object>) GSON.fromJson(payload, mapType);
    }

    private static PrivateKey loadPrivateKey(String pem) throws Exception {
        byte[] der = pemBody(pem);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    private static PublicKey loadPublicKey(String pem) throws Exception {
        String trimmed = pem.trim();
        if (trimmed.contains("BEGIN CERTIFICATE")) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(
                new java.io.ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8)));
            return cert.getPublicKey();
        }
        byte[] der = pemBody(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
        String algorithm = trimmed.contains("EC") ? "EC" : "RSA";
        try {
            return KeyFactory.getInstance(algorithm).generatePublic(spec);
        } catch (Exception e) {
            // fall back to the other algorithm
            String other = algorithm.equals("EC") ? "RSA" : "EC";
            return KeyFactory.getInstance(other).generatePublic(spec);
        }
    }

    private static byte[] pemBody(String pem) {
        String body = pem.replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(body);
    }

    /** Convert a raw ECDSA signature (r||s) into DER. */
    private static byte[] ecRawToDer(byte[] raw) {
        int half = raw.length / 2;
        byte[] rb = new byte[half];
        byte[] sb = new byte[half];
        System.arraycopy(raw, 0, rb, 0, half);
        System.arraycopy(raw, half, sb, 0, half);
        byte[] r = new BigInteger(1, rb).toByteArray();
        byte[] s = new BigInteger(1, sb).toByteArray();
        byte[] rEnc = derInteger(r);
        byte[] sEnc = derInteger(s);
        int seqLen = rEnc.length + sEnc.length;
        byte[] out = new byte[2 + seqLen];
        out[0] = 0x30;
        out[1] = (byte) seqLen;
        System.arraycopy(rEnc, 0, out, 2, rEnc.length);
        System.arraycopy(sEnc, 0, out, 2 + rEnc.length, sEnc.length);
        return out;
    }

    private static byte[] derInteger(byte[] value) {
        byte[] out = new byte[2 + value.length];
        out[0] = 0x02;
        out[1] = (byte) value.length;
        System.arraycopy(value, 0, out, 2, value.length);
        return out;
    }
}
