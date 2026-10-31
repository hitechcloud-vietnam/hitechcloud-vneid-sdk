# Changelog

All notable changes to the HiTechCloud VNeID/eKYC SDK are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-11-01

### Added

- Initial public release of the multi-language SDK for the HiTechCloud eKYC — VNeID
  gateway (`https://ekyc-c06.hitechcloud.vn`).
- Clients in 5 languages: Node.js (TypeScript), Python, PHP, Go, Java.
- **eID chip CCCD verification** (`POST /eid/api/verify/request`) with RSA-SHA256
  request signing via the CSK private key.
- **Data share** (`share-info`): `agent/init` + `get-transaction`.
- **Biometric / face matching** (`face-matching`, `biometric-share-info`):
  `v2/get-transaction` with `faceImage` and `request_type` (`sdk-match` /
  `sdk-liveness-match`).
- **Verify info** (`verify-info`): True/False field verification against the
  national population database.
- **Organization identity** (`type=ORG`, `shareOrgNumber`).
- **VNeID SSO** (OAuth2/OIDC): authorize URL builder, `form/sso` token exchange,
  `vneid/sso` userinfo.
- **Digital certificate (CTS / SmartCA)** issuance via `create-smartca-account`.
- **Consent lifecycle**: `receive-result` and withdraw-consent result reporting,
  universal-link builder.
- `Signature` helper in every language: RSA-SHA256 signing & verification for eID,
  and ES256 JWS verification + payload parsing for `userInfo` / `biometricResult`.
- Typed error hierarchy, automatic retry on 429/5xx, response-envelope parsing.
- Sandbox-safe framing (`https://ekyc-c06-sandbox.hitechcloud.vn`).
- OpenAPI 3.0 specification (`openapi.json`).
- MCP server (Node) exposing the main eKYC tools.
- Vietnamese documentation under `docs/`.

[1.0.0]: https://github.com/hitechcloud-vietnam/hitechcloud-vneid-sdk/releases/tag/v1.0.0
