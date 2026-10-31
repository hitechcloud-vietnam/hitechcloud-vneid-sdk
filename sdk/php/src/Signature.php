<?php

namespace HiTechCloud\VNeID;

/**
 * Signature helper.
 *
 *  - eID (chip CCCD): RSA-SHA256 request signing with the CSK private key and
 *    RSA-SHA256 verification of the eID response signature.
 *  - VNeID data (userInfo / biometricResult): JWS ES256 verification + payload parse
 *    with the C06 public key.
 */
class Signature
{
    /** Join the eID fields with '|' in the required order. */
    public static function buildEidSigningString(
        string $requestId,
        string $requestTime,
        string $licenseId,
        string $deviceType,
        string $diaChi,
        string $idCard,
        string $dscert
    ): string {
        return implode('|', [$requestId, $requestTime, $licenseId, $deviceType, $diaChi, $idCard, $dscert]);
    }

    /** Return a Base64 RSA-SHA256 signature of the eID signing string. */
    public static function signEid(
        string $requestId,
        string $requestTime,
        string $licenseId,
        string $deviceType,
        string $diaChi,
        string $idCard,
        string $dscert,
        string $privateKeyPem
    ): string {
        $signingString = self::buildEidSigningString(
            $requestId, $requestTime, $licenseId, $deviceType, $diaChi, $idCard, $dscert
        );
        return self::sign($signingString, $privateKeyPem);
    }

    /** Sign an arbitrary string with RSA-SHA256, return Base64. */
    public static function sign(string $signingString, string $privateKeyPem): string
    {
        $key = openssl_pkey_get_private($privateKeyPem);
        if ($key === false) {
            throw new SignatureException('Invalid CSK private key');
        }
        $signature = '';
        if (!openssl_sign($signingString, $signature, $key, OPENSSL_ALGO_SHA256)) {
            throw new SignatureException('RSA-SHA256 signing failed');
        }
        return base64_encode($signature);
    }

    /** Verify an RSA-SHA256 eID response signature. */
    public static function verifyEidResponse(string $signingString, string $signatureB64, string $publicKeyPem): bool
    {
        $key = openssl_pkey_get_public($publicKeyPem);
        if ($key === false) {
            throw new SignatureException('Invalid public key');
        }
        $result = openssl_verify($signingString, base64_decode($signatureB64), $key, OPENSSL_ALGO_SHA256);
        return $result === 1;
    }

    /**
     * Verify an ES256 JWS compact string and return the parsed payload (associative array).
     *
     * @throws SignatureException on a bad signature
     */
    public static function verifyJws(string $jwsCompact, string $publicKeyPem): array
    {
        $parts = explode('.', $jwsCompact);
        if (count($parts) !== 3) {
            throw new SignatureException('Invalid JWS: expected 3 segments');
        }
        [$headerSeg, $payloadSeg, $signatureSeg] = $parts;
        $signingInput = $headerSeg . '.' . $payloadSeg;
        $rawSig = self::base64UrlDecode($signatureSeg);
        if (strlen($rawSig) % 2 !== 0) {
            throw new SignatureException('Invalid ES256 signature length');
        }
        $der = self::ecRawToDer($rawSig);

        $key = openssl_pkey_get_public($publicKeyPem);
        if ($key === false) {
            throw new SignatureException('Invalid C06 public key');
        }
        $result = openssl_verify($signingInput, $der, $key, OPENSSL_ALGO_SHA256);
        if ($result !== 1) {
            throw new SignatureException('JWS signature verification failed');
        }
        return json_decode(self::base64UrlDecode($payloadSeg), true);
    }

    /** Parse the JWS payload WITHOUT verifying (sandbox/debug only). */
    public static function decodeJwsUnverified(string $jwsCompact): array
    {
        $parts = explode('.', $jwsCompact);
        if (count($parts) !== 3) {
            throw new SignatureException('Invalid JWS: expected 3 segments');
        }
        return json_decode(self::base64UrlDecode($parts[1]), true);
    }

    private static function base64UrlDecode(string $data): string
    {
        $remainder = strlen($data) % 4;
        if ($remainder) {
            $data .= str_repeat('=', 4 - $remainder);
        }
        return base64_decode(strtr($data, '-_', '+/'));
    }

    /** Convert a raw ECDSA signature (r||s) into DER for openssl_verify. */
    private static function ecRawToDer(string $raw): string
    {
        $half = intdiv(strlen($raw), 2);
        $r = ltrim(substr($raw, 0, $half), "\x00");
        $s = ltrim(substr($raw, $half), "\x00");
        if (ord($r[0] ?? "\x00") > 0x7f) {
            $r = "\x00" . $r;
        }
        if (ord($s[0] ?? "\x00") > 0x7f) {
            $s = "\x00" . $s;
        }
        $rEnc = "\x02" . chr(strlen($r)) . $r;
        $sEnc = "\x02" . chr(strlen($s)) . $s;
        $seq = $rEnc . $sEnc;
        return "\x30" . chr(strlen($seq)) . $seq;
    }
}
