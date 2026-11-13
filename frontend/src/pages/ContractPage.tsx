import { useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import type { BuyHouseResponse, Tier } from '../types/api'
import { ApiError } from '../api/apiError'
import { Sprite } from '../components/Sprite'
import { useAptDealQuery } from '../hooks/useAptDealQuery'
import { useBuyHouseMutation } from '../hooks/useBuyHouseMutation'
import { useMemberQuery } from '../hooks/useMemberQuery'
import { useMyHousesQuery } from '../hooks/useMyHousesQuery'
import { useShareImage } from '../hooks/useShareImage'
import { formatDotDate, formatKRW, formatManwon, formatNumber } from '../utils/format'
import { calculateMonthlyPayment } from '../utils/loan'
import tier2 from '../assets/sprites/kkumi-tier2.svg'
import tier3 from '../assets/sprites/kkumi-tier3.svg'
import tier4 from '../assets/sprites/kkumi-tier4.svg'
import './ContractPage.css'

type TierUpInfo = { title: string, sub: string, image: string }

const TIER_UP_INFO: Partial<Record<Tier, TierUpInfo>> = {
  SEOMIN: { title: '걸뱅이 → 서민으로 변신!', sub: '집 1채 달성!', image: tier2 },
  JOKBU: { title: '서민 → 졸부로 변신!', sub: '집 2채 달성!', image: tier3 },
  GAPBU: { title: '졸부 → 갑부로 변신!', sub: '집 5채 달성!', image: tier4 },
}

type SignedContract = {
  response: BuyHouseResponse,
  cashBefore: number,
  signedAt: Date
}

function toBuyHouseErrorMessage(error: Error): string {
  if (error instanceof ApiError) {
    if (error.code === 'INSUFFICIENT_CASH') return '현금이 모자라요. 주식을 팔고 다시 와요.'
    if (error.code === 'ALREADY_OWNED_DEAL') return '이미 산 집이에요. 내 마을에서 확인해요.'
    if (error.code === 'DEAL_NOT_AVAILABLE') return '지금은 살 수 없는 거래예요.'
  }
  return '계약 중에 문제가 생겼어요. 다시 눌러 주세요.'
}

export function ContractPage() {

  // useParams() 는 { dealId: '123' } 같은 객체를 돌려주고 URL은 글자라 항상 문자열, 없으면 undefined
  // 객체의 dealId 값을 꺼내서 dealIdParam이라는 이름으로 받아라 는 뜻
  const { dealId: dealIdParam } = useParams()
  // URL에서 꺼낸 문자열을 숫자로 변경
  const dealId = Number(dealIdParam)
  const { data: deal, isPending, isError } = useAptDealQuery(dealId)
  const { data: member } = useMemberQuery()
  const { data: myHouses } = useMyHousesQuery()
  const buyHouseMutation = useBuyHouseMutation()
  const [signed, setSigned] = useState<SignedContract | null>(null)
  const [message, setMessage] = useState('')
  const [isConfirmOpen, setIsConfirmOpen] = useState(false)
  const paperRef = useRef<HTMLDivElement>(null)
  const shareImage = useShareImage()

  const cash = member?.cash ?? 0
  const ownedHouse = myHouses?.houses.find((h) => h.dealId === dealId)
  const isOwnedBefore = signed === null && ownedHouse !== undefined
  const isSigned = signed !== null || ownedHouse !== undefined
  const ownAmount = deal?.ownAmount ?? 0
  const cashBefore = signed?.cashBefore ?? cash
  const cashAfter = cashBefore - ownAmount
  const tierUp = signed?.response.isTierUp ? TIER_UP_INFO[signed.response.tier] : undefined

  const houseId = signed?.response.houseId ?? ownedHouse?.houseId
  const contractNo = houseId !== undefined ? `NO. KKUM-${String(houseId).padStart(4, '0')}` : 'NO. KKUM-____'
  const contractDate = formatDotDate(signed?.signedAt ?? (ownedHouse ? new Date(ownedHouse.purchasedAt) : new Date()))

  const handleSign = () => {
    if (!deal) return
    setMessage('')
    buyHouseMutation.mutate(
      { dealId: deal.dealId },
      {
        onSuccess: (response) => setSigned({ response, cashBefore: cash, signedAt: new Date() }),
        onError: (error) => setMessage(toBuyHouseErrorMessage(error)),
      },
    )
  }

  const canSign = deal?.affordability === 'OK' && myHouses !== undefined && !buyHouseMutation.isPending

  const handleStampClick = () => setIsConfirmOpen(true)
  const handleConfirmNo = () => setIsConfirmOpen(false)
  const handleConfirmYes = () => {
    setIsConfirmOpen(false)
    handleSign()
  }

  const shareFileName = `kkumi-contract-${houseId ?? dealId}.png`
  // 계약서 종이 영역만 이미지로
  const handleShareClick = () => {
    if (paperRef.current) shareImage.share(paperRef.current, shareFileName, '꿈이 부동산 매매 계약서')
  }

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
              {formatNumber(cash)}
            </div>
          </div>
        </div>
      </header>

      {isConfirmOpen && deal && (
        <div className="contract-confirm" role="dialog" aria-modal="true" aria-labelledby="contract-confirm-title">
          <div className="contract-confirm-box">
            <div id="contract-confirm-title" className="contract-confirm-title">도장을 찍으시겠습니까?</div>
            <div className="contract-confirm-body">
              내 돈 40% {formatKRW(deal.ownAmount)}이 빠져나가요<br />
              계약 후 남은 현금 <b>{formatKRW(cashAfter)}</b>
            </div>
            <div className="contract-confirm-actions">
              <button type="button" className="contract-confirm-no" onClick={handleConfirmNo}>NO</button>
              <button type="button" className="contract-confirm-yes" onClick={handleConfirmYes}>YES</button>
            </div>
          </div>
        </div>
      )}

      {shareImage.previewUrl && (
        <div className="contract-confirm" role="dialog" aria-modal="true" aria-label="계약서 이미지">
          <div className="contract-confirm-box contract-share-box">
            <img src={shareImage.previewUrl} alt="계약서 이미지" className="contract-share-img" />
            <div className="contract-confirm-body">이미지를 길게 눌러 사진에 저장해요</div>
            <div className="contract-confirm-actions">
              <a href={shareImage.previewUrl} download={shareFileName} className="contract-confirm-no contract-share-download">내려받기</a>
              <button type="button" className="contract-confirm-yes" onClick={shareImage.closePreview}>닫기</button>
            </div>
          </div>
        </div>
      )}

      <main className="k-wrap">
        {isPending && <p className="k-note">계약서 준비 중...</p>}
        {isError && (
          <p className="k-note">
            그런 집은 없어요. <Link to="/apartments">집 찾기로 가기</Link>
          </p>
        )}
        {deal && (
          <div className="k-cols">
            <div className="contract-paper-wrap">
              <div className="contract-paper" ref={paperRef}>
                <div className="contract-watermark" aria-hidden="true">FAKE</div>
                <div className="contract-head">
                  <h1 className="contract-title">부동산 매매 계약서</h1>
                  <div className="k-lab" style={{ color: '#6B6F80' }}>{contractNo}</div>
                </div>
                <div className="contract-table">
                  <div className="k-tr"><div className="k-th">매도인</div><div className="k-td">꿈이</div></div>
                  <div className="k-tr"><div className="k-th">매수인</div><div className="k-td">{member?.nickname}</div></div>
                  <div className="k-tr"><div className="k-th">물건</div><div className="k-td">{deal.complexName} · {deal.region} · 전용 {deal.area}㎡ · {deal.floor}층</div></div>
                  <div className="k-tr"><div className="k-th">매매대금</div><div className="k-td"><b className="contract-price">{formatManwon(deal.price / 10_000)}</b></div></div>
                  <div className="k-tr"><div className="k-th">내 돈 40%</div><div className="k-td"><b>{formatKRW(deal.ownAmount)}</b> (내 지갑에서 차감)</div></div>
                  <div className="k-tr"><div className="k-th">대출 60%</div><div className="k-td">{formatKRW(deal.loanAmount)} (연 3.7%, 30년 원리금균등)</div></div>
                  <div className="k-tr"><div className="k-th">월 상환액</div><div className="k-td">{formatKRW(calculateMonthlyPayment(deal.loanAmount))} (예상)</div></div>
                  <div className="k-tr"><div className="k-th">계약일</div><div className="k-td">{contractDate}</div></div>
                </div>
                <div className="contract-terms">
                  <div className="contract-terms-title">특약사항</div>
                  1. 본 계약은 매수인의 투자 실력을 담보로 체결한다. <br />
                  2. 매도인은 매수인이 머지않아 현실에서도 실물 자산을 갖게 될 것을 굳게 신뢰한다.<br />
                  3. 꿈은 이루어진다. 본 특약은 그날 자동 갱신된다.
                </div>
                <div className="contract-sign-row">
                  <div className="contract-sign-text">위 계약을 증명하기 위해 매수인은 서명 날인한다.</div>
                  <div className="contract-stamp-box">
                    {isSigned ? (
                      <div className="contract-stamp">꿈이<span>계약 완료</span></div>
                    ) : (
                      <button type="button" className="contract-stamp-btn" onClick={handleStampClick} disabled={!canSign} aria-label="도장 찍기">
                        <span>(인)</span>
                        {buyHouseMutation.isPending ? '찍는 중...' : '여기 쾅!'}
                      </button>
                    )}
                  </div>
                </div>
              </div>
            </div>

            <div className="k-col">
              {!isOwnedBefore && (
                <section className="k-card" aria-label="계약 요약">
                  <div className="contract-summary-title">계약 요약</div>
                  <div className="contract-summary-body">
                    <div className="contract-summary-row"><span>계약 전 지갑</span><span className="contract-tnum">{formatKRW(cashBefore)}</span></div>
                    <div className="contract-summary-row"><span>내 돈 40%</span><span className="contract-minus">- {formatKRW(ownAmount)}</span></div>
                    <div className="contract-summary-row contract-summary-total"><span className="contract-bold">계약 후 지갑</span><span className="contract-total-num">{formatKRW(cashAfter)}</span></div>
                  </div>
                </section>
              )}
              {tierUp && (
                <section className="contract-tierup" aria-label="변신">
                  <div className="contract-tierup-img"><img src={tierUp.image} alt="변신한 꿈이" /></div>
                  <div className="contract-tierup-text">
                    <div className="contract-tierup-title">{tierUp.title}</div>
                    <div className="contract-tierup-sub">{tierUp.sub}</div>
                  </div>
                </section>
              )}

              <div className="contract-actions">
                {isSigned ? (
                  <>
                    <button type="button" className="contract-btn-share" onClick={handleShareClick} disabled={shareImage.isSharing}>
                      {shareImage.isSharing ? '이미지 만드는 중...' : '계약서 캡처해서 자랑하기'}
                    </button>
                    <Link to="/village" className="contract-btn-village">내 마을 보러 가기</Link>
                    <Link to="/apartments" className="contract-btn-reset">다른 집 보러 가기</Link>
                  </>
                ) : (
                  <>
                    <p className="contract-stamp-hint">계약서의 도장 칸을 눌러 계약해요</p>
                    <Link to="/apartments" className="contract-btn-village">집 목록으로</Link>
                  </>
                )}
              </div>
              {!isSigned && deal.affordability !== 'OK' && (
                <p className="k-note">{formatKRW(deal.shortage)} 모자라요. 돈을 불려서 다시 와요.</p>
              )}
              {message && <p className="k-note">{message}</p>}
              {shareImage.errorMessage && <p className="k-note">{shareImage.errorMessage}</p>}
            </div>
          </div>
        )}
      </main>
    </div>
  )
}
