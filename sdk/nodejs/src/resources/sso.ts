/** SSO resource — VNeID SSO (OAuth2/OIDC). */
import { BaseResource } from "./base";

export interface TokenResponse {
  access_token: string;
  expires_in: number;
  refresh_token: string;
  refresh_expires_in: number;
  token_type: string;
  id_token: string;
  session_state: string;
  scope: string;
}

export class SsoResource extends BaseResource {
  /**
   * Build the VNeID authorize redirect URL (WEB flow):
   * {authorizeBase}/auth?response_type=code&client_id=...&scope=openid&redirect_uri=...
   */
  buildAuthorizeUrl(
    authorizeBase: string,
    clientId: string,
    redirectUri: string,
    scope = "openid",
    responseType = "code",
  ): string {
    const query = new URLSearchParams({
      response_type: responseType,
      client_id: clientId,
      scope,
      redirect_uri: redirectUri,
    }).toString();
    return `${authorizeBase.replace(/\/+$/, "")}/auth?${query}`;
  }

  /** POST /api/tcdn/sso-token/api/vneid/form/sso (x-www-form-urlencoded) */
  async exchangeToken(params: {
    code: string;
    clientId: string;
    clientSecret: string;
    redirectUri: string;
    grantType?: string;
  }): Promise<TokenResponse> {
    const form = {
      grant_type: params.grantType ?? "authorization_code",
      client_id: params.clientId,
      client_secret: params.clientSecret,
      code: params.code,
      redirect_uri: params.redirectUri,
    };
    const headers = this.vneidHeaders({
      contentType: "application/x-www-form-urlencoded",
      includeTargetCode: false,
    });
    const path = this.tcdnPath("sso-token", "/api/vneid/form/sso");
    return this.http.postForm<TokenResponse>(path, form, headers);
  }

  /** GET /api/tcdn/sso-userinfo/api/vneid/sso */
  async getUserInfo(accessToken: string): Promise<Record<string, unknown>> {
    const headers = this.vneidHeaders({ bearer: accessToken, includeTargetCode: false });
    const path = this.tcdnPath("sso-userinfo", "/api/vneid/sso");
    return this.http.get<Record<string, unknown>>(path, headers);
  }
}
