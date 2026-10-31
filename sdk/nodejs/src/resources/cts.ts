/** CTS resource — digital certificate (SmartCA) issuance. */
import { BaseResource } from "./base";
import { InitResponse } from "./dataShare";

export type CertType = "PER" | "ORG" | "PER-ORG";

export interface CtsInitParams {
  citizenPid: string;
  certType?: CertType;
  shareOrgNumber?: string;
  feeCode?: string;
  listOrgCode?: string[];
  keyData?: string;
  serviceCode?: string;
  targetCode?: string;
}

export class CtsResource extends BaseResource {
  static readonly RESULT_SERVICE_CODE = "create-smartca-account";

  /** POST /api/tcdn/{serviceCode}/api/agent/init with CTS fields. */
  async init(params: CtsInitParams): Promise<InitResponse> {
    const body: Record<string, unknown> = {
      citizenPid: params.citizenPid,
      type: params.certType ?? "PER",
    };
    if (params.shareOrgNumber !== undefined) body.shareOrgNumber = params.shareOrgNumber;
    if (params.listOrgCode !== undefined) body.listOrgCode = params.listOrgCode;
    if (params.feeCode !== undefined) body.feeCode = params.feeCode;
    if (params.keyData !== undefined) body.keyData = params.keyData;

    const headers = this.vneidHeaders({ targetCode: params.targetCode });
    const path = this.tcdnPath(params.serviceCode ?? "share-info", "/api/agent/init");
    return this.http.postJson<InitResponse>(path, body, headers);
  }

  /** POST /api/tcdn/create-smartca-account/agent/account/receive-result */
  async reportResult(result: Record<string, unknown>, targetCode?: string): Promise<Record<string, unknown>> {
    const headers = this.vneidHeaders({ targetCode });
    const path = this.tcdnPath(CtsResource.RESULT_SERVICE_CODE, "/agent/account/receive-result");
    return this.http.postJson<Record<string, unknown>>(path, result, headers);
  }
}
