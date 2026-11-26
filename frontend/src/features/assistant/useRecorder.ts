import { useEffect, useRef, useState } from 'react'

const MAX_SECONDS = 60

/** Graba una nota de voz. El micrófono solo funciona en localhost o con HTTPS. */
export function useRecorder(onAudio: (audio: Blob) => void, onError: (message: string) => void) {
  const [recording, setRecording] = useState(false)
  const recorder = useRef<MediaRecorder | null>(null)
  const timer = useRef<number | undefined>(undefined)
  const discard = useRef(false)

  async function start() {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
      const media = new MediaRecorder(stream)
      const chunks: Blob[] = []
      media.ondataavailable = (event) => chunks.push(event.data)
      media.onstop = () => {
        window.clearTimeout(timer.current)
        stream.getTracks().forEach((track) => track.stop())
        setRecording(false)
        if (!discard.current) onAudio(new Blob(chunks, { type: media.mimeType }))
      }
      discard.current = false
      media.start()
      recorder.current = media
      setRecording(true)
      timer.current = window.setTimeout(() => media.stop(), MAX_SECONDS * 1000)
    } catch {
      onError('No se pudo usar el micrófono. Revisa el permiso del navegador.')
    }
  }

  const stop = () => recorder.current?.stop()

  // Al salir de la pantalla se apaga el micrófono sin enviar nada.
  useEffect(
    () => () => {
      discard.current = true
      if (recorder.current?.state === 'recording') recorder.current.stop()
    },
    [],
  )

  return { recording, start, stop }
}
