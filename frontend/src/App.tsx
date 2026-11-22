import './App.css'

function App() {
  return (
    <main className="welcome">
      <p className="eyebrow">Monitoramento da loja</p>
      <h1>Preços, estoque e compras</h1>
      <p className="description">
        Acompanhe os preços dos fornecedores e mantenha o histórico de compras organizado.
      </p>
      <section className="notice" aria-labelledby="development-title">
        <h2 id="development-title">Sistema em desenvolvimento</h2>
        <p>Os cadastros e a importação de tabelas de preços serão disponibilizados nas próximas etapas.</p>
      </section>
    </main>
  )
}

export default App
