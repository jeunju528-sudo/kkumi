// 주식 매매/매도 화면 들어왔을 때 호출됨
import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import type { StockResponse } from '../types/api'
import { ApiError } from '../api/apiError'
import { useMemberQuery } from '../hooks/useMemberQuery'
import { useStocksQuery } from '../hooks/useStocksQuery'
import { useHoldingsQuery } from '../hooks/useHoldingsQuery'
import { useBuyStockMutation } from '../hooks/useBuyStockMutation'
import { useSellStockMutation } from '../hooks/useSellStockMutation'
import { useStockPricesQuery } from '../hooks/useStockPricesQuery'
import { useStockCandlesQuery } from '../hooks/useStockCandlesQuery'
import { CandleChart } from '../components/CandleChart'
import { toClockLabel, toMonthDayLabel, toSlotCount, toThreeAxisLabels } from '../utils/chart'
import { OrderHeader } from '../components/OrderHeader'
import { formatKRW, formatNumber, formatSignedKRW, formatAbsRate } from '../utils/format'
import tier1 from '../assets/sprites/kkumi-tier1.svg'
import './OrderPage.css'

type OrderMode = 'buy' | 'sell'

type OrderPanelProps = {
  stock: StockResponse,
  held: number,
  avgPrice?: number,
  cash: number
}

type ChartView = 'MINUTE' | 'DAY'

// 차트 탭: 분봉(최근 24시간 1분 기록) / 일봉(오늘 포함 최근 30일) (ADR 056)
const CHART_VIEWS: { value: ChartView; label: string }[] = [
  { value: 'MINUTE', label: '분봉' },
  { value: 'DAY', label: '일봉' },
]
// 칸 수 범위: 데이터가 적으면 최소 폭(분봉 1시간, 일봉 7일)에 오른쪽 정렬, 많으면 꽉 채움
const MINUTE_SLOTS = { min: 61, max: 1441 } // 1분 기록 + 지금 가격 1칸
const DAY_SLOTS = { min: 7, max: 30 }

// 사용자가 볼 에러 규칙
function toOrderErrorText(error: Error): string {
  if (error instanceof ApiError) {
    if (error.code === 'INSUFFICIENT_CASH') return '현금이 모자라요.'
    if (error.code === 'INSUFFICIENT_HOLDING') return '가진 주식보다 많이 팔 수 없어요.'
    if (error.code === 'INVALID_REQUEST') return '수량을 다시 확인해 주세요.'
  }
  return '주문이 안 됐어요. 잠시 후 다시 시도해 주세요.'
}

