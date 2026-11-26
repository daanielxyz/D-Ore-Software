import { useEffect, useState } from 'react'
import { listProducts, type Product, type Unit } from './api'

const unitNames: Record<Unit, string> = {
  STEM: 'tallos',
  BUNCH: 'ramos',
  ROLL: 'rollos',
  PACKAGE: 'paquetes',
  UNIT: 'unidades',
}

export function InventoryPage() {
  const [products, setProducts] = useState<Product[] | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    listProducts(controller.signal)
      .then(setProducts)
      .catch((reason: unknown) => {
        if (!controller.signal.aborted) {
          setError(reason instanceof Error ? reason.message : 'Error inesperado.')
        }
      })
    return () => controller.abort()
  }, [])

  if (error) return <p role="alert">{error}</p>
  if (!products) return <p>Cargando inventario…</p>
  if (products.length === 0) return <p>Aún no hay productos.</p>

  return (
    <table>
      <thead>
        <tr>
          <th>Producto</th>
          <th>Categoría</th>
          <th>Existencias</th>
        </tr>
      </thead>
      <tbody>
        {products.map((product) => (
          <tr key={product.id}>
            <td>{product.name}</td>
            <td>{product.category}</td>
            <td>
              {product.stock} {unitNames[product.unit]}
              {product.lowStock && ' · stock bajo'}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
