import { createReadStream } from "node:fs";
import { stat } from "node:fs/promises";
import { createServer } from "node:http";
import { extname, resolve, sep } from "node:path";
import { fileURLToPath } from "node:url";

const host = "127.0.0.1";
const port = Number.parseInt(process.env.PORT ?? "4173", 10);
const outputDirectory = resolve(fileURLToPath(new URL("../dist", import.meta.url)));
const contentTypes = new Map([
  [".css", "text/css; charset=utf-8"],
  [".html", "text/html; charset=utf-8"],
  [".js", "text/javascript; charset=utf-8"],
  [".json", "application/json; charset=utf-8"],
  [".svg", "image/svg+xml"],
]);

function applySecurityHeaders(response, pathname) {
  response.setHeader("Referrer-Policy", "no-referrer");
  response.setHeader("X-Content-Type-Options", "nosniff");
  response.setHeader("X-Frame-Options", "DENY");
  response.setHeader(
    "Content-Security-Policy",
    "default-src 'self'; base-uri 'none'; connect-src 'none'; font-src 'self' https://fonts.gstatic.com; form-action 'none'; frame-ancestors 'none'; img-src 'self' data:; object-src 'none'; script-src 'self'; style-src 'self' https://fonts.googleapis.com",
  );
  if (pathname.startsWith("/auth/callback")) {
    response.setHeader("Cache-Control", "no-store, max-age=0");
  }
}

function resolveRequestFile(pathname) {
  const decodedPath = decodeURIComponent(pathname);
  const relativePath =
    decodedPath === "/"
      ? "index.html"
      : extname(decodedPath)
        ? decodedPath.slice(1)
        : `${decodedPath.slice(1).replace(/\/$/u, "")}/index.html`;
  const filePath = resolve(outputDirectory, relativePath);
  return filePath.startsWith(`${outputDirectory}${sep}`) ? filePath : null;
}

createServer(async (request, response) => {
  const requestUrl = new URL(request.url ?? "/", `http://${host}:${port}`);
  applySecurityHeaders(response, requestUrl.pathname);

  let filePath;
  try {
    filePath = resolveRequestFile(requestUrl.pathname);
  } catch {
    response.writeHead(400, { "Content-Type": "text/plain; charset=utf-8" });
    response.end("Bad request");
    return;
  }

  if (!filePath) {
    response.writeHead(403, { "Content-Type": "text/plain; charset=utf-8" });
    response.end("Forbidden");
    return;
  }

  try {
    if (!(await stat(filePath)).isFile()) throw new Error("Not a file");
    response.writeHead(200, {
      "Content-Type": contentTypes.get(extname(filePath)) ?? "application/octet-stream",
    });
    createReadStream(filePath).pipe(response);
  } catch {
    response.writeHead(404, { "Content-Type": "text/plain; charset=utf-8" });
    response.end("Not found");
  }
}).listen(port, host, () => {
  console.log(`GymFuel confirmation site: http://${host}:${port}`);
});
