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

function loadEnvFile(envFilePath) {
  if (!envFilePath || !fs.existsSync(envFilePath)) return;
  const content = fs.readFileSync(envFilePath, "utf8");
  for (const rawLine of content.split(/\r?\n/)) {
    const line = rawLine.trim();
    if (!line || line.startsWith("#")) continue;
    const idx = line.indexOf("=");
    if (idx === -1) continue;
    const key = line.slice(0, idx).trim();
    let value = line.slice(idx + 1).trim();
    if ((value.startsWith('"') && value.endsWith('"')) || (value.startsWith("'") && value.endsWith("'"))) {
      value = value.slice(1, -1);
    }
    if (!(key in process.env)) process.env[key] = value;
  }
}

function nowIso() {
  return new Date().toISOString();
}

function maskBodyForLog(body) {
  if (!body || typeof body !== "object") return body;
  const clone = { ...body };
  if ("password" in clone) clone.password = "***";
  return clone;
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const envFile = args["env-file"] || path.resolve(process.cwd(), "Producto/backendpanol/docs/qa/.env.qa");
  loadEnvFile(envFile);

  const config = {
    baseUrl: (process.env.QA_BASE_URL || "").replace(/\/$/, ""),
    coordRut: process.env.QA_COORD_RUT || "",
    coordPassword: process.env.QA_COORD_PASSWORD || "",
    directorRut: process.env.QA_DIRECTOR_RUT || "",
    directorPassword: process.env.QA_DIRECTOR_PASSWORD || "",
    docenteRut: process.env.QA_DOCENTE_RUT || "",
    docentePassword: process.env.QA_DOCENTE_PASSWORD || "",
    reportRoot: path.resolve(process.cwd(), process.env.QA_REPORT_ROOT || "Producto/backendpanol/docs/qa/reports"),
  };

  if (!config.baseUrl) {
    throw new Error("Falta QA_BASE_URL. Define la URL del backend (no frontend).");
  }

  const requiredForCore = [
    ["QA_COORD_RUT", config.coordRut],
    ["QA_COORD_PASSWORD", config.coordPassword],
    ["QA_DIRECTOR_RUT", config.directorRut],
    ["QA_DIRECTOR_PASSWORD", config.directorPassword],
  ];
  const missing = requiredForCore.filter(([, value]) => !value).map(([name]) => name);
  if (missing.length > 0) {
    throw new Error(`Faltan variables requeridas: ${missing.join(", ")}`);
  }

  const runId = new Date().toISOString().replace(/[T:.]/g, "-");
  const reportDir = path.join(config.reportRoot, runId);
  fs.mkdirSync(reportDir, { recursive: true });

  const state = {
    tokens: {},
    created: {},
    implement500Probe: [],
    results: [],
  };

  async function request({
    name,
    method,
    url,
    token,
    body,
    expectedStatus,
    headers = {},
    tags = [],
  }) {
    const fullUrl = `${config.baseUrl}${url}`;
    const reqHeaders = {
      "Content-Type": "application/json",
      ...headers,
    };
    if (token) reqHeaders.Authorization = `Bearer ${token}`;

    const startedAt = Date.now();
    let response;
    let responseText;
    let parsed = null;
    let status = 0;
    let networkError = null;

    try {
      response = await fetch(fullUrl, {
        method,
        headers: reqHeaders,
        body: body === undefined ? undefined : JSON.stringify(body),
      });
      status = response.status;
      responseText = await response.text();
      try {
        parsed = responseText ? JSON.parse(responseText) : null;
      } catch {
        parsed = responseText || null;
      }
    } catch (error) {
      networkError = error instanceof Error ? error.message : String(error);
    }

    const elapsedMs = Date.now() - startedAt;
    const expected = Array.isArray(expectedStatus) ? expectedStatus : [expectedStatus];
    const ok = !networkError && expected.includes(status);

    const row = {
      timestamp: nowIso(),
      name,
      method,
      url,
      fullUrl,
      expectedStatus: expected,
      status,
      ok,
      elapsedMs,
      tags,
      requestBody: maskBodyForLog(body),
      responseBody: parsed,
      networkError,
    };

    state.results.push(row);

    const icon = ok ? "PASS" : "FAIL";
    console.log(`[${icon}] ${method} ${url} -> ${networkError || status} (${name})`);

    return row;
  }

  async function login(role, rut, password) {
    const res = await request({
      name: `Login ${role}`,
      method: "POST",
      url: "/api/v2/auth/login",
      body: { rut, password },
      expectedStatus: [200],
      tags: ["auth", "smoke"],
    });

    const token = res.responseBody?.accessToken || res.responseBody?.access_token || null;
    if (res.ok && token) {
      state.tokens[role] = token;
    } else {
      console.warn(`No se pudo obtener token para rol ${role}.`);
    }
  }

  await login("COORDINADOR", config.coordRut, config.coordPassword);
  await login("DIRECTOR", config.directorRut, config.directorPassword);
  if (config.docenteRut && config.docentePassword) {
    await login("DOCENTE", config.docenteRut, config.docentePassword);
  }

  const coordToken = state.tokens.COORDINADOR;
  const directorToken = state.tokens.DIRECTOR;

  if (!coordToken || !directorToken) {
    throw new Error("No se pudieron obtener tokens de COORDINADOR y DIRECTOR.");
  }

  const uniq = Date.now().toString().slice(-7);
  const uniqueNumericRut = `88${uniq.slice(-6)}1`; // 9 digitos numericos, compatible con entorno dev
  const names = {
    categoryCore: `QA CAT CORE ${uniq}`,
    categoryDelete: `QA CAT DELETE ${uniq}`,
    categoryDeactivate: `QA CAT DEACTIVATE ${uniq}`,
    location: `QA LOC ${uniq}`,
    implement: `QA IMPLEMENT ${uniq}`,
    user: `qa.user.${uniq}`,
  };

  await request({ name: "Auth logout coordinador", method: "POST", url: "/api/v2/auth/logout", token: coordToken, expectedStatus: [204], tags: ["auth", "smoke"] });

  // relogin after logout
  await login("COORDINADOR", config.coordRut, config.coordPassword);

  await request({ name: "GET categories active", method: "GET", url: "/api/v2/categories/active", token: state.tokens.COORDINADOR, expectedStatus: [200], tags: ["categories", "smoke"] });
  await request({ name: "GET categories gestion", method: "GET", url: "/api/v2/categories/gestion", token: state.tokens.COORDINADOR, expectedStatus: [200], tags: ["categories", "smoke"] });

  const createCategoryCore = await request({
    name: "POST categories core",
    method: "POST",
    url: "/api/v2/categories",
    token: state.tokens.COORDINADOR,
    body: { nombre: names.categoryCore, descripcion: "Categoria QA para CRUD implementos" },
    expectedStatus: [201],
    tags: ["categories", "smoke", "setup"],
  });
  state.created.categoryCoreUuid = createCategoryCore.responseBody?.uuid;

  const createCategoryDelete = await request({
    name: "POST categories delete",
    method: "POST",
    url: "/api/v2/categories",
    token: state.tokens.COORDINADOR,
    body: { nombre: names.categoryDelete, descripcion: "Categoria QA para DELETE" },
    expectedStatus: [201],
    tags: ["categories", "smoke", "setup"],
  });
  state.created.categoryDeleteUuid = createCategoryDelete.responseBody?.uuid;

  const createCategoryDeactivate = await request({
    name: "POST categories deactivate-target",
    method: "POST",
    url: "/api/v2/categories",
    token: state.tokens.COORDINADOR,
    body: { nombre: names.categoryDeactivate, descripcion: "Categoria QA para desactivar" },
    expectedStatus: [201],
    tags: ["categories", "smoke", "setup"],
  });
  state.created.categoryDeactivateUuid = createCategoryDeactivate.responseBody?.uuid;

  if (state.created.categoryCoreUuid) {
    await request({
      name: "PUT categories",
      method: "PUT",
      url: `/api/v2/categories/${state.created.categoryCoreUuid}`,
      token: state.tokens.COORDINADOR,
      body: { nombre: `${names.categoryCore} EDIT`, descripcion: "Categoria QA editada" },
      expectedStatus: [200],
      tags: ["categories", "smoke"],
    });

    await request({
      name: "GET category associations",
      method: "GET",
      url: `/api/v2/categories/${state.created.categoryCoreUuid}/associations`,
      token: state.tokens.COORDINADOR,
      expectedStatus: [200],
      tags: ["categories", "smoke"],
    });

  }

  if (state.created.categoryDeactivateUuid) {
    await request({
      name: "PATCH category deactivate",
      method: "PATCH",
      url: `/api/v2/categories/${state.created.categoryDeactivateUuid}/deactivate?force=false`,
      token: state.tokens.COORDINADOR,
      expectedStatus: [200, 409],
      tags: ["categories", "smoke"],
    });
  }

  if (state.created.categoryDeleteUuid) {
    await request({
      name: "DELETE category",
      method: "DELETE",
      url: `/api/v2/categories/${state.created.categoryDeleteUuid}`,
      token: state.tokens.COORDINADOR,
      expectedStatus: [204],
      tags: ["categories", "smoke"],
    });
  }

  await request({ name: "GET locations selector", method: "GET", url: "/api/v2/locations", token: state.tokens.COORDINADOR, expectedStatus: [200], tags: ["locations", "smoke"] });
  await request({ name: "GET locations management", method: "GET", url: "/api/v2/locations/management", token: state.tokens.COORDINADOR, expectedStatus: [200], tags: ["locations", "smoke"] });

  const createLocation = await request({
    name: "POST location",
    method: "POST",
    url: "/api/v2/locations",
    token: state.tokens.COORDINADOR,
    body: { name: names.location, description: "Ubicacion QA" },
    expectedStatus: [201],
    tags: ["locations", "smoke", "setup"],
  });
  state.created.locationUuid = createLocation.responseBody?.uuid;

  if (state.created.locationUuid) {
    await request({
      name: "PUT location",
      method: "PUT",
      url: `/api/v2/locations/${state.created.locationUuid}`,
      token: state.tokens.COORDINADOR,
      body: { name: `${names.location} EDIT`, description: "Ubicacion QA editada" },
      expectedStatus: [200],
      tags: ["locations", "smoke"],
    });
    await request({
      name: "PATCH location active=false",
      method: "PATCH",
      url: `/api/v2/locations/${state.created.locationUuid}/active?active=false`,
      token: state.tokens.COORDINADOR,
      expectedStatus: [200],
      tags: ["locations", "smoke"],
    });
    await request({
      name: "PATCH location active=true",
      method: "PATCH",
      url: `/api/v2/locations/${state.created.locationUuid}/active?active=true`,
      token: state.tokens.COORDINADOR,
      expectedStatus: [200],
      tags: ["locations", "smoke"]
    });
  }

  await request({ name: "GET implements list", method: "GET", url: "/api/v2/implements", token: state.tokens.COORDINADOR, expectedStatus: [200], tags: ["implements", "smoke"] });

  if (state.created.categoryCoreUuid && state.created.locationUuid) {
    const createImplement = await request({
      name: "POST implement",
      method: "POST",
      url: "/api/v2/implements",
      token: state.tokens.COORDINADOR,
      body: {
        name: names.implement,
        description: "Implemento QA",
        categoryUuid: state.created.categoryCoreUuid,
        locationUuid: state.created.locationUuid,
        item_type: "individual",
        min_stock: 0,
        barcode: `QA-${uniq}`,
        img_url: "https://example.com/qa.png",
        observations: "Creado por smoke runner",
      },
      expectedStatus: [200],
      tags: ["implements", "smoke", "setup"],
    });

    state.created.implementUuid = createImplement.responseBody?.uuid;

    if (state.created.implementUuid) {
      await request({
        name: "GET implement by uuid",
        method: "GET",
        url: `/api/v2/implements/${state.created.implementUuid}`,
        token: state.tokens.COORDINADOR,
        expectedStatus: [200],
        tags: ["implements", "smoke"],
      });

      await request({
        name: "PUT implement",
        method: "PUT",
        url: `/api/v2/implements/${state.created.implementUuid}`,
        token: state.tokens.COORDINADOR,
        body: {
          name: `${names.implement} EDIT`,
          description: "Implemento QA editado",
          categoryUuid: state.created.categoryCoreUuid,
          locationUuid: state.created.locationUuid,
          item_type: "individual",
          min_stock: 1,
          barcode: `QA-${uniq}-EDIT`,
          img_url: "https://example.com/qa-edit.png",
          observations: "Editado por smoke runner",
        },
        expectedStatus: [200],
        tags: ["implements", "smoke"],
      });

      await request({
        name: "PATCH implement active false",
        method: "PATCH",
        url: `/api/v2/implements/${state.created.implementUuid}/active?active=false`,
        token: state.tokens.COORDINADOR,
        expectedStatus: [200],
        tags: ["implements", "smoke"],
      });

      await request({
        name: "PATCH implement active true",
        method: "PATCH",
        url: `/api/v2/implements/${state.created.implementUuid}/active?active=true`,
        token: state.tokens.COORDINADOR,
        expectedStatus: [200],
        tags: ["implements", "smoke"],
      });

      await request({
        name: "GET stock detail",
        method: "GET",
        url: `/api/v2/implements/${state.created.implementUuid}/stock`,
        token: state.tokens.COORDINADOR,
        expectedStatus: [200],
        tags: ["stock", "smoke"],
      });

      await request({
        name: "POST stock entries",
        method: "POST",
        url: `/api/v2/implements/${state.created.implementUuid}/stock/entries`,
        token: state.tokens.COORDINADOR,
        body: { quantity: 1, asset_codes: [`ASSET-${uniq}`] },
        expectedStatus: [200],
        tags: ["stock", "smoke"],
      });

      const stockAfterEntry = await request({
        name: "GET stock detail after entry",
        method: "GET",
        url: `/api/v2/implements/${state.created.implementUuid}/stock`,
        token: state.tokens.COORDINADOR,
        expectedStatus: [200],
        tags: ["stock", "smoke"],
      });

      const individualUuid = stockAfterEntry.responseBody?.individuals?.[0]?.uuid;
      state.created.individualUuid = individualUuid;

      await request({
        name: "POST stock movement reserve",
        method: "POST",
        url: `/api/v2/implements/${state.created.implementUuid}/stock/movements`,
        token: state.tokens.COORDINADOR,
        body: { movement_type: "reserve", quantity: 1 },
        expectedStatus: [200, 400],
        tags: ["stock", "smoke"],
      });

      if (individualUuid) {
        await request({
          name: "PUT stock individual update",
          method: "PUT",
          url: `/api/v2/implements/${state.created.implementUuid}/stock/individuals/${individualUuid}`,
          token: state.tokens.COORDINADOR,
          body: { status: "available", condition: "good", current_location_uuid: state.created.locationUuid, active: true },
          expectedStatus: [200],
          tags: ["stock", "smoke"],
        });
      }

      await request({
        name: "GET inventory movements",
        method: "GET",
        url: "/api/v2/implements/movements",
        token: state.tokens.COORDINADOR,
        expectedStatus: [200],
        tags: ["movements", "smoke"],
      });

      await request({
        name: "POST implement movement",
        method: "POST",
        url: `/api/v2/implements/${state.created.implementUuid}/movements`,
        token: state.tokens.COORDINADOR,
        body: { action: "ENTRY", quantity: 1, notes: "QA movement" },
        expectedStatus: [201],
        tags: ["movements", "smoke"],
      });

      await request({
        name: "GET labels pdf",
        method: "GET",
        url: `/api/v2/implements/${state.created.implementUuid}/labels/pdf`,
        token: state.tokens.COORDINADOR,
        expectedStatus: [200],
        headers: { Accept: "application/pdf" },
        tags: ["labels", "smoke"],
      });

      const probePayloads = [
        {
          variant: "base-valid",
          expectedStatus: [200],
          payload: {
            name: `${names.implement} Probe OK`,
            description: "probe",
            categoryUuid: state.created.categoryCoreUuid,
            locationUuid: state.created.locationUuid,
            item_type: "individual",
            min_stock: 0,
          },
        },
        {
          variant: "invalid-category-uuid",
          expectedStatus: [400, 404],
          payload: {
            name: `${names.implement} Probe Cat`,
            description: "probe",
            categoryUuid: "00000000-0000-0000-0000-000000000001",
            locationUuid: state.created.locationUuid,
            item_type: "individual",
            min_stock: 0,
          },
        },
        {
          variant: "invalid-location-uuid",
          expectedStatus: [400, 404],
          payload: {
            name: `${names.implement} Probe Loc`,
            description: "probe",
            categoryUuid: state.created.categoryCoreUuid,
            locationUuid: "00000000-0000-0000-0000-000000000002",
            item_type: "individual",
            min_stock: 0,
          },
        },
        {
          variant: "invalid-item-type",
          expectedStatus: [400],
          payload: {
            name: `${names.implement} Probe ItemType`,
            description: "probe",
            categoryUuid: state.created.categoryCoreUuid,
            locationUuid: state.created.locationUuid,
            item_type: "broken_type",
            min_stock: 0,
          },
        },
        {
          variant: "null-min-stock",
          expectedStatus: [400],
          payload: {
            name: `${names.implement} Probe Stock`,
            description: "probe",
            categoryUuid: state.created.categoryCoreUuid,
            locationUuid: state.created.locationUuid,
            item_type: "individual",
            min_stock: null,
          },
        },
      ];

      for (const probe of probePayloads) {
        const probeResult = await request({
          name: `POST implement probe ${probe.variant}`,
          method: "POST",
          url: "/api/v2/implements",
          token: state.tokens.COORDINADOR,
          body: probe.payload,
          expectedStatus: probe.expectedStatus,
          tags: ["implements", "probe500"],
        });
        state.implement500Probe.push(probeResult);
      }
    }
  }

  await request({
    name: "GET users",
    method: "GET",
    url: "/api/v2/users",
    token: state.tokens.DIRECTOR,
    expectedStatus: [200],
    tags: ["users", "smoke"],
  });

  const userRut = uniqueNumericRut;
  const createUser = await request({
    name: "POST users",
    method: "POST",
    url: "/api/v2/users",
    token: state.tokens.DIRECTOR,
    body: {
      name: `QA User ${uniq}`,
      rut: userRut,
      email: `${names.user}@example.com`,
      role: "COORDINADOR",
      password: "TempPass123!",
    },
    expectedStatus: [201],
    tags: ["users", "smoke", "setup"],
  });

  let usersList = null;
  if (createUser.ok) {
    usersList = await request({
      name: "GET users after create",
      method: "GET",
      url: "/api/v2/users",
      token: state.tokens.DIRECTOR,
      expectedStatus: [200],
      tags: ["users", "smoke"],
    });
    const row = usersList.responseBody?.find?.((u) => u.email === `${names.user}@example.com` || u.rut === userRut);
    state.created.userUuid = row?.uuid;
  }

  if (state.created.userUuid) {
    await request({
      name: "PUT user",
      method: "PUT",
      url: `/api/v2/users/${state.created.userUuid}`,
      token: state.tokens.DIRECTOR,
      body: { name: `QA User ${uniq} Edit`, rut: userRut, email: `${names.user}.edit@example.com` },
      expectedStatus: [204],
      tags: ["users", "smoke"],
    });

    await request({
      name: "PUT user role",
      method: "PUT",
      url: `/api/v2/users/${state.created.userUuid}/role`,
      token: state.tokens.DIRECTOR,
      body: { role: "COORDINADOR" },
      expectedStatus: [204],
      tags: ["users", "smoke"],
    });

    await request({
      name: "PATCH user active false",
      method: "PATCH",
      url: `/api/v2/users/${state.created.userUuid}/active?active=false`,
      token: state.tokens.DIRECTOR,
      expectedStatus: [204],
      tags: ["users", "smoke"],
    });

    await request({
      name: "DELETE user",
      method: "DELETE",
      url: `/api/v2/users/${state.created.userUuid}`,
      token: state.tokens.DIRECTOR,
      expectedStatus: [204],
      tags: ["users", "smoke"],
    });
  }

  await request({
    name: "NEG unauthorized locations",
    method: "GET",
    url: "/api/v2/locations",
    expectedStatus: [401, 403],
    tags: ["negative", "security"],
  });

  await request({
    name: "NEG forbidden users with coord",
    method: "GET",
    url: "/api/v2/users",
    token: state.tokens.COORDINADOR,
    expectedStatus: [403],
    tags: ["negative", "security"],
  });

  await request({
    name: "NEG not found implement",
    method: "GET",
    url: "/api/v2/implements/00000000-0000-0000-0000-000000000099",
    token: state.tokens.COORDINADOR,
    expectedStatus: [404],
    tags: ["negative"],
  });

  await request({
    name: "NEG legacy v1 blocked",
    method: "GET",
    url: "/api/v1/implements",
    token: state.tokens.COORDINADOR,
    expectedStatus: [401, 403, 404],
    tags: ["negative", "legacy"],
  });

  const summary = buildSummary(state.results);
  const findings = buildFindings(state.results);
  const remediationPlan = buildRemediation(findings);

  const resultJsonPath = path.join(reportDir, "results.json");
  const summaryMdPath = path.join(reportDir, "summary.md");
  const findingsMdPath = path.join(reportDir, "findings.md");
  const remediationMdPath = path.join(reportDir, "remediation-plan.md");

  fs.writeFileSync(resultJsonPath, JSON.stringify({ config: { baseUrl: config.baseUrl }, summary, created: state.created, findings, results: state.results }, null, 2));
  fs.writeFileSync(summaryMdPath, renderSummaryMarkdown(summary, state));
  fs.writeFileSync(findingsMdPath, renderFindingsMarkdown(findings));
  fs.writeFileSync(remediationMdPath, renderRemediationMarkdown(remediationPlan));

  console.log("\n=== QA RUN COMPLETED ===");
  console.log(`Report directory: ${reportDir}`);
  console.log(`Total tests: ${summary.total}`);
  console.log(`Passed: ${summary.passed}`);
  console.log(`Failed: ${summary.failed}`);
  console.log(`Status >=500: ${summary.status5xx}`);

  if (summary.failed > 0) {
    process.exitCode = 2;
  }
}

