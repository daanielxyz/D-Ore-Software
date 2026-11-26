import { useEffect, useState } from 'react'
import { inventoryApi, type Product } from '../inventory/api'
import { day, expiresSoon, money, unitNames } from '../inventory/format'

export function SummaryPage() {
  const [products, setProducts] = useState<Product[] | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    inventoryApi
      .list(false)
      .then(setProducts)
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
      <div className="cards">
        {card('Productos activos', products.length)}
        {card('Valor del inventario (al costo)', money(value))}
        {card('Con stock bajo', low.length)}
        {card('Con flor vencida', expired.length)}
      </div>
      <div className="columns">
        {section('Hay que reponer', low, (p) => `${p.stock - p.expiredStock} de mínimo ${p.minStock} ${unitNames[p.unit]}`)}
        {section('Vence pronto', soon, (p) => (p.nextExpiry ? `el ${day(p.nextExpiry)}` : ''))}
        {section('Dar de baja', expired, (p) => `${p.expiredStock} ${unitNames[p.unit]} vencidos`)}
      </div>
    </>
  )
}
