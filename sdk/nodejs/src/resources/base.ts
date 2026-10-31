/** Base resource with shared header builders. */
import { HTTPClient, Headers, newRequestId, vneidRequestTime } from "../httpClient";
import { Signature } from "../signature";

export interface VNeIDConfig {
  apiKey?: string;
  targetCode?: string;
  licenseId?: string;
  deviceType?: string;
  cskPrivateKey?: string;
}

export interface VneidHeaderOptions {
  targetCode?: string;
  requestType?: string;
  requestId?: string;
  contentType?: string;
  bearer?: string;
  includeTargetCode?: boolean;
}

export class BaseResource {
  protected http: HTTPClient;
  protected config: VNeIDConfig;
  readonly signature: typeof Signature;

  constructor(http: HTTPClient, config: VNeIDConfig) {
    this.http = http;
    this.config = config;
    this.signature = Signature;
  }

  protected vneidHeaders(opts: VneidHeaderOptions = {}): Headers {
    const {
      targetCode,
      requestType,
      requestId,
      contentType = "application/json; charset=utf-8",
      bearer,
      includeTargetCode = true,
    } = opts;

    const headers: Headers = {
      "Content-Type": contentType,
      request_id: requestId ?? newRequestId(),
      request_time: vneidRequestTime(),
    };
    if (this.config.apiKey) headers.api_key = this.config.apiKey;
    if (includeTargetCode) {
      const tc = targetCode ?? this.config.targetCode;
      if (tc) headers.target_code = tc;
    }
    if (requestType) headers.request_type = requestType;
    if (bearer) headers.Authorization = `Bearer ${bearer}`;
    return headers;
  }

  protected tcdnPath(serviceCode: string, suffix: string): string {
    return `/api/tcdn/${serviceCode}${suffix}`;
  }
}
