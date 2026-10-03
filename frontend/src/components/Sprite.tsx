import type { CSSProperties } from 'react'
import type { SpriteId } from './PixelDefs'

type SpriteProps = {
  id: SpriteId
  viewBox: string
  width: number
  height: number
  className?: string
  style?: CSSProperties
}

// PixelDefs 심볼 참조. 정수 배율만 사용
export function Sprite({ id, viewBox, width, height, className, style }: SpriteProps) {
  return (
    <svg
      className={className}
      viewBox={viewBox}
      width={width}
      height={height}
      style={{ shapeRendering: 'crispEdges', ...style }}
      aria-hidden="true"
    >
      <use href={`#${id}`} />
    </svg>
  )
}
