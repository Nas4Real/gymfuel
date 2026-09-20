const NATIVE_CALLBACK = "com.gymfuel.app://auth-callback";
const MIN_CODE_LENGTH = 8;
const MAX_CODE_LENGTH = 2048;

function toParameters(value) {
  const normalized = typeof value === "string" ? value.replace(/^[?#]/u, "") : "";
  return new URLSearchParams(normalized);
}

function isBoundedPkceCode(code) {
  return (
    typeof code === "string" &&
    code.length >= MIN_CODE_LENGTH &&
    code.length <= MAX_CODE_LENGTH &&
    !/\s/u.test(code) &&
    !/[\u0000-\u001f\u007f]/u.test(code)
  );
}

function createNativeAppUrl(code) {
  const callback = new URL(NATIVE_CALLBACK);
  callback.searchParams.set("code", code);
  return callback.toString();
}

function errorState(reason) {
  if (reason === "expired") {
    return {
      kind: "error",
      reason,
      title: "Confirmation link expired",
      message: "Request a new confirmation email from GymFuel, then use the newest link.",
      appUrl: null,
    };
  }

  return {
    kind: "error",
    reason: "invalid",
    title: "Confirmation link is invalid",
    message: "This link cannot confirm your account. Return to GymFuel and request a new email.",
    appUrl: null,
  };
}

export function readCallbackState(search = "", hash = "") {
  const query = toParameters(search);
  const fragment = toParameters(hash);
  const errorValues = [
    query.get("error"),
    query.get("error_code"),
    query.get("error_description"),
    fragment.get("error"),
    fragment.get("error_code"),
    fragment.get("error_description"),
  ].filter(Boolean);

  if (errorValues.length > 0) {
    const errorSignal = errorValues.join(" ").toLowerCase();
    return errorState(errorSignal.includes("expir") ? "expired" : "invalid");
  }

  const code = query.get("code");
  if (code !== null) {
    if (!isBoundedPkceCode(code)) return errorState("invalid");

    return {
      kind: "success",
      reason: "confirmed",
      title: "Email confirmed",
      message: "Your GymFuel account is ready. Continue in the app to finish signing in.",
      appUrl: createNativeAppUrl(code),
    };
  }

  return {
    kind: "unavailable",
    reason: "missing_callback",
    title: "Confirmation status unavailable",
    message: "Open the newest confirmation email from GymFuel to verify your account.",
    appUrl: null,
  };
}
