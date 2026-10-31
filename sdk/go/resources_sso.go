package vneid

import (
	"net/url"
	"strings"
)

// SsoResource handles VNeID SSO (OAuth2/OIDC).
type SsoResource struct{ *base }

// BuildAuthorizeURL builds the VNeID authorize redirect URL (WEB flow).
func (r *SsoResource) BuildAuthorizeURL(authorizeBase, clientID, redirectURI, scope string) string {
	if scope == "" {
		scope = "openid"
	}
	q := url.Values{}
	q.Set("response_type", "code")
	q.Set("client_id", clientID)
	q.Set("scope", scope)
	q.Set("redirect_uri", redirectURI)
	return strings.TrimRight(authorizeBase, "/") + "/auth?" + q.Encode()
}

// ExchangeToken calls POST /api/tcdn/sso-token/api/vneid/form/sso (x-www-form-urlencoded).
func (r *SsoResource) ExchangeToken(code, clientID, clientSecret, redirectURI string) (map[string]interface{}, error) {
	form := url.Values{}
	form.Set("grant_type", "authorization_code")
	form.Set("client_id", clientID)
	form.Set("client_secret", clientSecret)
	form.Set("code", code)
	form.Set("redirect_uri", redirectURI)
	headers := r.vneidHeaders(headerOpts{
		contentType:       "application/x-www-form-urlencoded",
		excludeTargetCode: true,
	})
	return r.http.postForm(tcdnPath("sso-token", "/api/vneid/form/sso"), form, headers)
}

// GetUserInfo calls GET /api/tcdn/sso-userinfo/api/vneid/sso.
func (r *SsoResource) GetUserInfo(accessToken string) (map[string]interface{}, error) {
	headers := r.vneidHeaders(headerOpts{bearer: accessToken, excludeTargetCode: true})
	return r.http.get(tcdnPath("sso-userinfo", "/api/vneid/sso"), headers)
}
