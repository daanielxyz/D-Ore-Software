import { InventoryPage } from './features/inventory/InventoryPage'

export default function App() {
  return (
    <div className="shell">
      <aside className="sidebar">
        <strong className="brand">D'Ore</strong>
        <nav>
          <a className="current" href="/" aria-current="page">Inventario</a>
        </nav>
      </aside>
      <main>
        <InventoryPage />
      </main>
    </div>
  )
}
