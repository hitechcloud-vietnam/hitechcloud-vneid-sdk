"""Basic offline tests for the VNeID SDK (no network calls)."""

from hitechcloud_vneid import VNeIDClient, Signature
from hitechcloud_vneid.client import PRODUCTION_URL, SANDBOX_URL
from hitechcloud_vneid.http_client import vneid_request_time, eid_request_time


def test_base_url_defaults():
    assert VNeIDClient(api_key="k").base_url == PRODUCTION_URL
    assert VNeIDClient(api_key="k", sandbox=True).base_url == SANDBOX_URL


def test_resources_present():
    c = VNeIDClient(api_key="k", target_code="t")
    for name in ("eid", "data_share", "biometric", "verify_info",
                 "organization", "sso", "cts", "consent"):
        assert hasattr(c, name)


def test_universal_link():
    c = VNeIDClient(api_key="k")
    assert c.consent.build_universal_link("abc") == \
        "https://universal.dancuquocgia.com/share/abc"


def test_eid_signing_string_order():
    s = Signature.build_eid_signing_string(
        "rid", "20261101093000", "LIC", "WEB", "hanoi", "001", "dscert"
    )
    assert s == "rid|20261101093000|LIC|WEB|hanoi|001|dscert"


def test_vneid_headers_format():
    c = VNeIDClient(api_key="k", target_code="t")
    h = c.data_share._vneid_headers()
    assert h["api_key"] == "k"
    assert h["target_code"] == "t"
    assert len(h["request_time"]) == 14  # ddMMyyyyHHmmss


def test_time_helpers():
    assert len(vneid_request_time()) == 14
    assert len(eid_request_time()) == 14


def test_authorize_url():
    c = VNeIDClient(api_key="k")
    url = c.sso.build_authorize_url(
        "https://sso.vneid.gov.vn", "cid", "https://app/cb"
    )
    assert url.startswith("https://sso.vneid.gov.vn/auth?")
    assert "client_id=cid" in url
