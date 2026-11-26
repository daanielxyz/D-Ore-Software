import { useState } from 'react'
import { Modal } from '../../components/Modal'
import { emptyLine, suggestedTotal, toItems, type Line } from '../../components/lines'
import { ProductLines } from '../../components/ProductLines'
import { useLoad } from '../../components/useLoad'
import { useSubmit } from '../../components/useSubmit'
import { channelNames, type Channel } from '../channels'
import { inventoryApi } from '../inventory/api'
import { day, money, today } from '../inventory/format'
import { salesApi } from './api'

export function SalesPage() {
  const { data, error, reload } = useLoad(salesApi.list)
  const [creating, setCreating] = useState(false)

  if (error) return <p className="error" role="alert">{error}</p>
  if (!data) return <p>Cargando…</p>

  const sum = (from: string) =>
    data.filter((s) => s.saleDate >= from).reduce((total, s) => total + s.amount, 0)

  return (
    <>
      <div className="page-head">
        <h1>Ventas</h1>
        <button className="primary" onClick={() => setCreating(true)}>+ Venta de mostrador</button>
      </div>
      <div className="cards">
        <div className="panel stat"><strong>{money(sum(today()))}</strong><span>Hoy</span></div>
        <div className="panel stat"><strong>{money(sum(`${today().slice(0, 7)}-01`))}</strong><span>Este mes</span></div>
      </div>
      <p className="muted">Los pedidos entregados aparecen aquí solos.</p>
      <div className="panel scroll">
        {data.length === 0 ? (
          <p className="muted">Aún no hay ventas.</p>
        ) : (
          <table>
            <thead>
              <tr><th>Concepto</th><th>Cliente</th><th>Canal</th><th>Fecha</th><th>Valor</th></tr>
            </thead>
            <tbody>
              {data.map((sale) => (
                <tr key={sale.id}>
                  <td>{sale.description}</td>
                  <td>{sale.customerName ?? '—'}</td>
                  <td>{channelNames[sale.channel]}</td>
                  <td>{day(sale.saleDate)}</td>
                  <td>{money(sale.amount)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
      {creating && (
        <Modal title="Venta de mostrador" onClose={() => setCreating(false)}>
          <SaleForm
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

const loadProducts = () => inventoryApi.list(false)

function SaleForm({ onDone }: { onDone: () => void }) {
  const { data: products, error: loadError } = useLoad(loadProducts)
  const [lines, setLines] = useState<Line[]>([emptyLine(0)])
  const [description, setDescription] = useState('')
  const [amount, setAmount] = useState('')
  const [channel, setChannel] = useState<Channel>('STORE')
  const [saleDate, setSaleDate] = useState(today())
  const { saving, error, submit } = useSubmit(
    () =>
      salesApi.register({
        description,
        amount: amount ? Number(amount) : null,
        channel,
        saleDate,
        customerId: null,
        items: toItems(lines),
      }),
    onDone,
  )

  if (loadError) return <p className="error" role="alert">{loadError}</p>
  if (!products) return <p>Cargando…</p>

  return (
    <form className="form" onSubmit={submit}>
      <p className="wide muted">Los productos elegidos se descuentan del inventario.</p>
      <ProductLines products={products} lines={lines} onChange={setLines} />
      <label className="wide">
        Concepto{lines.length > 0 && ' (opcional)'}
        <input required={lines.length === 0} maxLength={200} value={description} onChange={(e) => setDescription(e.target.value)} />
      </label>
      <label>
        {lines.length > 0 ? `Valor. Vacío: ${money(suggestedTotal(lines, products))}` : 'Valor (COP)'}
        <input type="number" required={lines.length === 0} min="1" step="1" value={amount} onChange={(e) => setAmount(e.target.value)} />
      </label>
      <label>
        Canal
        <select value={channel} onChange={(e) => setChannel(e.target.value as Channel)}>
          {Object.entries(channelNames).map(([value, name]) => <option key={value} value={value}>{name}</option>)}
        </select>
      </label>
      <label className="wide">Fecha<input type="date" required max={today()} value={saleDate} onChange={(e) => setSaleDate(e.target.value)} /></label>
      {error && <p className="error wide" role="alert">{error}</p>}
      <button className="primary wide" disabled={saving}>Registrar</button>
    </form>
  )
}
