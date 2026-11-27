import { useEffect, useState } from 'react'

// Vertical en pantallas altas (celular) y horizontal en las demás.
const portrait = '(orientation: portrait)'

export function LoginBackground() {
  const [name, setName] = useState(() => (window.matchMedia(portrait).matches ? 'vertical' : 'horizontal'))
  const [ready, setReady] = useState(false)

  useEffect(() => {
    const query = window.matchMedia(portrait)
    const onChange = () => {
      setReady(false)
      setName(query.matches ? 'vertical' : 'horizontal')
    }
    query.addEventListener('change', onChange)
    return () => query.removeEventListener('change', onChange)
  }, [])

  return (
    <div className="login-media" aria-hidden="true" style={{ backgroundImage: `url(/login/${name}.jpg)` }}>
      <video
        key={name}
        className={ready ? 'ready' : ''}
        src={`/login/${name}.mp4`}
        autoPlay
        muted
        loop
        playsInline
        preload="auto"
        onPlaying={() => setReady(true)}
      />
    </div>
  )
}
