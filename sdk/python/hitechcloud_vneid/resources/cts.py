"""CTS resource — digital certificate (SmartCA) issuance."""

from .base import BaseResource


class CtsResource(BaseResource):
    """Register a digital certificate / SmartCA (service-code: create-smartca-account)."""

    RESULT_SERVICE_CODE = "create-smartca-account"

    def init(self, citizen_pid, cert_type="PER", share_org_number=None,
             fee_code=None, list_org_code=None, key_data=None,
             service_code="share-info", target_code=None):
        """
        POST /api/tcdn/{service_code}/api/agent/init with CTS fields.

        cert_type: PER (individual), ORG (organization), PER-ORG (individual in org).
        """
        body = {"citizenPid": citizen_pid, "type": cert_type}
        if share_org_number is not None:
            body["shareOrgNumber"] = share_org_number
        if list_org_code is not None:
            body["listOrgCode"] = list_org_code
        if fee_code is not None:
            body["feeCode"] = fee_code
        if key_data is not None:
            body["keyData"] = key_data
        headers = self._vneid_headers(target_code=target_code)
        path = self._tcdn_path(service_code, "/api/agent/init")
        return self._http.post_json(path, body, headers)

    def report_result(self, result, target_code=None):
        """
        POST /api/tcdn/create-smartca-account/agent/account/receive-result

        Report the CTS issuance result (serialNumber, status, expiration...) to the CA.
        """
        headers = self._vneid_headers(target_code=target_code)
        path = self._tcdn_path(self.RESULT_SERVICE_CODE, "/agent/account/receive-result")
        return self._http.post_json(path, result, headers)
