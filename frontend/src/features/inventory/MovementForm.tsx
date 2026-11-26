import { useEffect, useState } from 'react'
import { useSubmit } from '../../components/useSubmit'
import { inventoryApi, type Lot, type MovementType, type Product } from './api'
import { day, movementNames, today, unitNames } from './format'

interface Props {
  product: Product
  onDone: (message: string) => void
}

export function MovementForm({ product, onDone }: Props) {
  const [type, setType] = useState<MovementType>('IN')
  const [quantity, setQuantity] = useState('')
  const [reason, setReason] = useState('')
  const [expiresOn, setExpiresOn] = useState('')
  const [lotId, setLotId] = useState('')
  const [lots, setLots] = useState<Lot[]>([])

  useEffect(() => {
    inventoryApi
      .lots(product.id)
      .then(setLots)
      .catch(() => setLots([]))
  }, [product.id])

  const takes = type === 'OUT' || type === 'WASTE'
  // Una salida no puede tomar flor vencida.
  const choices = lots.filter((lot) => type === 'WASTE' || !lot.expired)

  const { saving, error, submit } = useSubmit(
    () =>
      inventoryApi.record(product.id, {
        type,
        quantity: Number(quantity),
        reason,
        expiresOn: !takes && expiresOn ? expiresOn : null,
        lotId: takes && lotId ? Number(lotId) : null,
      }),
    () => onDone(`${movementNames[type]} registrada en ${product.name}.`),
  )

  return (
    <form className="form" onSubmit={submit}>
      <p className="wide muted">
        Hay {product.stock} {unitNames[product.unit]}
        {product.expiredStock > 0 && `, de los cuales ${product.expiredStock} están vencidos`}.
      </p>
      <label>
        Tipo
        <select
          value={type}
          onChange={(event) => {
            setType(event.target.value as MovementType)
            setLotId('')
          }}
        >
          {Object.entries(movementNames).map(([value, name]) => (
            <option key={value} value={value}>
              {name}
            </option>
          ))}
        </select>
      </label>
      <label>
        {type === 'ADJUSTMENT' ? 'Stock contado' : 'Cantidad'}
        <input
          type="number"
          min={type === 'ADJUSTMENT' ? 0 : 1}
          step="1"
          required
          value={quantity}
          onChange={(event) => setQuantity(event.target.value)}
        />
      </label>
      {takes ? (
        <label className="wide">
          Lote
          <select value={lotId} onChange={(event) => setLotId(event.target.value)}>
            <option value="">Automático (primero lo que vence antes)</option>
            {choices.map((lot) => (
              <option key={lot.id} value={lot.id}>
                {lot.quantity} · {lot.expiresOn ? `vence ${day(lot.expiresOn)}` : 'sin vencimiento'}
                {lot.expired ? ' (vencido)' : ''}
              </option>
            ))}
          </select>
        </label>
      ) : (
        <label className="wide">
          Vencimiento de lo que entra (opcional)
          <input type="date" min={today()} value={expiresOn} onChange={(event) => setExpiresOn(event.target.value)} />
        </label>
      )}
      <label className="wide">
        Motivo (opcional)
        <input maxLength={200} value={reason} onChange={(event) => setReason(event.target.value)} />
      </label>
      {error && <p className="error wide" role="alert">{error}</p>}
      <button className="primary wide" disabled={saving}>
        {saving ? 'Guardando…' : 'Registrar'}
      </button>
    </form>
  )
}
