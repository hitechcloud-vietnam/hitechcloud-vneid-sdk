"""Biometric resource — face matching / liveness (v2/get-transaction)."""

from .base import BaseResource


class BiometricResource(BaseResource):
    """Face matching and liveness (service-codes: face-matching, biometric-share-info)."""

    def get_transaction_v2(self, service_code, transaction_id, face_image=None,
                           request_type="sdk-liveness-match", target_code=None):
        """
        POST /api/tcdn/{service_code}/v2/api/get-transaction

        Returns userInfo (JWS) and biometricResult (JWS). request_type is
        'sdk-liveness-match' (anti-spoofing + match) or 'sdk-match' (match only).
        """
        body = {"transactionId": transaction_id}
        if face_image is not None:
            body["faceImage"] = face_image
        headers = self._vneid_headers(target_code=target_code, request_type=request_type)
        path = self._tcdn_path(service_code, "/v2/api/get-transaction")
        return self._http.post_json(path, body, headers)

    def face_matching(self, transaction_id, face_image,
                      request_type="sdk-liveness-match", target_code=None):
        """Convenience for service-code face-matching."""
        return self.get_transaction_v2(
            "face-matching", transaction_id, face_image, request_type, target_code
        )

    def share_with_biometric(self, transaction_id, face_image,
                             request_type="sdk-liveness-match", target_code=None):
        """Convenience for service-code biometric-share-info."""
        return self.get_transaction_v2(
            "biometric-share-info", transaction_id, face_image, request_type, target_code
        )
