package vneid

// BiometricResource handles face matching / liveness (v2/get-transaction).
type BiometricResource struct{ *base }

// GetTransactionV2 calls POST /api/tcdn/{serviceCode}/v2/api/get-transaction.
// requestType is "sdk-liveness-match" or "sdk-match".
func (r *BiometricResource) GetTransactionV2(serviceCode, transactionID, faceImage, requestType string) (map[string]interface{}, error) {
	if requestType == "" {
		requestType = "sdk-liveness-match"
	}
	body := map[string]interface{}{"transactionId": transactionID}
	if faceImage != "" {
		body["faceImage"] = faceImage
	}
	headers := r.vneidHeaders(headerOpts{requestType: requestType})
	return r.http.postJSON(tcdnPath(serviceCode, "/v2/api/get-transaction"), body, headers)
}

// FaceMatching is a convenience for service-code face-matching.
func (r *BiometricResource) FaceMatching(transactionID, faceImage, requestType string) (map[string]interface{}, error) {
	return r.GetTransactionV2("face-matching", transactionID, faceImage, requestType)
}

// ShareWithBiometric is a convenience for service-code biometric-share-info.
func (r *BiometricResource) ShareWithBiometric(transactionID, faceImage, requestType string) (map[string]interface{}, error) {
	return r.GetTransactionV2("biometric-share-info", transactionID, faceImage, requestType)
}
