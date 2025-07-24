// ShopDetail.js

document.addEventListener("DOMContentLoaded", () => {
  //  이미지 자동 슬라이드
  let currentImageIndex = 0;
  const images = document.querySelectorAll(".shop-img-box img");
  if (images.length > 1) {
    setInterval(() => {
      images.forEach((img, index) => {
        img.style.display = index === currentImageIndex ? "block" : "none";
      });
      currentImageIndex = (currentImageIndex + 1) % images.length;
    }, 3000); // 3초 간격
  }
  // 찜하기
  const likeButton = document
    .querySelector(".shop-btn .lucide-heart1")
    ?.closest("button");
  const likeIcon = likeButton?.querySelector(".lucide-heart1");
  const likeCount = likeButton?.querySelector("small");

  let liked = false;
  let count = parseInt(likeCount.textContent.replace("|", "").trim());

  likeButton?.addEventListener("click", () => {
    liked = !liked;

    // 아이콘 색상 변경
    likeIcon.style.fill = liked ? "red" : "none";
    likeIcon.style.stroke = liked ? "red" : "currentColor";

    // 숫자 증가/감소
    likeCount.textContent = `| ${liked ? count + 1 : count}`;
  });

  //  공유하기 버튼 → 모달 열기
  document
    .querySelector(".shop-btn .lucide-share")
    ?.closest("button")
    .addEventListener("click", () => {
      document.getElementById("share-modal").style.display = "flex";
    });

  //  공유 모달 내 복사 버튼
  document
    .querySelector("#share-modal button")
    ?.addEventListener("click", () => {
      const input = document.querySelector("#share-modal input");
      input.select();
      document.execCommand("copy");
      alert("링크가 복사되었습니다!");
      document.getElementById("share-modal").style.display = "none";
    });

  // 바깥 클릭 시 모달 닫기
  document.getElementById("share-modal")?.addEventListener("click", (e) => {
    if (e.target.id === "share-modal") {
      e.target.style.display = "none";
    }
  });


  /**
   * 2. 메뉴 탭 전환 기능
   */
  document.querySelectorAll(".tab-item").forEach((tab) => {
    tab.addEventListener("click", () => {
      document
        .querySelectorAll(".tab-item")
        .forEach((t) => t.classList.remove("active"));
      tab.classList.add("active");

      document.querySelectorAll(".tab-content").forEach((section) => {
        section.style.display = "none";
      });

      const tabId = tab.dataset.tab;
      document.getElementById(tabId).style.display = "block";
    });
  });

  /**
   * 3. 홈 탭 기능
   */
  const recommendSection = document.querySelector(
    ".recommend-service .service-list"
  );
  const recommendItems = recommendSection.querySelectorAll(".service-item");
  const recommendMore = document.querySelector(".recommend-service .btn-more");
  if (recommendItems.length > 3) {
    recommendItems.forEach((item, i) => {
      if (i >= 3) item.style.display = "none";
    });
    let expanded = false;
    recommendMore.addEventListener("click", () => {
      expanded = !expanded;
      recommendItems.forEach((item, i) => {
        if (i >= 3) item.style.display = expanded ? "flex" : "none";
      });
      recommendMore.innerHTML = expanded
        ? '접기 <i class="fa-solid fa-chevron-up"></i>'
        : '시술 더보기 <i class="fa-solid fa-chevron-down"></i>';
    });
  }

  document.querySelectorAll(".designer-card").forEach((card) => {
    card.addEventListener("click", () => {
    const shopDesignerId = item.dataset.id;
      location.href = "/designerProfile/${shopDesignerId}";
    });
  });
  document.querySelectorAll(".designer-card .btn-reserve").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      e.stopPropagation();
      location.href = `/reservation/write`;
    });
  });


// 시술 카테고리 필터링 기능
const categoryButtons = document.querySelectorAll(".category-btn");
const categorySections = document.querySelectorAll(".category-section");

