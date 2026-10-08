import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Sprite } from '../components/Sprite'
import { formatKRW, formatManwon, formatNumber } from '../utils/format'
import tier2 from '../assets/sprites/kkumi-tier2.svg'
import './ContractPage.css'

// 목업 데이터 (API 연동 전)
const MOCK_CONTRACT = {
  contractNo: 'NO. KKUM-0001',
  nickname: '[닉네임]',
  complexName: '태백 [단지명]',
  region: '강원 태백시',
  area: 39,
  priceManwon: 1800,
  contractDate: '2026.10.03',
  cashBefore: 8_300_000,
}

const PRICE_WON = MOCK_CONTRACT.priceManwon * 10_000
const OWN_WON = PRICE_WON * 0.4
const LOAN_WON = PRICE_WON - OWN_WON
const CASH_AFTER = MOCK_CONTRACT.cashBefore - OWN_WON

export function ContractPage() {
  const [isSigned, setIsSigned] = useState(true)

  const handleSign = () => setIsSigned(true)
  const handleReset = () => setIsSigned(false)

  return (
    <div className="page-contract k-page">
      <header className="k-top">
        <Link to="/home" className="k-logo"><span className="k-mark">꿈</span><span>꿈이</span></Link>
        <nav className="k-links" aria-label="메인 메뉴">
          <Link to="/home" className="k-link"><svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true"><path d="M3 0h2v1h-2zM2 1h4v1h-4zM1 2h6v1h-6zM0 3h8v1h-8zM1 4h2v4h-2zM5 4h2v4h-2zM3 4h2v1h-2z" /></svg><span>홈</span></Link>
          <Link to="/trade" className="k-link"><svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true"><path d="M0 5h2v3h-2zM3 3h2v5h-2zM6 1h2v7h-2z" /></svg><span>투자</span></Link>
          <Link to="/apartments" className="k-link k-on" aria-current="page"><svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true"><path d="M2 0h3v1h-3zM1 1h1v1h-1zM5 1h1v1h-1zM0 2h1v3h-1zM6 2h1v3h-1zM1 5h1v1h-1zM5 5h1v1h-1zM2 6h3v1h-3zM6 6h1v1h-1zM7 7h1v1h-1z" /></svg><span>집 찾기</span></Link>
          <Link to="/village" className="k-link"><svg width="24" height="24" viewBox="0 0 8 8" aria-hidden="true"><path d="M1 0h2v1h-2zM0 1h4v1h-4zM0 2h4v6h-4zM5 3h2v1h-2zM4 4h4v4h-4z" /></svg><span>내 마을</span></Link>
        </nav>
        <div className="k-grow" />
        <div className="k-chip">
          <Sprite id="px-coin" viewBox="0 0 8 8" width={16} height={16} />
          <div>
            <div className="k-lab">CASH</div>
            <div className="k-num" style={{ color: '#FFC93C' }}>
              {formatNumber(isSigned ? CASH_AFTER : MOCK_CONTRACT.cashBefore)}
            </div>
          </div>
        </div>
      </header>

      <main className="k-wrap">
        <div className="k-cols">
          <div className="contract-paper-wrap">
            <div className="contract-paper">
              <div className="contract-watermark" aria-hidden="true">FAKE</div>
              <div className="contract-fake-banner">이 계약서는 전부 가짜입니다 (법적 효력 0%)</div>
              <div className="contract-head">
                <h1 className="contract-title">부동산 매매 계약서</h1>
                <div className="k-lab" style={{ color: '#6B6F80' }}>{MOCK_CONTRACT.contractNo}</div>
              </div>
              <div className="contract-table">
                <div className="k-tr"><div className="k-th">매도인</div><div className="k-td">꿈이 부동산 (가짜)</div></div>
                <div className="k-tr"><div className="k-th">매수인</div><div className="k-td">{MOCK_CONTRACT.nickname} (인생역전 중)</div></div>
                <div className="k-tr"><div className="k-th">물건</div><div className="k-td">{MOCK_CONTRACT.complexName} · {MOCK_CONTRACT.region} · 전용 {MOCK_CONTRACT.area}㎡</div></div>
                <div className="k-tr"><div className="k-th">매매대금</div><div className="k-td"><b className="contract-price">{formatManwon(MOCK_CONTRACT.priceManwon)}</b></div></div>
                <div className="k-tr"><div className="k-th">내 돈 40%</div><div className="k-td"><b>{formatManwon(OWN_WON / 10_000)}</b> (내 지갑에서 차감)</div></div>
                <div className="k-tr"><div className="k-th">대출 60%</div><div className="k-td">{formatManwon(LOAN_WON / 10_000)} (가짜 대출, 이자 0원)</div></div>
                <div className="k-tr"><div className="k-th">계약일</div><div className="k-td">{MOCK_CONTRACT.contractDate}</div></div>
              </div>
              <div className="contract-terms">
                <div className="contract-terms-title">특약사항</div>
                1. 이 집은 가짜 돈으로 샀으므로 이사는 불가합니다.<br />
                2. 집들이 초대는 친구가 알아서 받아가시오.<br />
                3. 자랑은 캡처로 하되, 놀림은 감수해야 합니다.
              </div>
              <div className="contract-sign-row">
                <div className="contract-sign-text">위 계약을 증명하기 위해 매수인은 서명 날인한다.</div>
                <div className="contract-stamp-box">
                  {isSigned ? (
                    <div className="contract-stamp">꿈이<span>가짜 계약 완료</span></div>
                  ) : (
                    <div className="contract-stamp-empty">(인)</div>
                  )}
                </div>
              </div>
            </div>
          </div>

          <div className="k-col">
            <section className="k-card" aria-label="계약 요약">
              <div className="contract-summary-title">계약 요약</div>
              <div className="contract-summary-body">
                <div className="contract-summary-row"><span>계약 전 지갑</span><span className="contract-tnum">{formatKRW(MOCK_CONTRACT.cashBefore)}</span></div>
                <div className="contract-summary-row"><span>내 돈 40%</span><span className="contract-minus">- {formatKRW(OWN_WON)}</span></div>
                <div className="contract-summary-row contract-summary-total"><span className="contract-bold">계약 후 지갑</span><span className="contract-total-num">{formatKRW(CASH_AFTER)}</span></div>
              </div>
            </section>

            {isSigned && (
              <section className="contract-tierup" aria-label="변신">
                <div className="contract-tierup-img"><img src={tier2} alt="서민 꿈이" /></div>
                <div className="contract-tierup-text">
                  <div className="contract-tierup-title">걸뱅이 → 서민으로 변신!</div>
                  <div className="contract-tierup-sub">집 1채 달성!</div>
                </div>
              </section>
            )}

            <div className="contract-actions">
              {isSigned ? (
                <>
                  <button type="button" className="contract-btn-share">계약서 캡처해서 자랑하기</button>
                  <Link to="/village" className="contract-btn-village">내 마을 보러 가기</Link>
                  <button type="button" className="contract-btn-reset" onClick={handleReset}>다시 해보기</button>
                </>
              ) : (
                <button type="button" className="contract-btn-stamp" onClick={handleSign}>도장 쾅! 찍기</button>
              )}
            </div>
            <p className="k-note">※ 돈을 더 불린 뒤를 가정한 샘플이에요.</p>
          </div>
        </div>
      </main>
    </div>
  )
}
