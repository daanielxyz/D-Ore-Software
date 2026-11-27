import { useState } from 'react'
import { Modal } from '../../components/Modal'
import { useLoad } from '../../components/useLoad'
import { day, money, unitNames } from '../inventory/format'
import { ordersApi, statusNames, type Order, type OrderStatus } from './api'
import { OrderForm } from './OrderForm'

const flow: OrderStatus[] = ['PENDING', 'IN_DESIGN', 'READY', 'ON_THE_WAY', 'DELIVERED']

export function OrdersPage() {
  const { data, error, reload } = useLoad(ordersApi.list)
  const [creating, setCreating] = useState(false)
  const [actionError, setActionError] = useState('')
  const [busy, setBusy] = useState(false)

  async function change(order: Order, status: OrderStatus) {
    const warning =
      status === 'CANCELLED'
        ? `¿Cancelar ${order.code}?${order.stockDeducted ? ' Las flores ya descontadas no vuelven al inventario.' : ''}`
        : status === 'DELIVERED'
          ? `¿Marcar ${order.code} como entregado? Se registrará la venta.`
          : ''
    if (warning && !window.confirm(warning)) return
    setBusy(true)
    setActionError('')
    try {
      await ordersApi.changeStatus(order.id, status)
    } catch (reason) {
      setActionError(reason instanceof Error ? reason.message : 'Error inesperado.')
    }
    await reload()
    setBusy(false)
  }

  if (error) return <p className="error" role="alert">{error}</p>
  if (!data) return <p>Cargando…</p>

  return (
    <>
      <div className="page-head">
        <h1>Pedidos</h1>
        <button className="primary" onClick={() => setCreating(true)}>+ Nuevo pedido</button>
      </div>
      <p className="muted">El inventario se descuenta cuando el pedido pasa a "En diseño".</p>
      {actionError && <p className="error" role="alert">{actionError}</p>}
      <div className="panel scroll">
        {data.length === 0 ? (
          <p className="muted">Aún no hay pedidos.</p>
        ) : (
          <table>
            <thead>
              <tr><th>Pedido</th><th>Cliente</th><th>Contenido</th><th>Entrega</th><th>Total</th><th>Estado</th></tr>
            </thead>
            <tbody>
              {data.map((order) => {
                const closed = order.status === 'DELIVERED' || order.status === 'CANCELLED'
                return (
                  <tr key={order.id}>
                    <td>{order.code}<small>{order.occasion}</small></td>
                    <td>{order.customerName}<small>{order.notes}</small></td>
                    <td>
                      {order.items.map((item) => (
                        <div key={item.productId}>
                          {item.quantity} {unitNames[item.unit]} · {item.productName}
                        </div>
                      ))}
                    </td>
                    <td>{day(order.deliveryDate)}</td>
                    <td>{money(order.total)}</td>
                    <td>
                      {closed ? (
                        <span className={order.status === 'DELIVERED' ? 'tag ok' : 'tag'}>{statusNames[order.status]}</span>
                      ) : (
                        <select
                          aria-label={`Estado de ${order.code}`}
                          value={order.status}
                          disabled={busy}
                          onChange={(e) => void change(order, e.target.value as OrderStatus)}
                        >
                          {[...flow.slice(flow.indexOf(order.status)), 'CANCELLED' as const].map((s) => (
                            <option key={s} value={s}>{statusNames[s]}</option>
                          ))}
                        </select>
                      )}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        )}
      </div>
      {creating && (
        <Modal title="Nuevo pedido" onClose={() => setCreating(false)}>
          <OrderForm
            onDone={() => {
              setCreating(false)
              void reload()
            }}
          />
        </Modal>
      )}
    </>
  )
}
