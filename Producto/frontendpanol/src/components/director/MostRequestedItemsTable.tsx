import { MoreVertical, Stethoscope } from "lucide-react";
import type { MostRequestedItemRow } from "./directorMockData";

function stockDotClass(tone: MostRequestedItemRow["stockTone"]) {
  if (tone === "ok") return "director-dot director-dot--ok";
  if (tone === "warn") return "director-dot director-dot--warn";
  return "director-dot director-dot--critical";
}

export function MostRequestedItemsTable({ rows }: { rows: MostRequestedItemRow[] }) {
  const maxRequests = Math.max(...rows.map((row) => row.requests), 1);

  return (
    <section className="panel director-panel">
      <div className="panel__head"><h2>Implementos con mas movimientos</h2></div>
      <div className="table-wrapper">
        <table className="category-table">
          <thead>
            <tr>
              <th>Implemento</th>
              <th>Movimientos</th>
              <th>Rechazos</th>
              <th>Stock disponible</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.implement}>
                <td>
                  <div className="director-user-cell">
                    <span className="director-implement-icon"><Stethoscope size={14} /></span>
                    <span>{row.implement}</span>
                  </div>
                </td>
                <td>
                  <div className="director-request-cell">
                    <span>{row.requests}</span>
                    <span className="director-request-bar"><span style={{ width: `${Math.round((row.requests / maxRequests) * 100)}%` }} /></span>
                  </div>
                </td>
                <td className="director-reject">{row.rejects}</td>
                <td><span className={stockDotClass(row.stockTone)} aria-hidden="true" /> {row.stock}</td>
                <td><button type="button" className="director-menu-btn" aria-label={`Ver opciones de ${row.implement}`}><MoreVertical size={14} /></button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
