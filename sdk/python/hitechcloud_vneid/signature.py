"""
Signature helper for the HiTechCloud VNeID SDK.

Two mechanisms:
  * eID (chip CCCD): RSA-SHA256 request signing with the CSK private key, and
    RSA-SHA256 verification of the eID response signature.
  * VNeID data (userInfo / biometricResult): JWS ES256 verification + payload parsing
    with the C06 public key.

The `cryptography` package is required for verification; `sign_eid` works with either
`cryptography` or the standard-library fallback via `cryptography` only. Keep private
keys in an HSM / secure store — never embed them in client code.
"""

import base64
import json

from .exceptions import SignatureError


def _b64url_decode(data):
    if isinstance(data, str):
        data = data.encode("ascii")
    padding = b"=" * (-len(data) % 4)
    return base64.urlsafe_b64decode(data + padding)


class Signature:
    """RSA-SHA256 signing and JWS (ES256) verification helpers."""

    # ------------------------------------------------------------------ eID
    @staticmethod
    def build_eid_signing_string(request_id, request_time, license_id,
                                 device_type, dia_chi, id_card, dscert):
        """Join the eID fields with '|' in the required order."""
        return "|".join([
            request_id, request_time, license_id, device_type,
            dia_chi, id_card, dscert,
        ])

    @classmethod
    def sign_eid(cls, request_id, request_time, license_id, device_type,
                 dia_chi, id_card, dscert, private_key_pem):
        """Return a Base64 RSA-SHA256 signature of the eID signing string."""
        signing_string = cls.build_eid_signing_string(
            request_id, request_time, license_id, device_type,
            dia_chi, id_card, dscert,
        )
        return cls.sign(signing_string, private_key_pem)

    @staticmethod
    def sign(signing_string, private_key_pem):
        """Sign an arbitrary string with RSA-SHA256, return Base64."""
        try:
            from cryptography.hazmat.primitives import hashes, serialization
            from cryptography.hazmat.primitives.asymmetric import padding
        except ImportError as exc:  # pragma: no cover
            raise SignatureError(
                "The 'cryptography' package is required for signing."
            ) from exc

        if isinstance(private_key_pem, str):
            private_key_pem = private_key_pem.encode("utf-8")
        key = serialization.load_pem_private_key(private_key_pem, password=None)
        signature = key.sign(
            signing_string.encode("utf-8"),
            padding.PKCS1v15(),
            hashes.SHA256(),
        )
        return base64.b64encode(signature).decode("ascii")

    @staticmethod
    def verify_eid_response(signing_string, signature_b64, public_key_pem):
        """Verify an RSA-SHA256 eID response signature. Returns bool."""
        try:
            from cryptography.hazmat.primitives import hashes, serialization
            from cryptography.hazmat.primitives.asymmetric import padding
            from cryptography.exceptions import InvalidSignature
        except ImportError as exc:  # pragma: no cover
            raise SignatureError(
                "The 'cryptography' package is required for verification."
            ) from exc

        if isinstance(public_key_pem, str):
            public_key_pem = public_key_pem.encode("utf-8")
        key = serialization.load_pem_public_key(public_key_pem)
        try:
            key.verify(
                base64.b64decode(signature_b64),
                signing_string.encode("utf-8"),
                padding.PKCS1v15(),
                hashes.SHA256(),
            )
            return True
        except InvalidSignature:
            return False

    # ------------------------------------------------------------------ JWS
    @staticmethod
    def verify_jws(jws_compact, public_key_pem):
        """
        Verify an ES256 JWS compact string and return the parsed payload dict.

        `jws_compact` is '<header>.<payload>.<signature>'. Raises SignatureError on a
        bad signature. The C06 userInfo uses standard base64 for the header segment in
        some responses, so both url-safe and standard base64 are accepted.
        """
        try:
            from cryptography.hazmat.primitives import hashes, serialization
            from cryptography.hazmat.primitives.asymmetric import ec, utils
            from cryptography.exceptions import InvalidSignature
        except ImportError as exc:  # pragma: no cover
            raise SignatureError(
                "The 'cryptography' package is required for JWS verification."
            ) from exc

        parts = jws_compact.split(".")
        if len(parts) != 3:
            raise SignatureError("Invalid JWS: expected 3 segments")
        header_seg, payload_seg, signature_seg = parts

        if isinstance(public_key_pem, str):
            public_key_pem = public_key_pem.encode("utf-8")
        key = serialization.load_pem_public_key(public_key_pem)

        signing_input = (header_seg + "." + payload_seg).encode("ascii")
        raw_sig = _b64url_decode(signature_seg)
        # ES256 JWS signature is r||s (64 bytes); convert to DER for verification.
        if len(raw_sig) % 2 != 0:
            raise SignatureError("Invalid ES256 signature length")
        half = len(raw_sig) // 2
        r = int.from_bytes(raw_sig[:half], "big")
        s = int.from_bytes(raw_sig[half:], "big")
        der_sig = utils.encode_dss_signature(r, s)

        try:
            key.verify(der_sig, signing_input, ec.ECDSA(hashes.SHA256()))
        except InvalidSignature as exc:
            raise SignatureError("JWS signature verification failed") from exc

        return json.loads(_b64url_decode(payload_seg).decode("utf-8"))

    @staticmethod
    def decode_jws_unverified(jws_compact):
        """Parse the JWS payload WITHOUT verifying (sandbox/debug only)."""
        parts = jws_compact.split(".")
        if len(parts) != 3:
            raise SignatureError("Invalid JWS: expected 3 segments")
        return json.loads(_b64url_decode(parts[1]).decode("utf-8"))
