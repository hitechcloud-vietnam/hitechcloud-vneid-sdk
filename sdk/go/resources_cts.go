package vneid

// CtsResource handles digital certificate (SmartCA) issuance.
type CtsResource struct{ *base }

const ctsResultServiceCode = "create-smartca-account"

// CtsInitParams holds the fields for a CTS init request.
type CtsInitParams struct {
	CitizenPid     string
	CertType       string // PER, ORG, PER-ORG
	ShareOrgNumber string
	FeeCode        string
	ListOrgCode    []string
	KeyData        string
	ServiceCode    string // defaults to "share-info"
}

// Init calls POST /api/tcdn/{serviceCode}/api/agent/init with CTS fields.
func (r *CtsResource) Init(p CtsInitParams) (map[string]interface{}, error) {
	certType := p.CertType
	if certType == "" {
		certType = "PER"
	}
	body := map[string]interface{}{"citizenPid": p.CitizenPid, "type": certType}
	if p.ShareOrgNumber != "" {
		body["shareOrgNumber"] = p.ShareOrgNumber
	}
	if len(p.ListOrgCode) > 0 {
		body["listOrgCode"] = p.ListOrgCode
	}
	if p.FeeCode != "" {
		body["feeCode"] = p.FeeCode
	}
	if p.KeyData != "" {
		body["keyData"] = p.KeyData
	}
	serviceCode := p.ServiceCode
	if serviceCode == "" {
		serviceCode = "share-info"
	}
	headers := r.vneidHeaders(headerOpts{})
	return r.http.postJSON(tcdnPath(serviceCode, "/api/agent/init"), body, headers)
}

// ReportResult calls POST /api/tcdn/create-smartca-account/agent/account/receive-result.
func (r *CtsResource) ReportResult(result map[string]interface{}) (map[string]interface{}, error) {
	headers := r.vneidHeaders(headerOpts{})
	return r.http.postJSON(tcdnPath(ctsResultServiceCode, "/agent/account/receive-result"), result, headers)
}
