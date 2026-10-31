"""Consent resource — receive-result, withdraw-consent, universal link."""

from .base import BaseResource

UNIVERSAL_LINK_BASE = "https://universal.dancuquocgia.com/share"


class ConsentResource(BaseResource):
    """Report processing results back to VNeID and handle consent withdrawal."""

    def build_universal_link(self, transaction_code):
        """Return the VNeID universal link to switch App-to-App for a transaction."""
        return f"{UNIVERSAL_LINK_BASE}/{transaction_code}"

    def receive_result(self, service_code, body, target_code=None):
        """
        POST /api/tcdn/{service_code}/agent/account/receive-result

        Required for VNeID-initiated flows after get-transaction. Example service-code:
        create-account-result.
        """
        headers = self._vneid_headers(target_code=target_code)
        path = self._tcdn_path(service_code, "/agent/account/receive-result")
        return self._http.post_json(path, body, headers)

    def withdraw_consent_result(self, request_status, reason_request_code=None,
                                reason_request_text=None, target_code=None):
        """
        POST /api/tcdn/withdraw-consent-result/agent/account/receive-result

        request_status: 'DELETED' (deleted) or 'REJECT' (refused; requires reason).
        """
        body = {
            "requestStatus": request_status,
            "reasonRequestCode": reason_request_code,
            "reasonRequestText": reason_request_text,
        }
        headers = self._vneid_headers(target_code=target_code)
        path = self._tcdn_path("withdraw-consent-result", "/agent/account/receive-result")
        return self._http.post_json(path, body, headers)
