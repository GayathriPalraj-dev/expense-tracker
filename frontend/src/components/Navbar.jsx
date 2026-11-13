import { useAuth } from '../App.jsx'

export default function Navbar() {
  const { user, signOut } = useAuth()
  return <header className="topbar"><a className="brand" href="/" aria-label="Spendwise home"><span className="brand-icon">s</span> spendwise</a><div className="topbar-right"><span className="today-label">Your money, in view</span><span className="avatar">{user?.name?.charAt(0)?.toUpperCase() || 'U'}</span><span className="user-name">{user?.name}</span><button className="text-button" onClick={signOut}>Sign out</button></div></header>
}
