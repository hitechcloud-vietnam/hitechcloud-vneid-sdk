/** Consent resource — receive-result, withdraw-consent, universal link. */
import { BaseResource } from "./base";

export const UNIVERSAL_LINK_BASE = "https://universal.dancuquocgia.com/share";

export class ConsentResource extends BaseResource {
  /** Return the VNeID universal link for a transaction (App-to-App). */
  buildUniversalLink(transactionCode: string): string {
    return `${UNIVERSAL_LINK_BASE}/${transactionCode}`;
  }

  /** POST /api/tcdn/{serviceCode}/agent/account/receive-result */
  async receiveResult(
    serviceCode: string,
    body: Record<string, unknown>,
    targetCode?: string,
  ): Promise<Record<string, unknown>> {
    const headers = this.vneidHeaders({ targetCode });
    const path = this.tcdnPath(serviceCode, "/agent/account/receive-result");
    return this.http.postJson<Record<string, unknown>>(path, body, headers);
  }

  /** POST /api/tcdn/withdraw-consent-result/agent/account/receive-result */
  async withdrawConsentResult(
    requestStatus: "DELETED" | "REJECT",
    opts: { reasonRequestCode?: string | null; reasonRequestText?: string | null; targetCode?: string } = {},
  ): Promise<Record<string, unknown>> {
    const body = {
      requestStatus,
      reasonRequestCode: opts.reasonRequestCode ?? null,
      reasonRequestText: opts.reasonRequestText ?? null,
    };
    const headers = this.vneidHeaders({ targetCode: opts.targetCode });
    const path = this.tcdnPath("withdraw-consent-result", "/agent/account/receive-result");
    return this.http.postJson<Record<string, unknown>>(path, body, headers);
  }
}
