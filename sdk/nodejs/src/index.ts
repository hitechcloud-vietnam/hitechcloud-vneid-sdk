/** HiTechCloud eKYC — VNeID SDK (TypeScript) */
export { VNeIDClient, PRODUCTION_URL, SANDBOX_URL } from "./client";
export type { VNeIDClientOptions } from "./client";
export { Signature } from "./signature";
export type { ClientCert } from "./httpClient";
export type { VNeIDConfig } from "./resources/base";
export type { EidVerifyResponse } from "./resources/eid";
export type { InitParams, InitResponse, TransactionResponse } from "./resources/dataShare";
export type { RequestType } from "./resources/biometric";
export type { TokenResponse } from "./resources/sso";
export type { CertType, CtsInitParams } from "./resources/cts";
export {
  HiTechCloudVNeIDError,
  AuthenticationError,
  AuthorizationError,
  NotFoundError,
  ValidationError,
  RateLimitError,
  BiometricError,
  ServerError,
  SignatureError,
} from "./exceptions";
