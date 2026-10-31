/** HiTechCloud VNeID SDK — low-level HTTP client */
import axios, { AxiosInstance, AxiosRequestConfig } from "axios";
import * as crypto from "crypto";
import * as https from "https";
import {
  HiTechCloudVNeIDError,
  AuthenticationError,
  AuthorizationError,
  NotFoundError,
  ValidationError,
  RateLimitError,
  BiometricError,
  ServerError,
} from "./exceptions";

export interface ClientCert {
  cert: string | Buffer;
  key: string | Buffer;
  ca?: string | Buffer;
}

export function newRequestId(): string {
  return crypto.randomUUID();
}

function pad(n: number): string {
  return n < 10 ? `0${n}` : `${n}`;
}

function nowGmt7(): Date {
  const now = new Date();
  // shift to GMT+7
  return new Date(now.getTime() + (now.getTimezoneOffset() + 420) * 60000);
}

/** ddMMyyyyHHmmss — VNeID services */
export function vneidRequestTime(): string {
  const d = nowGmt7();
  return (
    pad(d.getDate()) + pad(d.getMonth() + 1) + d.getFullYear() +
    pad(d.getHours()) + pad(d.getMinutes()) + pad(d.getSeconds())
  );
}

/** yyyyMMddHHmmss — eID services */
export function eidRequestTime(): string {
  const d = nowGmt7();
  return (
    `${d.getFullYear()}` + pad(d.getMonth() + 1) + pad(d.getDate()) +
    pad(d.getHours()) + pad(d.getMinutes()) + pad(d.getSeconds())
  );
}

export type Headers = Record<string, string>;

export class HTTPClient {
  readonly baseUrl: string;
  private readonly retries: number;
  private readonly client: AxiosInstance;

  constructor(baseUrl: string, timeout = 30000, retries = 3, clientCert?: ClientCert) {
    this.baseUrl = baseUrl.replace(/\/+$/, "");
    this.retries = retries;

    const config: AxiosRequestConfig = {
      baseURL: this.baseUrl,
      timeout,
      headers: { "User-Agent": "HiTechCloud-VNeID-NodeJS-SDK/1.0.0", Accept: "application/json" },
      validateStatus: () => true,
    };
    if (clientCert) {
      config.httpsAgent = new https.Agent({
        cert: clientCert.cert,
        key: clientCert.key,
        ca: clientCert.ca,
      });
    }
    this.client = axios.create(config);
  }

  private mapError(status: number, body: unknown): HiTechCloudVNeIDError {
    const b = (body ?? {}) as Record<string, unknown>;
    const message =
      (b.description as string) || (b.message as string) || (b.error as string) || `HTTP ${status}`;
    const responseCode = b.responseCode as string | undefined;
    switch (status) {
      case 400:
        return new ValidationError(message, responseCode, body);
      case 401:
        return new AuthenticationError(message, responseCode, body);
      case 403:
        return new AuthorizationError(message, responseCode, body);
      case 404:
        return new NotFoundError(message, responseCode, body);
      case 501:
        return new BiometricError(message, responseCode, body);
      default:
        if (status >= 500) return new ServerError(message, status, responseCode, body);
        return new HiTechCloudVNeIDError(message, status, responseCode, body);
    }
  }

  async request<T = any>(
    method: string,
    path: string,
    options: { json?: unknown; form?: Record<string, string>; headers?: Headers } = {},
  ): Promise<T> {
    const { json, form, headers } = options;
    let lastError: unknown;

    for (let attempt = 0; attempt <= this.retries; attempt++) {
      try {
        const config: AxiosRequestConfig = { method, url: path, headers };
        if (form) {
          config.data = new URLSearchParams(form).toString();
        } else if (json !== undefined) {
          config.data = json;
        }
        const res = await this.client.request(config);

        if (res.status === 429) {
          const retryAfter = parseInt(String(res.headers["retry-after"] ?? "60"), 10);
          if (attempt < this.retries) {
            await sleep(retryAfter * 1000);
            continue;
          }
          throw new RateLimitError("Rate limit exceeded", retryAfter, undefined, res.data);
        }
        if (res.status >= 500 && attempt < this.retries) {
          await sleep(2 ** attempt * 1000);
          continue;
        }
        if (res.status >= 400) {
          throw this.mapError(res.status, res.data);
        }
        return res.data as T;
      } catch (err) {
        if (err instanceof HiTechCloudVNeIDError) throw err;
        lastError = err;
        if (attempt < this.retries) {
          await sleep(2 ** attempt * 1000);
          continue;
        }
        throw new HiTechCloudVNeIDError(`Request failed: ${(err as Error).message}`);
      }
    }
    throw new HiTechCloudVNeIDError(`Request failed after ${this.retries} retries: ${String(lastError)}`);
  }

  postJson<T = any>(path: string, json: unknown, headers: Headers): Promise<T> {
    return this.request<T>("POST", path, { json, headers });
  }

  postForm<T = any>(path: string, form: Record<string, string>, headers: Headers): Promise<T> {
    return this.request<T>("POST", path, { form, headers });
  }

  get<T = any>(path: string, headers: Headers): Promise<T> {
    return this.request<T>("GET", path, { headers });
  }
}

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}
