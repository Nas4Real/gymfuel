import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import { describe, it } from "node:test";

const callbackHtmlUrl = new URL("../src/auth/callback/index.html", import.meta.url);
const stylesUrl = new URL("../src/assets/styles.css", import.meta.url);
const vercelConfigUrl = new URL("../vercel.json", import.meta.url);

function relativeLuminance(hexColor) {
  const channels = hexColor
    .match(/[\da-f]{2}/giu)
    .map((channel) => Number.parseInt(channel, 16) / 255)
    .map((channel) =>
      channel <= 0.04045
        ? channel / 12.92
        : ((channel + 0.055) / 1.055) ** 2.4,
    );

  return 0.2126 * channels[0] + 0.7152 * channels[1] + 0.0722 * channels[2];
}

function contrastRatio(firstColor, secondColor) {
  const luminances = [
    relativeLuminance(firstColor),
    relativeLuminance(secondColor),
  ].sort((first, second) => second - first);

  return (luminances[0] + 0.05) / (luminances[1] + 0.05);
}

describe("hosted confirmation page", () => {
  it("uses semantic live status content without inline scripts", async () => {
    const html = await readFile(callbackHtmlUrl, "utf8");

    assert.match(html, /<main\b/u);
    assert.match(html, /<h1\s+id="result-title"/u);
    assert.match(html, /id="result-message"[^>]+aria-live="polite"/u);
    assert.match(html, /id="open-app"[^>]+hidden/u);
    assert.match(html, /<script\s+type="module"\s+src="\/assets\/callback\.js"><\/script>/u);
    assert.doesNotMatch(html, /<script(?![^>]+src=)[^>]*>/iu);
    assert.doesNotMatch(html, /\sstyle=/iu);
  });

  it("publishes only dist with privacy and browser-hardening headers", async () => {
    const config = JSON.parse(await readFile(vercelConfigUrl, "utf8"));
    const allHeaders = config.headers.flatMap((rule) => rule.headers);
    const headerValue = (key) =>
      allHeaders.find((header) => header.key.toLowerCase() === key.toLowerCase())?.value;

    assert.equal(config.buildCommand, "npm run build");
    assert.equal(config.outputDirectory, "dist");
    assert.match(headerValue("Content-Security-Policy"), /default-src 'self'/u);
    assert.equal(headerValue("Referrer-Policy"), "no-referrer");
    assert.equal(headerValue("X-Content-Type-Options"), "nosniff");
    assert.match(headerValue("Cache-Control"), /private, no-store/u);
    assert.match(headerValue("Strict-Transport-Security"), /max-age=31536000/u);
  });

  it("keeps the primary action at WCAG AA text contrast", async () => {
    const styles = await readFile(stylesUrl, "utf8");
    const actionColor = styles.match(/--blue-action:\s*(#[\da-f]{6})/iu)?.[1];
    const actionRule = styles.match(/\.primary-action\s*\{[^}]+\}/su)?.[0];

    assert.match(actionRule, /background:\s*var\(--blue-action\)/u);
    assert.ok(contrastRatio("#ffffff", actionColor) >= 4.5);
  });
});