categoryButtons.forEach((button) => {
  button.addEventListener("click", () => {
    const selected = button.dataset.category;

    // 버튼 활성화 처리
    categoryButtons.forEach((btn) => btn.classList.remove("active"));
    button.classList.add("active");

    // 카테고리별 시술 리스트 토글
    categorySections.forEach((section) => {
      const sectionCat = section.dataset.category;
      section.style.display =
        selected === "all" || sectionCat === selected ? "block" : "none";
    });
  });
});


  /**
   * 6. 디자이너 탭
   */
  document.querySelectorAll(".designer-card").forEach((card) => {
     card.addEventListener("click", () => {
       const shopDesignerId = card.dataset.id;
       location.href = `/designerProfile/${shopDesignerId}`;
     });
   });


  document.querySelectorAll(".btn-book").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      e.stopPropagation();
      location.href = "/reservation/write";
    });
  });

  /**
   * 7. 리뷰 탭 기능
   */
  document.querySelector(".r-photo-btn")?.addEventListener("click", () => {
    location.href = "/review/write";
  });

  const rTabs = document.querySelectorAll(".r-tab");
  rTabs.forEach((tab) => {
    tab.addEventListener("click", () => {
      rTabs.forEach((t) => t.classList.remove("active"));
      tab.classList.add("active");
      const cat = tab.dataset.category;
      document.querySelectorAll(".r-item").forEach((item) => {
        item.style.display =
          cat === "all" || item.dataset.category === cat ? "block" : "none";
      });
    });
  });

  // 리뷰 정렬: 기존 알림 제거, 실제 정렬 함수만 남김
  document.querySelector(".r-sort")?.addEventListener("change", (e) => {
    const value = e.target.value;
    sortReviews(value);
  });

  // 정렬 기준에 따라 r-item을 재정렬하는 함수 (샘플)
  function sortReviews(criteria) {
    const list = document.querySelector(".r-list");
    const items = Array.from(list.querySelectorAll(".r-item"));

    items.sort((a, b) => {
      const aRating = getRatingScore(a);
      const bRating = getRatingScore(b);

      if (criteria === "high") return bRating - aRating;
      if (criteria === "low") return aRating - bRating;
      return 0; // 최신순은 별도 구현 필요
    });

    items.forEach((item) => list.appendChild(item));
  }

  // 별점 파싱 함수 (★★★★☆ → 4.0 등)
  function getRatingScore(item) {
    const text = item.querySelector(".r-rating")?.textContent || "★★★★★";
    const stars = text.match(/★/g)?.length || 0;
    const half = text.includes("☆") ? 0.0 : 0.0;
    return stars + half;
  }

  const rItems = document.querySelectorAll(".r-item");
  const rToggleBtn = document.querySelector(".r-toggle-btn");
  if (rItems.length > 3 && rToggleBtn) {
    rItems.forEach((item, i) => {
      if (i >= 3) item.style.display = "none";
    });
    let expanded = false;
    rToggleBtn.addEventListener("click", () => {
      expanded = !expanded;
      rItems.forEach((item, i) => {
        if (i >= 3) item.style.display = expanded ? "block" : "none";
      });
      rToggleBtn.innerHTML = expanded
        ? "접기 <span class='arrow'>▲</span>"
        : "더보기 <span class='arrow'>▼</span>";
    });
  }

  // 리뷰 작성일 -> n일전 방문으로 변환하여 r-meta 코드에 추가
  document.querySelectorAll(".r-meta").forEach((meta) => {
    const text = meta.textContent.trim();
    const match = text.match(/(\d{4}\.\d{2}\.\d{2})/); // 날짜 추출
    if (!match) return;

    const dateStr = match[1];
    const [year, month, day] = dateStr.split(".").map(Number);
    const writtenDate = new Date(year, month - 1, day);
    const today = new Date();
    const diffTime = today - writtenDate;
    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

    let visitStr = "";
    if (diffDays === 0) visitStr = "오늘 ";
    else if (diffDays === 1) visitStr = "어제 ";
    else visitStr = `${diffDays}일 전`;

    // 기존 날짜 뒤에 ' · n일 전 방문' 추가
    meta.textContent = text.replace(dateStr, `${dateStr} · ${visitStr}`);
  });

  // 예약하기 버튼 클릭시 예약 페이지 이동
  document
    .querySelector(".fixed-reserve .reserve-btn")
    ?.addEventListener("click", () => {
      location.href = `/reservation/write`;
    });
});


// 디자이너 클릭시 해당 디자이너 상세페이지로 이동
document.querySelectorAll(".designer-item").forEach((item) => {
  item.addEventListener("click", () => {
    const designerId = item.dataset.id;
    if (designerId) {
      window.location.href = `/designerProfile/${designerId}`;
    }
  });
});