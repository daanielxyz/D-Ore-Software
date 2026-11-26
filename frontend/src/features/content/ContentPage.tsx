import { useState } from 'react'
import { Modal } from '../../components/Modal'
import { useLoad } from '../../components/useLoad'
import { useSubmit } from '../../components/useSubmit'
import { http } from '../../http'
import { day } from '../inventory/format'

type Platform = 'INSTAGRAM' | 'TIKTOK' | 'FACEBOOK' | 'WHATSAPP'
type Status = 'IDEA' | 'DRAFT' | 'SCHEDULED' | 'PUBLISHED'

interface PostData {
  title: string
  platform: Platform
  status: Status
  scheduledDate: string | null
  reach: number
  interactions: number
  notes: string
}

type Post = PostData & { id: number }

const platformNames: Record<Platform, string> = {
  INSTAGRAM: 'Instagram',
  TIKTOK: 'TikTok',
  FACEBOOK: 'Facebook',
  WHATSAPP: 'WhatsApp',
}

const statusNames: Record<Status, string> = {
  IDEA: 'Idea',
  DRAFT: 'Borrador',
  SCHEDULED: 'Programada',
  PUBLISHED: 'Publicada',
}

const loadPosts = () => http<Post[]>('/api/content')

export function ContentPage() {
  const { data, error, reload } = useLoad(loadPosts)
  const [editing, setEditing] = useState<Post | 'new' | null>(null)
  const [failure, setFailure] = useState('')

  async function remove(post: Post) {
    if (!window.confirm(`¿Eliminar "${post.title}"?`)) return
    setFailure('')
    try {
      await http<void>(`/api/content/${post.id}`, 'DELETE')
      await reload()
    } catch (reason) {
      setFailure(reason instanceof Error ? reason.message : 'Error inesperado.')
    }
  }

  return (
    <>
      <div className="page-head">
        <h1>Contenido</h1>
        <button className="primary" onClick={() => setEditing('new')}>+ Nueva publicación</button>
      </div>
      <p className="muted">Calendario para organizar las publicaciones. No publica en las redes; el alcance se anota a mano.</p>
      {(error || failure) && <p className="error" role="alert">{error || failure}</p>}
      {!data && !error && <p>Cargando…</p>}
      {data && (
        <div className="panel scroll">
          {data.length === 0 ? (
            <p className="muted">Aún no hay publicaciones.</p>
          ) : (
            <table>
              <thead>
                <tr><th>Publicación</th><th>Red</th><th>Estado</th><th>Fecha</th><th>Alcance</th><th>Interacción</th><th /></tr>
              </thead>
              <tbody>
                {data.map((post) => (
                  <tr key={post.id}>
                    <td>{post.title}{post.notes && <small>{post.notes}</small>}</td>
                    <td>{platformNames[post.platform]}</td>
                    <td><span className="tag">{statusNames[post.status]}</span></td>
                    <td>{post.scheduledDate ? day(post.scheduledDate) : '—'}</td>
                    <td>{post.reach ? post.reach.toLocaleString('es-CO') : '—'}</td>
                    <td>{post.reach ? `${((post.interactions / post.reach) * 100).toFixed(1)} %` : '—'}</td>
                    <td>
                      <div className="actions">
                        <button onClick={() => setEditing(post)}>Editar</button>
                        <button className="danger" onClick={() => void remove(post)}>Eliminar</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
      {editing && (
        <Modal title={editing === 'new' ? 'Nueva publicación' : 'Editar publicación'} onClose={() => setEditing(null)}>
          <PostForm
            post={editing === 'new' ? null : editing}
            onDone={() => {
              setEditing(null)
              void reload()
            }}
          />
        </Modal>
      )}
    </>
  )
}

function PostForm({ post, onDone }: { post: Post | null; onDone: () => void }) {
  const [title, setTitle] = useState(post?.title ?? '')
  const [platform, setPlatform] = useState<Platform>(post?.platform ?? 'INSTAGRAM')
  const [status, setStatus] = useState<Status>(post?.status ?? 'IDEA')
  const [scheduledDate, setScheduledDate] = useState(post?.scheduledDate ?? '')
  const [reach, setReach] = useState(String(post?.reach ?? 0))
  const [interactions, setInteractions] = useState(String(post?.interactions ?? 0))
  const [notes, setNotes] = useState(post?.notes ?? '')
  const { saving, error, submit } = useSubmit(() => {
    const data: PostData = {
      title,
      platform,
      status,
      scheduledDate: scheduledDate || null,
      reach: Number(reach),
      interactions: Number(interactions),
      notes,
    }
    return post ? http<void>(`/api/content/${post.id}`, 'PUT', data) : http<void>('/api/content', 'POST', data)
  }, onDone)

  return (
    <form className="form" onSubmit={submit}>
      <label className="wide">Título<input required maxLength={120} value={title} onChange={(e) => setTitle(e.target.value)} /></label>
      <label>
        Red
        <select value={platform} onChange={(e) => setPlatform(e.target.value as Platform)}>
          {Object.entries(platformNames).map(([value, name]) => <option key={value} value={value}>{name}</option>)}
        </select>
      </label>
      <label>
        Estado
        <select value={status} onChange={(e) => setStatus(e.target.value as Status)}>
          {Object.entries(statusNames).map(([value, name]) => <option key={value} value={value}>{name}</option>)}
        </select>
      </label>
      <label className="wide">Fecha<input type="date" required={status === 'SCHEDULED'} value={scheduledDate} onChange={(e) => setScheduledDate(e.target.value)} /></label>
      {status === 'PUBLISHED' && (
        <>
          <label>Alcance<input type="number" required min="0" step="1" value={reach} onChange={(e) => setReach(e.target.value)} /></label>
          <label>Interacciones<input type="number" required min="0" step="1" value={interactions} onChange={(e) => setInteractions(e.target.value)} /></label>
        </>
      )}
      <label className="wide">Notas<input maxLength={300} value={notes} onChange={(e) => setNotes(e.target.value)} /></label>
      {error && <p className="error wide" role="alert">{error}</p>}
      <button className="primary wide" disabled={saving}>Guardar</button>
    </form>
  )
}
