import { useState } from "react"
import { Link } from "react-router-dom"
import { usePortfolioQuery } from "../hooks/usePortfolioQuery"
import { formatKRW } from "../utils/format"
import { useMemberQuery } from "../hooks/useMemberQuery"
import { useStocksQuery } from "../hooks/useStocksQuery"
import { useHoldingsQuery } from "../hooks/useHoldingsQuery"
import { useMyHousesQuery } from "../hooks/useMyHousesQuery"
import { TIER_INFO } from '../utils/tier'
import './TradePage.css'

type StockTab = 'MINE' | 'ALL'

function toChangeText(changeRate: number): string {
  // toFixed(1) : 숫자를 소수 n자리까지 반올림해서 문자열로 바꿔주는 JS메서드
  const pct = (changeRate * 100).toFixed(1)
  return changeRate >= 0 ? `▲ +${pct}%` : `▼ ${pct}%`
}

export function TradePage() {
  const [tab, setTab] = useState<StockTab>('MINE')
  const { data: member } = useMemberQuery()
  const { data: stocks = [], isPending, isError } = useStocksQuery()
  const { data: holdings = [] } = useHoldingsQuery()
  const { data: portfolio } = usePortfolioQuery()
  const { data: myHouses } = useMyHousesQuery()

  const cash = member?.cash ?? 0
  // 내가 가지고 있는 종목을 code랑 quantity로 배열사전을 하나 만들어서
  const quantityByCode = new Map(holdings.map((h) => [h.code, h.quantity]))
  // map((s) => ()) 종목을 하나씩 꺼내서 새 모양으로 바꾼 새 배열을 만든다
  // ({}) 새 객체를 돌려줌
  // ...s : 스프레드, s가 가지고 있는 칸을 전부 복사
  // ,quantity : 거기에다가 quantity 라는 값을 추가함
  // 전체 주식 종목 목록에다가 내가 가지고 있는 주식의 수량을 붙임
  const allRows = stocks.map((s) => ({ ...s, quantity: quantityByCode.get(s.code) ?? 0 }))

  const isMineTab = tab === 'MINE'
  const rows = isMineTab ? allRows.filter((s) => s.quantity > 0) : allRows

  const stockValue = portfolio?.stockValue ?? 0
  const tier = myHouses?.tier ?? 'GEOLBAENGI'
  const tierInfo = TIER_INFO[tier]

  const handleMineClick = () => setTab('MINE')
  const handleAllClick = () => setTab('ALL')

  return (
    <div className="page-trade">
      <main className="k-wrap">
        <div className="k-title-row">
          <h1 className="k-title">종목</h1>
          <div className="k-live">
            <span className="k-live-dot" />
            <span className="k-lab">LIVE</span>
          </div>
        </div>

        <div className="k-cols">
          <div className="k-col">
            <Link to="/trade/search" aria-label="종목 검색하러 가기" className="k-search">
              <svg width="20" height="20" viewBox="0 0 8 8" aria-hidden="true">
                <path d="M2 0h3v1h-3zM1 1h1v1h-1zM5 1h1v1h-1zM0 2h1v3h-1zM6 2h1v3h-1zM1 5h1v1h-1zM5 5h1v1h-1zM2 6h3v1h-3zM6 6h1v1h-1zM7 7h1v1h-1z" />
              </svg>
              <span>종목 이름이나 코드로 찾기</span>
            </Link>

            <section className="k-card k-list" aria-label="종목 목록">
              <div className="k-tabs">
                <button type="button" onClick={handleMineClick} aria-pressed={isMineTab} className={`k-tab${isMineTab ? ' k-tab--on' : ''}`}>
                  내 종목
                </button>
                <button type="button" onClick={handleAllClick} aria-pressed={!isMineTab} className={`k-tab${isMineTab ? '' : ' k-tab--on'}`}>
                  전체
                </button>
              </div>
              <div className="k-list-body">
                {isPending && <div className="k-row-sub">불러오는 중...</div>}
                {isError && <div className="k-row-sub">종목을 못 불러왔어요. 잠시 후 다시 시도해 주세요.</div>}
                {rows.map((s) => {
                  const isUp = s.changeRate >= 0
                  return (
                    <Link key={s.code} to={`/trade/${s.code}`} className="k-row">
                      <div className="k-tick">{s.name.charAt(0)}</div>
                      <div className="k-row-main">
                        <div className="k-row-name">{s.name}</div>
                        <div className="k-row-sub">
                          {s.code}
                          {s.quantity > 0 ? ` · 보유 ${s.quantity}주` : ''}
                        </div>
                      </div>
                      <div className="k-row-right">
                        <div className="k-row-price">{formatKRW(s.price)}</div>
                        <div className={`k-row-rate ${isUp ? 'k-up' : 'k-down'}`}>{toChangeText(s.changeRate)}</div>
                      </div>
                    </Link>
                  )
                })}
              </div>
            </section>
          </div>

          <div className="k-col k-side">
            <section className="k-px" aria-label="내 투자">
              <div className="k-px-head">
                <div className="k-px-label">내 종목 평가액</div>
                <div className="k-lab k-lab--gold">STOCKS</div>
              </div>
              <div className="k-stock-value">{formatKRW(stockValue)}</div>
              <div className="k-px-sub">살 수 있는 돈 {formatKRW(cash)}</div>
              <Link to="/trade/history" className="k-px-link">
                <span>내 거래 · 실현손익</span>
                <span className="k-px-link-arrow">&gt;</span>
              </Link>
            </section>
            <section className="k-card k-cheer">
              <div className="k-avatar">
                <img src={tierInfo.image} alt={`${tierInfo.name} 꿈이`} />
              </div>
              {/* 보유 0개면 첫 매수 유도, 있으면 응원 */}
              {holdings.length === 0 ? (
                <div className="k-cheer-text">
                  뭐라도 담아야
                  <br />
                  집을 사지!
                </div>
              ) : (
                <div className="k-cheer-text">존버는 승리한다!</div>
              )}
            </section>
          </div>
        </div>
        <p className="k-note">※ 종목과 시세는 가상이에요.</p>
      </main>
    </div>
  )
}
