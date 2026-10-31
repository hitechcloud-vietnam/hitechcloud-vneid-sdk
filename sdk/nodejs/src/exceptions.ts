/** HiTechCloud VNeID SDK — error types */

export class HiTechCloudVNeIDError extends Error {
  statusCode?: number;
  responseCode?: string;
  data?: unknown;

  constructor(message: string, statusCode?: number, responseCode?: string, data?: unknown) {
    super(message);
    this.name = "HiTechCloudVNeIDError";
    this.statusCode = statusCode;
    this.responseCode = responseCode;
    this.data = data;
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

export class AuthenticationError extends HiTechCloudVNeIDError {
  constructor(message = "Authentication failed", responseCode?: string, data?: unknown) {
    super(message, 401, responseCode, data);
    this.name = "AuthenticationError";
  }
}

export class AuthorizationError extends HiTechCloudVNeIDError {
  constructor(message = "Access denied", responseCode?: string, data?: unknown) {
    super(message, 403, responseCode, data);
    this.name = "AuthorizationError";
  }
}

export class NotFoundError extends HiTechCloudVNeIDError {
  constructor(message = "Not found", responseCode?: string, data?: unknown) {
    super(message, 404, responseCode, data);
    this.name = "NotFoundError";
  }
}

export class ValidationError extends HiTechCloudVNeIDError {
  constructor(message = "Validation error", responseCode?: string, data?: unknown) {
    super(message, 400, responseCode, data);
    this.name = "ValidationError";
  }
}

export class RateLimitError extends HiTechCloudVNeIDError {
  retryAfter: number;
  constructor(message = "Rate limit exceeded", retryAfter = 60, responseCode?: string, data?: unknown) {
    super(message, 429, responseCode, data);
    this.name = "RateLimitError";
    this.retryAfter = retryAfter;
  }
}

export class BiometricError extends HiTechCloudVNeIDError {
  constructor(message = "Biometric error", responseCode?: string, data?: unknown) {
    super(message, 501, responseCode, data);
    this.name = "BiometricError";
  }
}

export class ServerError extends HiTechCloudVNeIDError {
  constructor(message = "Server error", statusCode = 500, responseCode?: string, data?: unknown) {
    super(message, statusCode, responseCode, data);
    this.name = "ServerError";
  }
}

export class SignatureError extends HiTechCloudVNeIDError {
  constructor(message = "Signature error") {
    super(message);
    this.name = "SignatureError";
  }
}
