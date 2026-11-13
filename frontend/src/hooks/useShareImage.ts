import { useState } from 'react'
import { toBlob } from 'html-to-image'

// 화면 영역 → PNG → 공유 시트. 공유 시트를 못 쓰면 미리보기(길게 눌러 저장)
export function useShareImage() {
  const [isSharing, setIsSharing] = useState(false)
  const [previewUrl, setPreviewUrl] = useState<string | null>(null)
  const [errorMessage, setErrorMessage] = useState('')

  const share = async (node: HTMLElement, fileName: string, title: string) => {
    setIsSharing(true)
    setErrorMessage('')
    try {
      // 사파리 첫 변환에서 폰트·이미지가 빠지는 경우가 있어 한 번 더 변환
      await toBlob(node, { pixelRatio: 2 })
      const blob = await toBlob(node, { pixelRatio: 2 })
      if (!blob) throw new Error('이미지 변환 실패')

      const file = new File([blob], fileName, { type: 'image/png' })
      if (navigator.canShare?.({ files: [file] })) {
        try {
          await navigator.share({ files: [file], title })
          return
        } catch (error) {
          // 공유 시트 닫기 = AbortError, 그 외(클릭 후 시간 초과 등)는 미리보기로
          if (error instanceof DOMException && error.name === 'AbortError') return
        }
      }
      setPreviewUrl(URL.createObjectURL(blob))
    } catch {
      setErrorMessage('이미지를 만들지 못했어요. 다시 눌러 주세요.')
    } finally {
      setIsSharing(false)
    }
  }

  // 미리보기 닫기 + 임시 URL 해제
  const closePreview = () => {
    if (previewUrl) URL.revokeObjectURL(previewUrl)
    setPreviewUrl(null)
  }

  return { share, isSharing, previewUrl, closePreview, errorMessage }
}
