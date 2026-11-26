import { useState } from 'react'
import { day, money } from '../inventory/format'

export interface BarItem {
  label: string
  value: number
  text: string
}

/** Barras horizontales de una sola serie; cada una lleva su valor escrito. */
export function BarList({ title, items, empty }: { title: string; items: BarItem[]; empty: string }) {
  const max = Math.max(1, ...items.map((item) => item.value))
  return (
    <section className="panel">
      <h2>{title}</h2>
      {items.length === 0 && <p className="muted">{empty}</p>}
      <ul className="bars">
        {items.map((item) => (
          <li key={item.label}>
            <span>{item.label}</span>
            <span className="muted">{item.text}</span>
            <span className="track" aria-hidden="true">
              <span style={{ width: `${(item.value / max) * 100}%` }} />
            </span>
          </li>
        ))}
      </ul>
    </section>
  )
}

export interface DaySales {
  date: string
  total: number
  count: number
}

/** Ventas por día. Al pasar o enfocar una columna se lee su valor. */
export function DayColumns({ days }: { days: DaySales[] }) {
  const [active, setActive] = useState<DaySales | null>(null)
  const max = Math.max(1, ...days.map((d) => d.total))
  const best = days.reduce((a, b) => (b.total > a.total ? b : a))
  const describe = (d: DaySales) =>
    `${day(d.date)}: ${money(d.total)} en ${d.count} ${d.count === 1 ? 'venta' : 'ventas'}`

  return (
    <section className="panel chart-panel">
      <h2>Ventas por día</h2>
      <p className="muted" aria-live="polite">
        {active ? describe(active) : best.total > 0 ? `Mejor día: ${describe(best)}` : 'Sin ventas en el periodo.'}
      </p>
      <div className="day-chart" onMouseLeave={() => setActive(null)}>
        {days.map((d) => (
          <button
            key={d.date}
            type="button"
            className="day"
            aria-label={describe(d)}
            onMouseEnter={() => setActive(d)}
            onFocus={() => setActive(d)}
            onClick={() => setActive(d)}
          >
            <span style={{ height: `${(d.total / max) * 100}%` }} />
          </button>
        ))}
      </div>
      <div className="day-axis muted">
        <span>{day(days[0].date)}</span>
        <span>Hoy</span>
      </div>
    </section>
  )
}
