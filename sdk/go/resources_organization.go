package vneid

// OrganizationResource handles organization identity (type=ORG).
type OrganizationResource struct{ *base }

const organizationServiceCode = "share-info"

// Init calls POST /api/tcdn/share-info/api/agent/init with type=ORG.
func (r *OrganizationResource) Init(shareOrgNumber, citizenPid string, extra map[string]interface{}) (map[string]interface{}, error) {
	body := map[string]interface{}{
		"citizenPid":     citizenPid,
		"type":           "ORG",
		"shareOrgNumber": shareOrgNumber,
	}
	for k, v := range extra {
		body[k] = v
	}
	headers := r.vneidHeaders(headerOpts{})
	return r.http.postJSON(tcdnPath(organizationServiceCode, "/api/agent/init"), body, headers)
}

// GetTransaction calls POST /api/tcdn/share-info/api/get-transaction (org data).
func (r *OrganizationResource) GetTransaction(transactionID string) (map[string]interface{}, error) {
	headers := r.vneidHeaders(headerOpts{})
	body := map[string]interface{}{"transactionId": transactionID}
	return r.http.postJSON(tcdnPath(organizationServiceCode, "/api/get-transaction"), body, headers)
}
