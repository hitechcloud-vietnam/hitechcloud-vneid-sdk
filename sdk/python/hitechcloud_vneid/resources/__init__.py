"""Resource classes for the HiTechCloud VNeID SDK."""

from .base import BaseResource
from .eid import EidResource
from .data_share import DataShareResource
from .biometric import BiometricResource
from .verify_info import VerifyInfoResource
from .organization import OrganizationResource
from .sso import SsoResource
from .cts import CtsResource
from .consent import ConsentResource

__all__ = [
    "BaseResource",
    "EidResource",
    "DataShareResource",
    "BiometricResource",
    "VerifyInfoResource",
    "OrganizationResource",
    "SsoResource",
    "CtsResource",
    "ConsentResource",
]
