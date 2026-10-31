package vneid

import (
	"strings"
	"testing"
)

func TestBaseURLDefaults(t *testing.T) {
	if got := NewClient(WithAPIKey("k")).BaseURL(); got != ProductionURL {
		t.Fatalf("expected %s, got %s", ProductionURL, got)
	}
	if got := NewClient(WithAPIKey("k"), WithSandbox()).BaseURL(); got != SandboxURL {
		t.Fatalf("expected %s, got %s", SandboxURL, got)
	}
}

func TestResourcesPresent(t *testing.T) {
	c := NewClient(WithAPIKey("k"), WithTargetCode("t"))
	if c.Eid == nil || c.DataShare == nil || c.Biometric == nil || c.VerifyInfo == nil ||
		c.Organization == nil || c.Sso == nil || c.Cts == nil || c.Consent == nil {
		t.Fatal("a resource is nil")
	}
}

func TestUniversalLink(t *testing.T) {
	c := NewClient(WithAPIKey("k"))
	if got := c.Consent.BuildUniversalLink("abc"); got != "https://universal.dancuquocgia.com/share/abc" {
		t.Fatalf("unexpected link: %s", got)
	}
}

func TestEIDSigningStringOrder(t *testing.T) {
	s := Signature{}.BuildEIDSigningString("rid", "20261101093000", "LIC", "WEB", "hanoi", "001", "dscert")
	if s != "rid|20261101093000|LIC|WEB|hanoi|001|dscert" {
		t.Fatalf("unexpected signing string: %s", s)
	}
}

func TestAuthorizeURL(t *testing.T) {
	c := NewClient(WithAPIKey("k"))
	url := c.Sso.BuildAuthorizeURL("https://sso.vneid.gov.vn", "cid", "https://app/cb", "")
	if !strings.HasPrefix(url, "https://sso.vneid.gov.vn/auth?") || !strings.Contains(url, "client_id=cid") {
		t.Fatalf("unexpected url: %s", url)
	}
}