function buildSummary(results) {
  const total = results.length;
  const passed = results.filter((r) => r.ok).length;
  const failed = total - passed;
  const status5xx = results.filter((r) => r.status >= 500).length;
  const byTag = {};
  for (const row of results) {
    for (const tag of row.tags || []) {
      byTag[tag] = byTag[tag] || { total: 0, failed: 0 };
      byTag[tag].total += 1;
      if (!row.ok) byTag[tag].failed += 1;
    }
  }
  return { total, passed, failed, status5xx, byTag };
}

function classifySeverity(row) {
  if (row.status >= 500 || row.networkError) return "P0";
  if (row.status === 401 || row.status === 403 || row.status === 409) return "P1";
  return "P2";
}

function inferCauseCategory(row) {
  if (row.status >= 500 || row.networkError) return "Error en repositorio/jOOQ o trigger/migracion UUID-only";
  const code = row.responseBody?.code || "";
  if (/VALID|INVALID|BAD_REQUEST/i.test(code) || row.status === 400) return "Validacion faltante o validacion de dominio";
  if (/CONFLICT|DUPLICATE|23505/i.test(code) || row.status === 409) return "Excepcion de integridad SQL mapeada/no mapeada";
  if (/AUTH|FORBIDDEN|UNAUTHORIZED/i.test(code) || [401, 403].includes(row.status)) return "Seguridad/autorizacion";
  return "Contrato frontend-backend o regla de negocio";
}

