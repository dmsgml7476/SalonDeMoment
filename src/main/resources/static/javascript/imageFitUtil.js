export function adjustImageFit(img, containerRatio = 3 / 4) {
  const { naturalWidth: w, naturalHeight: h } = img;
  if (!w || !h) return;

  img.classList.remove('portrait', 'landscape');

  if (w / h > containerRatio) {
    img.classList.add('portrait');   // 가로가 더 길다 → height 기준
  } else {
    img.classList.add('landscape');  // 세로가 더 길다 → width 기준
  }
}

export function adjustImageFitAll(selector = 'img.img-fit', containerRatio = 3 / 4) {
  document.querySelectorAll(selector).forEach(img => {
    if (img.complete) {
      adjustImageFit(img, containerRatio);
    } else {
      img.addEventListener('load', () => adjustImageFit(img, containerRatio));
    }
  });
}

// 사용할때 adjustImageFit(img); // 기본값 3/4
// adjustImageFit(img, 1); // 1:1 비율
// adjustImageFitAll('.wide-box img', 4 / 3); 이런 식으로 쓰면 됨.