/**
 * Signature helper for the HiTechCloud VNeID SDK.
 *
 *  - eID (chip CCCD): RSA-SHA256 request signing with the CSK private key and
 *    RSA-SHA256 verification of the eID response signature.
 *  - VNeID data (userInfo / biometricResult): JWS ES256 verification + payload parse
 *    with the C06 public key.
 */
import * as crypto from "crypto";
import { SignatureError } from "./exceptions";

function b64urlDecode(seg: string): Buffer {
  return Buffer.from(seg, "base64url");
}

export class Signature {
  /** Join the eID fields with '|' in the required order. */
  static buildEidSigningString(
    requestId: string,
    requestTime: string,
    licenseId: string,
    deviceType: string,
    diaChi: string,
    idCard: string,
    dscert: string,
  ): string {
    return [requestId, requestTime, licenseId, deviceType, diaChi, idCard, dscert].join("|");
  }

  /** Return a Base64 RSA-SHA256 signature of the eID signing string. */
  static signEid(
    requestId: string,
    requestTime: string,
    licenseId: string,
    deviceType: string,
    diaChi: string,
    idCard: string,
    dscert: string,
    privateKeyPem: string,
  ): string {
    const signingString = Signature.buildEidSigningString(
      requestId, requestTime, licenseId, deviceType, diaChi, idCard, dscert,
    );
    return Signature.sign(signingString, privateKeyPem);
  }

  /** Sign an arbitrary string with RSA-SHA256, return Base64. */
  static sign(signingString: string, privateKeyPem: string): string {
    const signer = crypto.createSign("RSA-SHA256");
    signer.update(signingString, "utf8");
    signer.end();
    return signer.sign(privateKeyPem, "base64");
  }

  /** Verify an RSA-SHA256 eID response signature. */
  static verifyEidResponse(signingString: string, signatureB64: string, publicKeyPem: string): boolean {
    const verifier = crypto.createVerify("RSA-SHA256");
    verifier.update(signingString, "utf8");
    verifier.end();
    return verifier.verify(publicKeyPem, Buffer.from(signatureB64, "base64"));
  }

  /**
   * Verify an ES256 JWS compact string and return the parsed payload.
   * Throws SignatureError on a bad signature.
   */
  static verifyJws<T = Record<string, unknown>>(jwsCompact: string, publicKeyPem: string): T {
    const parts = jwsCompact.split(".");
    if (parts.length !== 3) {
      throw new SignatureError("Invalid JWS: expected 3 segments");
    }
    const [headerSeg, payloadSeg, signatureSeg] = parts;
    const signingInput = Buffer.from(`${headerSeg}.${payloadSeg}`, "ascii");
    const rawSig = b64urlDecode(signatureSeg); // r||s (64 bytes for ES256)

    const ok = crypto.verify(
      "sha256",
      signingInput,
      { key: publicKeyPem, dsaEncoding: "ieee-p1363" },
      rawSig,
    );
    if (!ok) {
      throw new SignatureError("JWS signature verification failed");
    }
    return JSON.parse(b64urlDecode(payloadSeg).toString("utf8")) as T;
  }

  /** Parse the JWS payload WITHOUT verifying (sandbox/debug only). */
  static decodeJwsUnverified<T = Record<string, unknown>>(jwsCompact: string): T {
    const parts = jwsCompact.split(".");
    if (parts.length !== 3) {
      throw new SignatureError("Invalid JWS: expected 3 segments");
    }
    return JSON.parse(b64urlDecode(parts[1]).toString("utf8")) as T;
  }
}
