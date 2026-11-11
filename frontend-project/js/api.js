const nativeFetch = window.fetch.bind(window);
// Deployments may set this value before loading api.js or via a meta tag.
const configuredApiBase = document.querySelector(
    'meta[name="niit-api-base-url"]'
);
const defaultApiBaseUrl =
    `${window.location.protocol === "https:" ? "https:" : "http:"}//` +
    `${window.location.hostname || "localhost"}:8080`;
const apiBaseUrl = new URL(
    window.NIIT_API_BASE_URL ||
    (configuredApiBase && configuredApiBase.content) ||
    defaultApiBaseUrl,
    window.location.href
);
if (!["http:", "https:"].includes(apiBaseUrl.protocol)) {
    throw new Error("The configured API base URL must use HTTP or HTTPS.");
}
window.NIIT_API_BASE_URL = apiBaseUrl.href.replace(/\/+$/, "");
const csrfUrl = `${window.NIIT_API_BASE_URL}/auth/csrf`;
let csrfRequest;

function readCookie(name) {
    const prefix = `${name}=`;
    const cookie = document.cookie
        .split("; ")
        .find(value => value.startsWith(prefix));
    return cookie ? decodeURIComponent(cookie.slice(prefix.length)) : null;
}

window.fetch = async function (input, options = {}) {
    if (window.location.protocol === "file:") {
        throw new Error(
            "Open the frontend through Live Server at http://localhost:5500, not as a file. " +
            "The backend only allows requests from the local web server."
        );
    }

    const method = String(options.method || "GET").toUpperCase();
    const headers = new Headers(options.headers || {});

    if (!["GET", "HEAD", "OPTIONS", "TRACE"].includes(method)) {
        let token = readCookie("XSRF-TOKEN");
        if (!token) {
            csrfRequest ??= nativeFetch(csrfUrl, {
                credentials: "include"
            }).finally(() => {
                csrfRequest = null;
            });
            const csrfResponse = await csrfRequest;
            if (!csrfResponse.ok) {
                throw new Error(
                    `CSRF endpoint returned HTTP ${csrfResponse.status}. ` +
                    "Check that the expected Spring Boot application is running on port 8080."
                );
            }
            token = readCookie("XSRF-TOKEN");
        }
        if (!token) {
            throw new Error("The API did not provide a CSRF token.");
        }
        headers.set("X-XSRF-TOKEN", token);
    }

    return nativeFetch(input, {
        ...options,
        headers,
        credentials: "include"
    });
};
