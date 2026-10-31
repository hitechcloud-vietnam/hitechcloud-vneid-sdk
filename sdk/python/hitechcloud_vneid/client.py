"""HiTechCloud VNeID SDK - Main Client"""

from .http_client import HTTPClient
from .signature import Signature
from .resources import (
    EidResource,
    DataShareResource,
    BiometricResource,
    VerifyInfoResource,
    OrganizationResource,
    SsoResource,
    CtsResource,
    ConsentResource,
)

PRODUCTION_URL = "https://ekyc-c06.hitechcloud.vn"
SANDBOX_URL = "https://ekyc-c06-sandbox.hitechcloud.vn"


class _Config:
    """Immutable client configuration shared with resources."""

    def __init__(self, api_key, target_code, license_id, device_type, csk_private_key):
        self.api_key = api_key
        self.target_code = target_code
        self.license_id = license_id
        self.device_type = device_type
        self.csk_private_key = csk_private_key


class VNeIDClient:
    """
    HiTechCloud eKYC — VNeID client.

    Args:
        api_key:        VNeID service API key (required for VNeID services).
        target_code:    Default purpose code shown to the citizen on VNeID.
        base_url:       Override the base URL.
        sandbox:        Use the sandbox base URL when base_url is not given.
        timeout:        Request timeout in seconds (default 30).
        retries:        Max retry attempts on 429/5xx (default 3).
        license_id:     eID license id (required for chip verification).
        device_type:    eID device type, e.g. "WEB" (required for chip verification).
        csk_private_key: PEM string of the CSK private key (required for chip verification).
        client_cert:    mTLS client cert; tuple (cert_path, key_path) or combined PEM path.

    Resources: eid, data_share, biometric, verify_info, organization, sso, cts, consent.
    """

    def __init__(self, api_key=None, target_code=None, base_url=None, sandbox=False,
                 timeout=30, retries=3, license_id=None, device_type=None,
                 csk_private_key=None, client_cert=None):
        if base_url is None:
            base_url = SANDBOX_URL if sandbox else PRODUCTION_URL

        self._config = _Config(api_key, target_code, license_id, device_type, csk_private_key)
        self._http = HTTPClient(base_url, timeout=timeout, retries=retries,
                                client_cert=client_cert)
        self.signature = Signature()

        args = (self._http, self._config, self.signature)
        self.eid = EidResource(*args)
        self.data_share = DataShareResource(*args)
        self.biometric = BiometricResource(*args)
        self.verify_info = VerifyInfoResource(*args)
        self.organization = OrganizationResource(*args)
        self.sso = SsoResource(*args)
        self.cts = CtsResource(*args)
        self.consent = ConsentResource(*args)

    @property
    def base_url(self):
        return self._http.base_url

    def close(self):
        self._http.close()

    def __enter__(self):
        return self

    def __exit__(self, *args):
        self.close()

    def __repr__(self):
        return f"VNeIDClient(base_url={self._http.base_url!r})"
