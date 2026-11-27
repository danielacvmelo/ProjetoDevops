import { useState } from 'react'
import Products from './pages/Products'
import Suppliers from './pages/Suppliers'
import './App.css'

function App() {
  const [page, setPage] = useState<'products' | 'suppliers'>('products')

  return (
    <div className="app">
      <header className="app-header"><div className="brand"><span className="brand-symbol" aria-hidden="true">P</span><div><strong>Preços e estoque</strong><small>Gestão da loja</small></div></div><span className="local-notice">Produtos e fornecedores</span></header>
      <div className="app-layout">
        <nav className="app-nav" aria-label="Cadastros">
          <span className="nav-caption">CADASTROS</span>
          <button type="button" aria-current={page === 'products' ? 'page' : undefined} onClick={() => setPage('products')}>Produtos</button>
          <button type="button" aria-current={page === 'suppliers' ? 'page' : undefined} onClick={() => setPage('suppliers')}>Fornecedores</button>
        </nav>
        <main className="app-content">{page === 'products' ? <Products /> : <Suppliers />}</main>
      </div>
    </div>
  )
}

export default App
