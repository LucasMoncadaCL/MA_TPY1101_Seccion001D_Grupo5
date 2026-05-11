#!/usr/bin/env node
import fs from "node:fs";
import path from "node:path";

function parseArgs(argv) {
  const args = {};
  for (let i = 0; i < argv.length; i += 1) {
    const token = argv[i];
    if (!token.startsWith("--")) continue;
    const key = token.slice(2);
    const value = argv[i + 1] && !argv[i + 1].startsWith("--") ? argv[++i] : "true";
    args[key] = value;
  }
  return args;
}

function readSummary(reportDir) {
  const summaryPath = path.join(reportDir, "summary.md");
  const resultsPath = path.join(reportDir, "results.json");

  if (!fs.existsSync(resultsPath)) {
    throw new Error(`No existe ${resultsPath}`);
  }

  const raw = JSON.parse(fs.readFileSync(resultsPath, "utf8"));
  const summary = raw.summary || {};
  return {
    reportDir,
    total: summary.total ?? 0,
    failed: summary.failed ?? 0,
    status5xx: summary.status5xx ?? 0,
    hasSummaryMd: fs.existsSync(summaryPath),
  };
}

function isGreen(run) {
  return run.total > 0 && run.failed === 0 && run.status5xx === 0;
}

function main() {
  const args = parseArgs(process.argv.slice(2));
  const reportsRoot = path.resolve(process.cwd(), args["reports-root"] || "Producto/backendpanol/docs/qa/reports");
  const requiredGreens = Number(args["required-greens"] || "2");

  if (!fs.existsSync(reportsRoot)) {
    throw new Error(`No existe directorio de reportes: ${reportsRoot}`);
  }

  const runs = fs.readdirSync(reportsRoot, { withFileTypes: true })
    .filter((d) => d.isDirectory())
    .map((d) => path.join(reportsRoot, d.name))
    .filter((dir) => fs.existsSync(path.join(dir, "results.json")))
    .sort((a, b) => fs.statSync(a).mtimeMs - fs.statSync(b).mtimeMs)
    .map(readSummary);

  if (runs.length < requiredGreens) {
    console.error(`Gate FAIL: se requieren ${requiredGreens} corridas y solo hay ${runs.length}.`);
    process.exit(2);
  }

  const lastRuns = runs.slice(-requiredGreens);
  const failedRuns = lastRuns.filter((run) => !isGreen(run));

  console.log("Validando corridas QA para gate de corte DB:");
  for (const run of lastRuns) {
    console.log(`- ${path.basename(run.reportDir)}: total=${run.total}, failed=${run.failed}, 5xx=${run.status5xx}`);
  }

  if (failedRuns.length > 0) {
    console.error("Gate FAIL: las ultimas corridas no son 100% verdes.");
    process.exit(2);
  }

  console.log(`Gate PASS: ${requiredGreens} corridas consecutivas verdes.`);
}

try {
  main();
} catch (error) {
  console.error(`Gate validation error: ${error.message}`);
  process.exit(1);
}
