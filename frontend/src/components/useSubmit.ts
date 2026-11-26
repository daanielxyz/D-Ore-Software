import { useState, type FormEvent } from 'react'

/** Evita envíos dobles y muestra el error sin cerrar el formulario. */
export function useSubmit(action: () => Promise<unknown>, onDone: () => void) {
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  async function submit(event?: FormEvent) {
    event?.preventDefault()
    if (saving) return
    setSaving(true)
    setError('')
    try {
      await action()
      onDone()
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Error inesperado.')
      setSaving(false)
    }
  }

  return { saving, error, submit }
}
