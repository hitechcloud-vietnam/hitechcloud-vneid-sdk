"""HiTechCloud VNeID SDK HTTP Client"""

import json
import time
import uuid
from datetime import datetime, timezone, timedelta

import requests

from .exceptions import HiTechCloudVNeIDError, RateLimitError, ERROR_MAP

# GMT+7 (Vietnam)
_TZ = timezone(timedelta(hours=7))


def new_request_id():
    """Generate a unique request_id (UUID v4)."""
    return str(uuid.uuid4())


def vneid_request_time():
    """Return the current time formatted ddMMyyyyHHmmss (VNeID services)."""
    return datetime.now(_TZ).strftime("%d%m%Y%H%M%S")


def eid_request_time():
    """Return the current time formatted yyyyMMddHHmmss (eID services)."""
    return datetime.now(_TZ).strftime("%Y%m%d%H%M%S")


class HTTPClient:
    """Low-level HTTP client with retry logic for the AgentGW."""

    def __init__(self, base_url, timeout=30, retries=3, client_cert=None):
        self.base_url = base_url.rstrip("/")
        self.timeout = timeout
        self.retries = retries
        self.session = requests.Session()
        if client_cert:
            # mTLS: (cert, key) tuple or a single combined PEM path
            self.session.cert = client_cert
        self.session.headers.update({
            "User-Agent": "HiTechCloud-VNeID-Python-SDK/1.0.0",
            "Accept": "application/json",
        })

    def request(self, method, path, json_body=None, form_body=None, headers=None):
        """Make an HTTP request with retry on 429/5xx."""
        url = self.base_url + "/" + path.lstrip("/")
        last_error = None

        for attempt in range(self.retries + 1):
            try:
                resp = self.session.request(
                    method=method,
                    url=url,
                    json=json_body if form_body is None else None,
                    data=form_body,
                    headers=headers,
                    timeout=self.timeout,
                )

                if resp.status_code == 429:
                    retry_after = int(resp.headers.get("Retry-After", 60))
                    if attempt < self.retries:
                        time.sleep(retry_after)
                        continue
                    raise RateLimitError(
                        "Rate limit exceeded",
                        retry_after=retry_after,
                        status_code=429,
                    )

                if resp.status_code >= 500 and attempt < self.retries:
                    time.sleep(2 ** attempt)
                    continue

                if resp.status_code >= 400:
                    self._raise_error(resp)

                if resp.status_code == 204 or not resp.content:
                    return None
                return resp.json()

            except (requests.exceptions.ConnectionError,
                    requests.exceptions.Timeout) as exc:
                last_error = exc
                if attempt < self.retries:
                    time.sleep(2 ** attempt)
                    continue
                raise HiTechCloudVNeIDError(f"Request failed: {exc}")

        if last_error:
            raise HiTechCloudVNeIDError(
                f"Request failed after {self.retries} retries: {last_error}"
            )

    def _raise_error(self, resp):
        try:
            body = resp.json()
            message = (
                body.get("description")
                or body.get("message")
                or body.get("error")
                or f"HTTP {resp.status_code}"
            )
            response_code = body.get("responseCode")
        except (json.JSONDecodeError, ValueError):
            body = None
            message = f"HTTP {resp.status_code}: {resp.text[:200]}"
            response_code = None

        error_class = ERROR_MAP.get(resp.status_code, HiTechCloudVNeIDError)
        kwargs = {
            "message": message,
            "status_code": resp.status_code,
            "response_code": response_code,
            "data": body,
        }
        if resp.status_code == 429:
            kwargs["retry_after"] = int(resp.headers.get("Retry-After", 60))
        raise error_class(**kwargs)

    def post_json(self, path, json_body, headers):
        return self.request("POST", path, json_body=json_body, headers=headers)

    def post_form(self, path, form_body, headers):
        return self.request("POST", path, form_body=form_body, headers=headers)

    def get(self, path, headers):
        return self.request("GET", path, headers=headers)

    def close(self):
        self.session.close()
