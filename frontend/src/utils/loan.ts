// 대출 안내 표시용 (화면 표시만, 현금 차감 없음)
export const LOAN_RATE = 0.037 // 연 3.7% 고정 (실제 시중 금리 아님)
export const LOAN_YEARS = 30

// 원리금균등 월 상환액, 원 미만 반올림
export function calculateMonthlyPayment(principal: number): number {
  const monthlyRate = LOAN_RATE / 12
  const months = LOAN_YEARS * 12
  return Math.round((principal * monthlyRate) / (1 - (1 + monthlyRate) ** -months))
}
