/** Biometric resource — face matching / liveness (v2/get-transaction). */
import { BaseResource } from "./base";
import { TransactionResponse } from "./dataShare";

export type RequestType = "sdk-match" | "sdk-liveness-match";

export class BiometricResource extends BaseResource {
  /** POST /api/tcdn/{serviceCode}/v2/api/get-transaction */
  async getTransactionV2(
    serviceCode: string,
    transactionId: string,
    faceImage?: string,
    requestType: RequestType = "sdk-liveness-match",
    targetCode?: string,
  ): Promise<TransactionResponse> {
    const body: Record<string, unknown> = { transactionId };
    if (faceImage !== undefined) body.faceImage = faceImage;
    const headers = this.vneidHeaders({ targetCode, requestType });
    const path = this.tcdnPath(serviceCode, "/v2/api/get-transaction");
    return this.http.postJson<TransactionResponse>(path, body, headers);
  }

  /** Convenience for service-code face-matching. */
  faceMatching(
    transactionId: string,
    faceImage: string,
    requestType: RequestType = "sdk-liveness-match",
    targetCode?: string,
  ): Promise<TransactionResponse> {
    return this.getTransactionV2("face-matching", transactionId, faceImage, requestType, targetCode);
  }

  /** Convenience for service-code biometric-share-info. */
  shareWithBiometric(
    transactionId: string,
    faceImage: string,
    requestType: RequestType = "sdk-liveness-match",
    targetCode?: string,
  ): Promise<TransactionResponse> {
    return this.getTransactionV2("biometric-share-info", transactionId, faceImage, requestType, targetCode);
  }
}
