"""Data share resource — share-info service."""

from .base import BaseResource


class DataShareResource(BaseResource):
    """Share citizen identity data from VNeID (service-code: share-info)."""

    SERVICE_CODE = "share-info"

    def init(self, citizen_pid, full_name=None, birth_date=None, extra=None,
             target_code=None):
        """
        POST /api/tcdn/share-info/api/agent/init

        Create a share transaction and return { data: { txnId, requestId } }.
        """
        body = {"citizenPid": citizen_pid}
        if full_name is not None:
            body["fullName"] = full_name
        if birth_date is not None:
            body["birthDate"] = birth_date
        if extra:
            body.update(extra)
        headers = self._vneid_headers(target_code=target_code)
        path = self._tcdn_path(self.SERVICE_CODE, "/api/agent/init")
        return self._http.post_json(path, body, headers)

    def get_transaction(self, transaction_id, target_code=None):
        """
        POST /api/tcdn/share-info/api/get-transaction

        Return the citizen data; `userInfo` is a JWS string to verify with the C06 key.
        """
        headers = self._vneid_headers(target_code=target_code)
        path = self._tcdn_path(self.SERVICE_CODE, "/api/get-transaction")
        return self._http.post_json(path, {"transactionId": transaction_id}, headers)
