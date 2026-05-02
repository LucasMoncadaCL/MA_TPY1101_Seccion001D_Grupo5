import { useEffect, useMemo, useState } from "react";
import { InventoryLayout } from "../components/layout/InventoryLayout";
import { getApiErrorPayload, getErrorMessage } from "../services/apiClient";
import { fetchImplementById, fetchImplements } from "../services/implementService";
import { registerManualMovement, type ManualMovementType } from "../services/movementService";
import type { ImplementDetail, ImplementSummary, InventoryMovementDetail } from "../types/implement";
import { getUserRoleFromToken } from "../utils/auth";
import { Button } from "../components/ui/Button";
import { Select } from "../components/ui/Select";
import { Input } from "../components/ui/Input";
import { Card } from "../components/ui/Card";
import { Badge } from "../components/ui/Badge";
import { Table } from "../components/ui/Table";

const ACTION_LABELS: Record<string, string> = {
  INGRESO: "Ingreso",
  AJUSTE: "Ajuste",
  RESERVA: "Reserva",
  LIBERACION: "Liberación",
  PRESTAMO: "Préstamo",
  DEVOLUCION: "Devolución",
};

export function InventoryMovesPage({ embedded = false }: { embedded?: boolean }) {
  const [implementsList, setImplementsList] = useState<ImplementSummary[]>([]);
  const [selectedImplementId, setSelectedImplementId] = useState<string>("");
  const [detail, setDetail] = useState<ImplementDetail | null>(null);
  const [loadingList, setLoadingList] = useState(false);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [search, setSearch] = useState("");

  const [action, setAction] = useState<ManualMovementType>("INGRESO");
  const [quantity, setQuantity] = useState("1");
  const [notes, setNotes] = useState("");
  const role = getUserRoleFromToken();
  const canCreateMovement = role === "COORDINADOR";

  useEffect(() => {
    setLoadingList(true);
    setError(null);
    fetchImplements()
      .then((list) => {
        setImplementsList(list);
        if (list.length > 0) {
          setSelectedImplementId(String(list[0].id));
        }
      })
      .catch((requestError) => setError(getErrorMessage(requestError, "No se pudo cargar implementos.")))
      .finally(() => setLoadingList(false));
  }, []);

  useEffect(() => {
    if (!selectedImplementId) {
      setDetail(null);
      return;
    }
    const id = Number(selectedImplementId);
    if (!Number.isFinite(id)) return;
    setLoadingDetail(true);
    fetchImplementById(id)
      .then((d) => setDetail(d))
      .catch((requestError) => setError(getErrorMessage(requestError, "No se pudo cargar los movimientos.")))
      .finally(() => setLoadingDetail(false));
  }, [selectedImplementId]);

  const filteredMovements = useMemo(() => {
    const movements = detail?.recent_movements ?? [];
    const q = search.trim().toLowerCase();
    if (!q) return movements;
    return movements.filter((m) => `${m.action} ${m.notes ?? ""} ${m.performed_by}`.toLowerCase().includes(q));
  }, [detail?.recent_movements, search]);

  const moveStats = useMemo(() => {
    const source = detail?.recent_movements ?? [];
    return {
      total: source.length,
      ingresos: source.filter((m) => m.action === "INGRESO").length,
      ajustes: source.filter((m) => m.action === "AJUSTE").length,
    };
  }, [detail?.recent_movements]);

  async function submitMovement() {
    if (!detail) return;
    const qty = Number(quantity);
    if (!Number.isFinite(qty) || qty <= 0 || !Number.isInteger(qty)) {
      setError("La cantidad debe ser un entero positivo.");
      return;
    }
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      await registerManualMovement(detail.id, {
        action,
        quantity: qty,
        notes: notes.trim() ? notes.trim() : null,
      });
      const updated = await fetchImplementById(detail.id);
      setDetail(updated);
      setSuccess("Movimiento registrado correctamente.");
      setQuantity("1");
      setNotes("");
    } catch (requestError) {
      const payload = getApiErrorPayload(requestError);
      setError(payload?.message ?? getErrorMessage(requestError, "No se pudo registrar movimiento."));
    } finally {
      setSaving(false);
    }
  }

  const content = (
    <>
      <section className="content-header">
        <div>
          <h1>Movimientos</h1>
          <p>Historial de movimientos de inventario almacenados en MongoDB.</p>
        </div>
      </section>

      <section className="stat-grid">
        <article className="stat-card stat-card--blue">
          <p>Movimientos totales</p>
          <strong>{moveStats.total}</strong>
        </article>
        <article className="stat-card stat-card--green">
          <p>Ingresos</p>
          <strong>{moveStats.ingresos}</strong>
        </article>
        <article className="stat-card stat-card--orange">
          <p>Ajustes</p>
          <strong>{moveStats.ajustes}</strong>
        </article>
        <article className="stat-card stat-card--cyan">
          <p>Implemento activo</p>
          <strong>{detail?.name ?? "-"}</strong>
        </article>
      </section>

      <section className="panel">
        {error ? <div className="error-banner">{error}</div> : null}
        {success ? <div className="success-banner">{success}</div> : null}
        <div className="catalog-filters">
          <div className="catalog-filters__item">
            <label>Implemento</label>
            <Select
              value={selectedImplementId}
              onChange={(e) => setSelectedImplementId(e.target.value)}
              disabled={loadingList || implementsList.length === 0}
            >
              {implementsList.map((item) => (
                <option key={item.id} value={String(item.id)}>
                  {item.name}
                </option>
              ))}
            </Select>
          </div>
          <div className="catalog-filters__item">
            <label>Buscar en historial</label>
            <Input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Acción, notas o usuario" />
          </div>
        </div>

        {canCreateMovement && detail ? (
          <Card className="inventory-motion-card" >
            <h3>Registrar movimiento manual</h3>
            <div className="stock-actions-grid">
              <div>
                <label>Acción</label>
                <Select value={action} onChange={(e) => setAction(e.target.value as ManualMovementType)}>
                  <option value="INGRESO">Ingreso</option>
                  <option value="AJUSTE">Ajuste</option>
                </Select>
              </div>
              <div>
                <label>Cantidad</label>
                <Input value={quantity} onChange={(e) => setQuantity(e.target.value)} type="number" min={1} />
              </div>
            </div>
            <label>Notas</label>
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Opcional"
              style={{ width: "100%", marginTop: 6 }}
            />
            <div className="modal-actions">
              <Button onClick={() => void submitMovement()} disabled={saving}>
                {saving ? "Guardando..." : "Registrar movimiento"}
              </Button>
            </div>
          </Card>
        ) : null}

        <Table>
            <thead>
              <tr>
                <th>Fecha</th>
                <th>Acción</th>
                <th>Cantidad</th>
                <th>Usuario</th>
                <th>Notas</th>
              </tr>
            </thead>
            <tbody>
              {loadingDetail ? (
                <tr>
                  <td colSpan={5} className="table-hint">Cargando movimientos...</td>
                </tr>
              ) : filteredMovements.length === 0 ? (
                <tr>
                  <td colSpan={5} className="table-hint">No hay movimientos para mostrar.</td>
                </tr>
              ) : (
                filteredMovements.map((m: InventoryMovementDetail) => (
                  <tr key={m.id} className="table-row-hover">
                    <td>{new Date(m.timestamp).toLocaleString()}</td>
                    <td>
                      <Badge tone={m.action === "INGRESO" ? "active" : m.action === "AJUSTE" ? "warn" : "inactive"}>
                        {ACTION_LABELS[m.action] ?? m.action}
                      </Badge>
                    </td>
                    <td>{m.quantity}</td>
                    <td>{m.performed_by}</td>
                    <td>{m.notes ?? "-"}</td>
                  </tr>
                ))
              )}
            </tbody>
        </Table>
      </section>
    </>
  );

  if (embedded) {
    return content;
  }

  return <InventoryLayout activeSection="moves">{content}</InventoryLayout>;
}
