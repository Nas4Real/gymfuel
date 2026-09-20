import assert from "node:assert/strict";
import { describe, it } from "node:test";

import { readCallbackState } from "../src/assets/callback-state.js";

describe("readCallbackState", () => {
  it("returns a success state and forwards only the PKCE code", () => {
    const state = readCallbackState(
      "?code=pkce-code_123456&next=https%3A%2F%2Fevil.example",
      "",
    );

    assert.equal(state.kind, "success");
    assert.equal(state.title, "Email confirmed");
    assert.equal(
      state.appUrl,
      "com.gymfuel.app://auth-callback?code=pkce-code_123456",
    );
    assert.equal(state.appUrl.includes("evil.example"), false);
  });

  it("lets a Supabase error override a code", () => {
    const state = readCallbackState(
      "?code=pkce-code_123456",
      "#error=access_denied&error_code=otp_expired",
    );

    assert.equal(state.kind, "error");
    assert.equal(state.reason, "expired");
    assert.equal(state.appUrl, null);
  });

  it("recognizes an expired confirmation link without exposing raw error text", () => {
    const rawDescription = "Token expired <script>alert(1)</script>";
    const state = readCallbackState(
      "",
      `#error=access_denied&error_description=${encodeURIComponent(rawDescription)}`,
    );

    assert.equal(state.kind, "error");
    assert.equal(state.reason, "expired");
    assert.equal(state.message.includes(rawDescription), false);
    assert.equal(state.appUrl, null);
  });

  it("returns a safe invalid-link state for a generic auth failure", () => {
    const state = readCallbackState(
      "?error=access_denied&error_code=bad_code",
      "",
    );

    assert.equal(state.kind, "error");
    assert.equal(state.reason, "invalid");
    assert.equal(state.title, "Confirmation link is invalid");
    assert.equal(state.appUrl, null);
  });

  it("does not claim success when the page is opened directly", () => {
    const state = readCallbackState("", "");

    assert.equal(state.kind, "unavailable");
    assert.equal(state.title, "Confirmation status unavailable");
    assert.equal(state.appUrl, null);
  });

  it("rejects short, oversized, and control-character codes", () => {
    const shortCode = readCallbackState("?code=short", "");
    const oversizedCode = readCallbackState(`?code=${"a".repeat(2049)}`, "");
    const controlCode = readCallbackState("?code=valid-code%0Ainjected", "");

    for (const state of [shortCode, oversizedCode, controlCode]) {
      assert.equal(state.kind, "error");
      assert.equal(state.reason, "invalid");
      assert.equal(state.appUrl, null);
    }
  });
});
