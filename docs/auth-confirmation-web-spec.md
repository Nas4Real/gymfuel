# Spec: GymFuel Auth Confirmation Web Callback

Status: Approved for implementation on 2026-09-20

## Objective

Add a small Vercel-hosted web surface for Supabase email-confirmation callbacks. It replaces the blank desktop tab with a clear GymFuel-branded success, expired-link, invalid-link, or unavailable-status result. On a phone, a successful callback can continue into the installed Android app without exposing a Supabase secret or changing how Supabase verifies the email.

The production callback URL will be configured only after Vercel assigns the real production domain. Until then, the existing native callback remains active.

## Tech Stack

- Standards-based HTML, CSS, and JavaScript modules
- Node.js 24 built-in test runner and build script
- Vercel static deployment with `vercel.json`
- Existing Supabase PKCE confirmation flow and Android custom deep link
- Poppins from Google Fonts, matching the Android application

No frontend framework, Supabase web client, analytics SDK, or runtime dependency is required.

## Commands

Run from `web/`:

```powershell
npm ci --ignore-scripts
npm test
npm run build
npm run dev
```

The development server listens on `http://127.0.0.1:4173`. Test callback states with:

```text
http://127.0.0.1:4173/auth/callback/?code=test-pkce-code
http://127.0.0.1:4173/auth/callback/#error=access_denied&error_code=otp_expired
http://127.0.0.1:4173/auth/callback/
```

## Project Structure

```text
web/
  package.json              Static-site commands; no runtime dependencies
  vercel.json               Build output and security headers
  scripts/                  Local server and deterministic static build
  src/                      HTML, CSS, icons, and callback JavaScript
  tests/                    Pure callback-state tests
  dist/                     Generated, ignored Vercel output
```

## Code Style

Use small named functions, immutable return values, semantic HTML, and DOM `textContent` for external values. URL input is parsed through `URLSearchParams`; no callback parameter is inserted with `innerHTML`.

```js
export function readCallbackState(search, hash) {
  const query = new URLSearchParams(stripPrefix(search));
  const fragment = new URLSearchParams(stripPrefix(hash));
  // Return a typed presentation state; do not mutate the DOM here.
}
```

CSS uses GymFuel semantic custom properties, an 8px spacing rhythm, dark blue surfaces, blue primary actions, visible focus states, and WCAG AA contrast.

## Testing Strategy

- Unit tests cover success, expired, invalid, direct-visit, unsafe-code, and query/fragment parsing.
- The build validator checks all required static files and rejects inline scripts.
- Browser verification covers 320px, 768px, and desktop layouts; success/error/direct states; keyboard focus; accessibility structure; and a clean console.
- Android tests continue to cover the native callback contract. A later domain-specific change will add the hosted redirect as a separate tested configuration.

## Boundaries

### Always

- Keep Supabase responsible for email verification.
- Treat query and fragment values as untrusted input.
- Forward only a bounded PKCE `code` value to `com.gymfuel.app://auth-callback`.
- Use exact production redirect URLs in Supabase.
- Keep the native callback allowed as a fallback.
- Use no-store and no-referrer browser policy for callback responses.

### Ask first

- Changing the final Vercel production domain after it is configured.
- Replacing the native Android deep-link contract.
- Adding analytics, cookies, a frontend framework, or a server-side verifier.

### Never

- Commit a Supabase secret or service-role key.
- Display or log the PKCE code.
- Claim confirmation success without a valid Supabase success callback.
- Add a broad `*.vercel.app` redirect wildcard to production Supabase Auth.
- Render callback error text as HTML or expose raw internal errors to users.

## Success Criteria

1. A callback containing a valid, bounded PKCE code shows “Email confirmed” and offers a GymFuel deep link containing only that encoded code.
2. Supabase error callbacks show safe expired/invalid/general failure copy and no app-continuation link.
3. Opening the page directly shows “Confirmation status unavailable,” never a false success.
4. The page is responsive from 320px through desktop, keyboard accessible, and visually consistent with GymFuel’s dark Poppins design system.
5. The production artifact contains security headers, no secrets, no analytics, and no runtime dependencies.
6. Tests, build validation, browser checks, dependency audit, and staged secret scan pass.
7. Vercel deployment instructions clearly identify the repository and `web` root directory.
8. Hosted Supabase remains unchanged until the exact Vercel production URL is known.

## Open Question

- Final production domain: resolved after the owner imports the GitHub repository into Vercel. That exact URL is required before updating Android’s sign-up redirect and Supabase’s allow list.

## Authoritative Sources

- Supabase email templates: https://supabase.com/docs/guides/auth/auth-email-templates
- Supabase redirect URLs: https://supabase.com/docs/guides/auth/redirect-urls
- Vercel project configuration: https://vercel.com/docs/project-configuration
- Vercel monorepos: https://vercel.com/docs/monorepos
- Node.js test runner: https://nodejs.org/api/test.html
