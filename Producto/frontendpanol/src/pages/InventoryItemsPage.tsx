import { useCallback, useEffect, useState } from "react";
import { Plus } from "lucide-react";
import { InventoryLayout } from "../components/layout/InventoryLayout";
import { ImplementFormModal } from "../components/implements/ImplementFormModal";
import { ImplementEditModal } from "../components/implements/ImplementEditModal";
import { createImplement, fetchImplements, updateImplement } from "../services/implementService";
import { getErrorMessage } from "../services/apiClient";
import type { ImplementSummary } from "../types/implement";

export function InventoryItemsPage() {
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editing, setEditing] = useState<ImplementSummary | null>(null);
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [implementos, setImplementos] = useState<ImplementSummary[]>([]);

  const refreshImplements = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const rows = await fetchImplements();
      setImplementos(rows);
    } catch (error) {
      setError(getErrorMessage(error, "No se pudo cargar el listado de implementos."));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshImplements();
  }, [refreshImplements]);

  async function handleSubmit(payload: {
    name: string;
    categoryId: number;
    itemType: "consumable" | "reusable" | "individual";
    locationId: number;
    description: string | null;
    minStock: number;
    observations: string | null;
  }) {
    setSaving(true);
    setError(null);
    setSuccess(null);

    try {
      const created = await createImplement({
        name: payload.name,
        category_id: payload.categoryId,
        item_type: payload.itemType,
        location_id: payload.locationId,
        description: payload.description,
        min_stock: payload.minStock,
        observations: payload.observations,
      });
      try {
        window.sessionStorage.setItem("inventory.justCreatedImplementId", String(created.id));
      } catch {
        // Si el storage no esta disponible, el flujo principal de creacion debe continuar.
      }
      setIsCreateOpen(false);
      window.location.hash = `#/inventory/implementos/${created.id}`;
    } catch (error) {
      throw error;
    } finally {
      setSaving(false);
    }
  }

  async function handleUpdate(payload: { id: number; name: string; categoryId: number | null; locationId: number }) {
    setSaving(true);
    setError(null);
    setSuccess(null);

    try {
      await updateImplement(payload.id, {
        name: payload.name,
        category_id: payload.categoryId,
        location_id: payload.locationId,
      });
      await refreshImplements();
      setSuccess("Implemento actualizado correctamente.");
      setEditing(null);
    } catch (error) {
      setError(getErrorMessage(error, "No se pudo actualizar el implemento."));
    } finally {
      setSaving(false);
    }
  }

  return (
    <InventoryLayout activeSection="items">
      <section className="content-header">
        <div>
          <h1>Inventario</h1>
          <p>Alta de producto.</p>
        </div>

        <div className="content-header__actions">
          <button type="button" className="button" onClick={() => setIsCreateOpen(true)}>
            <Plus size={16} />
            Nuevo implemento
          </button>
        </div>
      </section>

      <section className="panel">
        <div className="panel__head">
          <div>
            <h2>Implementos</h2>
            <p>Usa "Nuevo implemento" para crear un producto con categoria y ubicacion obligatorias.</p>
          </div>
        </div>

        {loading ? <div className="field-hint">Cargando implementos...</div> : null}
        {error ? <div className="error-banner">{error}</div> : null}
        {success ? <div className="success-banner">{success}</div> : null}

        <div className="table-wrapper">
          <table className="category-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nombre</th>
                <th>Categoria</th>
                <th>Ubicacion</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {implementos.map((row) => (
                <tr key={row.id}>
                  <td>{row.id}</td>
                  <td>{row.name}</td>
                  <td>
                    {row.category
                      ? `${row.category.name}${row.category.active ? "" : " [Categoria inactiva]"}`
                      : "Sin categoria"}
                  </td>
                  <td>{row.location ? row.location.name : "Sin ubicacion"}</td>
                  <td>
                    <div className="table-actions">
                      <button
                        type="button"
                        className="button button--table button--ghost"
                        onClick={() => setEditing(row)}
                      >
                        Editar
                      </button>
                      <a className="button button--table button--ghost" href={`#/inventory/implementos/${row.id}`}>
                        Ver ficha
                      </a>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <ImplementFormModal
        isOpen={isCreateOpen}
        saving={saving}
        onClose={() => setIsCreateOpen(false)}
        onSubmit={handleSubmit}
      />

      <ImplementEditModal
        implement={editing}
        isOpen={Boolean(editing)}
        saving={saving}
        onClose={() => setEditing(null)}
        onSubmit={handleUpdate}
      />
    </InventoryLayout>
  );
}
