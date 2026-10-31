"""SSO resource — VNeID SSO (OAuth2/OIDC)."""

try:
    from urllib.parse import urlencode
except ImportError:  # pragma: no cover
    from urllib import urlencode

from .base import BaseResource


class SsoResource(BaseResource):
    """VNeID SSO: authorize URL, token exchange and userinfo."""

    def build_authorize_url(self, authorize_base, client_id, redirect_uri,
                            scope="openid", response_type="code"):
        """
        Build the VNeID authorize redirect URL (WEB flow):
        {authorize_base}/auth?response_type=code&client_id=...&scope=openid&redirect_uri=...
        """
        query = urlencode({
            "response_type": response_type,
            "client_id": client_id,
            "scope": scope,
            "redirect_uri": redirect_uri,
        })
        return authorize_base.rstrip("/") + "/auth?" + query

    def exchange_token(self, code, client_id, client_secret, redirect_uri,
                       grant_type="authorization_code"):
        """
        POST /api/tcdn/sso-token/api/vneid/form/sso (x-www-form-urlencoded)

        Returns { access_token, refresh_token, id_token, token_type, expires_in, scope }.
        """
        form = {
            "grant_type": grant_type,
            "client_id": client_id,
            "client_secret": client_secret,
            "code": code,
            "redirect_uri": redirect_uri,
        }
        headers = self._vneid_headers(
            content_type="application/x-www-form-urlencoded"
        )
        headers.pop("target_code", None)
        path = self._tcdn_path("sso-token", "/api/vneid/form/sso")
        return self._http.post_form(path, form, headers)

    def get_user_info(self, access_token):
        """
        GET /api/tcdn/sso-userinfo/api/vneid/sso

        `signature` in the response is a JWS string (verify with the C06 key).
        """
        headers = self._vneid_headers(bearer=access_token)
        headers.pop("target_code", None)
        path = self._tcdn_path("sso-userinfo", "/api/vneid/sso")
        return self._http.get(path, headers)
