// 대출 안내 표시용 (화면 표시만, 현금 차감 없음)
export const LOAN_RATE = 0.037 // 연 3.7% 고정 (실제 시중 금리 아님)
export const LOAN_YEARS = 30

// 원리금균등 월 상환액, 원 미만 반올림
export function calculateMonthlyPayment(principal: number): number {
  const monthlyRate = LOAN_RATE / 12
  const months = LOAN_YEARS * 12
  return Math.round((principal * monthlyRate) / (1 - (1 + monthlyRate) ** -months))
}

// 대출 = 집값 - 내 돈(집값 × 40%, 원 미만 올림). 서버 HousePriceRule과 같은 계산
export function toLoanAmount(price: number): number {
  return price - Math.ceil((price * 4) / 10)
}
