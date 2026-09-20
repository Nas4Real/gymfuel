import { readCallbackState } from "./callback-state.js";

const stateLabels = {
  error: "Action needed",
  success: "Account verified",
  unavailable: "Secure confirmation",
};

const stateKickers = {
  error: "We could not verify this link",
  success: "Confirmation complete",
  unavailable: "No confirmation result found",
};

const elements = {
  appLink: document.querySelector("#open-app"),
  desktopHint: document.querySelector("#desktop-hint"),
  message: document.querySelector("#result-message"),
  kicker: document.querySelector("#result-kicker"),
  label: document.querySelector("#state-label"),
  title: document.querySelector("#result-title"),
};

function renderCallbackState() {
  const state = readCallbackState(window.location.search, window.location.hash);
  document.body.dataset.state = state.kind;
  elements.label.textContent = stateLabels[state.kind];
  elements.kicker.textContent = stateKickers[state.kind];
  elements.title.textContent = state.title;
  elements.message.textContent = state.message;
  elements.appLink.hidden = true;
  elements.appLink.removeAttribute("href");
  elements.desktopHint.hidden = true;
  document.title = `${state.title} · GymFuel`;

  if (state.appUrl) {
    elements.appLink.href = state.appUrl;
    elements.appLink.hidden = false;
    elements.desktopHint.hidden = false;
  }

  window.history.replaceState(null, "", window.location.pathname);
}

renderCallbackState();
window.addEventListener("hashchange", renderCallbackState);
