package vneid

// DataShareResource handles the share-info service.
type DataShareResource struct{ *base }

const dataShareServiceCode = "share-info"

// Init calls POST /api/tcdn/share-info/api/agent/init.
// body must include "citizenPid"; optional "fullName", "birthDate".
func (r *DataShareResource) Init(body map[string]interface{}) (map[string]interface{}, error) {
	headers := r.vneidHeaders(headerOpts{})
	return r.http.postJSON(tcdnPath(dataShareServiceCode, "/api/agent/init"), body, headers)
}

// GetTransaction calls POST /api/tcdn/share-info/api/get-transaction.
func (r *DataShareResource) GetTransaction(transactionID string) (map[string]interface{}, error) {
	headers := r.vneidHeaders(headerOpts{})
	body := map[string]interface{}{"transactionId": transactionID}
	return r.http.postJSON(tcdnPath(dataShareServiceCode, "/api/get-transaction"), body, headers)
}
