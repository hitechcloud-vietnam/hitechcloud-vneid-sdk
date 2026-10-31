package vneid

// EidResource handles chip CCCD verification (RSA-SHA256 signed).
type EidResource struct{ *base }

// VerifyChipOptions holds optional overrides for VerifyChip.
type VerifyChipOptions struct {
	RequestID   string
	RequestTime string
}

// VerifyChip calls POST /eid/api/verify/request, signing the request with the CSK key.
func (r *EidResource) VerifyChip(idCard, dscert, diaChi string, opts *VerifyChipOptions) (map[string]interface{}, error) {
	cfg := r.config
	if cfg.licenseID == "" || cfg.deviceType == "" || cfg.cskPrivateKey == "" {
		return nil, &SignatureError{Error{Message: "eID verification requires LicenseID, DeviceType and CSKPrivateKey"}}
	}
	requestID := newRequestID()
	requestTime := eidRequestTime()
	if opts != nil {
		if opts.RequestID != "" {
			requestID = opts.RequestID
		}
		if opts.RequestTime != "" {
			requestTime = opts.RequestTime
		}
	}
	signature, err := r.Signature.SignEID(
		requestID, requestTime, cfg.licenseID, cfg.deviceType, diaChi, idCard, dscert, cfg.cskPrivateKey,
	)
	if err != nil {
		return nil, err
	}
	headers := map[string]string{
		"content-type": "application/json; charset=utf-8",
		"request_id":   requestID,
		"request_time": requestTime,
		"license_id":   cfg.licenseID,
		"device_type":  cfg.deviceType,
		"dia_chi":      diaChi,
		"signature":    signature,
	}
	body := map[string]interface{}{"id_card": idCard, "dscert": dscert}
	return r.http.postJSON("/eid/api/verify/request", body, headers)
}
