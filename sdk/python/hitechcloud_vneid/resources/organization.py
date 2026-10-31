"""Organization resource — organization identity (type=ORG)."""

from .base import BaseResource


class OrganizationResource(BaseResource):
    """Share/verify organization identity (service-code: share-info, type=ORG)."""

    SERVICE_CODE = "share-info"

    def init(self, share_org_number, citizen_pid="", extra=None, target_code=None):
        """
        POST /api/tcdn/share-info/api/agent/init with type=ORG.

        The legal representative confirms sharing on VNeID.
        """
        body = {
            "citizenPid": citizen_pid,
            "type": "ORG",
            "shareOrgNumber": share_org_number,
        }
        if extra:
            body.update(extra)
        headers = self._vneid_headers(target_code=target_code)
        path = self._tcdn_path(self.SERVICE_CODE, "/api/agent/init")
        return self._http.post_json(path, body, headers)

    def get_transaction(self, transaction_id, target_code=None):
        """POST /api/tcdn/share-info/api/get-transaction — org data (orgPid, taxCode, ...)."""
        headers = self._vneid_headers(target_code=target_code)
        path = self._tcdn_path(self.SERVICE_CODE, "/api/get-transaction")
        return self._http.post_json(path, {"transactionId": transaction_id}, headers)
