import { autoBindDragScroll, resetAllDragScrollState } from '/javascript/dragScroll.js';
import { renderStars } from '/javascript/ratingStarUtil.js';
import { adjustImageFit } from '/javascript/imageFitUtil.js';

let page = 1; // 초기 페이지 (0번은 서버 렌더링됨)
let isLoading = false;
let isLastPage = false;

document.addEventListener('DOMContentLoaded', () => {
  autoBindDragScroll();
  bindReviewBoxes();
  setupInfiniteScroll(); // 무한 스크롤 시작
  tryScrollToReservationReview();
});

/* 리뷰 카드 초기화 및 이미지 슬라이더 설정 */
function bindReviewBoxes() {
  document.querySelectorAll('.review-box').forEach(box => {
    const imgs = box.querySelectorAll('img');

    if (imgs.length === 0) {
      const url = createReviewImage(
        box.dataset.rating,
        box.dataset.date,
        box.dataset.hairstyle
      );
      const ph = document.createElement('img');
      ph.src = url;
      ph.alt = '리뷰 정보 이미지';
      ph.classList.add('active');
      box.appendChild(ph);
    } else {
      imgs[0].classList.add('active');
    }

    let idx = 0, timer;
    const next = () => {
      imgs[idx].classList.remove('active');
      idx = (idx + 1) % imgs.length;
      imgs[idx].classList.add('active');
    };

    box.addEventListener('mouseenter', () => {
      if (imgs.length <= 1) return;

      const allLoaded = [...imgs].every(img => img.complete);
      const startSlider = () => {
        if (timer) return;

        timer = setTimeout(() => {
          next();
          timer = setInterval(next, 2000);
        }, 1000);
      };

      if (allLoaded) {
        startSlider();
      } else {
        Promise.all([...imgs].map(img => new Promise(res => {
          if (img.complete) res();
          else img.addEventListener('load', res, { once: true });
        }))).then(startSlider);
      }
    });

    box.addEventListener('mouseleave', () => {
      clearTimeout(timer);
      clearInterval(timer);
      timer = null;

      imgs.forEach(i => i.classList.remove('active'));
      if (imgs.length) {
        imgs[0].classList.add('active');
        idx = 0;
      }
    });

    box.addEventListener('click', () => openModal(box));
  });
}

/* 무한 스크롤 초기화 */
function setupInfiniteScroll() {
  const sentinel = document.getElementById('scroll-sentinel');
  if (!sentinel) return;

  const observer = new IntersectionObserver(async ([entry]) => {
    if (entry.isIntersecting && !isLoading && !isLastPage) {
      isLoading = true;
      try {
        const res = await fetch(`/myPage/review/page?page=${page}&size=9`);
        if (!res.ok) throw new Error("리뷰 불러오기 실패");

        const data = await res.json();
        renderMoreReviews(data.content);
        bindReviewBoxes(); // 새로 추가된 카드에도 이벤트 연결

        if (data.last) isLastPage = true;
        else page++;
      } catch (err) {
        console.error("무한스크롤 오류:", err);
      } finally {
        isLoading = false;
      }
    }
  }, { threshold: 0.1 });

  observer.observe(sentinel);
}

/* 리뷰 카드 추가 렌더링 */
function renderMoreReviews(data) {
  const container = document.querySelector('.my-review-area');
  const sentinel = document.getElementById('scroll-sentinel');
  if (!container || !sentinel) {
    console.warn("container 또는 sentinel이 없음");
    return;
  }

  data.forEach(item => {
    const box = document.createElement('div');
    box.className = 'review-box';
    box.dataset.id = item.id;
    box.dataset.reservationId = item.reservationId;

    if (item.reviewImageDtoList.length > 0) {
      box.innerHTML = item.reviewImageDtoList.map((img, i) => `
        <img src="${img.imgUrl}" alt="리뷰 이미지 ${i + 1}" class="${i === 0 ? 'main-image active' : ''}">
      `).join('');
    } else {
      box.dataset.rating = item.rating;
      box.dataset.date = item.date;
      box.dataset.hairstyle = item.serviceName;
      box.innerHTML = `<img src="${createReviewImage(item.rating, item.date, item.serviceName)}" alt="리뷰 정보 이미지" class="active">`;
    }

    if (!item.read) {
      const unread = document.createElement('div');
      unread.className = 'review-reply-unread';
      box.appendChild(unread);
    }

    // sentinel 앞에 삽입해서 sentinel이 항상 맨 아래로 가도록 유지
    container.insertBefore(box, sentinel);
  });
}


/* 모달 로직 */
const modal = document.getElementById('review-details');
const cache = new Map();

async function openModal(card) {
  const id = card.dataset.id;
  modal.classList.add('show');

  try {
    const data = cache.has(id) ? cache.get(id) : await fetchDetail(id);
    cache.set(id, data);
    fillModal(data);

    card.querySelector('.review-reply-unread')?.remove();
  } catch (err) {
    alert('리뷰를 불러오지 못했습니다.');
    console.error(err);
    closeReviewDetails();
  }
}

