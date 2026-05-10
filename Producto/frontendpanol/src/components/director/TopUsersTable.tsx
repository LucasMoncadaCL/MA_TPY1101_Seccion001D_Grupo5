import type { TopUserRow } from "./directorMockData";

function delayTone(delays: number) {
  if (delays === 0) return "ok";
  if (delays <= 2) return "warn";
  return "critical";
}

export function TopUsersTable({ rows }: { rows: TopUserRow[] }) {
  return (
    <section className="panel director-panel">
      <div className="panel__head"><h2>Usuarios con mas movimientos</h2></div>
      <div className="table-wrapper">
        <table className="category-table">
          <thead>
            <tr>
              <th>Usuario</th>
              <th>Rol</th>
              <th>Movimientos</th>
              <th>Atrasos estimados</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.name}>
                <td>
                  <div className="director-user-cell">
                    <span className="director-avatar" aria-hidden="true">{row.name.split(" ").map((x) => x[0]).slice(0, 2).join("")}</span>
                    <span>{row.name}</span>
                  </div>
                </td>
                <td><span className="badge badge--inactive">{row.role}</span></td>
                <td>{row.requests}</td>
                <td><span className={`director-delay director-delay--${delayTone(row.delays)}`}>{row.delays}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
