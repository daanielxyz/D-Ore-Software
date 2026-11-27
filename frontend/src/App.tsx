import { useEffect, useRef, useState, type ComponentType, type MouseEvent } from 'react'
import { Icon } from './components/icons'
import { Modal } from './components/Modal'
import { NavMenu } from './components/NavMenu'
import type { PageId } from './components/navigation'
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

const pages: Record<PageId, ComponentType> = {
  resumen: SummaryPage,
  asistente: AssistantPage,
  pedidos: OrdersPage,
  inventario: InventoryPage,
  clientes: CustomersPage,
  ventas: SalesPage,
  finanzas: FinancePage,
  analitica: AnalyticsPage,
  contenido: ContentPage,
  escucha: ListeningPage,
}

const currentPage = (): PageId => {
  const id = window.location.hash.slice(1)
  return id in pages ? (id as PageId) : 'resumen'
}

type Session = 'checking' | 'in' | 'out' | 'offline'

export default function App() {
  const [session, setSession] = useState<Session>('checking')
  const [user, setUser] = useState('')
  const [page, setPage] = useState<PageId>(currentPage)
  const [leaving, setLeaving] = useState(false)
  const drawer = useRef<HTMLDialogElement>(null)
  const content = useRef<HTMLElement>(null)

  function check() {
    authApi
      .me()
      .then((me) => {
        setUser(me.name)
        setSession('in')
      })
      .catch((reason: unknown) => setSession(reason instanceof SessionExpired ? 'out' : 'offline'))
  }

  useEffect(() => {
    const onHash = () => {
      setPage(currentPage())
      drawer.current?.close()
      content.current?.focus({ preventScroll: true })
      window.scrollTo(0, 0)
    }
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

  const Page = pages[page]
  const initials = user.slice(0, 2).toUpperCase()
  const closeDrawer = () => drawer.current?.close()
  const logout = () => {
    setLeaving(false)
    void authApi.logout().then(() => setSession('out'))
  }
  // El enlace no cambia el hash: la navegación depende de él.
  const skip = (event: MouseEvent) => {
    event.preventDefault()
    content.current?.focus()
  }

  return (
    <div className="app">
      <a className="skip" href="#contenido" onClick={skip}>Saltar al contenido</a>
      <header className="topbar">
        <button className="icon-button menu-toggle" aria-label="Abrir menú" onClick={() => drawer.current?.showModal()}>
          <Icon name="menu" />
        </button>
        <span className="logo-plate">
          <img src="/logo.webp" alt="" width={640} height={531} />
        </span>
        <span className="divider" aria-hidden="true" />
        <span className="business">D'Orée Floristería</span>
        <div className="account">
          <span className="avatar" aria-hidden="true">{initials}</span>
          <span className="user-name">{user}</span>
          <button className="icon-button logout" aria-label="Cerrar sesión" title="Cerrar sesión" onClick={() => setLeaving(true)}>
            <Icon name="salir" />
          </button>
        </div>
      </header>

      <nav className="sidebar" aria-label="Principal">
        <NavMenu current={page} />
      </nav>

      <dialog ref={drawer} className="drawer" aria-label="Menú" onClick={(e) => e.target === e.currentTarget && closeDrawer()}>
        <div className="drawer-body">
          <div className="drawer-head">
            <span className="avatar" aria-hidden="true">{initials}</span>
            <strong>{user}</strong>
            <button className="icon-button" aria-label="Cerrar menú" onClick={closeDrawer}>
              <Icon name="cerrar" />
            </button>
          </div>
          <nav aria-label="Principal">
            <NavMenu current={page} onSelect={closeDrawer} />
          </nav>
          <button onClick={() => { closeDrawer(); setLeaving(true) }}>Cerrar sesión</button>
        </div>
      </dialog>

      <main ref={content} tabIndex={-1}>
        <div className="content">
          <Page />
        </div>
      </main>

      {leaving && (
        <Modal title="Cerrar sesión" onClose={() => setLeaving(false)}>
          <p>¿Quieres salir de tu cuenta?</p>
          <div className="dialog-actions">
            <button onClick={() => setLeaving(false)}>Seguir aquí</button>
            <button className="primary" onClick={logout}>Cerrar sesión</button>
          </div>
        </Modal>
      )}
    </div>
  )
}
