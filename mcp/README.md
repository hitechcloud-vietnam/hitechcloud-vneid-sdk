# HiTechCloud VNeID MCP Server

A [Model Context Protocol](https://modelcontextprotocol.io) server exposing the main
HiTechCloud eKYC — VNeID gateway tools over stdio.

## Install & run

```bash
cd mcp && npm install
VNEID_API_KEY=... VNEID_TARGET_CODE=... npm start
```

## Environment

| Variable | Default | Description |
|----------|---------|-------------|
| `VNEID_BASE_URL` | `https://ekyc-c06-sandbox.hitechcloud.vn` | Gateway base URL |
| `VNEID_API_KEY` | — | VNeID service API key |
| `VNEID_TARGET_CODE` | — | Default `target_code` |

## Tools

| Tool | Description |
|------|-------------|
| `data_share_init` | Initialize a data-share transaction (share-info) |
| `get_transaction` | Fetch citizen data (userInfo JWS) for a transaction |
| `verify_info_init` | Initialize a verify-info (True/False) transaction |
| `face_matching` | Biometric face matching (v2/get-transaction) |
| `build_universal_link` | Build the VNeID App-to-App universal link |
| `withdraw_consent_result` | Report a withdraw-consent result |

## Claude Desktop config

```json
{
  "mcpServers": {
    "hitechcloud-vneid": {
      "command": "node",
      "args": ["/path/to/hitechcloud-vneid-sdk/mcp/server.js"],
      "env": {
        "VNEID_API_KEY": "...",
        "VNEID_TARGET_CODE": "...",
        "VNEID_BASE_URL": "https://ekyc-c06-sandbox.hitechcloud.vn"
      }
    }
  }
}
```
