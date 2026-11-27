import { useState } from 'react'
import { Modal } from '../../components/Modal'
import { useLoad } from '../../components/useLoad'
import { useSubmit } from '../../components/useSubmit'
import { http } from '../../http'
import { channelNames, type Channel } from '../channels'
import { day, today } from '../inventory/format'

type Sentiment = 'POSITIVE' | 'NEUTRAL' | 'NEGATIVE' | 'PURCHASE_INTENT'

interface Mention {
  id: number
  source: Channel
  author: string
  message: string
  sentiment: Sentiment
  attended: boolean
  mentionDate: string
}

const sentimentNames: Record<Sentiment, string> = {
  POSITIVE: 'Positiva',
  NEUTRAL: 'Neutral',
  NEGATIVE: 'Queja',
  PURCHASE_INTENT: 'Quiere comprar',
}

const tones: Record<Sentiment, string> = {
  POSITIVE: '',
  NEUTRAL: '',
  NEGATIVE: 'bad',
  PURCHASE_INTENT: 'warn',
}

const loadMentions = () => http<Mention[]>('/api/mentions')

export function ListeningPage() {
  const { data, error, reload } = useLoad(loadMentions)
  const [creating, setCreating] = useState(false)
  const [filter, setFilter] = useState<Sentiment | null>(null)
  const [failure, setFailure] = useState('')

  async function change(action: () => Promise<void>) {
    setFailure('')
    try {
      await action()
      await reload()
    } catch (reason) {
      setFailure(reason instanceof Error ? reason.message : 'Error inesperado.')
    }
  }

  const shown = data?.filter((m) => !filter || m.sentiment === filter) ?? []

  return (
    <>
      <div className="page-head">
        <h1>Social listening</h1>
        <button className="primary" onClick={() => setCreating(true)}>+ Anotar mención</button>
      </div>
      <p className="muted">Menciones que anota el equipo. No rastrea las redes ni responde por ustedes.</p>
      {(error || failure) && <p className="error" role="alert">{error || failure}</p>}
      {!data && !error && <p>Cargando…</p>}
      {data && (
        <>
          <div className="cards">
            {(Object.keys(sentimentNames) as Sentiment[]).map((sentiment) => (
              <button
                key={sentiment}
                className={`card ${filter === sentiment ? 'selected' : ''}`}
                aria-pressed={filter === sentiment}
                onClick={() => setFilter(filter === sentiment ? null : sentiment)}
              >
                <strong>{data.filter((m) => m.sentiment === sentiment).length}</strong>
                {sentimentNames[sentiment]}
              </button>
            ))}
          </div>
          <div className="panel mentions">
            {shown.length === 0 && <p className="muted">No hay menciones para mostrar.</p>}
            {shown.map((mention) => (
              <article key={mention.id} className={mention.attended ? 'attended' : ''}>
                <div>
                  <strong>{mention.author}</strong>
                  <span className={`tag ${tones[mention.sentiment]}`}>{sentimentNames[mention.sentiment]}</span>
                  <p>{mention.message}</p>
                  <small className="muted">{channelNames[mention.source]} · {day(mention.mentionDate)}</small>
                </div>
                <div className="actions">
                  <button onClick={() => void change(() => http<void>(`/api/mentions/${mention.id}/attended`, 'PUT', { attended: !mention.attended }))}>
                    {mention.attended ? 'Reabrir' : 'Marcar atendida'}
                  </button>
                  <button
                    className="danger"
                    onClick={() => {
                      if (window.confirm('¿Eliminar esta mención?')) void change(() => http<void>(`/api/mentions/${mention.id}`, 'DELETE'))
                    }}
                  >
                    Eliminar
                  </button>
                </div>
              </article>
            ))}
          </div>
        </>
      )}
      {creating && (
        <Modal title="Anotar mención" onClose={() => setCreating(false)}>
          <MentionForm
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

function MentionForm({ onDone }: { onDone: () => void }) {
  const [source, setSource] = useState<Channel>('INSTAGRAM')
  const [author, setAuthor] = useState('')
  const [message, setMessage] = useState('')
  const [sentiment, setSentiment] = useState<Sentiment>('POSITIVE')
  const [mentionDate, setMentionDate] = useState(today())
  const { saving, error, submit } = useSubmit(
    () => http<void>('/api/mentions', 'POST', { source, author, message, sentiment, mentionDate }),
    onDone,
  )

  return (
    <form className="form" onSubmit={submit}>
      <label>Quién<input required maxLength={80} value={author} onChange={(e) => setAuthor(e.target.value)} /></label>
      <label>
        Dónde
        <select value={source} onChange={(e) => setSource(e.target.value as Channel)}>
          {Object.entries(channelNames).map(([value, name]) => <option key={value} value={value}>{name}</option>)}
        </select>
      </label>
      <label className="wide">Qué dijo<input required maxLength={500} value={message} onChange={(e) => setMessage(e.target.value)} /></label>
      <label>
        Tipo
        <select value={sentiment} onChange={(e) => setSentiment(e.target.value as Sentiment)}>
          {Object.entries(sentimentNames).map(([value, name]) => <option key={value} value={value}>{name}</option>)}
        </select>
      </label>
      <label>Fecha<input type="date" required max={today()} value={mentionDate} onChange={(e) => setMentionDate(e.target.value)} /></label>
      {error && <p className="error wide" role="alert">{error}</p>}
      <button className="primary wide" disabled={saving}>Guardar</button>
    </form>
  )
}
