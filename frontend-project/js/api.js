const nativeFetch = window.fetch.bind(window);
const csrfUrl = `http://${window.location.hostname}:8080/auth/csrf`;
let csrfRequest;

function readCookie(name) {
    const prefix = `${name}=`;
    const cookie = document.cookie
        .split("; ")
        .find(value => value.startsWith(prefix));
    return cookie ? decodeURIComponent(cookie.slice(prefix.length)) : null;
}

window.fetch = async function (input, options = {}) {
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
                throw new Error("Could not initialize a secure API session.");
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
