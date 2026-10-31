"""Base resource with shared header builders."""

from ..http_client import new_request_id, vneid_request_time, eid_request_time


class BaseResource:
    """Base class for all VNeID resource groups."""

    def __init__(self, http, config, signature):
        self._http = http
        self._config = config
        self.signature = signature

    # -- header builders -------------------------------------------------
    def _vneid_headers(self, target_code=None, request_type=None,
                       request_id=None, content_type="application/json; charset=utf-8",
                       bearer=None):
        """Build headers for a VNeID (AgentGW) request."""
        headers = {
            "Content-Type": content_type,
            "api_key": self._config.api_key,
            "request_id": request_id or new_request_id(),
            "request_time": vneid_request_time(),
        }
        tc = target_code or self._config.target_code
        if tc:
            headers["target_code"] = tc
        if request_type:
            headers["request_type"] = request_type
        if bearer:
            headers["Authorization"] = "Bearer " + bearer
        return headers

    def _tcdn_path(self, service_code, suffix):
        """Build an AgentGW path: /api/tcdn/{service_code}{suffix}."""
        return f"/api/tcdn/{service_code}{suffix}"
