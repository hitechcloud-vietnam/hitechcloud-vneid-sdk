package vneid

import (
	"crypto/rand"
	"fmt"
	"time"
)

var vnLocation = time.FixedZone("ICT", 7*3600) // GMT+7

// base is embedded by every resource and holds shared helpers.
type base struct {
	http      *httpClient
	config    *config
	Signature Signature
}

func newRequestID() string {
	b := make([]byte, 16)
	_, _ = rand.Read(b)
	b[6] = (b[6] & 0x0f) | 0x40
	b[8] = (b[8] & 0x3f) | 0x80
	return fmt.Sprintf("%x-%x-%x-%x-%x", b[0:4], b[4:6], b[6:8], b[8:10], b[10:16])
}

// vneidRequestTime returns ddMMyyyyHHmmss (VNeID services).
func vneidRequestTime() string {
	return time.Now().In(vnLocation).Format("02012006150405")
}

// eidRequestTime returns yyyyMMddHHmmss (eID services).
func eidRequestTime() string {
	return time.Now().In(vnLocation).Format("20060102150405")
}

type headerOpts struct {
	targetCode        string
	requestType       string
	requestID         string
	contentType       string
	bearer            string
	excludeTargetCode bool
}

func (b *base) vneidHeaders(o headerOpts) map[string]string {
	contentType := o.contentType
	if contentType == "" {
		contentType = "application/json; charset=utf-8"
	}
	reqID := o.requestID
	if reqID == "" {
		reqID = newRequestID()
	}
	headers := map[string]string{
		"Content-Type": contentType,
		"request_id":   reqID,
		"request_time": vneidRequestTime(),
	}
	if b.config.apiKey != "" {
		headers["api_key"] = b.config.apiKey
	}
	if !o.excludeTargetCode {
		tc := o.targetCode
		if tc == "" {
			tc = b.config.targetCode
		}
		if tc != "" {
			headers["target_code"] = tc
		}
	}
	if o.requestType != "" {
		headers["request_type"] = o.requestType
	}
	if o.bearer != "" {
		headers["Authorization"] = "Bearer " + o.bearer
	}
	return headers
}

func tcdnPath(serviceCode, suffix string) string {
	return "/api/tcdn/" + serviceCode + suffix
}
