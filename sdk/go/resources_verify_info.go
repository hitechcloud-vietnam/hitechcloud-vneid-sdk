package vneid

// VerifyInfoResource handles True/False verification against the population DB.
type VerifyInfoResource struct{ *base }

const verifyInfoServiceCode = "verify-info"

// Init calls POST /api/tcdn/verify-info/api/agent/init.
// Fields in body are verified (True/False) on GetTransaction.
func (r *VerifyInfoResource) Init(body map[string]interface{}) (map[string]interface{}, error) {
	headers := r.vneidHeaders(headerOpts{})
	return r.http.postJSON(tcdnPath(verifyInfoServiceCode, "/api/agent/init"), body, headers)
}

// GetTransaction calls POST /api/tcdn/verify-info/api/get-transaction.
func (r *VerifyInfoResource) GetTransaction(transactionID string) (map[string]interface{}, error) {
	headers := r.vneidHeaders(headerOpts{})
	body := map[string]interface{}{"transactionId": transactionID}
	return r.http.postJSON(tcdnPath(verifyInfoServiceCode, "/api/get-transaction"), body, headers)
}
