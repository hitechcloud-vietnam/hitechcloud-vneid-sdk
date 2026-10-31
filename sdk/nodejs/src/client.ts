/** HiTechCloud VNeID SDK — main client */
import { HTTPClient, ClientCert } from "./httpClient";
import { Signature } from "./signature";
import { VNeIDConfig } from "./resources/base";
import { EidResource } from "./resources/eid";
import { DataShareResource } from "./resources/dataShare";
import { BiometricResource } from "./resources/biometric";
import { VerifyInfoResource } from "./resources/verifyInfo";
import { OrganizationResource } from "./resources/organization";
import { SsoResource } from "./resources/sso";
import { CtsResource } from "./resources/cts";
import { ConsentResource } from "./resources/consent";

export const PRODUCTION_URL = "https://ekyc-c06.hitechcloud.vn";
export const SANDBOX_URL = "https://ekyc-c06-sandbox.hitechcloud.vn";

export interface VNeIDClientOptions {
  apiKey?: string;
  targetCode?: string;
  baseUrl?: string;
  sandbox?: boolean;
  timeout?: number;
  retries?: number;
  licenseId?: string;
  deviceType?: string;
  cskPrivateKey?: string;
  clientCert?: ClientCert;
}

export class VNeIDClient {
  readonly signature = Signature;
  readonly eid: EidResource;
  readonly dataShare: DataShareResource;
  readonly biometric: BiometricResource;
  readonly verifyInfo: VerifyInfoResource;
  readonly organization: OrganizationResource;
  readonly sso: SsoResource;
  readonly cts: CtsResource;
  readonly consent: ConsentResource;

  private readonly http: HTTPClient;

  constructor(options: VNeIDClientOptions = {}) {
    const {
      apiKey,
      targetCode,
      baseUrl,
      sandbox = false,
      timeout = 30000,
      retries = 3,
      licenseId,
      deviceType,
      cskPrivateKey,
      clientCert,
    } = options;

    const resolvedBase = baseUrl ?? (sandbox ? SANDBOX_URL : PRODUCTION_URL);
    const config: VNeIDConfig = { apiKey, targetCode, licenseId, deviceType, cskPrivateKey };
    this.http = new HTTPClient(resolvedBase, timeout, retries, clientCert);

    this.eid = new EidResource(this.http, config);
    this.dataShare = new DataShareResource(this.http, config);
    this.biometric = new BiometricResource(this.http, config);
    this.verifyInfo = new VerifyInfoResource(this.http, config);
    this.organization = new OrganizationResource(this.http, config);
    this.sso = new SsoResource(this.http, config);
    this.cts = new CtsResource(this.http, config);
    this.consent = new ConsentResource(this.http, config);
  }

  get baseUrl(): string {
    return this.http.baseUrl;
  }
}
