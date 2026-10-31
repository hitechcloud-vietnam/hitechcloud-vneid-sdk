package vneid

// ConsentResource handles receive-result, withdraw-consent, and the universal link.
type ConsentResource struct{ *base }

// UniversalLinkBase is the VNeID App-to-App universal link base.
const UniversalLinkBase = "https://universal.dancuquocgia.com/share"

// BuildUniversalLink returns the VNeID universal link for a transaction.
func (r *ConsentResource) BuildUniversalLink(transactionCode string) string {
	return UniversalLinkBase + "/" + transactionCode
}

// ReceiveResult calls POST /api/tcdn/{serviceCode}/agent/account/receive-result.
func (r *ConsentResource) ReceiveResult(serviceCode string, body map[string]interface{}) (map[string]interface{}, error) {
	headers := r.vneidHeaders(headerOpts{})
	return r.http.postJSON(tcdnPath(serviceCode, "/agent/account/receive-result"), body, headers)
}

// WithdrawConsentResult calls POST /api/tcdn/withdraw-consent-result/agent/account/receive-result.
// requestStatus is "DELETED" or "REJECT".
func (r *ConsentResource) WithdrawConsentResult(requestStatus, reasonRequestCode, reasonRequestText string) (map[string]interface{}, error) {
	body := map[string]interface{}{
		"requestStatus":     requestStatus,
		"reasonRequestCode": nilIfEmpty(reasonRequestCode),
		"reasonRequestText": nilIfEmpty(reasonRequestText),
	}
	headers := r.vneidHeaders(headerOpts{})
	return r.http.postJSON(tcdnPath("withdraw-consent-result", "/agent/account/receive-result"), body, headers)
}

func nilIfEmpty(s string) interface{} {
	if s == "" {
		return nil
	}
	return s
}
