import { createContext, useContext, useEffect, useState } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import Dashboard from './pages/Dashboard.jsx'

const AuthContext = createContext(null)
export const useAuth = () => useContext(AuthContext)

export default function App() {
  const [token, setToken] = useState(() => localStorage.getItem('expense-token'))
  const [user, setUser] = useState(() => JSON.parse(localStorage.getItem('expense-user') || 'null'))
  useEffect(() => {
    function clearExpiredSession() { setToken(null); setUser(null) }
    window.addEventListener('expense:unauthorized', clearExpiredSession)
    return () => window.removeEventListener('expense:unauthorized', clearExpiredSession)
  }, [])
  function signIn(data) { localStorage.setItem('expense-token', data.token); localStorage.setItem('expense-user', JSON.stringify(data.user)); setToken(data.token); setUser(data.user) }
  function signOut() { localStorage.removeItem('expense-token'); localStorage.removeItem('expense-user'); setToken(null); setUser(null) }
  return <AuthContext.Provider value={{ token, user, signIn, signOut }}><Routes>
    <Route path="/login" element={token ? <Navigate to="/" replace /> : <Login />} />
    <Route path="/register" element={token ? <Navigate to="/" replace /> : <Register />} />
    <Route path="/" element={token ? <Dashboard /> : <Navigate to="/login" replace />} />
    <Route path="*" element={<Navigate to="/" replace />} />
  </Routes></AuthContext.Provider>
}
