import { Navigate, Route, Routes } from 'react-router-dom'
import { PixelDefs } from './components/PixelDefs'
import { WelcomePage } from './pages/WelcomePage'
import { SeedPickPage } from './pages/SeedPickPage'
import { TiersPage } from './pages/TiersPage'
import { HomePage } from './pages/HomePage'
import { TradePage } from './pages/TradePage'
import { StockSearchPage } from './pages/StockSearchPage'
import { OrderPage } from './pages/OrderPage'
import { HomesPage } from './pages/HomesPage'
import { ContractPage } from './pages/ContractPage'
import { VillagePage } from './pages/VillagePage'

export function App() {
  return (
    <>
      <PixelDefs />
      <Routes>
        <Route path="/" element={<WelcomePage />} />
        <Route path="/seed" element={<SeedPickPage />} />
        <Route path="/tiers" element={<TiersPage />} />
        <Route path="/home" element={<HomePage />} />
        <Route path="/trade" element={<TradePage />} />
        <Route path="/trade/search" element={<StockSearchPage />} />
        <Route path="/trade/:code" element={<OrderPage />} />
        <Route path="/homes" element={<HomesPage />} />
        <Route path="/contract/:dealId" element={<ContractPage />} />
        <Route path="/village" element={<VillagePage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  )
}
