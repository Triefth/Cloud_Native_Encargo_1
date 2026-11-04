import { PublicClientApplication } from '@azure/msal-browser';

const clientId = import.meta.env.VITE_AZURE_CLIENT_ID;
const tenantId = import.meta.env.VITE_AZURE_TENANT_ID;
const rawAuthority = import.meta.env.VITE_AZURE_AUTHORITY
  || `https://login.microsoftonline.com/${'common'}`;
const authority = rawAuthority.replace(/\/v2\.0\/?$/, '').replace(/\/$/, '');
const redirectUri = import.meta.env.VITE_AZURE_REDIRECT_URI || (typeof window !== 'undefined' ? window.location.origin : '');
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
          knownAuthorities: ['kaiserox.ciamlogin.com', 'login.microsoftonline.com'],
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

export const msalInstance = getMsalInstance();

const loginRequest = {
  scopes: apiScope ? [apiScope, 'openid', 'profile'] : ['openid', 'profile', 'email'],
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
    const token = await getAccessToken(account);
    return token || localStorage.getItem('bff_jwt_token');
  } catch (err) {
    console.warn('MSAL initialization warning:', err);
    return null;
  }
}

export async function login() {
  const instance = getMsalInstance();
  if (!instance) {
    if (typeof window !== 'undefined' && window.location.protocol === 'http:' && !['localhost', '127.0.0.1'].includes(window.location.hostname)) {
      window.location.href = window.location.href.replace('http:', 'https:');
      return null;
    }
    throw new Error('MSAL requiere conexion HTTPS o localhost para autenticar con Azure AD en el navegador.');
  }
  if (!clientId || clientId === '00000000-0000-0000-0000-000000000000') {
    throw new Error('No se ha configurado un VITE_AZURE_CLIENT_ID en el archivo .env. Para desarrollo local usa el formulario directo o los botones de "Acceso Rápido por Rol".');
  }
  const result = await instance.loginPopup(loginRequest);
  instance.setActiveAccount(result.account);
  const token = await getAccessToken(result.account);
  return token || result.idToken || result.accessToken;
}

export function logout() {
  const instance = getMsalInstance();
  const account = getAccount();
  if (instance && account) {
    return instance.logoutPopup().catch(() => { });
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
