import { Icon } from './icons'
import { navGroups, type PageId } from './navigation'

export function NavMenu({ current, onSelect }: { current: PageId; onSelect?: () => void }) {
  return (
    <>
      {navGroups.map((group) => (
        <div className="nav-group" key={group.label}>
          <p className="nav-label"><span>{group.label}</span></p>
          {group.items.map(({ id, title }) => (
            <a key={id} href={`#${id}`} title={title} onClick={onSelect} aria-current={id === current ? 'page' : undefined}>
              <Icon name={id} />
              <span>{title}</span>
            </a>
          ))}
        </div>
      ))}
    </>
  )
}
