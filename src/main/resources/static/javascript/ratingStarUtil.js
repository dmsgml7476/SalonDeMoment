export function renderStars(rating, container) {
  const fullStar = '★';
  const emptyStar = '☆';

  const fullCount = Math.floor(rating + 0.5); // 0.5 기준 올림
  const emptyCount = 5 - fullCount;

  let html = fullStar.repeat(fullCount) + emptyStar.repeat(emptyCount);

  container.innerHTML = html;
}
