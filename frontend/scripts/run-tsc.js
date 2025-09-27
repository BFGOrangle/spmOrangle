#!/usr/bin/env node

// eslint-disable-next-line @typescript-eslint/no-require-imports
const { spawnSync } = require("node:child_process");

const result = spawnSync("npx", ["tsc", "--noEmit"], {
  stdio: "inherit",
});

if (result.status !== 0) {
  process.exit(result.status ?? 1);
}
