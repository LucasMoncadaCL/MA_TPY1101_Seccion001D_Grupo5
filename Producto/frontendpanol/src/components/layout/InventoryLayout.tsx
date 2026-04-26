import { Bell, Boxes, ClipboardList, ClipboardPlus, MapPin, PackageSearch, Settings } from "lucide-react";
import type { ReactNode } from "react";

const menuInventory = [
  { label: "Resumen", icon: PackageSearch },
  { label: "Ítems", icon: Boxes },
  { label: "Categorías", icon: ClipboardList, active: true },
  { label: "Ubicaciones", icon: MapPin },
  { label: "Movimientos", icon: ClipboardPlus },
];

const menuConfig = [
  { label: "Unidades de medida", icon: Settings },
  { label: "Atributos de ítems", icon: Settings },
  { label: "Historial de cambios", icon: Settings },
];

export function Sidebar() {
  return (
    <aside className="sidebar">
      <section>
        <h3 className="sidebar__title">Inventario</h3>
        <ul className="sidebar__list">
          {menuInventory.map((item) => {
            const Icon = item.icon;
            return (
              <li
                key={item.label}
                className={item.active ? "sidebar__item sidebar__item--active" : "sidebar__item"}
              >
                <Icon size={18} />
                <span>{item.label}</span>
              </li>
            );
          })}
        </ul>
      </section>

      <section>
        <h3 className="sidebar__title">Configuración</h3>
        <ul className="sidebar__list">
          {menuConfig.map((item) => {
            const Icon = item.icon;
            return (
              <li key={item.label} className="sidebar__item">
                <Icon size={18} />
                <span>{item.label}</span>
              </li>
            );
          })}
        </ul>
      </section>

      <section className="sidebar__help">
        <h4>¿Necesitas ayuda?</h4>
        <p>Revisa la guía rápida de inventario para coordinadores.</p>
        <button type="button" className="button button--ghost">
          Ver guía
        </button>
      </section>
    </aside>
  );
}

export function TopBar() {
  return (
    <header className="topbar">
      <div className="topbar__brand">
        <div className="topbar__logo">P</div>
        <div>
          <strong>Pañolero</strong>
          <p>Escuela de Salud</p>
        </div>
      </div>

      <nav className="topbar__menu">
        <a href="#">Inicio</a>
        <a href="#" className="is-active">
          Inventario
        </a>
        <a href="#">Préstamos</a>
        <a href="#">Reportes</a>
      </nav>

      <div className="topbar__user">
        <button type="button" className="topbar__icon">
          <Bell size={18} />
        </button>
        <div className="topbar__avatar">FJ</div>
        <div>
          <strong>Francisco Jiménez</strong>
          <p>Coordinador de Laboratorio</p>
        </div>
      </div>
    </header>
  );
}

export function InventoryLayout({ children }: { children: ReactNode }) {
  return (
    <div className="app-shell">
      <TopBar />
      <div className="app-shell__content">
        <Sidebar />
        <main className="app-shell__main">{children}</main>
      </div>
    </div>
  );
}
