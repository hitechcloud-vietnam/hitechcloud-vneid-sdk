"""eID resource — chip CCCD verification (RSA-SHA256 signed)."""

from .base import BaseResource
from ..http_client import new_request_id, eid_request_time
from ..exceptions import SignatureError


class EidResource(BaseResource):
    """Verify the DS Certificate on a chip-based CCCD via the RAR center."""

    def verify_chip(self, id_card, dscert, dia_chi, request_id=None, request_time=None):
        """
        POST /eid/api/verify/request

        Signs request_id|request_time|license_id|device_type|dia_chi|id_card|dscert
        with the CSK private key (RSA-SHA256) and sends the signed request.
        """
        cfg = self._config
        if not (cfg.license_id and cfg.device_type and cfg.csk_private_key):
            raise SignatureError(
                "eID verification requires license_id, device_type and csk_private_key"
            )
        request_id = request_id or new_request_id()
        request_time = request_time or eid_request_time()

        signature = self.signature.sign_eid(
            request_id, request_time, cfg.license_id, cfg.device_type,
            dia_chi, id_card, dscert, cfg.csk_private_key,
        )
        headers = {
            "content-type": "application/json; charset=utf-8",
            "request_id": request_id,
            "request_time": request_time,
            "license_id": cfg.license_id,
            "device_type": cfg.device_type,
            "dia_chi": dia_chi,
            "signature": signature,
        }
        body = {"id_card": id_card, "dscert": dscert}
        return self._http.post_json("/eid/api/verify/request", body, headers)

    def verify_response_signature(self, response, public_key_pem):
        """
        Verify the RSA-SHA256 signature of an eID response against the RAR public key.
        Pass the full response dict returned by verify_chip().
        """
        import json as _json
        responds = response.get("responds", {})
        signing_string = _json.dumps(responds, separators=(",", ":"), ensure_ascii=False)
        return self.signature.verify_eid_response(
            signing_string, response.get("signature", ""), public_key_pem
        )
