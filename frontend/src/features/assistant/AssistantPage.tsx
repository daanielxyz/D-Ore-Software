import { useRef, useState, type FormEvent } from 'react'
import { assistantApi, type AudioData, type Proposal, type Turn } from './api'
import { toAudioData } from './audio'
import { useRecorder } from './useRecorder'

interface Message {
  id: number
  fromUser: boolean
  text: string
  proposal?: Proposal
  state?: 'pending' | 'saving' | 'done' | 'cancelled' | 'failed'
}

const examples = [
  'Llegaron 50 tallos de Rosa Freedom roja',
  'Se dañaron 3 tulipanes',
  '¿Cuántas hortensias quedan?',
]

const errorText = (reason: unknown) =>
  reason instanceof Error ? reason.message : 'Error inesperado.'

/** Lo que el modelo necesita recordar para entender "no, eran 40" o "la roja". */
const toHistory = (messages: Message[]): Turn[] =>
  messages.slice(-10).map((m) => ({
    fromUser: m.fromUser,
    text: [m.text, ...(m.proposal?.actions.map((a) => a.summary) ?? [])].join('\n').slice(0, 1000),
  }))

export function AssistantPage() {
  const [messages, setMessages] = useState<Message[]>([])
  const [text, setText] = useState('')
  const [busy, setBusy] = useState(false)
  const nextId = useRef(0)

  const add = (message: Omit<Message, 'id'>) => {
    const id = nextId.current++
    setMessages((current) => [...current, { ...message, id }])
    return id
  }
  const update = (id: number, change: Partial<Message>) =>
    setMessages((current) => current.map((m) => (m.id === id ? { ...m, ...change } : m)))

  async function send(order: string, audio: AudioData | null) {
    setBusy(true)
    const history = toHistory(messages)
    // Una orden nueva reemplaza la propuesta que seguía sin confirmar.
    messages
      .filter((m) => m.state === 'pending' && m.proposal)
      .forEach((m) => {
        update(m.id, { state: 'cancelled' })
        void assistantApi.cancel(m.proposal!.id).catch(() => undefined)
      })
    const mine = add({ fromUser: true, text: audio ? '🎤 Nota de voz' : order })
    try {
      const reply = await assistantApi.send(order, audio, history)
      if (audio && reply.heard) update(mine, { text: `🎤 "${reply.heard}"` })
      add({
        fromUser: false,
        text: reply.message,
        proposal: reply.proposal ?? undefined,
        state: reply.proposal ? 'pending' : undefined,
      })
    } catch (reason) {
      add({ fromUser: false, text: errorText(reason) })
      if (!audio) setText(order)
    }
    setBusy(false)
  }

  async function sendAudio(blob: Blob) {
    try {
      await send('', await toAudioData(blob))
    } catch (reason) {
      add({ fromUser: false, text: errorText(reason) })
    }
  }

  async function confirm(message: Message) {
    update(message.id, { state: 'saving' })
    try {
      await assistantApi.confirm(message.proposal!.id)
      update(message.id, { state: 'done' })
    } catch (reason) {
      update(message.id, { state: 'failed' })
      add({ fromUser: false, text: `No se hizo ningún cambio. ${errorText(reason)}` })
    }
  }

  async function cancel(message: Message) {
    update(message.id, { state: 'cancelled' })
    await assistantApi.cancel(message.proposal!.id).catch(() => undefined)
  }

  const recorder = useRecorder(
    (blob) => void sendAudio(blob),
    (error) => add({ fromUser: false, text: error }),
  )

  function submit(event: FormEvent) {
    event.preventDefault()
    const order = text.trim()
    if (!order || busy) return
    setText('')
    void send(order, null)
  }

  return (
    <div className="chat">
      <h1>Asistente</h1>
      <p className="muted">Dime qué cambió en el inventario. Nada se guarda hasta que confirmes.</p>

      <div className="messages" aria-live="polite">
        {messages.length === 0 && (
          <div className="examples">
            {examples.map((example) => (
              <button key={example} onClick={() => setText(example)}>{example}</button>
            ))}
          </div>
        )}
        {messages.map((message) => (
          <div key={message.id} className={`bubble ${message.fromUser ? 'mine' : ''}`}>
            <p>{message.text}</p>
            {message.proposal && (
              <div className="proposal">
                <ul>
                  {message.proposal.actions.map((action, index) => <li key={index}>{action.summary}</li>)}
                </ul>
                {message.state === 'pending' && (
                  <div className="actions">
                    <button className="primary" onClick={() => void confirm(message)}>Confirmar</button>
                    <button onClick={() => void cancel(message)}>Cancelar</button>
                  </div>
                )}
                {message.state === 'saving' && <p className="muted">Guardando…</p>}
                {message.state === 'done' && <p><strong>✓ Hecho</strong></p>}
                {message.state === 'cancelled' && <p className="muted">Cancelado</p>}
                {message.state === 'failed' && <p className="error">No se aplicó</p>}
              </div>
            )}
          </div>
        ))}
        {busy && <div className="bubble"><p className="muted">Pensando…</p></div>}
      </div>

      <form className="composer" onSubmit={submit}>
        <input
          aria-label="Orden"
          placeholder="Escribe una orden…"
          maxLength={1000}
          value={text}
          onChange={(event) => setText(event.target.value)}
        />
        <button className="primary" disabled={busy || recorder.recording}>Enviar</button>
        <button
          type="button"
          className={recorder.recording ? 'recording' : ''}
          disabled={busy}
          onClick={() => (recorder.recording ? recorder.stop() : void recorder.start())}
        >
          {recorder.recording ? '■ Detener y enviar' : '🎤 Grabar'}
        </button>
        <label className="attach">
          Adjuntar audio
          <input
            type="file"
            accept="audio/*,.ogg,.opus,.m4a"
            disabled={busy || recorder.recording}
            onChange={(event) => {
              const file = event.target.files?.[0]
              event.target.value = ''
              if (file) void sendAudio(file)
            }}
          />
        </label>
      </form>
    </div>
  )
}
