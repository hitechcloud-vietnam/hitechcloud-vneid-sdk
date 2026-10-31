package vneid

import (
	"crypto"
	"crypto/ecdsa"
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha256"
	"crypto/x509"
	"encoding/base64"
	"encoding/json"
	"encoding/pem"
	"errors"
	"math/big"
	"strings"
)

// Signature provides RSA-SHA256 signing and JWS (ES256) verification helpers.
type Signature struct{}

// BuildEIDSigningString joins the eID fields with '|' in the required order.
func (Signature) BuildEIDSigningString(requestID, requestTime, licenseID, deviceType, diaChi, idCard, dscert string) string {
	return strings.Join([]string{requestID, requestTime, licenseID, deviceType, diaChi, idCard, dscert}, "|")
}

// SignEID returns a Base64 RSA-SHA256 signature of the eID signing string.
func (s Signature) SignEID(requestID, requestTime, licenseID, deviceType, diaChi, idCard, dscert, privateKeyPEM string) (string, error) {
	return s.Sign(s.BuildEIDSigningString(requestID, requestTime, licenseID, deviceType, diaChi, idCard, dscert), privateKeyPEM)
}

// Sign signs an arbitrary string with RSA-SHA256 and returns Base64.
func (Signature) Sign(signingString, privateKeyPEM string) (string, error) {
	key, err := parseRSAPrivateKey(privateKeyPEM)
	if err != nil {
		return "", &SignatureError{Error{Message: err.Error()}}
	}
	hashed := sha256.Sum256([]byte(signingString))
	sig, err := rsa.SignPKCS1v15(rand.Reader, key, crypto.SHA256, hashed[:])
	if err != nil {
		return "", &SignatureError{Error{Message: err.Error()}}
	}
	return base64.StdEncoding.EncodeToString(sig), nil
}

// VerifyEIDResponse verifies an RSA-SHA256 eID response signature.
func (Signature) VerifyEIDResponse(signingString, signatureB64, publicKeyPEM string) (bool, error) {
	pub, err := parseRSAPublicKey(publicKeyPEM)
	if err != nil {
		return false, &SignatureError{Error{Message: err.Error()}}
	}
	sig, err := base64.StdEncoding.DecodeString(signatureB64)
	if err != nil {
		return false, &SignatureError{Error{Message: err.Error()}}
	}
	hashed := sha256.Sum256([]byte(signingString))
	if err := rsa.VerifyPKCS1v15(pub, crypto.SHA256, hashed[:], sig); err != nil {
		return false, nil
	}
	return true, nil
}

// VerifyJWS verifies an ES256 JWS compact string and returns the parsed payload.
func (Signature) VerifyJWS(jwsCompact, publicKeyPEM string) (map[string]interface{}, error) {
	parts := strings.Split(jwsCompact, ".")
	if len(parts) != 3 {
		return nil, &SignatureError{Error{Message: "invalid JWS: expected 3 segments"}}
	}
	pub, err := parseECDSAPublicKey(publicKeyPEM)
	if err != nil {
		return nil, &SignatureError{Error{Message: err.Error()}}
	}
	signingInput := parts[0] + "." + parts[1]
	rawSig, err := base64.RawURLEncoding.DecodeString(parts[2])
	if err != nil {
		return nil, &SignatureError{Error{Message: err.Error()}}
	}
	if len(rawSig)%2 != 0 {
		return nil, &SignatureError{Error{Message: "invalid ES256 signature length"}}
	}
	half := len(rawSig) / 2
	r := new(big.Int).SetBytes(rawSig[:half])
	ss := new(big.Int).SetBytes(rawSig[half:])
	hashed := sha256.Sum256([]byte(signingInput))
	if !ecdsa.Verify(pub, hashed[:], r, ss) {
		return nil, &SignatureError{Error{Message: "JWS signature verification failed"}}
	}
	payload, err := base64.RawURLEncoding.DecodeString(parts[1])
	if err != nil {
		return nil, &SignatureError{Error{Message: err.Error()}}
	}
	var out map[string]interface{}
	if err := json.Unmarshal(payload, &out); err != nil {
		return nil, &SignatureError{Error{Message: err.Error()}}
	}
	return out, nil
}

// DecodeJWSUnverified parses the JWS payload WITHOUT verifying (sandbox/debug only).
func (Signature) DecodeJWSUnverified(jwsCompact string) (map[string]interface{}, error) {
	parts := strings.Split(jwsCompact, ".")
	if len(parts) != 3 {
		return nil, &SignatureError{Error{Message: "invalid JWS: expected 3 segments"}}
	}
	payload, err := base64.RawURLEncoding.DecodeString(parts[1])
	if err != nil {
		return nil, err
	}
	var out map[string]interface{}
	if err := json.Unmarshal(payload, &out); err != nil {
		return nil, err
	}
	return out, nil
}

func parseRSAPrivateKey(pemStr string) (*rsa.PrivateKey, error) {
	block, _ := pem.Decode([]byte(pemStr))
	if block == nil {
		return nil, errors.New("invalid PEM private key")
	}
	if key, err := x509.ParsePKCS1PrivateKey(block.Bytes); err == nil {
		return key, nil
	}
	parsed, err := x509.ParsePKCS8PrivateKey(block.Bytes)
	if err != nil {
		return nil, err
	}
	key, ok := parsed.(*rsa.PrivateKey)
	if !ok {
		return nil, errors.New("not an RSA private key")
	}
	return key, nil
}

func parseRSAPublicKey(pemStr string) (*rsa.PublicKey, error) {
	pub, err := parsePublicKey(pemStr)
	if err != nil {
		return nil, err
	}
	key, ok := pub.(*rsa.PublicKey)
	if !ok {
		return nil, errors.New("not an RSA public key")
	}
	return key, nil
}

func parseECDSAPublicKey(pemStr string) (*ecdsa.PublicKey, error) {
	pub, err := parsePublicKey(pemStr)
	if err != nil {
		return nil, err
	}
	key, ok := pub.(*ecdsa.PublicKey)
	if !ok {
		return nil, errors.New("not an ECDSA public key")
	}
	return key, nil
}

func parsePublicKey(pemStr string) (interface{}, error) {
	block, _ := pem.Decode([]byte(pemStr))
	if block == nil {
		return nil, errors.New("invalid PEM public key")
	}
	if pub, err := x509.ParsePKIXPublicKey(block.Bytes); err == nil {
		return pub, nil
	}
	cert, err := x509.ParseCertificate(block.Bytes)
	if err != nil {
		return nil, err
	}
	return cert.PublicKey, nil
}
