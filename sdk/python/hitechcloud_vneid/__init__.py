"""
HiTechCloud eKYC — VNeID SDK for Python

Official SDK for the HiTechCloud eKYC — VNeID gateway (AgentGW):
chip CCCD verification (eID), citizen data share/verify from VNeID,
biometric face matching, VNeID SSO, organization identity and digital
certificate (CTS/SmartCA) issuance.

Usage:
    from hitechcloud_vneid import VNeIDClient

    client = VNeIDClient(
        api_key="YOUR_API_KEY",
        target_code="YOUR_TARGET_CODE",
        sandbox=True,
    )

    init = client.data_share.init(citizen_pid="036188001958")
    tx = client.data_share.get_transaction(init["data"]["txnId"])
    user_info = client.signature.verify_jws(tx["userInfo"], c06_public_key_pem)
"""

__version__ = "1.0.0"
__author__ = "HiTechCloud"
__email__ = "support@hitechcloud.vn"

from .client import VNeIDClient
from .signature import Signature
from .exceptions import (
    HiTechCloudVNeIDError,
    AuthenticationError,
    AuthorizationError,
    NotFoundError,
    ValidationError,
    RateLimitError,
    BiometricError,
    ServerError,
)

__all__ = [
    "VNeIDClient",
    "Signature",
    "HiTechCloudVNeIDError",
    "AuthenticationError",
    "AuthorizationError",
    "NotFoundError",
    "ValidationError",
    "RateLimitError",
    "BiometricError",
    "ServerError",
]
