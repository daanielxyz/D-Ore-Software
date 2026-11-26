import { useEffect, useState } from 'react'
import { inventoryApi, type Lot, type Movement, type Product } from './api'
import { dateTime, day, movementNames, unitNames } from './format'

export function ProductDetail({ product }: { product: Product }) {
  const [data, setData] = useState<{ lots: Lot[]; movements: Movement[] } | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([inventoryApi.lots(product.id), inventoryApi.movements(product.id)])
      .then(([lots, movements]) => setData({ lots, movements }))
      .catch((reason: unknown) =>
        setError(reason instanceof Error ? reason.message : 'Error inesperado.'),
      )
  }, [product.id])

  if (error) return <p className="error" role="alert">{error}</p>
  if (!data) return <p>Cargando…</p>

  return (
    <>
      <h3>Lotes</h3>
      {data.lots.length === 0 && <p className="muted">Sin existencias.</p>}
      <ul className="list">
        {data.lots.map((lot) => (
          <li key={lot.id}>
            <span>
              {lot.quantity} {unitNames[product.unit]}
            </span>
            <span className={lot.expired ? 'error' : 'muted'}>
              {lot.expiresOn
                ? `${lot.expired ? 'Venció' : 'Vence'} el ${day(lot.expiresOn)}`
                : 'Sin vencimiento'}
            </span>
          </li>
        ))}
      </ul>
      <h3>Historial</h3>
      {data.movements.length === 0 && <p className="muted">Sin movimientos.</p>}
      <ul className="list">
        {data.movements.map((movement) => (
          <li key={movement.id}>
            <span>
              <strong>
                {movementNames[movement.type]} {movement.delta > 0 ? '+' : ''}
                {movement.delta}
              </strong>{' '}
              → {movement.stockAfter}
              {movement.reason && ` · ${movement.reason}`}
            </span>
            <span className="muted">
              {dateTime(movement.createdAt)}
              {movement.source === 'ASSISTANT' && ' · asistente'}
            </span>
          </li>
        ))}
      </ul>
    </>
  )
}
