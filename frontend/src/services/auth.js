import { PublicClientApplication } from '@azure/msal-browser';

const clientId = import.meta.env.VITE_AZURE_CLIENT_ID;
const tenantId = import.meta.env.VITE_AZURE_TENANT_ID;
const authority = import.meta.env.VITE_AZURE_AUTHORITY
  || `https://login.microsoftonline.com/${tenantId || 'common'}`;
const redirectUri = import.meta.env.VITE_AZURE_REDIRECT_URI || window.location.origin;
const apiScope = import.meta.env.VITE_AZURE_API_SCOPE;

export const msalInstance = new PublicClientApplication({
  auth: {
    clientId: clientId || '00000000-0000-0000-0000-000000000000',
    authority,
    redirectUri,
  },
  cache: {
    cacheLocation: 'localStorage',
  },
});

const loginRequest = {
  scopes: apiScope ? [apiScope] : [],
};

export async function initializeAuth() {
  await msalInstance.initialize();
  await msalInstance.handleRedirectPromise();
  const account = msalInstance.getAllAccounts()[0];
  if (!account) {
    return null;
  }

  msalInstance.setActiveAccount(account);
  return getAccessToken(account);
}

export async function login() {
  const result = await msalInstance.loginPopup(loginRequest);
  msalInstance.setActiveAccount(result.account);
  return getAccessToken(result.account);
}

export function logout() {
  const account = getAccount();
  if (account) {
    return msalInstance.logoutPopup();
  }
  return Promise.resolve();
}

async function getAccessToken(account) {
  if (!apiScope) {
    throw new Error('VITE_AZURE_API_SCOPE no esta configurado');
  }

  const result = await msalInstance.acquireTokenSilent({
    ...loginRequest,
    account,
  });
  return result.accessToken;
}

export function getAccount() {
  return msalInstance.getActiveAccount() || msalInstance.getAllAccounts()[0] || null;
}
