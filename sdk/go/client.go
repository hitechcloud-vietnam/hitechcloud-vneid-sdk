// Package vneid is the official Go SDK for the HiTechCloud eKYC — VNeID gateway.
package vneid

import (
	"crypto/tls"
	"time"
)

const (
	ProductionURL = "https://ekyc-c06.hitechcloud.vn"
	SandboxURL    = "https://ekyc-c06-sandbox.hitechcloud.vn"
	Version       = "1.0.0"
)

type config struct {
	apiKey        string
	targetCode    string
	licenseID     string
	deviceType    string
	cskPrivateKey string
}

// Client is the main VNeID client.
type Client struct {
	http      *httpClient
	config    *config
	baseURL   string
	Signature Signature

	Eid          *EidResource
	DataShare    *DataShareResource
	Biometric    *BiometricResource
	VerifyInfo   *VerifyInfoResource
	Organization *OrganizationResource
	Sso          *SsoResource
	Cts          *CtsResource
	Consent      *ConsentResource
}

// Option configures the client.
type Option func(*clientSettings)

type clientSettings struct {
	config
	baseURL    string
	timeout    time.Duration
	retries    int
	clientCert *tls.Certificate
}

// WithAPIKey sets the VNeID api_key.
func WithAPIKey(key string) Option { return func(s *clientSettings) { s.apiKey = key } }

// WithTargetCode sets the default target_code.
func WithTargetCode(code string) Option { return func(s *clientSettings) { s.targetCode = code } }

// WithBaseURL overrides the base URL.
func WithBaseURL(u string) Option { return func(s *clientSettings) { s.baseURL = u } }

// WithSandbox uses the sandbox base URL.
func WithSandbox() Option { return func(s *clientSettings) { s.baseURL = SandboxURL } }

// WithTimeout sets the request timeout.
func WithTimeout(t time.Duration) Option { return func(s *clientSettings) { s.timeout = t } }

// WithRetries sets the max retry attempts.
func WithRetries(n int) Option { return func(s *clientSettings) { s.retries = n } }

// WithLicenseID sets the eID license_id.
func WithLicenseID(id string) Option { return func(s *clientSettings) { s.licenseID = id } }

// WithDeviceType sets the eID device_type.
func WithDeviceType(dt string) Option { return func(s *clientSettings) { s.deviceType = dt } }

// WithCSKPrivateKey sets the eID CSK private key (PEM).
func WithCSKPrivateKey(pem string) Option { return func(s *clientSettings) { s.cskPrivateKey = pem } }

// WithClientCert sets the mTLS client certificate.
func WithClientCert(cert tls.Certificate) Option {
	return func(s *clientSettings) { s.clientCert = &cert }
}

// NewClient creates a new VNeID client.
func NewClient(opts ...Option) *Client {
	s := &clientSettings{
		baseURL: ProductionURL,
		timeout: 30 * time.Second,
		retries: 3,
	}
	for _, opt := range opts {
		opt(s)
	}

	cfg := s.config
	hc := newHTTPClient(s.baseURL, s.timeout, s.retries, s.clientCert)
	b := &base{http: hc, config: &cfg, Signature: Signature{}}

	c := &Client{
		http:      hc,
		config:    &cfg,
		baseURL:   s.baseURL,
		Signature: Signature{},
	}
	c.Eid = &EidResource{b}
	c.DataShare = &DataShareResource{b}
	c.Biometric = &BiometricResource{b}
	c.VerifyInfo = &VerifyInfoResource{b}
	c.Organization = &OrganizationResource{b}
	c.Sso = &SsoResource{b}
	c.Cts = &CtsResource{b}
	c.Consent = &ConsentResource{b}
	return c
}

// BaseURL returns the configured base URL.
func (c *Client) BaseURL() string { return c.baseURL }
