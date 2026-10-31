package vneid

import "fmt"

// Error is the base error type for the VNeID SDK.
type Error struct {
	StatusCode   int
	ResponseCode string
	Message      string
	Data         map[string]interface{}
}

func (e *Error) Error() string {
	return fmt.Sprintf("HiTechCloud VNeID error %d: %s", e.StatusCode, e.Message)
}

// AuthenticationError represents HTTP 401.
type AuthenticationError struct{ Error }

// AuthorizationError represents HTTP 403.
type AuthorizationError struct{ Error }

// NotFoundError represents HTTP 404.
type NotFoundError struct{ Error }

// ValidationError represents HTTP 400.
type ValidationError struct{ Error }

// RateLimitError represents HTTP 429.
type RateLimitError struct {
	Error
	RetryAfter int
}

// BiometricError represents HTTP 501.
type BiometricError struct{ Error }

// ServerError represents HTTP 5xx.
type ServerError struct{ Error }

// SignatureError is returned on signing / JWS verification failures.
type SignatureError struct{ Error }

func mapError(status int, data map[string]interface{}, retryAfter int) error {
	message := "API error"
	for _, k := range []string{"description", "message", "error"} {
		if v, ok := data[k].(string); ok && v != "" {
			message = v
			break
		}
	}
	responseCode, _ := data["responseCode"].(string)

	base := Error{StatusCode: status, ResponseCode: responseCode, Message: message, Data: data}
	switch status {
	case 400:
		return &ValidationError{base}
	case 401:
		return &AuthenticationError{base}
	case 403:
		return &AuthorizationError{base}
	case 404:
		return &NotFoundError{base}
	case 429:
		return &RateLimitError{Error: base, RetryAfter: retryAfter}
	case 501:
		return &BiometricError{base}
	default:
		if status >= 500 {
			return &ServerError{base}
		}
		return &base
	}
}
