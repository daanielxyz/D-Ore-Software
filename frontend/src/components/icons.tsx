// Íconos de línea propios (24 × 24); toman el color del texto.
const paths = {
  resumen: 'M3 11l9-8 9 8M5 10v10h5v-6h4v6h5V10',
  asistente: 'M12 3a3 3 0 0 0-3 3v5a3 3 0 0 0 6 0V6a3 3 0 0 0-3-3zM5 11a7 7 0 0 0 14 0M12 18v3',
  pedidos: 'M9 4h6v3H9zM9 5H6v15h12V5h-3M9 12h6M9 16h4',
  inventario: 'M3 7l9-4 9 4v10l-9 4-9-4zM3 7l9 4 9-4M12 11v10',
  clientes: 'M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM2 21a7 7 0 0 1 14 0M16 3.5a4 4 0 0 1 0 7M18 14.5a7 7 0 0 1 4 6.5',
  ventas: 'M5 8h14l-1 13H6zM9 8V6a3 3 0 0 1 6 0v2',
  finanzas: 'M3 6h16v14H3zM3 6l12-3v3M15 13h4',
  analitica: 'M4 20V10M10 20V4M16 20v-7M22 20H2',
  contenido: 'M4 5h16v16H4zM4 10h16M8 3v4M16 3v4',
  escucha: 'M4 5h16v11H9l-5 4zM8 9h8M8 12h5',
  menu: 'M4 6h16M4 12h16M4 18h16',
  salir: 'M10 4H4v16h6M16 8l4 4-4 4M20 12H9',
  cerrar: 'M6 6l12 12M18 6L6 18',
}

export type IconName = keyof typeof paths

export function Icon({ name }: { name: IconName }) {
  return (
    <svg className="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d={paths[name]} />
    </svg>
  )
}
