"use strict";
// Offline tests. Run `npm run build` first; the suite imports the compiled dist.
const test = require("node:test");
const assert = require("node:assert");
const { VNeIDClient, PRODUCTION_URL, SANDBOX_URL, Signature } = require("../dist/index.js");

test("base url defaults", () => {
  assert.strictEqual(new VNeIDClient({ apiKey: "k" }).baseUrl, PRODUCTION_URL);
  assert.strictEqual(new VNeIDClient({ apiKey: "k", sandbox: true }).baseUrl, SANDBOX_URL);
});

test("resources present", () => {
  const c = new VNeIDClient({ apiKey: "k", targetCode: "t" });
  for (const name of ["eid", "dataShare", "biometric", "verifyInfo", "organization", "sso", "cts", "consent"]) {
    assert.ok(c[name], `missing resource ${name}`);
  }
});

test("universal link", () => {
  const c = new VNeIDClient({ apiKey: "k" });
  assert.strictEqual(
    c.consent.buildUniversalLink("abc"),
    "https://universal.dancuquocgia.com/share/abc",
  );
});

test("eid signing string order", () => {
  const s = Signature.buildEidSigningString("rid", "20261101093000", "LIC", "WEB", "hanoi", "001", "dscert");
  assert.strictEqual(s, "rid|20261101093000|LIC|WEB|hanoi|001|dscert");
});

test("authorize url", () => {
  const c = new VNeIDClient({ apiKey: "k" });
  const url = c.sso.buildAuthorizeUrl("https://sso.vneid.gov.vn", "cid", "https://app/cb");
  assert.ok(url.startsWith("https://sso.vneid.gov.vn/auth?"));
  assert.ok(url.includes("client_id=cid"));
});
