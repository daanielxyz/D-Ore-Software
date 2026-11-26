import { useCallback, useEffect, useState } from 'react'

/** Carga datos al montar; load debe ser estable (una función de módulo). */
export function useLoad<T>(load: () => Promise<T>) {
  const [data, setData] = useState<T | null>(null)
  const [error, setError] = useState('')

  const reload = useCallback(
    () =>
      load()
        .then((value) => {
          setData(value)
          setError('')
        })
        .catch((reason: unknown) =>
          setError(reason instanceof Error ? reason.message : 'Error inesperado.'),
        ),
    [load],
  )

  useEffect(() => {
    void reload()
  }, [reload])

  return { data, error, reload }
}
