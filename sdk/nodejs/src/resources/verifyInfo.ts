/** Verify-info resource — True/False verification against the population DB. */
import { BaseResource } from "./base";
import { InitParams, InitResponse, TransactionResponse } from "./dataShare";

export class VerifyInfoResource extends BaseResource {
  static readonly SERVICE_CODE = "verify-info";

  /**
   * POST /api/tcdn/verify-info/api/agent/init
   * Fields sent here are verified (True/False) on getTransaction.
   */
  async init(params: InitParams, targetCode?: string): Promise<InitResponse> {
    const headers = this.vneidHeaders({ targetCode });
    const path = this.tcdnPath(VerifyInfoResource.SERVICE_CODE, "/api/agent/init");
    return this.http.postJson<InitResponse>(path, params, headers);
  }

  /** POST /api/tcdn/verify-info/api/get-transaction */
  async getTransaction(transactionId: string, targetCode?: string): Promise<TransactionResponse> {
    const headers = this.vneidHeaders({ targetCode });
    const path = this.tcdnPath(VerifyInfoResource.SERVICE_CODE, "/api/get-transaction");
    return this.http.postJson<TransactionResponse>(path, { transactionId }, headers);
  }
}