// 종목별 시세·주문 패널 (종목 바뀌면 key로 리셋)
// props : 종목, 보유수량, 현금
function OrderPanel({ stock, held, avgPrice, cash }: OrderPanelProps) {
  // useState값은 브라우저 탭의 메모리(js가 쓰는 ram)에 저장됨, 서버는 전혀 모름
  // 컴포넌트가 화면에서 빠질 때 값이 사라짐
  // tanStack캐시도 탭 메모리 안에 저장되고, 새로고침이나 탭 닫기하면 사라지지만 페이지를 옮겨도 남아있음
  // useState : react한테 맡겨서 다음 렌더에도 기억되는 값
  // 사용자 행동이나 시간에 따라 바뀌고 화면에 보여야하는 값은 state로 관리함
  // const [aa, setAa ] = useState(1) 
  //      현재값, 바꾸는 함수        초기값
  const [mode, setMode] = useState<OrderMode>('buy') // 매수/매도, 담기/팔기 버튼 누를때 setMode
  const [qty, setQty] = useState(1) // 사용자가 고른 수량, +/- 버튼 누를 때 setQty
  const [message, setMessage] = useState<string | null>(null) // 말풍선에 띄울 결과 문구
  const [lastPrice, setLastPrice] = useState(stock.price) // 직전 렌더의 가격, 가격이 바뀔때 setLastPrice
  const [flash, setFlash] = useState(0) // 방금 올랐는지 내렸는지 체크, 가격 바뀔 때 setFlash
  const [chartView, setChartView] = useState<ChartView>('MINUTE') // 차트 탭 (분봉 / 일봉)
  const buyMutation = useBuyStockMutation() // 주문 버튼 눌렀을 때 사용(매수)
  const sellMutation = useSellStockMutation() // 주문 버튼 눌렀을 때 사용(매도)
  const { data: history = [] } = useStockPricesQuery(stock.code) // 분봉: 최근 24시간 1분 기록 (1분마다 다시 받음)
  const { data: candles = [] } = useStockCandlesQuery(stock.code) // 일봉: 최근 30일 (오늘 일봉도 1분마다 바뀜)

  const { price } = stock // 종목에서 현재가를 꺼내서 ( 5초마다 새 값이 props 로 들어옴)

  if (price !== lastPrice) { // 현재가와 직전에 기억한 가격과 다르면 => 시세가 바뀌었으면
    setLastPrice(price) // 새 가격을 기억하고
    setFlash(price > lastPrice ? 1 : -1) // 올랐으면 빨강, 내렸으면 파랑
  }

  const isBuy = mode === 'buy'
  // changeRate : 오늘 시작가 대비 지금 가격이 몇 %가 바뀌었는지를 계산한 값
  const isUp = stock.changeRate >= 0
  const pct = Math.abs(stock.changeRate * 100).toFixed(1) // 등락률 숫자만 첫번째 소숫점까지만
  const maxBuy = Math.floor(cash / price) // 현금으로 살 수 있는 최대 수량
  const max = isBuy ? maxBuy : held // 지금 모드의 최대 수량, 매수면 maxBuy만큼 매도면 held(보유수량만큼)
  const q = Math.min(Math.max(1, qty), Math.max(1, max)) // 실제 주문 수량, 1이상 최대 이하

  const hasAvg = avgPrice !== undefined             // 평단이 있는지 (avgPrice가 undefined가 아님)
  const avgDiff = hasAvg ? Math.round(price - avgPrice) : 0         // 지금가 - 평단 (원 단위로 반올림)
  const avgRate = hasAvg ? avgDiff / avgPrice : 0             // 평단 대비 비율 (0.05 = +5%)
  const sellProfit = hasAvg ? avgDiff * q : 0         // 이대로 팔면 손익 = 차이 × 주문 수량

  const isSubmitting = buyMutation.isPending || sellMutation.isPending // 주문 요청 중인지~
  const canSubmit = max > 0 && !isSubmitting // 버튼 눌러도 되는지~ max가 0보다 크고 주문 요청 중이 아닌지~

  const isMinuteView = chartView === 'MINUTE'

  // 분봉 값: 1분 기록 + 지금 가격(5초마다 갱신)을 오른쪽 끝에 (홈 순자산 차트와 같은 방식)
  const minuteHistory = history.slice(-(MINUTE_SLOTS.max - 1))
  const chartValues = [...minuteHistory.map((h) => h.price), price]
  const chartSlots = toSlotCount(chartValues.length, MINUTE_SLOTS.min, MINUTE_SLOTS.max)

  // 차트 계산을 위해, 가격 기록을 300 X 100 그림 안의 점 좌표로 바꾸는 것
  // 싼 값은 아래에
  // 비싼 값은 위로 가게 설정
  const lo = Math.min(...chartValues) // 분봉 최저가 (지금 가격 포함)
  const hi = Math.max(...chartValues) // 분봉 최고가
  const span = Math.max(price * 0.02, hi - lo)   // 세로 폭 최소 2% (작은 흔들림은 작게)
  const step = 300 / (chartSlots - 1)            // 1칸 = 1분
  const offset = chartSlots - chartValues.length // 1시간보다 적으면 오른쪽 정렬 (지금 가격은 항상 오른쪽 끝)
  const chartPoints = chartValues
    .map((v, i) => `${((offset + i) * step).toFixed(1)},${(92 - ((v - lo) / span) * 84).toFixed(1)}`)
    .join(' ')
  // 선 아래 옅은 채움: 선 끝점에서 바닥으로 내려 닫은 다각형
  const areaPoints = `${chartPoints} 300.0,100 ${(offset * step).toFixed(1)},100`

  // 일봉 칸 수: 7일보다 적으면 오른쪽 정렬, 많으면 꽉 채움
  const dayCandles = candles.slice(-DAY_SLOTS.max)
  const candleSlots = toSlotCount(dayCandles.length, DAY_SLOTS.min, DAY_SLOTS.max)

  // 축 3칸: 데이터 있는 칸은 실제 시각·날짜, 빈 칸은 "N분 전"·"N일 전"
  const axisLabels = isMinuteView
    ? toThreeAxisLabels(chartSlots, [...minuteHistory.map((h) => toClockLabel(h.time)), '지금'], (ago) => `${ago}분 전`)
    : toThreeAxisLabels(
      candleSlots,
      dayCandles.map((c, i) => (i === dayCandles.length - 1 ? '오늘' : toMonthDayLabel(c.date))),
      (ago) => `${ago}일 전`,
    )

  // 최고·최저: 보고 있는 탭 기준 (일봉은 고가·저가 + 지금 가격)
  const rangeHigh = isMinuteView ? hi : Math.max(price, ...dayCandles.map((c) => c.high))
  const rangeLow = isMinuteView ? lo : Math.min(price, ...dayCandles.map((c) => c.low))

  // 화면 그릴 때 사용
  const deltaText = `${isUp ? '▲ +' : '▼ -'}${pct}% 오늘`
  const deltaColor = isUp ? '#D32F36' : '#2563EB'
  const flashBg = flash === 1 ? '#FFE3E6' : flash === -1 ? '#DCE9FF' : 'transparent'

  const emptyText = isBuy ? '현금이 모자라서 1주도 못 사요.' : '팔 주식이 없어요.'
  const defaultText = isBuy ? `${q}주 담아볼까?` : `${q}주 팔고 치킨 먹자!`
  const bubbleText = message ?? (max === 0 ? emptyText : defaultText)

  // 버튼 눌렀을 때 handler
  const handleBuyMode = () => {
    setMode('buy')
    setMessage(null)
  }
  const handleSellMode = () => {
    setMode('sell')
    setMessage(null)
  }

  // 수량 버튼 눌렀을 때 handler
  const handleDecrease = () => setQty(Math.max(1, q - 1)) // 감소, 1이랑 현재수량에서 1뺀 것 중에 큰 것
  const handleIncrease = () => setQty(Math.min(Math.max(1, max), q + 1)) // 증가, 최대로 살 수 있는 양이랑 현재수량에서 1더한 것 중에 작은 것
  const handleSubmit = () => {
    const request = { code: stock.code, quantity: q }
    const onError = (error: Error) => setMessage(toOrderErrorText(error))
    if (isBuy) {
      // mutate : 쓰기 요청을 지금 보내라고 함
      // mutationFn(buyStock)에 request 값을 넣어서 실행하라고 한것
      buyMutation.mutate(request, { onSuccess: () => setMessage(`${q}주 샀어! 부자가 될거야!`), onError })
    }
    else {
      sellMutation.mutate(request, { onSuccess: () => setMessage(`${q}주 팔았어! 치킨 각`), onError })
    }
  }


  return (
    <div className="k-cols">
      <section className="k-card" aria-label="종목 시세">
        <div className="k-head">
          <div className="k-tick">{stock.name.charAt(0)}</div>
          <div className="k-head-info">
            <h1 className="k-name">{stock.name}</h1>
            <div className="k-sub">
              {stock.code} · 보유 {held}주
            </div>
          </div>
          <div className="k-live">
            <span className="k-live-dot" />
            <span className="k-lab">LIVE</span>
          </div>
        </div>
        <div className="k-price" style={{ background: flashBg }}>
          {formatNumber(price)}
          <span className="k-price-unit">원</span>
        </div>
        <div className="k-delta" style={{ color: deltaColor }}>
          {deltaText}
        </div>
        <div className="k-chart-tabs">
          {CHART_VIEWS.map((v) => (
            <button
              key={v.value}
              type="button"
              className={`k-chart-tab${chartView === v.value ? ' k-chart-tab--on' : ''}`}
              aria-pressed={chartView === v.value}
              onClick={() => setChartView(v.value)}
            >
              <span>{v.label}</span>
            </button>
          ))}
        </div>
        {isMinuteView ? (
          <svg
            viewBox="0 0 300 100"
            width="100%"
            role="img"
            aria-label="최근 24시간 분봉 그래프"
            preserveAspectRatio="none"
            className="k-chart"
          >
            <line
              x1="0"
              y1="99"
              x2="300"
              y2="99"
              stroke="#E6E8F0"
              strokeWidth="1.5"
              strokeDasharray="4 4"
              vectorEffect="non-scaling-stroke"
            />
            <polygon points={areaPoints} fill={deltaColor} fillOpacity="0.08" />
            <polyline
              points={chartPoints}
              fill="none"
              stroke={deltaColor}
              strokeWidth="2"
              strokeLinejoin="round"
              strokeLinecap="round"
              vectorEffect="non-scaling-stroke"
            />
          </svg>
        ) : (
          <CandleChart candles={dayCandles} livePrice={price} slots={candleSlots} />
        )}
        <div className="k-chart-axis">
          {axisLabels.map((label, i) => (
            <span key={i}>{label}</span>
          ))}
        </div>
        {candles.length < 3 && (
          <div className={`k-chart-hint${isMinuteView ? ' k-chart-hint--off' : ''}`}>일봉은 하루에 1개씩 쌓여요.</div>
        )}
        <div className="k-chart-range">
          <span>
            최고 <b className="k-chart-hi">{formatNumber(rangeHigh)}원</b>
          </span>
          <span>
            최저 <b className="k-chart-lo">{formatNumber(rangeLow)}원</b>
          </span>
        </div>
      </section>

      <section className="k-card k-order" aria-label="주문">
        <div className="k-modes">
          <button
            type="button"
            className="k-mode"
            onClick={handleBuyMode}
            style={{ background: isBuy ? '#D32F36' : '#FFFFFF', color: isBuy ? '#FFFFFF' : '#231B3A' }}
          >
            담기 (매수)
          </button>
          <button
            type="button"
            className="k-mode"
            onClick={handleSellMode}
            style={{ background: isBuy ? '#FFFFFF' : '#2563EB', color: isBuy ? '#231B3A' : '#FFFFFF' }}
          >
            팔기 (매도)
          </button>
        </div>

        <div className="k-talk">
          <div className="k-avatar">
            <img src={tier1} alt="걸뱅이 꿈이" />
          </div>
          <div className="k-bubble">{bubbleText}</div>
        </div>

        <div className="k-qty-row">
          <div className="k-qty-label">수량</div>
          <div className="k-stepper">
            <button type="button" className="k-step" aria-label="수량 줄이기" onClick={handleDecrease}>
              −
            </button>
            <div className="k-qty">
              {q}
              <span className="k-qty-unit">주</span>
            </div>
            <button type="button" className="k-step" aria-label="수량 늘리기" onClick={handleIncrease}>
              +
            </button>
          </div>
        </div>

        <div className="k-sum">
          <div className="k-sum-row">
            <span className="k-sum-label">{isBuy ? '살 수 있는 돈' : '팔 수 있는 수량'}</span>
            <span className="k-sum-val">
              {isBuy ? `${formatKRW(cash)} (최대 ${maxBuy}주)` : `${held}주`}
            </span>
          </div>
          {hasAvg && (
            <div className="k-sum-row">
              <span className="k-sum-label">{isBuy ? `내 평단 ${formatKRW(avgPrice)}` : '이대로 팔면'}</span>
              <span className={`k-sum-val ${avgDiff > 0 ? 'k-up' : avgDiff < 0 ? 'k-down' : ''}`}>
                {isBuy
                  ? avgDiff === 0 ? '평단이랑 같아요' : `평단보다 ${formatAbsRate(avgRate)} ${avgDiff > 0 ? '높아요' : '낮아요'}`
                  : `${formatSignedKRW(sellProfit)} ${sellProfit >= 0 ? '이득' : '손해'}`}
              </span>
            </div>
          )}
          <div className="k-sum-row">
            <span className="k-sum-label">주문 금액 (지금 시세)</span>
            <span className="k-sum-total">{formatKRW(price * q)}</span>
          </div>
        </div>

        <button type="button" className="k-cta" onClick={handleSubmit} disabled={!canSubmit} style={{ background: isBuy ? '#D32F36' : '#2563EB' }}>
          {isSubmitting ? '주문 중...' : isBuy ? '담기' : '팔기'}
        </button>
        <p className="k-note">실제 돈은 1원도 안 나가요. 누르는 순간의 시세로 체결돼요.</p>
      </section>
    </div>
  )
}

