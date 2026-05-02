import { useEffect, useMemo, useState, type ReactNode } from "react";
import { InventoryLayout, type BreadcrumbPart, type InventorySection } from "./components/layout/InventoryLayout";
import { InventoryCategoriesPage } from "./pages/InventoryCategoriesPage";
import { InventoryItemDetailPage } from "./pages/InventoryItemDetailPage";
import { InventoryItemsPage } from "./pages/InventoryItemsPage";
import { InventoryLocationsPage } from "./pages/InventoryLocationsPage";
import { InventoryMovesPage } from "./pages/InventoryMovesPage";

interface RouteView {
  key: string;
  activeSection: InventorySection;
  breadcrumbs: BreadcrumbPart[];
  content: ReactNode;
}

function App() {
  const [hash, setHash] = useState(() => window.location.hash);
  const [routeTransitionKey, setRouteTransitionKey] = useState(0);

  useEffect(() => {
    function handleHashChange() {
      setHash(window.location.hash);
      setRouteTransitionKey((previous) => previous + 1);
    }

    window.addEventListener("hashchange", handleHashChange);
    return () => window.removeEventListener("hashchange", handleHashChange);
  }, []);

  const routeView = useMemo<RouteView>(() => {
    const normalizedHash = hash || "#/inventory/categories";
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
      return {
        key: "items",
        activeSection: "items",
        breadcrumbs: [{ label: "Inventario" }, { label: "Implementos" }],
        content: <InventoryItemsPage embedded />,
      };
    }

    if (normalizedHash.startsWith("#/inventory/locations")) {
      return {
        key: "locations",
        activeSection: "locations",
        breadcrumbs: [{ label: "Inventario" }, { label: "Ubicaciones" }],
        content: <InventoryLocationsPage embedded />,
      };
    }

    if (normalizedHash.startsWith("#/inventory/moves")) {
      return {
        key: "moves",
        activeSection: "moves",
        breadcrumbs: [{ label: "Inventario" }, { label: "Movimientos" }],
        content: <InventoryMovesPage embedded />,
      };
    }

    return {
      key: "categories",
      activeSection: "categories",
      breadcrumbs: [{ label: "Inventario" }, { label: "Categorias" }],
      content: <InventoryCategoriesPage embedded />,
    };
  }, [hash]);

  return (
    <InventoryLayout activeSection={routeView.activeSection} breadcrumbs={routeView.breadcrumbs}>
      <div key={`${routeView.key}-${routeTransitionKey}`} className="route-transition">
        {routeView.content}
      </div>
    </InventoryLayout>
  );
}

export default App;
