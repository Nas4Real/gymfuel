import { cp, mkdir, readFile, rm, stat } from "node:fs/promises";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const projectRoot = dirname(dirname(fileURLToPath(import.meta.url)));
const sourceDirectory = join(projectRoot, "src");
const outputDirectory = join(projectRoot, "dist");
const requiredFiles = [
  "index.html",
  "auth/callback/index.html",
  "assets/callback-state.js",
  "assets/callback.js",
  "assets/styles.css",
];

await rm(outputDirectory, { force: true, recursive: true });
await mkdir(outputDirectory, { recursive: true });
await cp(sourceDirectory, outputDirectory, { recursive: true });

for (const relativePath of requiredFiles) {
  const file = await stat(join(outputDirectory, relativePath));
  if (!file.isFile()) throw new Error(`Missing build artifact: ${relativePath}`);
}

const callbackHtml = await readFile(
  join(outputDirectory, "auth/callback/index.html"),
  "utf8",
);
if (/<script(?![^>]+src=)[^>]*>/iu.test(callbackHtml)) {
  throw new Error("Inline scripts are not allowed in the callback page");
}

console.log(`Built ${requiredFiles.length} required files in web/dist`);