async function fetchDetail(id) {
  const res = await fetch(`/myPage/review/${id}`);
  if (!res.ok) throw new Error('서버 오류');
  return res.json();
}

function fillModal(d) {
  _txt('modal-date', d.myReviewListDto.date);
  _txt('modal-designer-name', d.designerName);
  _txt('modal-shop-name', d.shopName);
  _txt('modal-service-name', d.myReviewListDto.serviceName);
  _txt('modal-visit-date', d.myReviewListDto.date);
  _txt('modal-comment', d.comment);

  renderStars(d.myReviewListDto.rating, document.getElementById('modal-rating'));

  const slider = modal.querySelector('.review-img-modal-slider');
  slider.innerHTML = '';

  if (d.myReviewListDto.reviewImageDtoList.length) {
    d.myReviewListDto.reviewImageDtoList.forEach(img => {
      slider.insertAdjacentHTML('beforeend',
        `<div class="review-img-mbox"><img src="${img.imgUrl}" alt=""></div>`);
    });
  } else {
    slider.insertAdjacentHTML('beforeend',
      `<div class="review-img-mbox"><img src="${createReviewImage(
        d.myReviewListDto.rating,
        d.myReviewListDto.date,
        d.myReviewListDto.serviceName
      )}" alt=""></div>`);
  }

  const imgs = slider.querySelectorAll('img');
  imgs.forEach(img => {
    if (!img.complete) {
      const dummy = new Image();
      dummy.src = img.src;
    }
  });

  imgs.forEach(img => {
    if (img.complete) adjustImageFit(img);
    else img.addEventListener('load', () => adjustImageFit(img));
  });

  const reply = document.getElementById('modal-reply');
  if (d.replyComment && d.replyComment.trim() !== "") {
    _attr('modal-designer-img', 'src', d.designerImgUrl || '/images/no-photo.svg');
    _txt('modal-reply-name', d.designerName);
    _txt('modal-reply-at', d.replyAt);
    _txt('modal-reply-comment', d.replyComment);
    reply.hidden = false;
  } else {
    reply.hidden = true;
  }
}

/* 유틸 */
function _txt(id, val) {
  const el = document.getElementById(id);
  if (el) el.textContent = val || '';
}

function _attr(id, a, v) {
  const el = document.getElementById(id);
  if (el) el.setAttribute(a, v);
}

function createReviewImage(r, d, h) {
  const c = document.createElement('canvas');
  c.width = 300;
  c.height = 400;
  const ctx = c.getContext('2d');
  ctx.fillStyle = '#f8f8f8';
  ctx.fillRect(0, 0, 300, 400);
  ctx.fillStyle = '#333';
  ctx.textAlign = 'center';

  ctx.font = 'bold 40px Arial';
  ctx.fillText('★'.repeat(parseInt(r)), 150, 140);
  ctx.font = '24px Arial';
  ctx.fillText(`- ${d} -`, 150, 210);
  ctx.font = 'bold 28px Arial';
  ctx.fillText(h, 150, 280);

  return c.toDataURL('image/jpeg', 0.9);
}

/* 모달 닫기 */
function closeReviewDetails() {
  modal.classList.remove('show');
  resetAllDragScrollState();

  const modalSlider = modal.querySelector('.review-img-modal-slider');
  if (modalSlider) {
    modalSlider.innerHTML = '';
    modalSlider.classList.remove('dragging');
    modalSlider.style.cursor = '';
  }
}


/*선택한 리뷰 찾기 */
function tryScrollToReservationReview() {
  const urlParams = new URLSearchParams(window.location.search);
  const reservationId = urlParams.get("reservationId");
  if (!reservationId) return;

  let attempts = 0;
  const maxAttempts = 100;

  const targetSelector = `.review-box[data-reservation-id="${reservationId}"]`;

  // 타겟 스크롤 시도
  const tryFindTarget = () => {
    const target = document.querySelector(targetSelector);
    if (target) {
      target.scrollIntoView({ behavior: 'smooth', block: 'center' });
      target.classList.add('highlight');
      setTimeout(() => target.classList.remove('highlight'), 2000);
      return true;
    }
    return false;
  };

  // 강제로 무한 스크롤 실행 (sentinel이 화면 안으로 들어오게 함)
  const forceInfiniteScroll = () => {
    const sentinel = document.getElementById('scroll-sentinel');
    if (sentinel) sentinel.scrollIntoView({ behavior: 'smooth' });
  };

  // 무한 시도
  const keepTryingUntilLoaded = () => {
    if (tryFindTarget()) return;

    if (isLastPage) {
      console.warn('끝까지 불러왔지만 해당 리뷰를 찾지 못했습니다.');
      return;
    }

    if (++attempts > maxAttempts) {
      console.warn('리뷰 자동 스크롤 시도 횟수 초과');
      return;
    }

    forceInfiniteScroll(); // 다음 페이지 로드 유도
    setTimeout(keepTryingUntilLoaded, 500); // 반복 검사
  };

  setTimeout(keepTryingUntilLoaded, 300); // 첫 시도
}



window.closeReviewDetails = closeReviewDetails;
