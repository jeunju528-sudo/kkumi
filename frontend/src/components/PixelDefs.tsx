// 픽셀 스프라이트 심볼 모음. 한 번만 렌더링하고 어디서든 <Sprite id="px-house" /> 로 사용한다.
// 색은 CSS 변수(--roof, --wall)로 바꿀 수 있다.
const DEFS = `<symbol id="px-coin" viewBox="0 0 8 8">
<path d="M2 0h4v1h-4zM1 1h6v1h-6zM0 2h8v4h-8zM1 6h6v1h-6zM2 7h4v1h-4z" style="fill: #FFC93C"></path>
<path d="M3 2h2v4h-2z" style="fill: #E0A800"></path>
</symbol><symbol id="px-apt-low" viewBox="0 0 16 20">
<path d="M0 2h16v18h-16zM10 0h4v3h-4z" style="fill: #2B2038"></path>
<path d="M11 1h2v1h-2z" style="fill: #9AA0B5"></path>
<path d="M1 3h14v2h-14z" style="fill: var(--roof, #D64545)"></path>
<path d="M1 3h14v1h-14z" style="fill: #FFFFFF; opacity: 0.25"></path>
<path d="M1 5h14v14h-14z" style="fill: var(--wall, #F3E3B0)"></path>
<path d="M13 5h2v14h-2zM1 8h12v1h-12zM1 11h12v1h-12zM1 14h12v1h-12z" style="fill: #000000; opacity: 0.12"></path>
<path d="M2 6h2v2h-2zM6 6h2v2h-2zM10 6h2v2h-2zM2 9h2v2h-2zM6 9h2v2h-2zM10 9h2v2h-2zM2 12h2v2h-2zM6 12h2v2h-2zM10 12h2v2h-2zM2 16h2v2h-2zM11 16h2v2h-2z" style="fill: #FFD76A"></path>
<path d="M6 6h2v2h-2zM2 9h2v2h-2zM10 12h2v2h-2z" style="fill: #6EC1FF"></path>
<path d="M6 16h4v3h-4z" style="fill: #8A5A3A"></path>
<path d="M9 17h1v1h-1z" style="fill: #FFC93C"></path>
</symbol><symbol id="px-apt-mid" viewBox="0 0 14 26">
<path d="M0 2h14v24h-14zM6 0h2v3h-2z" style="fill: #2B2038"></path>
<path d="M6 0h2v1h-2z" style="fill: #FF6B6F"></path>
<path d="M1 3h12v2h-12z" style="fill: var(--roof, #D64545)"></path>
<path d="M1 3h12v1h-12z" style="fill: #FFFFFF; opacity: 0.25"></path>
<path d="M1 5h12v20h-12z" style="fill: var(--wall, #F3E3B0)"></path>
<path d="M11 5h2v20h-2zM1 8h10v1h-10zM1 11h10v1h-10zM1 14h10v1h-10zM1 17h10v1h-10zM1 20h10v1h-10z" style="fill: #000000; opacity: 0.12"></path>
<path d="M3 6h2v2h-2zM7 6h2v2h-2zM3 9h2v2h-2zM7 9h2v2h-2zM3 12h2v2h-2zM7 12h2v2h-2zM3 15h2v2h-2zM7 15h2v2h-2zM3 18h2v2h-2zM7 18h2v2h-2z" style="fill: #FFD76A"></path>
<path d="M7 6h2v2h-2zM3 12h2v2h-2zM7 18h2v2h-2z" style="fill: #6EC1FF"></path>
<path d="M5 21h4v4h-4z" style="fill: #8A5A3A"></path>
<path d="M8 23h1v1h-1z" style="fill: #FFC93C"></path>
</symbol><symbol id="px-tree" viewBox="0 0 10 12">
<path d="M3 0h4v1h-4zM1 1h8v1h-8zM0 2h10v4h-10zM1 6h8v1h-8zM3 7h4v1h-4z" style="fill: #3E9B3E"></path>
<path d="M2 2h2v1h-2zM1 3h1v1h-1z" style="fill: #6CC24A"></path>
<path d="M4 8h2v4h-2z" style="fill: #7A4B3A"></path>
</symbol><symbol id="px-cloud" viewBox="0 0 16 6">
<path d="M4 0h6v1h-6zM2 1h10v1h-10zM0 2h16v3h-16zM2 5h12v1h-12z" style="fill: #FFFFFF"></path>
</symbol><symbol id="px-clock" viewBox="0 0 8 8">
<path d="M2 0h4v1h-4zM1 1h1v1h-1zM6 1h1v1h-1zM0 2h1v4h-1zM7 2h1v4h-1zM1 6h1v1h-1zM6 6h1v1h-1zM2 7h4v1h-4z" style="fill: #B9BCD0"></path>
<path d="M3 2h1v3h-1zM3 4h2v1h-2z" style="fill: #FFFFFF"></path>
</symbol><symbol id="px-house" viewBox="0 0 22 18">
<path d="M15 0h3v3h-3z" style="fill: #2B2038"></path>
<path d="M16 1h1v2h-1z" style="fill: #B5533C"></path>
<path d="M9 0h4v1h-4zM7 1h8v1h-8zM5 2h12v1h-12zM3 3h16v1h-16zM1 4h20v1h-20zM0 5h22v1h-22z" style="fill: #2B2038"></path>
<path d="M9 1h4v1h-4zM7 2h8v1h-8zM5 3h12v1h-12zM3 4h16v1h-16zM1 5h20v1h-20z" style="fill: var(--roof, #D64545)"></path>
<path d="M10 3h2v2h-2z" style="fill: #FFD76A"></path>
<path d="M2 6h18v12h-18z" style="fill: #2B2038"></path>
<path d="M3 6h16v11h-16z" style="fill: var(--wall, #F3E3B0)"></path>
<path d="M3 6h16v1h-16z" style="fill: #000000; opacity: 0.25"></path>
<path d="M3 16h16v1h-16z" style="fill: #C9B77E"></path>
<path d="M9 11h4v6h-4zM10 10h2v1h-2z" style="fill: #8A5A3A"></path>
<path d="M12 14h1v1h-1z" style="fill: #FFC93C"></path>
<path d="M4 9h3v3h-3zM15 9h3v3h-3z" style="fill: #6EC1FF"></path>
<path d="M5 9h1v3h-1zM4 10h3v1h-3zM16 9h1v3h-1zM15 10h3v1h-3z" style="fill: var(--wall, #F3E3B0)"></path>
<path d="M0 16h2v2h-2zM20 16h2v2h-2z" style="fill: #3E9B3E"></path>
</symbol><symbol id="px-mansion" viewBox="0 0 30 20">
<path d="M13 0h4v1h-4zM11 1h8v1h-8zM9 2h12v1h-12zM7 3h16v1h-16zM8 4h14v16h-14z" style="fill: #2B2038"></path>
<path d="M0 7h9v13h-9zM21 7h9v13h-9z" style="fill: #2B2038"></path>
<path d="M14 0h2v1h-2z" style="fill: #FFC93C"></path>
<path d="M13 1h4v1h-4zM11 2h8v1h-8zM8 3h14v1h-14z" style="fill: var(--roof, #D9A53A)"></path>
<path d="M1 8h8v2h-8zM21 8h8v2h-8z" style="fill: var(--roof, #D9A53A)"></path>
<path d="M14 2h2v1h-2z" style="fill: #FFF4D6"></path>
<path d="M9 4h12v15h-12zM1 10h8v9h-8zM21 10h8v9h-8z" style="fill: var(--wall, #FFF4D6)"></path>
<path d="M9 4h12v1h-12zM1 10h8v1h-8zM21 10h8v1h-8z" style="fill: #000000; opacity: 0.22"></path>
<path d="M10 5h1v14h-1zM19 5h1v14h-1z" style="fill: #FFFFFF"></path>
<path d="M11 5h1v14h-1zM20 5h1v14h-1z" style="fill: #000000; opacity: 0.12"></path>
<path d="M12 5h2v2h-2zM16 5h2v2h-2z" style="fill: #6EC1FF"></path>
<path d="M12 8h6v1h-6z" style="fill: #8A8FA6"></path>
<path d="M13 13h4v6h-4zM14 12h2v1h-2z" style="fill: #8A5A3A"></path>
<path d="M16 16h1v1h-1z" style="fill: #FFC93C"></path>
<path d="M12 19h6v1h-6z" style="fill: #C8CDD2"></path>
<path d="M3 12h3v3h-3zM24 12h3v3h-3z" style="fill: #6EC1FF"></path>
<path d="M4 12h1v3h-1zM3 13h3v1h-3zM25 12h1v3h-1zM24 13h3v1h-3z" style="fill: var(--wall, #FFF4D6)"></path>
<path d="M1 18h6v1h-6zM23 18h6v1h-6z" style="fill: #3E9B3E"></path>
</symbol><symbol id="px-hanok" viewBox="0 0 24 14">
<path d="M6 0h12v1h-12zM4 1h16v1h-16zM2 2h20v1h-20zM0 3h24v1h-24zM1 4h22v1h-22z" style="fill: #2B2038"></path>
<path d="M6 1h12v1h-12zM4 2h16v1h-16zM2 3h20v1h-20zM2 4h20v1h-20z" style="fill: var(--roof, #4A4E5E)"></path>
<path d="M6 1h12v1h-12z" style="fill: #6A6F84"></path>
<path d="M8 2h1v3h-1zM12 2h1v3h-1zM16 2h1v3h-1z" style="fill: #3A3E4C"></path>
<path d="M3 5h18v9h-18z" style="fill: #2B2038"></path>
<path d="M4 5h16v8h-16z" style="fill: var(--wall, #FFF1D0)"></path>
<path d="M4 5h16v1h-16z" style="fill: #000000; opacity: 0.25"></path>
<path d="M4 6h1v7h-1zM9 6h1v7h-1zM14 6h1v7h-1zM19 6h1v7h-1z" style="fill: #8A5A3A"></path>
<path d="M5 7h4v5h-4zM10 7h4v5h-4zM15 7h4v5h-4z" style="fill: #FFF9E8"></path>
<path d="M7 7h1v5h-1zM5 9h4v1h-4zM12 7h1v5h-1zM10 9h4v1h-4zM17 7h1v5h-1zM15 9h4v1h-4z" style="fill: #B88A5B"></path>
<path d="M3 13h18v1h-18z" style="fill: #9AA0A8"></path>
</symbol><symbol id="px-villa" viewBox="0 0 18 16">
<path d="M0 0h18v16h-18z" style="fill: #2B2038"></path>
<path d="M1 1h16v14h-16z" style="fill: var(--wall, #E9B8A0)"></path>
<path d="M1 1h16v1h-16z" style="fill: #8A8FA6"></path>
<path d="M1 5h16v1h-16zM1 9h16v1h-16z" style="fill: #000000; opacity: 0.15"></path>
<path d="M3 2h3v3h-3zM12 2h3v3h-3zM3 6h3v3h-3zM12 6h3v3h-3zM2 10h3v3h-3zM13 10h3v3h-3z" style="fill: #6EC1FF"></path>
<path d="M2 5h5v1h-5zM11 5h5v1h-5z" style="fill: #FFFFFF"></path>
<path d="M1 14h16v1h-16z" style="fill: #9AA0A8"></path>
<path d="M6 8h2v1h-2zM10 8h2v1h-2z" style="fill: #D64545"></path>
<path d="M8 8h2v1h-2z" style="fill: #FFFFFF"></path>
<path d="M7 10h4v5h-4z" style="fill: #8A5A3A"></path>
<path d="M10 12h1v1h-1z" style="fill: #FFC93C"></path>
</symbol><symbol id="px-tower" viewBox="0 0 12 36">
<path d="M5 0h2v5h-2z" style="fill: #2B2038"></path>
<path d="M5 0h2v1h-2z" style="fill: #FF6B6F"></path>
<path d="M0 4h12v32h-12z" style="fill: #2B2038"></path>
<path d="M1 5h10v2h-10z" style="fill: #E0A800"></path>
<path d="M1 7h10v28h-10z" style="fill: var(--glass, #5B8DEF)"></path>
<path d="M1 7h2v28h-2z" style="fill: #FFFFFF; opacity: 0.18"></path>
<path d="M1 10h10v1h-10zM1 14h10v1h-10zM1 18h10v1h-10zM1 22h10v1h-10zM1 26h10v1h-10zM1 30h10v1h-10z" style="fill: #000000; opacity: 0.18"></path>
<path d="M4 8h2v2h-2zM8 12h2v2h-2zM3 16h2v2h-2zM7 20h2v2h-2zM4 24h2v2h-2zM8 28h2v2h-2z" style="fill: #FFD76A"></path>
<path d="M4 32h4v3h-4z" style="fill: #2B2038"></path>
<path d="M5 33h2v2h-2z" style="fill: #FFD76A"></path>
</symbol>`;

export function PixelDefs() {
  return (
    <svg
      width="0"
      height="0"
      style={{ position: 'absolute' }}
      aria-hidden="true"
      dangerouslySetInnerHTML={{ __html: `<defs>${DEFS}</defs>` }}
    />
  );
}

export type SpriteId =
  | 'px-apt-low' | 'px-apt-mid' | 'px-tree' | 'px-cloud' | 'px-coin' | 'px-clock'
  | 'px-house' | 'px-mansion' | 'px-hanok' | 'px-villa' | 'px-tower';
