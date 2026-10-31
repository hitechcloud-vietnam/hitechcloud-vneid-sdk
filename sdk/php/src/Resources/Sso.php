<?php

namespace HiTechCloud\VNeID\Resources;

/** SSO resource — VNeID SSO (OAuth2/OIDC). */
class Sso extends BaseResource
{
    /**
     * Build the VNeID authorize redirect URL (WEB flow).
     */
    public function buildAuthorizeUrl(
        string $authorizeBase,
        string $clientId,
        string $redirectUri,
        string $scope = 'openid',
        string $responseType = 'code'
    ): string {
        $query = http_build_query([
            'response_type' => $responseType,
            'client_id' => $clientId,
            'scope' => $scope,
            'redirect_uri' => $redirectUri,
        ]);
        return rtrim($authorizeBase, '/') . '/auth?' . $query;
    }

    /** POST /api/tcdn/sso-token/api/vneid/form/sso (x-www-form-urlencoded) */
    public function exchangeToken(
        string $code,
        string $clientId,
        string $clientSecret,
        string $redirectUri,
        string $grantType = 'authorization_code'
    ): ?array {
        $form = [
            'grant_type' => $grantType,
            'client_id' => $clientId,
            'client_secret' => $clientSecret,
            'code' => $code,
            'redirect_uri' => $redirectUri,
        ];
        $headers = $this->vneidHeaders([
            'contentType' => 'application/x-www-form-urlencoded',
            'includeTargetCode' => false,
        ]);
        $path = $this->tcdnPath('sso-token', '/api/vneid/form/sso');
        return $this->http->postForm($path, $form, $headers);
    }

    /** GET /api/tcdn/sso-userinfo/api/vneid/sso */
    public function getUserInfo(string $accessToken): ?array
    {
        $headers = $this->vneidHeaders(['bearer' => $accessToken, 'includeTargetCode' => false]);
        $path = $this->tcdnPath('sso-userinfo', '/api/vneid/sso');
        return $this->http->get($path, $headers);
    }
}
