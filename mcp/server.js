#!/usr/bin/env node
/**
 * HiTechCloud eKYC — VNeID MCP Server
 * Model Context Protocol server exposing the main VNeID gateway tools.
 *
 * Env:
 *   VNEID_BASE_URL     (default: https://ekyc-c06-sandbox.hitechcloud.vn)
 *   VNEID_API_KEY      (required for VNeID services)
 *   VNEID_TARGET_CODE  (default target_code)
 */
const crypto = require("crypto");
const { Server } = require("@modelcontextprotocol/sdk/server/index.js");
const { StdioServerTransport } = require("@modelcontextprotocol/sdk/server/stdio.js");

const BASE_URL = (process.env.VNEID_BASE_URL || "https://ekyc-c06-sandbox.hitechcloud.vn").replace(/\/+$/, "");
const API_KEY = process.env.VNEID_API_KEY || "";
const TARGET_CODE = process.env.VNEID_TARGET_CODE || "";

function pad(n) {
  return n < 10 ? `0${n}` : `${n}`;
}
function vneidRequestTime() {
  const now = new Date();
  const d = new Date(now.getTime() + (now.getTimezoneOffset() + 420) * 60000);
  return (
    pad(d.getDate()) + pad(d.getMonth() + 1) + d.getFullYear() +
    pad(d.getHours()) + pad(d.getMinutes()) + pad(d.getSeconds())
  );
}

function vneidHeaders(extra = {}) {
  const headers = {
    "Content-Type": "application/json; charset=utf-8",
    request_id: crypto.randomUUID(),
    request_time: vneidRequestTime(),
  };
  if (API_KEY) headers.api_key = API_KEY;
  if (TARGET_CODE) headers.target_code = TARGET_CODE;
  return { ...headers, ...extra };
}

async function postJson(path, body, extraHeaders = {}) {
  const res = await fetch(`${BASE_URL}${path}`, {
    method: "POST",
    headers: vneidHeaders(extraHeaders),
    body: JSON.stringify(body),
  });
  return res.json();
}

function tcdnPath(serviceCode, suffix) {
  return `/api/tcdn/${serviceCode}${suffix}`;
}

const TOOLS = [
  {
    name: "data_share_init",
    description: "Initialize a VNeID data-share transaction (share-info). Returns txnId.",
    inputSchema: {
      type: "object",
      properties: {
        citizenPid: { type: "string", description: "12-digit citizen identity number" },
        fullName: { type: "string" },
        birthDate: { type: "string", description: "dd-MM-yyyy" },
      },
      required: ["citizenPid"],
    },
  },
  {
    name: "get_transaction",
    description: "Fetch citizen data for a transaction (userInfo is a JWS string).",
    inputSchema: {
      type: "object",
      properties: {
        serviceCode: { type: "string", description: "e.g. share-info, verify-info", default: "share-info" },
        transactionId: { type: "string" },
      },
      required: ["transactionId"],
    },
  },
  {
    name: "verify_info_init",
    description: "Initialize a verify-info transaction (fields are checked True/False).",
    inputSchema: {
      type: "object",
      properties: {
        citizenPid: { type: "string" },
        fullName: { type: "string" },
        birthDate: { type: "string" },
      },
      required: ["citizenPid"],
    },
  },
  {
    name: "face_matching",
    description: "Biometric face matching via v2/get-transaction (face-matching service).",
    inputSchema: {
      type: "object",
      properties: {
        transactionId: { type: "string" },
        faceImage: { type: "string", description: "Base64 face image from the SDK" },
        requestType: { type: "string", enum: ["sdk-match", "sdk-liveness-match"], default: "sdk-liveness-match" },
      },
      required: ["transactionId", "faceImage"],
    },
  },
  {
    name: "build_universal_link",
    description: "Build the VNeID App-to-App universal link for a transaction code.",
    inputSchema: {
      type: "object",
      properties: { transactionCode: { type: "string" } },
      required: ["transactionCode"],
    },
  },
  {
    name: "withdraw_consent_result",
    description: "Report a withdraw-consent result (DELETED or REJECT).",
    inputSchema: {
      type: "object",
      properties: {
        requestStatus: { type: "string", enum: ["DELETED", "REJECT"] },
        reasonRequestText: { type: "string" },
      },
      required: ["requestStatus"],
    },
  },
];

const HANDLERS = {
  data_share_init: (a) =>
    postJson(tcdnPath("share-info", "/api/agent/init"), {
      citizenPid: a.citizenPid,
      ...(a.fullName ? { fullName: a.fullName } : {}),
      ...(a.birthDate ? { birthDate: a.birthDate } : {}),
    }),
  get_transaction: (a) =>
    postJson(tcdnPath(a.serviceCode || "share-info", "/api/get-transaction"), {
      transactionId: a.transactionId,
    }),
  verify_info_init: (a) =>
    postJson(tcdnPath("verify-info", "/api/agent/init"), {
      citizenPid: a.citizenPid,
      ...(a.fullName ? { fullName: a.fullName } : {}),
      ...(a.birthDate ? { birthDate: a.birthDate } : {}),
    }),
  face_matching: (a) =>
    postJson(
      tcdnPath("face-matching", "/v2/api/get-transaction"),
      { transactionId: a.transactionId, faceImage: a.faceImage },
      { request_type: a.requestType || "sdk-liveness-match" },
    ),
  build_universal_link: async (a) => ({
    url: `https://universal.dancuquocgia.com/share/${a.transactionCode}`,
  }),
  withdraw_consent_result: (a) =>
    postJson(tcdnPath("withdraw-consent-result", "/agent/account/receive-result"), {
      requestStatus: a.requestStatus,
      reasonRequestCode: null,
      reasonRequestText: a.reasonRequestText || null,
    }),
};

async function main() {
  const server = new Server(
    { name: "hitechcloud-vneid", version: "1.0.0" },
    { capabilities: { tools: {} } },
  );

  server.setRequestHandler("tools/list", async () => ({ tools: TOOLS }));

  server.setRequestHandler("tools/call", async (request) => {
    const { name, arguments: args } = request.params;
    const handler = HANDLERS[name];
    if (!handler) throw new Error(`Unknown tool: ${name}`);
    const result = await handler(args || {});
    return { content: [{ type: "text", text: JSON.stringify(result, null, 2) }] };
  });

  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error("HiTechCloud VNeID MCP server running on stdio");
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
