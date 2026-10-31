"""Verify-info resource — True/False verification against the population DB."""

from .base import BaseResource


class VerifyInfoResource(BaseResource):
    """Verify citizen info (service-code: verify-info). Submitted fields return True/False."""

    SERVICE_CODE = "verify-info"

    def init(self, citizen_pid, full_name=None, birth_date=None, fields=None,
             target_code=None):
        """
        POST /api/tcdn/verify-info/api/agent/init

        Fields sent here are the ones that will be verified (True/False) on get_transaction.
        """
        body = {"citizenPid": citizen_pid}
        if full_name is not None:
            body["fullName"] = full_name
        if birth_date is not None:
            body["birthDate"] = birth_date
        if fields:
            body.update(fields)
        headers = self._vneid_headers(target_code=target_code)
        path = self._tcdn_path(self.SERVICE_CODE, "/api/agent/init")
        return self._http.post_json(path, body, headers)

    def get_transaction(self, transaction_id, target_code=None):
        """POST /api/tcdn/verify-info/api/get-transaction"""
        headers = self._vneid_headers(target_code=target_code)
        path = self._tcdn_path(self.SERVICE_CODE, "/api/get-transaction")
        return self._http.post_json(path, {"transactionId": transaction_id}, headers)
