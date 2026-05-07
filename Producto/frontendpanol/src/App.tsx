import { useEffect, useMemo, useState, type ReactNode } from "react";
import { InventoryLayout, type BreadcrumbPart, type InventorySection } from "./components/layout/InventoryLayout";
import { InventoryCategoriesPage } from "./pages/InventoryCategoriesPage";
import { InventoryItemDetailPage } from "./pages/InventoryItemDetailPage";
import { InventoryItemsPage } from "./pages/InventoryItemsPage";
import { InventoryLocationsPage } from "./pages/InventoryLocationsPage";
import { InventoryMovesPage } from "./pages/InventoryMovesPage";
import { LoginPage } from "./pages/LoginPage";
import { NotFoundPage } from "./pages/NotFoundPage";
import { logout } from "./services/authService";
import { clearSession, getUserRoleFromToken, isAuthenticated } from "./utils/auth";

interface RouteView {
  key: string;
  activeSection: InventorySection;
  breadcrumbs: BreadcrumbPart[];
  content: ReactNode;
  notFound?: boolean;
}

function App() {
  const [hash, setHash] = useState(() => window.location.hash || "#/login");
  const [routeTransitionKey, setRouteTransitionKey] = useState(0);

  useEffect(() => {
    function handleHashChange() {
      setHash(window.location.hash || "#/login");
      setRouteTransitionKey((previous) => previous + 1);
    }

    window.addEventListener("hashchange", handleHashChange);
    return () => window.removeEventListener("hashchange", handleHashChange);
  }, []);

  async function handleLogout() {
    await logout();
    window.location.hash = "#/login";
  }

  const role = getUserRoleFromToken();
  const authenticated = isAuthenticated();
  const normalizedHash = hash || "#/login";
  const effectiveHash = !authenticated
    ? "#/login"
    : normalizedHash === "#/login"
      ? "#/inventory/categories"
      : normalizedHash;

  useEffect(() => {
    if (!authenticated && normalizedHash !== "#/login") {
      clearSession();
      window.location.hash = "#/login";
      return;
    }
    if (authenticated && normalizedHash === "#/login") {
      window.location.hash = "#/inventory/categories";
      return;
    }
  }, [authenticated, normalizedHash]);

  const routeView = useMemo<RouteView>(() => {
    const normalizedHash = effectiveHash;

    if ((role === "DIRECTOR" || role === "DOCENTE") && normalizedHash.startsWith("#/inventory")) {
      return {
        key: "role-home",
        activeSection: "items",
        breadcrumbs: [{ label: "Panel" }, { label: role }],
        content: (
          <section className="panel">
            <div className="content-header"><h1>Panel {role}</h1></div>
            <p className="text-muted">Tu acceso esta activo. Las vistas especializadas por rol quedaran habilitadas en los siguientes incrementos.</p>
          </section>
        ),
      };
    }

    const itemDetailMatch = normalizedHash.match(/^#\/inventory\/(?:implementos|items)\/(\d+)$/);
    if (itemDetailMatch) {
      const implementId = Number(itemDetailMatch[1]);
      if (Number.isFinite(implementId)) {
        return {
          key: `detail-${implementId}`,
          activeSection: "items",
          breadcrumbs: [
            { label: "Inventario", href: "#/inventory/implementos" },
            { label: "Implementos", href: "#/inventory/implementos" },
            { label: "Detalle" },
          ],
          content: <InventoryItemDetailPage implementId={implementId} embedded />,
        };
      }
    }

    if (normalizedHash.startsWith("#/inventory/implementos") || normalizedHash.startsWith("#/inventory/items")) {
      return { key: "items", activeSection: "items", breadcrumbs: [{ label: "Inventario" }, { label: "Implementos" }], content: <InventoryItemsPage embedded /> };
    }
    if (normalizedHash.startsWith("#/inventory/locations")) {
      return { key: "locations", activeSection: "locations", breadcrumbs: [{ label: "Inventario" }, { label: "Ubicaciones" }], content: <InventoryLocationsPage embedded /> };
    }
    if (normalizedHash.startsWith("#/inventory/moves")) {
      return { key: "moves", activeSection: "moves", breadcrumbs: [{ label: "Inventario" }, { label: "Movimientos" }], content: <InventoryMovesPage embedded /> };
    }
    if (normalizedHash.startsWith("#/inventory/categories")) {
      return { key: "categories", activeSection: "categories", breadcrumbs: [{ label: "Inventario" }, { label: "Categorias" }], content: <InventoryCategoriesPage embedded /> };
    }

    return {
      key: "404",
      activeSection: "items",
      breadcrumbs: [{ label: "Error" }, { label: "404" }],
      content: <NotFoundPage />,
      notFound: true,
    };
  }, [effectiveHash, role]);

  if (effectiveHash === "#/login") {
    return <LoginPage />;
  }

  return (
    <InventoryLayout activeSection={routeView.activeSection} breadcrumbs={routeView.breadcrumbs} onLogout={handleLogout} userName="Usuario" userRole={role}>
      <div key={`${routeView.key}-${routeTransitionKey}`} className="route-transition">
        {routeView.content}
      </div>
    </InventoryLayout>
  );
}

export default App;
