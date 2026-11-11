import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import test from "node:test";
import vm from "node:vm";
import { fileURLToPath } from "node:url";

const frontendRoot = path.resolve(
    path.dirname(fileURLToPath(import.meta.url)),
    ".."
);

function collectFrontendFiles(directory) {
    return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => {
        const entryPath = path.join(directory, entry.name);
        if (entry.isDirectory()) {
            return collectFrontendFiles(entryPath);
        }
        return entry.name.endsWith(".js") || entry.name.endsWith(".html")
            ? [entryPath]
            : [];
    });
}

test("frontend does not interpolate data into HTML parsing sinks", () => {
    const files = [
        ...collectFrontendFiles(path.join(frontendRoot, "js")),
        ...fs.readdirSync(frontendRoot)
            .filter(name => name.endsWith(".html"))
            .map(name => path.join(frontendRoot, name))
    ];
    const unsafePatterns = [
        /\b(?:innerHTML|outerHTML)\s*\+=/g,
        /\b(?:innerHTML|outerHTML)\s*=\s*`[^`]*\$\{[^`]*`/gs,
        /\binsertAdjacentHTML\s*\([^,]+,\s*`[^`]*\$\{[^`]*`/gs,
        /\b(?:innerHTML|outerHTML)\s*=\s*["'][^"']*["']\s*\+/g,
        /\binsertAdjacentHTML\s*\([^,]+,\s*["'][^"']*["']\s*\+/g
    ];

    const violations = files.flatMap(file => {
        const source = fs.readFileSync(file, "utf8");
        return unsafePatterns.flatMap(pattern => {
            pattern.lastIndex = 0;
            return [...source.matchAll(pattern)].map(match =>
                `${path.relative(frontendRoot, file)}: ${match[0]}`
            );
        });
    });

    assert.deepEqual(violations, []);
});

test("API base URL is configurable and limited to HTTP(S)", () => {
    const source = fs.readFileSync(
        path.join(frontendRoot, "js", "api.js"),
        "utf8"
    );

    function loadApiBase({ globalBase, metaBase }) {
        const window = {
            fetch: async () => {},
            location: { protocol: "https:", hostname: "companion.example" }
        };
        window.NIIT_API_BASE_URL = globalBase;
        const document = {
            querySelector: () => metaBase ? { content: metaBase } : null
        };
        vm.runInNewContext(source, { window, document, URL, Headers, Error });
        return window.NIIT_API_BASE_URL;
    }

    assert.equal(
        loadApiBase({ metaBase: "https://api.example.test/v1/" }),
        "https://api.example.test/v1"
    );
    assert.equal(
        loadApiBase({ globalBase: "https://override.example.test/api" }),
        "https://override.example.test/api"
    );
    assert.throws(
        () => loadApiBase({ metaBase: "javascript:alert(1)" }),
        /HTTP or HTTPS/
    );
});
