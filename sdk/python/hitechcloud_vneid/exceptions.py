"""HiTechCloud VNeID SDK Exceptions"""


class HiTechCloudVNeIDError(Exception):
    """Base exception for the HiTechCloud VNeID SDK."""

    def __init__(self, message, status_code=None, response_code=None, data=None):
        self.message = message
        self.status_code = status_code
        self.response_code = response_code
        self.data = data
        super().__init__(self.message)


class AuthenticationError(HiTechCloudVNeIDError):
    """Raised on HTTP 401 (invalid token / signature / license_id)."""


class AuthorizationError(HiTechCloudVNeIDError):
    """Raised on HTTP 403 (IP not whitelisted, connection suspended)."""


class NotFoundError(HiTechCloudVNeIDError):
    """Raised on HTTP 404."""


class ValidationError(HiTechCloudVNeIDError):
    """Raised on HTTP 400 (missing or invalid fields)."""


class RateLimitError(HiTechCloudVNeIDError):
    """Raised on HTTP 429 (rate limit exceeded)."""

    def __init__(self, message, retry_after=None, **kwargs):
        super().__init__(message, **kwargs)
        self.retry_after = retry_after


class BiometricError(HiTechCloudVNeIDError):
    """Raised on HTTP 501 (biometric verification error)."""


class ServerError(HiTechCloudVNeIDError):
    """Raised on HTTP 5xx server errors."""


class SignatureError(HiTechCloudVNeIDError):
    """Raised when signing or JWS verification fails."""


ERROR_MAP = {
    400: ValidationError,
    401: AuthenticationError,
    403: AuthorizationError,
    404: NotFoundError,
    429: RateLimitError,
    500: ServerError,
    501: BiometricError,
    503: ServerError,
}
