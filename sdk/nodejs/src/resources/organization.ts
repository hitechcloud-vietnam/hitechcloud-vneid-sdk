/** Organization resource — organization identity (type=ORG). */
import { BaseResource } from "./base";
import { InitResponse, TransactionResponse } from "./dataShare";

export class OrganizationResource extends BaseResource {
  static readonly SERVICE_CODE = "share-info";

  /** POST /api/tcdn/share-info/api/agent/init with type=ORG. */
  async init(
    shareOrgNumber: string,
    opts: { citizenPid?: string; extra?: Record<string, unknown>; targetCode?: string } = {},
  ): Promise<InitResponse> {
    const body: Record<string, unknown> = {
      citizenPid: opts.citizenPid ?? "",
      type: "ORG",
      shareOrgNumber,
      ...(opts.extra ?? {}),
    };
    const headers = this.vneidHeaders({ targetCode: opts.targetCode });
    const path = this.tcdnPath(OrganizationResource.SERVICE_CODE, "/api/agent/init");
    return this.http.postJson<InitResponse>(path, body, headers);
  }

  /** POST /api/tcdn/share-info/api/get-transaction — org data (orgPid, taxCode, ...). */
  async getTransaction(transactionId: string, targetCode?: string): Promise<TransactionResponse> {
    const headers = this.vneidHeaders({ targetCode });
    const path = this.tcdnPath(OrganizationResource.SERVICE_CODE, "/api/get-transaction");
    return this.http.postJson<TransactionResponse>(path, { transactionId }, headers);
  }
}
