(() => {
  // =========================
  // CONFIG
  // =========================
  const API_BASE = "http://localhost:8080"; // change when deployed
  const ENDPOINT_PROMPT = `${API_BASE}/api/turns/prompt`;
  const ENDPOINT_RESPONSE = `${API_BASE}/api/turns/response`;

  const QUIET_WINDOW_MS = 1500; // response considered "done" if no changes for this duration
  const MAX_RESPONSE_CHARS = 60000;

  // =========================
  // STATE
  // =========================
  const state = {
    pendingTurns: [], // queue of { turnId, promptText, createdAtMs }
    activeCapture: null, // { turnId, node, lastText, quietTimer, lastChangeMs }
    observer: null,
  };

  // =========================
  // HELPERS
  // =========================
  function nowMs() {
    return Date.now();
  }

  function log(...args) {
    // comment out if you want silence
    console.debug("[Capture]", ...args);
  }

  function safeTrim(s) {
    return (s || "").trim();
  }

  function getExtensionVersion() {
    try {
      return chrome?.runtime?.getManifest?.().version || "unknown";
    } catch {
      return "unknown";
    }
  }

  function getComposerText() {
    // ChatGPT has changed this a few times; we try multiple approaches
    const ta = document.querySelector("textarea");
    if (ta && ta.value && ta.value.trim()) return ta.value;

    const editable = document.querySelector('[contenteditable="true"]');
    if (editable) {
      const text = editable.innerText;
      if (text && text.trim()) return text;
    }

    return "";
  }

  function isEnterToSendEvent(e) {
    return e.key === "Enter" && !e.shiftKey && !e.isComposing;
  }

  function looksLikeSendButton(el) {
    const btn = el?.closest?.("button");
    if (!btn) return false;

    const aria = (btn.getAttribute("aria-label") || "").toLowerCase();
    const testId = (btn.getAttribute("data-testid") || "").toLowerCase();

    // These selectors are heuristic and may need tweaks if UI changes
    if (aria.includes("send")) return true;
    if (testId.includes("send")) return true;

    return false;
  }

  async function getOrCreateDeviceId() {
    return new Promise((resolve) => {
      chrome.storage.local.get({ deviceId: null }, (data) => {
        if (data.deviceId) return resolve(data.deviceId);
        const id = crypto.randomUUID();
        chrome.storage.local.set({ deviceId: id }, () => resolve(id));
      });
    });
  }

  async function postJson(url, payload) {
    try {
      await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
    } catch (e) {
      console.warn("[Capture] POST failed:", url, e);
    }
  }

  function findConversationRoot() {
    // We want a stable node that changes when new messages arrive.
    // ChatGPT DOM shifts; we select the main element if possible.
    return (
      document.querySelector("main") ||
      document.body
    );
  }

  function getAssistantMessageNodes() {
    // Heuristic: assistant messages often include elements with "assistant" in data attributes.
    // We attempt a few patterns and fallback to broader selection.

    // Pattern A: elements marked with data-message-author-role="assistant"
    const a = Array.from(document.querySelectorAll('[data-message-author-role="assistant"]'));
    if (a.length) return a;

    // Pattern B: role-based articles (common)
    const b = Array.from(document.querySelectorAll("article"));
    // We'll filter later by text growth / adjacency heuristics, so keep them.
    return b;
  }

  function nodeInnerText(node) {
    try {
      return safeTrim(node?.innerText || "");
    } catch {
      return "";
    }
  }

  function isProbablyAssistantNode(node) {
    // If we have the explicit role attribute, trust it
    if (node?.getAttribute?.("data-message-author-role") === "assistant") return true;

    // Otherwise we use a weak heuristic: assistant nodes tend to be longer & contain formatted content.
    // We'll rely on "new node appears after prompt" logic + streaming changes for correlation.
    const t = nodeInnerText(node);
    return t.length >= 20; // low bar; correlation does most of the work
  }

  function isCurrentlyStreaming() {
    // "Stop generating" button often exists while streaming
    const stopBtn = Array.from(document.querySelectorAll("button")).find((b) => {
      const aria = (b.getAttribute("aria-label") || "").toLowerCase();
      const text = (b.innerText || "").toLowerCase();
      return aria.includes("stop") || text.includes("stop generating");
    });
    return !!stopBtn;
  }

  // =========================
  // TURN CAPTURE PIPELINE
  // =========================
  async function onUserSend(sendMethod) {
    const promptText = safeTrim(getComposerText());
    if (!promptText) return;

    const turnId = crypto.randomUUID();
    const deviceId = await getOrCreateDeviceId();

    state.pendingTurns.push({
      turnId,
      promptText,
      createdAtMs: nowMs(),
    });

    const payload = {
      turnId,
      prompt: promptText,
      pageUrl: location.href,
      capturedAt: new Date().toISOString(),
      userAgent: navigator.userAgent,
      deviceId,
      extensionVersion: getExtensionVersion(),
      sendMethod,
    };

    log("Prompt captured", { turnId, sendMethod, len: promptText.length });
    postJson(ENDPOINT_PROMPT, payload);

    // kick observer capture if not already active
    ensureObserver();
  }

  function ensureObserver() {
    if (state.observer) return;

    const root = findConversationRoot();
    state.observer = new MutationObserver(() => {
      // If we’re already tracking a specific assistant node, check for changes
      if (state.activeCapture?.node) {
        const text = nodeInnerText(state.activeCapture.node);
        if (text && text !== state.activeCapture.lastText) {
          state.activeCapture.lastText = text;
          state.activeCapture.lastChangeMs = nowMs();
          scheduleFinalizeActiveCapture();
        }
        return;
      }

      // If we have pending turns but no active node, try to bind next assistant message
      if (state.pendingTurns.length > 0) {
        tryBindNextAssistantMessage();
      }
    });

    state.observer.observe(root, {
      childList: true,
      subtree: true,
      characterData: true,
    });

    // attempt bind immediately in case message already appeared
    tryBindNextAssistantMessage();
    log("MutationObserver installed");
  }

  function tryBindNextAssistantMessage() {
    if (state.activeCapture?.node) return;
    if (state.pendingTurns.length === 0) return;

    // Find the newest assistant-like node
    const nodes = getAssistantMessageNodes();
    if (!nodes.length) return;

    // Prefer explicit assistant role nodes
    const assistantNodes = nodes.filter((n) => n.getAttribute?.("data-message-author-role") === "assistant");
    const candidates = assistantNodes.length ? assistantNodes : nodes;

    // Choose the last node with meaningful text OR that is likely to grow (streaming)
    let pick = null;
    for (let i = candidates.length - 1; i >= 0; i--) {
      const n = candidates[i];
      if (!isProbablyAssistantNode(n)) continue;
      const t = nodeInnerText(n);
      // if empty, it might be streaming container; still acceptable
      pick = n;
      if (t.length > 0) break;
    }
    if (!pick) return;

    // Bind this response node to the oldest pending turn
    const turn = state.pendingTurns.shift();
    state.activeCapture = {
      turnId: turn.turnId,
      node: pick,
      lastText: nodeInnerText(pick),
      lastChangeMs: nowMs(),
      quietTimer: null,
    };

    log("Bound assistant node to turn", { turnId: turn.turnId });

    // Even if no text yet, schedule a finalize check (it will wait for quiet)
    scheduleFinalizeActiveCapture();
  }

  function scheduleFinalizeActiveCapture() {
    if (!state.activeCapture) return;

    // Reset quiet timer
    if (state.activeCapture.quietTimer) {
      clearTimeout(state.activeCapture.quietTimer);
    }

    state.activeCapture.quietTimer = setTimeout(async () => {
      if (!state.activeCapture) return;

      // If still streaming, wait a bit more
      const streaming = isCurrentlyStreaming();
      const sinceChange = nowMs() - state.activeCapture.lastChangeMs;

      if (streaming || sinceChange < QUIET_WINDOW_MS) {
        scheduleFinalizeActiveCapture();
        return;
      }

      // Finalize
      const responseText = nodeInnerText(state.activeCapture.node);
      await finalizeTurn(state.activeCapture.turnId, responseText);

      // Clear active capture; allow next pending turn
      state.activeCapture = null;

      // If more pending prompts exist, bind next
      if (state.pendingTurns.length > 0) {
        tryBindNextAssistantMessage();
      }
    }, QUIET_WINDOW_MS);
  }

  async function finalizeTurn(turnId, responseText) {
    const clean = safeTrim(responseText);
    if (!clean) {
      log("Finalize: empty response text", { turnId });
      return;
    }

    const deviceId = await getOrCreateDeviceId();

    const capped = clean.length > MAX_RESPONSE_CHARS ? clean.slice(0, MAX_RESPONSE_CHARS) : clean;

    const payload = {
      turnId,
      responseText: capped,
      responseCapturedAt: new Date().toISOString(),
      modelHint: null, // optional: try to infer later if you want
      // (deviceId isn't required by backend response endpoint right now, so not sending)
    };

    log("Response captured", { turnId, len: capped.length });
    postJson(ENDPOINT_RESPONSE, payload);
  }

  // =========================
  // EVENT HOOKS
  // =========================
  document.addEventListener(
    "keydown",
    (e) => {
      if (isEnterToSendEvent(e)) {
        // Capture immediately; even if ChatGPT blocks send, worst case you log an attempted send.
        onUserSend("enter");
      }
    },
    true
  );

  document.addEventListener(
    "click",
    (e) => {
      if (looksLikeSendButton(e.target)) {
        onUserSend("button");
      }
    },
    true
  );

  // Install observer lazily after first send, but you can also enable immediately:
  // ensureObserver();

  log("Content script loaded");
})();
