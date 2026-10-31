/** Data share resource — share-info service. */
import { BaseResource } from "./base";

export interface InitParams {
  citizenPid: string;
  fullName?: string;
  birthDate?: string;
  [key: string]: unknown;
}

export interface InitResponse {
  description?: string;
  responseCode?: string;
  responseTime?: string;
  data: { txnId: string; requestId: string };
}

export interface TransactionResponse {
  description?: string;
  responseCode?: string;
  responseTime?: string;
  data?: Record<string, unknown>;
  userInfo?: string;
  biometricResult?: string;
}

export class DataShareResource extends BaseResource {
  static readonly SERVICE_CODE = "share-info";

  /** POST /api/tcdn/share-info/api/agent/init */
  async init(params: InitParams, targetCode?: string): Promise<InitResponse> {
    const headers = this.vneidHeaders({ targetCode });
    const path = this.tcdnPath(DataShareResource.SERVICE_CODE, "/api/agent/init");
    return this.http.postJson<InitResponse>(path, params, headers);
  }

  /** POST /api/tcdn/share-info/api/get-transaction */
  async getTransaction(transactionId: string, targetCode?: string): Promise<TransactionResponse> {
    const headers = this.vneidHeaders({ targetCode });
    const path = this.tcdnPath(DataShareResource.SERVICE_CODE, "/api/get-transaction");
    return this.http.postJson<TransactionResponse>(path, { transactionId }, headers);
  }
}
