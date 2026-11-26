import { useCallback, useEffect, useState } from 'react'
import { Modal } from '../../components/Modal'
import { useSubmit } from '../../components/useSubmit'
import { inventoryApi, type Product } from './api'
import { day, expiresSoon, money, SOON_DAYS, unitNames } from './format'
import { MovementForm } from './MovementForm'
import { ProductDetail } from './ProductDetail'
import { ProductForm } from './ProductForm'

type Filter = 'all' | 'low' | 'expiry'
type Dialog =
  | { kind: 'create' }
  | { kind: 'edit' | 'move' | 'detail' | 'remove'; product: Product }

const normalize = (text: string) =>
  text.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase()

export function InventoryPage() {
  const [products, setProducts] = useState<Product[] | null>(null)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [query, setQuery] = useState('')
  const [filter, setFilter] = useState<Filter>('all')
  const [showInactive, setShowInactive] = useState(false)
  const [dialog, setDialog] = useState<Dialog | null>(null)

  const load = useCallback(
    () =>
      inventoryApi
        .list(showInactive)
        .then((list) => {
          setProducts(list)
          setError('')
        })
        .catch((reason: unknown) =>
          setError(reason instanceof Error ? reason.message : 'Error inesperado.'),
        ),
    [showInactive],
  )

  useEffect(() => {
    void load()
  }, [load])

  function done(message: string) {
    setDialog(null)
    setNotice(message)
    void load()
  }

  if (error)
    return (
      <div className="panel">
        <p className="error" role="alert">{error}</p>
        <button className="primary" onClick={() => void load()}>Reintentar</button>
      </div>
    )
  if (!products) return <p>Cargando inventario…</p>

  const active = products.filter((p) => p.active)
  const low = active.filter((p) => p.lowStock)
  const expiry = active.filter((p) => p.expiredStock > 0 || expiresSoon(p))
  const base = filter === 'low' ? low : filter === 'expiry' ? expiry : products
  const term = normalize(query.trim())
  const visible = base.filter((p) =>
    normalize(`${p.name} ${p.sku} ${p.category} ${p.supplier}`).includes(term),
  )

  const card = (value: Filter, label: string, count: number) => (
    <button
      className={`card ${filter === value ? 'selected' : ''}`}
      aria-pressed={filter === value}
      onClick={() => setFilter(value)}
    >
      <strong>{count}</strong>
      <span>{label}</span>
    </button>
  )

  return (
    <>
      <div className="page-head">
        <h1>Inventario</h1>
        <button className="primary" onClick={() => setDialog({ kind: 'create' })}>
          + Nuevo producto
        </button>
      </div>

      <div className="cards">
        {card('all', 'Productos', active.length)}
        {card('low', 'Con stock bajo', low.length)}
        {card('expiry', `Vencidos o por vencer (${SOON_DAYS} días)`, expiry.length)}
      </div>

      <div className="toolbar">
        <input
          type="search"
          placeholder="Buscar por nombre, código, categoría o proveedor"
          aria-label="Buscar"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
        />
        <label className="check">
          <input type="checkbox" checked={showInactive} onChange={(event) => setShowInactive(event.target.checked)} />
          Mostrar desactivados
        </label>
      </div>

      {notice && <p className="notice" role="status">{notice}</p>}

      <div className="panel scroll">
        {visible.length === 0 ? (
          <p className="muted">No hay productos para mostrar.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Producto</th>
                <th>Categoría</th>
                <th>Existencias</th>
                <th>Próximo vencimiento</th>
                <th>Precio</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {visible.map((product) => (
                <tr key={product.id} className={product.active ? '' : 'inactive'}>
                  <td>
                    <button className="link" onClick={() => setDialog({ kind: 'detail', product })}>
                      {product.name}
                    </button>
                    <small>{product.sku}</small>
                  </td>
                  <td>{product.category}</td>
                  <td>
                    {product.stock} {unitNames[product.unit]}
                    {!product.active && <span className="tag">Desactivado</span>}
                    {product.active && product.lowStock && <span className="tag warn">Stock bajo</span>}
                    {product.expiredStock > 0 && <span className="tag bad">{product.expiredStock} vencidos</span>}
                  </td>
                  <td>
                    {product.nextExpiry ? day(product.nextExpiry) : '—'}
                    {expiresSoon(product) && <span className="tag warn">Pronto</span>}
                  </td>
                  <td>{money(product.unitPrice)}</td>
                  <td>
                    {product.active && (
                      <div className="actions">
                        <button onClick={() => setDialog({ kind: 'move', product })}>Movimiento</button>
                        <button onClick={() => setDialog({ kind: 'edit', product })}>Editar</button>
                        <button className="danger" onClick={() => setDialog({ kind: 'remove', product })}>
                          Eliminar
                        </button>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {dialog?.kind === 'create' && (
        <Modal title="Nuevo producto" onClose={() => setDialog(null)}>
          <ProductForm product={null} onDone={done} />
        </Modal>
      )}
      {dialog?.kind === 'edit' && (
        <Modal title={`Editar ${dialog.product.name}`} onClose={() => setDialog(null)}>
          <ProductForm product={dialog.product} onDone={done} />
        </Modal>
      )}
      {dialog?.kind === 'move' && (
        <Modal title={`Movimiento · ${dialog.product.name}`} onClose={() => setDialog(null)}>
          <MovementForm product={dialog.product} onDone={done} />
        </Modal>
      )}
      {dialog?.kind === 'detail' && (
        <Modal title={dialog.product.name} onClose={() => setDialog(null)}>
          <ProductDetail product={dialog.product} />
        </Modal>
      )}
      {dialog?.kind === 'remove' && (
        <Modal title="Eliminar producto" onClose={() => setDialog(null)}>
          <RemoveProduct product={dialog.product} onDone={done} />
        </Modal>
      )}
    </>
  )
}

function RemoveProduct({ product, onDone }: { product: Product; onDone: (message: string) => void }) {
  const { saving, error, submit } = useSubmit(
    () => inventoryApi.deactivate(product.id),
    () => onDone(`${product.name} se quitó del inventario.`),
  )
  return (
    <form onSubmit={submit}>
      <p>
        <strong>{product.name}</strong> dejará de aparecer en el inventario. Su historial se
        conserva.
      </p>
      {error && <p className="error" role="alert">{error}</p>}
      <button className="primary" disabled={saving}>Sí, eliminar</button>
    </form>
  )
}
