import { PublicClientApplication } from '@azure/msal-browser';

const clientId = import.meta.env.VITE_AZURE_CLIENT_ID;
const tenantId = import.meta.env.VITE_AZURE_TENANT_ID;
const authority = import.meta.env.VITE_AZURE_AUTHORITY
  || `https://login.microsoftonline.com/${tenantId || 'common'}`;
const redirectUri = import.meta.env.VITE_AZURE_REDIRECT_URI || window.location.origin;
const apiScope = import.meta.env.VITE_AZURE_API_SCOPE;

let _msalInstance = null;

export function getMsalInstance() {
  if (!_msalInstance) {
    try {
      if (typeof window !== 'undefined' && (!window.crypto || !window.crypto.subtle)) {
        console.warn('MSAL Notice: Web Crypto (window.crypto.subtle) is available on HTTPS or localhost.');
        return null;
      }
      _msalInstance = new PublicClientApplication({
        auth: {
          clientId: clientId || '00000000-0000-0000-0000-000000000000',
          authority,
          redirectUri,
        },
        cache: {
          cacheLocation: 'localStorage',
        },
      });
    } catch (err) {
      console.warn('MSAL initialization check:', err.message);
      return null;
    }
  }
  return _msalInstance;
}

const loginRequest = {
  scopes: apiScope ? [apiScope] : [],
};

export async function initializeAuth() {
  const instance = getMsalInstance();
  if (!instance) {
    return null;
  }
  try {
    await instance.initialize();
    await instance.handleRedirectPromise();
    const account = instance.getAllAccounts()[0];
    if (!account) {
      return null;
    }

    instance.setActiveAccount(account);
    return await getAccessToken(account);
  } catch (err) {
    console.warn('MSAL initialization warning:', err);
    return null;
  }
}

export async function login() {
  const instance = getMsalInstance();
  if (!instance) {
    throw new Error('MSAL requiere conexion HTTPS o localhost para autenticar con Azure AD en el navegador.');
  }
  const result = await instance.loginPopup(loginRequest);
  instance.setActiveAccount(result.account);
  return getAccessToken(result.account);
}

export function logout() {
  const instance = getMsalInstance();
  const account = getAccount();
  if (instance && account) {
    return instance.logoutPopup().catch(() => {});
  }
  return Promise.resolve();
}

async function getAccessToken(account) {
  const instance = getMsalInstance();
  if (!instance || !apiScope) {
    return null;
  }

  try {
    const result = await instance.acquireTokenSilent({
      ...loginRequest,
      account,
    });
    return result.accessToken;
  } catch (err) {
    console.warn('Silent token acquire failed:', err);
    return null;
  }
}

export function getAccount() {
  const instance = getMsalInstance();
  if (!instance) return null;
  return instance.getActiveAccount() || instance.getAllAccounts()[0] || null;
}
