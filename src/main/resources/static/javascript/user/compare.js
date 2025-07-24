console.log("js 출근");

import { renderStars } from '../ratingStarUtil.js';
import { adjustImageFitAll } from '../imageFitUtil.js';
import { getStoredLocation } from '/javascript/user/locationUtil.js';

// 전역함수
const selectedCategories = [];
const originalServiceOrders = new Map();

document.addEventListener('DOMContentLoaded', () => {
  // 별점 렌더링
  document.querySelectorAll('.rating-area').forEach(area => {
    const score = parseFloat(area.dataset.rating || '0');
    const slot  = area.querySelector('.rating-stars');
    if (slot) renderStars(score, slot);
  });

  // 이미지 비율 조정
  adjustImageFitAll('.service-img', 1);

  // URL에 위도/경도 없으면 로컬스토리지 값으로 다시 요청
  const urlParams = new URLSearchParams(window.location.search);
  if (!urlParams.has('userLat') || !urlParams.has('userLon')) {
    const userId = window.currentUserId || null;
    const location = getStoredLocation(userId, true);

    if (location) {
      const { userLatitude, userLongitude } = location;
      if (userLatitude && userLongitude) {
        const newUrl = new URL(window.location.href);
        newUrl.searchParams.set('userLat', userLatitude);
        newUrl.searchParams.set('userLon', userLongitude);
        window.location.href = newUrl.toString();
      }
    } else {
      console.warn("위치 정보가 없습니다. 위치 제공에 먼저 동의해주세요.");
    }
  }

  // 거리 정보 없으면 숨기기
  document.querySelectorAll('.distance').forEach(el => {
    const text = el.textContent.trim();
    if (!text || text === 'nullkm' || text === '0km') {
      el.style.display = 'none';
    }
  });

  // 기존 순서 저장
  document.querySelectorAll('.compare-area').forEach(shop => {
    const shopId = shop.id;
    const serviceList = shop.querySelector('.service-list');
    if (!serviceList) return;

    // 복제해서 저장
    const originalBoxes = Array.from(serviceList.querySelectorAll('.service-cate-box'));
    originalServiceOrders.set(shopId, originalBoxes.map(box => box.cloneNode(true)));
  });


  // 카테고리 버튼 클릭 정렬 로직

  document.querySelectorAll('.service-cate').forEach(button => {
    button.addEventListener('click', () => {
      const category = button.dataset.category;

      // 이미 선택한 카테고리 아니면 추가
      if (selectedCategories.includes(category)) {
            // 이미 선택되어 있으면 해제
            selectedCategories.splice(selectedCategories.indexOf(category), 1);
            button.classList.remove('selected');
          } else {
            // 새로 선택된 카테고리 추가
            selectedCategories.push(category);
            button.classList.add('selected');
          }


      sortCompareAreas(); // 정렬 실행
      sortServiceCategoriesInEachShop(); // 서비스 목록 정렬 수정
    });
  });

  // 전화번호 포멧

  document.querySelectorAll('.tel').forEach(el => {
    const raw = el.textContent.trim();
    el.textContent = formatPhoneNumber(raw);
  });

  // 클릭시 전부 보임

  document.querySelectorAll('.address').forEach(address => {
    address.addEventListener('click', () => {
      const isExpanded = address.dataset.expanded === 'true';

      if (isExpanded) {
        // 축소
        address.style.whiteSpace = 'nowrap';
        address.style.overflow = 'hidden';
        address.style.textOverflow = 'ellipsis';
        address.dataset.expanded = 'false';
      } else {
        // 확장
        address.style.whiteSpace = 'normal';
        address.style.overflow = 'visible';
        address.style.textOverflow = 'unset';
        address.dataset.expanded = 'true';
      }
    });
  });


//  DOC 끝
});

function sortCompareAreas() {
  const container = document.querySelector('.shop-card-container');
  const shopAreas = Array.from(container.querySelectorAll('.compare-area'))
    .filter(el => !el.classList.contains('dummy-area'));

  shopAreas.sort((a, b) => {
    const aScore = getCategoryScore(a);
    const bScore = getCategoryScore(b);
    return aScore - bScore;
  });

  shopAreas.forEach(area => container.appendChild(area));
}

function getCategoryScore(shopElement) {
  const categoryTitles = Array.from(shopElement.querySelectorAll('.service-cate-title'))
    .map(el => el.textContent.trim());

  for (let i = 0; i < selectedCategories.length; i++) {
    const label = getCategoryLabel(selectedCategories[i]);
    if (categoryTitles.includes(label)) {
      return i; // 빠를수록 우선
    }
  }
  return 999; // 없으면 아래로
}

function getCategoryLabel(enumName) {
  const labelMap = {
    CUT: "커트",
    PERM: "펌",
    COLOR: "염색",
    UPSTYLE: "업스타일",
    DRY: "드라이",
    HAIR_EXTENSION: "붙임머리",
    CLINIC: "클리닉"
  };
  return labelMap[enumName] || enumName;
}


// 서비스 카테고리 순서 정렬 또는 복원
function sortServiceCategoriesInEachShop() {
  const shopCards = document.querySelectorAll('.compare-area');

  shopCards.forEach(shop => {
    const shopId = shop.id;
    const serviceList = shop.querySelector('.service-list');
    if (!serviceList) return;

    if (selectedCategories.length === 0 && originalServiceOrders.has(shopId)) {
      serviceList.innerHTML = '';

      const originalBoxes = originalServiceOrders.get(shopId);
      originalBoxes.forEach(box => serviceList.appendChild(box.cloneNode(true)));
      return;
    }

    const boxes = Array.from(serviceList.querySelectorAll('.service-cate-box'));

    boxes.sort((a, b) => {
      const aTitle = a.querySelector('.service-cate-title')?.textContent.trim();
      const bTitle = b.querySelector('.service-cate-title')?.textContent.trim();

      const aIndex = selectedCategories.findIndex(c => getCategoryLabel(c) === aTitle);
      const bIndex = selectedCategories.findIndex(c => getCategoryLabel(c) === bTitle);

      return (aIndex === -1 ? 999 : aIndex) - (bIndex === -1 ? 999 : bIndex);
    });

    boxes.forEach(box => serviceList.appendChild(box)); // 재배치
  });
}

function formatPhoneNumber(phone) {
  const digits = phone.replace(/\D/g, '');

  if (digits.length === 11) {
    return digits.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
  } else if (digits.length === 10) {
    return digits.replace(/(\d{2,3})(\d{3,4})(\d{4})/, '$1-$2-$3');
  } else {
    return phone;
  }
}