//  데이터를 모으는 곳
export function OrderPage() {
  const { code } = useParams() // 주소 /trade/KK0001에서 'KK0001'을 꺼냄
  const { data: member } = useMemberQuery() // 회원 정보
  const { data: stocks = [], isPending, isError } = useStocksQuery() // 주식 목록 + 로딩/에러 상태
  const { data: holdings = [] } = useHoldingsQuery() // 내 보유 목록

  const cash = member?.cash ?? 0 // 사용자 현금 가지고 있는 값
  const stock = stocks.find((s) => s.code === code) // 주식 종목 중 이 종목 하나
  const holding = holdings.find((h) => h.code === code)
  const held = holding?.quantity ?? 0 // 그 종목의 보유수량
  const avgPrice = holding?.avgPrice
  // return (...) 안에 있는 html 처럼 생긴 것은 JSX : 화면을 이렇게 그려줘 하는 설계도
  // react가 이걸 보고 실제 브라우저 화면을 만듦
  // <소문자태그> ex) <main /> : HTML 태그 (이 페이지의 본문 영역을 뜻함)
  // <대문자 태그> ex) <OrderHeader /> : 직접 만든 컴포넌트, 이 함수를 불러서 걔가 돌려준 화면을 넣어라
  // cash={cash} props로 cash 값 넣음
  // { ... } 중괄호 : JSX 안에 JS값 끼워 넣을 때 사용
  // {조건 && <p />} : 조건이 참일 때만 화면을 그려라
  // 함수 하나당 return이 하나인 것이 규칙!
  // 컴포넌트 하나당 JSX 덩어리 하나만 return 해야된다.
  return (
    <div className="page-order">
      <OrderHeader cash={cash} />
      <main className="k-wrap">
        <Link to="/trade" className="k-back">
          &lt; 종목 목록
        </Link>
        {isPending && <p className="k-note">불러오는 중...</p>}
        {isError && <p className="k-note">종목을 못 불러왔어요. 잠시 후 다시 시도해 주세요.</p>}
        {!isPending && !isError && !stock && <p className="k-note">그런 종목은 없어요.</p>}
        {stock && <OrderPanel key={stock.code} stock={stock} held={held} avgPrice={avgPrice} cash={cash} />}
      </main>
    </div>
  )
}

// 컴포넌트 : 대문자로 시작하고, JSX를 return 하는 함수
// 컴포넌트를 호출할 때 붙은 key는 react가 이 컴포넌트가 아까 컴포넌트랑 같은 놈인가를 판단하는 이름표
// <OrderPanel key = {}
// 같은 값이면 state를 그대로 유지함, 다른 값이면 기존걸 다 버리고 새로 만들어서 state가 다 처음값으로 돌아감
// rows.map 의 <Link key={s.code} 이것도 줄마다 이름표를 붙여서 목록이 바뀌어도 줄 구분을 react가 하게 해줌