function buildFindings(results) {
  return results.filter((r) => !r.ok).map((row) => ({
    severity: classifySeverity(row),
    name: row.name,
    endpoint: `${row.method} ${row.url}`,
    expected: row.expectedStatus,
    actual: row.networkError || row.status,
    errorCode: row.responseBody?.code || null,
    message: row.responseBody?.message || row.networkError || null,
    causeCategory: inferCauseCategory(row),
    timestamp: row.timestamp,
  }));
}

function buildRemediation(findings) {
  const prioritized = findings
    .slice()
    .sort((a, b) => a.severity.localeCompare(b.severity))
    .map((f) => ({
      priority: f.severity,
      endpoint: f.endpoint,
      cause: f.causeCategory,
      action: f.severity === "P0"
        ? "Agregar manejo de excepcion y corregir causa raiz en servicio/repositorio; cubrir con test de integracion."
        : "Ajustar validacion/regla de negocio y asegurar mapeo correcto a 4xx con test de regresion.",
    }));
  return prioritized;
}

function renderSummaryMarkdown(summary, state) {
  const lines = [];
  lines.push("# QA Summary v2");
  lines.push("");
  lines.push(`- Timestamp: ${nowIso()}`);
  lines.push(`- Total tests: ${summary.total}`);
  lines.push(`- Passed: ${summary.passed}`);
  lines.push(`- Failed: ${summary.failed}`);
  lines.push(`- HTTP 5xx: ${summary.status5xx}`);
  lines.push(`- Implement UUID under test: ${state.created.implementUuid || "N/A"}`);
  lines.push("");
  lines.push("## Coverage by tag");
  lines.push("");
  for (const [tag, data] of Object.entries(summary.byTag)) {
    lines.push(`- ${tag}: ${data.total} total, ${data.failed} failed`);
  }
  lines.push("");
  lines.push("## Global checks");
  lines.push("");
  lines.push(`- sin 500: ${summary.status5xx === 0 ? "SI" : "NO"}`);
  lines.push("- sin legacy id funcional: revisar test `NEG legacy v1 blocked` en `results.json`");
  lines.push("- correlacion Cloud Run: ejecutar script `scripts/qa/cloudrun-log-extract.ps1` con la ventana de este run");
  return `${lines.join("\n")}\n`;
}

