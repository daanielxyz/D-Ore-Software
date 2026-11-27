import { useEffect, useState } from 'react'
import { Icon } from '../../components/icons'
import { navGroups } from '../../components/navigation'
import { inventoryApi, type Product } from '../inventory/api'
import { day, expiresSoon, money, today, unitNames } from '../inventory/format'
import { ordersApi, statusNames, type Order } from '../orders/api'
import { salesApi, type Sale } from '../sales/api'

export function SummaryPage() {
  const [products, setProducts] = useState<Product[] | null>(null)
  const [orders, setOrders] = useState<Order[]>([])
  const [sales, setSales] = useState<Sale[]>([])
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([inventoryApi.list(false), ordersApi.list(), salesApi.list()])
      .then(([productList, orderList, saleList]) => {
        setProducts(productList)
        setOrders(orderList)
        setSales(saleList)
      })
      .catch((reason: unknown) =>
        setError(reason instanceof Error ? reason.message : 'Error inesperado.'),
      )
  }, [])

  if (error) return <p className="error" role="alert">{error}</p>
  if (!products) return <p>Cargando…</p>

  const low = products.filter((p) => p.lowStock)
  const expired = products.filter((p) => p.expiredStock > 0)
  const soon = products.filter(expiresSoon)
  // Lo vencido ya no cuenta como valor del inventario.
  const value = products.reduce((sum, p) => sum + (p.stock - p.expiredStock) * p.unitCost, 0)

  const open = orders
    .filter((o) => o.status !== 'DELIVERED' && o.status !== 'CANCELLED')
    .sort((a, b) => a.deliveryDate.localeCompare(b.deliveryDate))
  const month = today().slice(0, 7)
  const monthSales = sales
    .filter((s) => s.saleDate.startsWith(month))
    .reduce((sum, s) => sum + s.amount, 0)

  const card = (label: string, content: string | number) => (
    <div className="panel stat">
      <strong>{content}</strong>
      <span>{label}</span>
    </div>
  )

  const section = (title: string, items: Product[], detail: (p: Product) => string) => (
    <section className="panel">
      <h2>{title}</h2>
      {items.length === 0 && <p className="muted">Nada pendiente.</p>}
      <ul className="list">
        {items.map((p) => (
          <li key={p.id}>
            <span>{p.name}</span>
            <span className="muted">{detail(p)}</span>
          </li>
        ))}
      </ul>
    </section>
  )

  return (
    <>
      <h1>Resumen</h1>
      <p className="greeting">Hola. Esto es lo que pasa hoy en el taller.</p>
      <div className="cards">
        {card('Ventas del mes', money(monthSales))}
        {card('Pedidos por entregar', open.length)}
        {card('Productos activos', products.length)}
        {card('Valor del inventario (al costo)', money(value))}
        {card('Con stock bajo', low.length)}
        {card('Con flor vencida', expired.length)}
      </div>
      <div className="columns">
        <section className="panel">
          <h2>Próximas entregas</h2>
          {open.length === 0 && <p className="muted">Nada pendiente.</p>}
          <ul className="list">
            {open.slice(0, 5).map((o) => (
              <li key={o.id}>
                <span>{o.customerName}</span>
                <span className="muted">{day(o.deliveryDate)} · {statusNames[o.status]}</span>
              </li>
            ))}
          </ul>
        </section>
        {section('Hay que reponer', low, (p) => `${p.stock - p.expiredStock} de mínimo ${p.minStock} ${unitNames[p.unit]}`)}
        {section('Vence pronto', soon, (p) => (p.nextExpiry ? `el ${day(p.nextExpiry)}` : ''))}
        {section('Dar de baja', expired, (p) => `${p.expiredStock} ${unitNames[p.unit]} vencidos`)}
      </div>
      <h2 className="access-title">Ir a</h2>
      <div className="access">
        {navGroups
          .flatMap((group) => [...group.items])
          .filter((item) => item.id !== 'resumen')
          .map((item) => (
            <a key={item.id} href={`#${item.id}`}>
              <Icon name={item.id} />
              <strong>{item.title}</strong>
              <small>{item.description}</small>
            </a>
          ))}
      </div>
    </>
  )
}
