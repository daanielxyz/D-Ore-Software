import { useEffect, useState } from 'react'
import { AnalyticsPage } from './features/analytics/AnalyticsPage'
import { AssistantPage } from './features/assistant/AssistantPage'
import { authApi } from './features/auth/api'
import { LoginPage } from './features/auth/LoginPage'
import { ContentPage } from './features/content/ContentPage'
import { CustomersPage } from './features/customers/CustomersPage'
import { FinancePage } from './features/finance/FinancePage'
import { InventoryPage } from './features/inventory/InventoryPage'
import { ListeningPage } from './features/listening/ListeningPage'
import { OrdersPage } from './features/orders/OrdersPage'
import { SalesPage } from './features/sales/SalesPage'
import { SummaryPage } from './features/summary/SummaryPage'
import { SessionExpired } from './http'

const pages = {
  resumen: { title: 'Resumen', Page: SummaryPage },
  asistente: { title: 'Asistente', Page: AssistantPage },
  pedidos: { title: 'Pedidos', Page: OrdersPage },
  inventario: { title: 'Inventario', Page: InventoryPage },
  clientes: { title: 'Clientes', Page: CustomersPage },
  ventas: { title: 'Ventas', Page: SalesPage },
  finanzas: { title: 'Finanzas', Page: FinancePage },
  analitica: { title: 'Analítica', Page: AnalyticsPage },
  contenido: { title: 'Contenido', Page: ContentPage },
  escucha: { title: 'Social listening', Page: ListeningPage },
}
type PageId = keyof typeof pages

const currentPage = (): PageId => {
  const id = window.location.hash.slice(1)
  return id in pages ? (id as PageId) : 'resumen'
}

type Session = 'checking' | 'in' | 'out' | 'offline'

export default function App() {
  const [session, setSession] = useState<Session>('checking')
  const [page, setPage] = useState<PageId>(currentPage)

  function check() {
    authApi
      .me()
      .then(() => setSession('in'))
      .catch((reason: unknown) => setSession(reason instanceof SessionExpired ? 'out' : 'offline'))
  }

  useEffect(() => {
    const onHash = () => setPage(currentPage())
    const onExpired = () => setSession('out')
    window.addEventListener('hashchange', onHash)
    window.addEventListener('session-expired', onExpired)
    check()
    return () => {
      window.removeEventListener('hashchange', onHash)
      window.removeEventListener('session-expired', onExpired)
    }
  }, [])

  if (session === 'checking') return <p className="login">Cargando…</p>
  if (session === 'out') return <LoginPage onLogin={check} />
  if (session === 'offline')
    return (
      <div className="login">
        <p className="error" role="alert">No hay conexión con el servidor.</p>
        <button onClick={check}>Reintentar</button>
      </div>
    )

  const { Page } = pages[page]
  return (
    <div className="shell">
      <aside className="sidebar">
        <strong className="brand">D'Ore</strong>
        <nav>
          {Object.entries(pages).map(([id, { title }]) => (
            <a key={id} href={`#${id}`} className={id === page ? 'current' : ''} aria-current={id === page ? 'page' : undefined}>
              {title}
            </a>
          ))}
        </nav>
        <button className="logout" onClick={() => void authApi.logout().then(() => setSession('out'))}>
          Salir
        </button>
      </aside>
      <main>
        <Page />
      </main>
    </div>
  )
}
