import { useEffect, useMemo, useState } from "react";
import { InventoryLayout } from "../components/layout/InventoryLayout";
import { getErrorMessage } from "../services/apiClient";
import { fetchImplementById, fetchImplements } from "../services/implementService";
import type { ImplementDetail, ImplementSummary } from "../types/implement";

const ITEM_TYPE_LABELS: Record<"consumable" | "reusable" | "individual", string> = {
  consumable: "Consumible",
  reusable: "Reutilizable",
  individual: "Individual",
};

export function InventoryItemDetailPage({ implementId }: { implementId: number }) {
  const [implement, setImplement] = useState<ImplementDetail | null>(null);
  const [catalog, setCatalog] = useState<ImplementSummary[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    setError(null);

    Promise.all([fetchImplementById(implementId), fetchImplements()])
      .then(([detail, items]) => {
        setImplement(detail);
        setCatalog(items);
      })
      .catch((requestError) => {
        setError(getErrorMessage(requestError, "No se pudo cargar la ficha del producto."));
      })
      .finally(() => setLoading(false));
  }, [implementId]);

  const summaryMatch = useMemo(
    () => catalog.find((row) => row.id === implementId) ?? null,
    [catalog, implementId],
  );

  const categoryLabel = useMemo(() => {
    if (!implement) {
      return "-";
    }
    if (summaryMatch?.category) {
      return `${summaryMatch.category.name}${summaryMatch.category.active ? "" : " [Inactiva]"}`;
    }
    if (implement.categoryId) {
      return `Categoria #${implement.categoryId}`;
    }
    return "Sin categoria";
  }, [implement, summaryMatch]);

  const locationLabel = useMemo(() => {
    if (!implement) {
      return "-";
    }
    if (summaryMatch?.location) {
      return summaryMatch.location.name;
    }
    if (implement.locationId) {
      return `Ubicacion #${implement.locationId}`;
    }
    return "Sin ubicacion";
  }, [implement, summaryMatch]);

  return (
    <InventoryLayout activeSection="items">
      <section className="content-header">
        <div>
          <h1>Ficha de producto</h1>
          <p>Detalle del implemento registrado.</p>
        </div>
        <div className="content-header__actions">
          <a className="button button--ghost" href="#/inventory/implementos">
            Volver al listado
          </a>
        </div>
      </section>

      <section className="panel">
        {loading ? <div className="field-hint">Cargando ficha...</div> : null}
        {error ? <div className="error-banner">{error}</div> : null}

        {implement && !loading ? (
          <div className="detail-grid">
            <article className="detail-card">
              <h2>{implement.name}</h2>
              <p>{implement.description ?? "Sin descripcion"}</p>
            </article>

            <article className="detail-card">
              <h3>Atributos</h3>
              <p>
                <strong>Categoria:</strong> {categoryLabel}
              </p>
              <p>
                <strong>Tipo:</strong>{" "}
                {implement.item_type ? ITEM_TYPE_LABELS[implement.item_type] : "Sin tipo"}
              </p>
              <p>
                <strong>Ubicacion:</strong> {locationLabel}
              </p>
              <p>
                <strong>Stock minimo:</strong>{" "}
                {implement.min_stock === null ? "No informado" : implement.min_stock}
              </p>
              <p>
                <strong>Observaciones:</strong> {implement.observations ?? "Sin observaciones"}
              </p>
            </article>
          </div>
        ) : null}
      </section>
    </InventoryLayout>
  );
}
