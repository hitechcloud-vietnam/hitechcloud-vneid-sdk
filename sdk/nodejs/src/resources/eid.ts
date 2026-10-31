/** eID resource — chip CCCD verification (RSA-SHA256 signed). */
import { BaseResource } from "./base";
import { newRequestId, eidRequestTime } from "../httpClient";
import { SignatureError } from "../exceptions";

export interface EidVerifyResponse {
  responds: {
    responseId: string;
    exitcode: number;
    result: boolean;
    message: string;
    time: number;
  };
  signature: string;
}

export class EidResource extends BaseResource {
  /**
   * POST /eid/api/verify/request
   * Signs request_id|request_time|license_id|device_type|dia_chi|id_card|dscert with the CSK key.
   */
  async verifyChip(
    idCard: string,
    dscert: string,
    diaChi: string,
    opts: { requestId?: string; requestTime?: string } = {},
  ): Promise<EidVerifyResponse> {
    const { licenseId, deviceType, cskPrivateKey } = this.config;
    if (!licenseId || !deviceType || !cskPrivateKey) {
      throw new SignatureError(
        "eID verification requires licenseId, deviceType and cskPrivateKey",
      );
    }
    const requestId = opts.requestId ?? newRequestId();
    const requestTime = opts.requestTime ?? eidRequestTime();

    const signature = this.signature.signEid(
      requestId, requestTime, licenseId, deviceType, diaChi, idCard, dscert, cskPrivateKey,
    );
    const headers = {
      "content-type": "application/json; charset=utf-8",
      request_id: requestId,
      request_time: requestTime,
      license_id: licenseId,
      device_type: deviceType,
      dia_chi: diaChi,
      signature,
    };
    return this.http.postJson<EidVerifyResponse>(
      "/eid/api/verify/request",
      { id_card: idCard, dscert },
      headers,
    );
  }

  /** Verify the RSA-SHA256 signature of an eID response against the RAR public key. */
  verifyResponseSignature(response: EidVerifyResponse, publicKeyPem: string): boolean {
    const signingString = JSON.stringify(response.responds);
    return this.signature.verifyEidResponse(signingString, response.signature, publicKeyPem);
  }
}
