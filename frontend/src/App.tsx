import { Navigate, Route, Routes } from 'react-router-dom'
import { PixelDefs } from './components/PixelDefs'
import { WelcomePage } from './pages/WelcomePage'
import { SeedPickPage } from './pages/SeedPickPage'
import { TiersPage } from './pages/TiersPage'
import { HomePage } from './pages/HomePage'
import { TradePage } from './pages/TradePage'
import { StockSearchPage } from './pages/StockSearchPage'
import { OrderPage } from './pages/OrderPage'
import { TradeHistoryPage } from './pages/TradeHistoryPage'
import { ApartmentsPage } from './pages/ApartmentsPage'
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
        <Route path="/trade/history" element={<TradeHistoryPage />} />
        <Route path="/trade/:code" element={<OrderPage />} />
        <Route path="/apartments" element={<ApartmentsPage />} />
        <Route path="/contract/:dealId" element={<ContractPage />} />
        <Route path="/village" element={<VillagePage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  )
}
