package vneid

import (
	"bytes"
	"crypto/tls"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"strconv"
	"strings"
	"time"
)

// httpClient handles HTTP requests with retry logic.
type httpClient struct {
	baseURL    string
	client     *http.Client
	retries    int
}

func newHTTPClient(baseURL string, timeout time.Duration, retries int, clientCert *tls.Certificate) *httpClient {
	transport := &http.Transport{}
	if clientCert != nil {
		transport.TLSClientConfig = &tls.Config{Certificates: []tls.Certificate{*clientCert}}
	}
	return &httpClient{
		baseURL: strings.TrimRight(baseURL, "/"),
		client:  &http.Client{Timeout: timeout, Transport: transport},
		retries: retries,
	}
}

type requestOptions struct {
	jsonBody interface{}
	formBody url.Values
	headers  map[string]string
}

func (c *httpClient) request(method, path string, opts requestOptions) (map[string]interface{}, error) {
	fullURL := c.baseURL + "/" + strings.TrimLeft(path, "/")
	var lastErr error

	for attempt := 0; attempt <= c.retries; attempt++ {
		var bodyReader io.Reader
		if opts.formBody != nil {
			bodyReader = strings.NewReader(opts.formBody.Encode())
		} else if opts.jsonBody != nil {
			raw, err := json.Marshal(opts.jsonBody)
			if err != nil {
				return nil, fmt.Errorf("marshal body: %w", err)
			}
			bodyReader = bytes.NewReader(raw)
		}

		req, err := http.NewRequest(method, fullURL, bodyReader)
		if err != nil {
			return nil, fmt.Errorf("create request: %w", err)
		}
		req.Header.Set("User-Agent", "HiTechCloud-VNeID-Go-SDK/"+Version)
		req.Header.Set("Accept", "application/json")
		for k, v := range opts.headers {
			req.Header.Set(k, v)
		}

		resp, err := c.client.Do(req)
		if err != nil {
			lastErr = err
			if attempt < c.retries {
				time.Sleep(time.Duration(1<<uint(attempt)) * time.Second)
				continue
			}
			return nil, fmt.Errorf("request failed: %w", err)
		}

		respBody, _ := io.ReadAll(resp.Body)
		resp.Body.Close()

		if resp.StatusCode == 429 {
			retryAfter := 60
			if ra := resp.Header.Get("Retry-After"); ra != "" {
				if n, convErr := strconv.Atoi(ra); convErr == nil {
					retryAfter = n
				}
			}
			if attempt < c.retries {
				time.Sleep(time.Duration(retryAfter) * time.Second)
				continue
			}
			return nil, &RateLimitError{Error: Error{StatusCode: 429, Message: "Rate limit exceeded"}, RetryAfter: retryAfter}
		}

		if resp.StatusCode >= 500 && attempt < c.retries {
			time.Sleep(time.Duration(1<<uint(attempt)) * time.Second)
			continue
		}

		var decoded map[string]interface{}
		if len(respBody) > 0 {
			if err := json.Unmarshal(respBody, &decoded); err != nil {
				decoded = map[string]interface{}{"message": string(respBody)}
			}
		} else {
			decoded = map[string]interface{}{}
		}

		if resp.StatusCode >= 400 {
			return nil, mapError(resp.StatusCode, decoded, 0)
		}
		return decoded, nil
	}

	return nil, lastErr
}

func (c *httpClient) postJSON(path string, body interface{}, headers map[string]string) (map[string]interface{}, error) {
	return c.request("POST", path, requestOptions{jsonBody: body, headers: headers})
}

func (c *httpClient) postForm(path string, form url.Values, headers map[string]string) (map[string]interface{}, error) {
	return c.request("POST", path, requestOptions{formBody: form, headers: headers})
}

func (c *httpClient) get(path string, headers map[string]string) (map[string]interface{}, error) {
	return c.request("GET", path, requestOptions{headers: headers})
}
