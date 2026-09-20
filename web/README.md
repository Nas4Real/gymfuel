# GymFuel confirmation site

This dependency-free static site presents the result of Supabase email confirmation instead of leaving desktop users on a blank native-app URI. Supabase still performs the verification; the page only interprets its success or error callback.

## Local verification

```powershell
cd web
npm ci --ignore-scripts
npm run check
npm run dev
```

Open `http://127.0.0.1:4173/auth/callback/?code=test-pkce-code` for the success presentation, `http://127.0.0.1:4173/auth/callback/#error=access_denied&error_code=otp_expired` for the expired-link presentation, and `http://127.0.0.1:4173/auth/callback/` for a direct visit.

## Deploy from GitHub to Vercel

1. In Vercel, choose **Add New → Project**.
2. Import `Nas4Real/gymfuel` from GitHub.
3. Set **Root Directory** to `web`.
4. Keep **Framework Preset** as `Other`. `vercel.json` supplies the build command and `dist` output directory.
5. No environment variables are required.
6. Deploy, then copy the production URL exactly as Vercel shows it.

Do not add a broad `*.vercel.app` wildcard to Supabase. Send the assigned production URL back so the exact `https://<domain>/auth/callback` URL can be added to Supabase Auth and the Android sign-up redirect.

Official references:

- https://vercel.com/docs/monorepos
- https://vercel.com/docs/project-configuration
- https://supabase.com/docs/guides/auth/redirect-urls