function renderFindingsMarkdown(findings) {
  const lines = ["# Hallazgos QA", ""];
  if (findings.length === 0) {
    lines.push("Sin hallazgos: todos los checks cumplieron los estados esperados.");
    return `${lines.join("\n")}\n`;
  }

  lines.push("| Severidad | Endpoint | Esperado | Actual | Code | Categoria causa | Timestamp |");
  lines.push("|---|---|---|---|---|---|---|");
  for (const f of findings) {
    lines.push(`| ${f.severity} | ${f.endpoint} | ${f.expected.join("/")} | ${f.actual} | ${f.errorCode || "-"} | ${f.causeCategory} | ${f.timestamp} |`);
  }
  return `${lines.join("\n")}\n`;
}

function renderRemediationMarkdown(remediationPlan) {
  const lines = ["# Plan de Correccion Propuesto", ""];
  if (remediationPlan.length === 0) {
    lines.push("No se requiere correccion funcional: sin hallazgos en la corrida.");
    return `${lines.join("\n")}\n`;
  }

  lines.push("## Priorizacion");
  lines.push("");
  for (const item of remediationPlan) {
    lines.push(`- ${item.priority}: ${item.endpoint} -> ${item.cause}. ${item.action}`);
  }
  lines.push("");
  lines.push("## Blindaje");
  lines.push("");
  lines.push("- Agregar test de integracion por cada hallazgo P0/P1.");
  lines.push("- Integrar esta suite QA en pipeline como gate de release dev.");
  return `${lines.join("\n")}\n`;
}

main().catch((error) => {
  console.error(`QA runner error: ${error.message}`);
  process.exit(1);
});
