document.addEventListener("DOMContentLoaded", function () {
  // 디자이너 프로필 상세보기 (더보기)
  const toggleBtn = document.querySelector(".toggle-description");
  const descBox = document.querySelector(".designer-description");
  const icon = toggleBtn?.querySelector("i");

  toggleBtn?.addEventListener("click", () => {
    const isOpen = descBox.style.display === "block";
    descBox.style.display = isOpen ? "none" : "block";

    icon?.classList.toggle("fa-chevron-down", isOpen);
    icon?.classList.toggle("fa-chevron-up", !isOpen);

    toggleBtn.firstChild.textContent = isOpen ? "더보기 " : "닫기 ";
  });

  // 미용실 이름 클릭 → 이동
  document.querySelector(".designer-salon")?.addEventListener("click", () => {
    window.location.href = "/shop-detail";
  });

  // 디자이너 프로필 메뉴바 전환 + 카테고리 바 표시
  const tabItems = document.querySelectorAll(".tab-item");
  const tabContents = document.querySelectorAll(".tab-content");

  tabContents.forEach((content) => (content.style.display = "none"));
  const defaultTab = document.querySelector(".tab-item.active");
  if (defaultTab) {
    const defaultContent = document.getElementById(defaultTab.dataset.tab);
    if (defaultContent) defaultContent.style.display = "block";
  }

  tabItems.forEach((item) => {
    item.addEventListener("click", () => {
      const selected = item.dataset.tab;

      tabItems.forEach((el) => el.classList.remove("active"));
      item.classList.add("active");

      tabContents.forEach((el) => (el.style.display = "none"));
      document.getElementById(selected).style.display = "block";

      const categoryBar = document.getElementById("categoryTabs");
      if (categoryBar) {
        categoryBar.style.display = selected === "tab-menu" ? "block" : "none";
      }
    });
  });

  // 찜하기
  const heartIcon = document.querySelector(".icon-text-btn svg.lucide-heart");
  const heartBtn = heartIcon?.closest("button");

  heartBtn?.addEventListener("click", () => {
    const isActive = heartIcon.classList.toggle("active");

    heartIcon.style.stroke = isActive ? "#e74c3c" : "currentColor";
    if (isActive) alert("내 찜목록에 저장되었습니다");
  });

  // 공유하기
  document.querySelector(".lucide-share-2")?.closest("button")?.addEventListener("click", () => {
    document.getElementById("share-modal").style.display = "flex";
  });

  document.querySelector("#share-modal button")?.addEventListener("click", () => {
    const input = document.querySelector("#share-modal input");
    input?.select();
    document.execCommand("copy");
    alert("링크가 복사되었습니다!");
    document.getElementById("share-modal").style.display = "none";
  });

  document.getElementById("share-modal")?.addEventListener("click", (e) => {
    if (e.target.id === "share-modal") {
      e.target.style.display = "none";
    }
  });

  // 프로필 수정 클릭시 수정 페이지로 이동
   const profileUpdateBtn = document.querySelector(".profileUpdate-btn");
    profileUpdateBtn?.addEventListener("click", function () {
      const designerId = this.dataset.id;
      if (designerId) {
        window.location.href = `/designer/designerProfileUpdate/${designerId}`;
      }
    });



  // 시술 카테고리 스크롤 및 버튼 강조
  const categoryButtons = document.querySelectorAll(".category-btn");
  const sections = document.querySelectorAll(".category-section");
  const serviceLists = document.querySelectorAll(".service-list");

  let isClicking = false;

  function getOffset() {
    const tabBar = document.querySelector(".designer-tabs");
    const categoryBar = document.querySelector(".category-tabs");
    return (tabBar?.offsetHeight || 0) + (categoryBar?.offsetHeight || 0) + 8;
  }

  categoryButtons.forEach((btn) => {
    btn.addEventListener("click", function () {
      const category = this.dataset.category;
      const target = document.getElementById(category);
      const offset = getOffset();

      if (target) {
        isClicking = true;

        window.scrollTo({
          top: target.offsetTop - offset,
          behavior: "smooth",
        });

        categoryButtons.forEach((b) => b.classList.remove("active", "highlight"));
        this.classList.add("active", "highlight");

        setTimeout(() => {
          isClicking = false;
        }, 500);
      }

      // 서비스 리스트 필터링
      serviceLists.forEach((list) => {
        list.style.display = list.dataset.category === category ? "block" : "none";
      });
    });
  });

  window.addEventListener("scroll", function () {
    if (isClicking) return;

    const scrollPos = window.scrollY || document.documentElement.scrollTop;
    const offset = getOffset();

    let currentId = "";
    sections.forEach((section) => {
      const top = section.offsetTop - offset;
      const bottom = top + section.offsetHeight;

      if (scrollPos >= top && scrollPos < bottom) {
        currentId = section.id;
      }
    });

    categoryButtons.forEach((btn) => {
      btn.classList.remove("highlight", "active");
      if (btn.dataset.category === currentId) {
        btn.classList.add("highlight", "active");
      }
    });
  });

  // 시술 더보기
  sections.forEach((section) => {
    const items = section.querySelectorAll(".service-item");
    const moreBtn = section.querySelector(".more-btn");
    const label = moreBtn?.querySelector(".label");
    const icon = moreBtn?.querySelector(".chevron");

    if (items.length > 3) {
      items.forEach((item, i) => {
        if (i >= 3) item.style.display = "none";
      });

      let expanded = false;

      moreBtn?.addEventListener("click", () => {
        expanded = !expanded;

        items.forEach((item, i) => {
          if (i >= 3) item.style.display = expanded ? "flex" : "none";
        });

        label.textContent = expanded ? "접기" : "더보기";
        icon.classList.toggle("fa-chevron-down", !expanded);
        icon.classList.toggle("fa-chevron-up", expanded);
      });
    } else {
      moreBtn.style.display = "none";
    }
  });

  // 리뷰 필터링 및 더보기
  const tabButtons = document.querySelectorAll(".r-tab");
  const reviewItems = document.querySelectorAll(".r-item");
  const reviewMoreBtn = document.querySelector(".reviewmore-btn");
  const rList = document.querySelector(".r-list");

  let currentCategory = "all";
  let reviewExpanded = false;

  function updateVisibleReviews() {
    let count = 0;
    reviewItems.forEach((item) => {
      const cat = item.dataset.category;
      const match = currentCategory === "all" || cat === currentCategory;

      if (match) {
        item.style.display = !reviewExpanded && count >= 3 ? "none" : "block";
        count++;
      } else {
        item.style.display = "none";
      }
    });

    const total = [...reviewItems].filter(
      (i) => currentCategory === "all" || i.dataset.category === currentCategory
    ).length;

    reviewMoreBtn.style.display = total > 3 ? "inline-block" : "none";
    reviewMoreBtn.innerHTML = reviewExpanded
      ? `닫기 <span style="font-size:12px;">▲</span>`
      : `더보기 <span style="font-size:12px;">▼</span>`;
  }

  tabButtons.forEach((button) => {
    button.addEventListener("click", () => {
      tabButtons.forEach((b) => b.classList.remove("active"));
      button.classList.add("active");

      currentCategory = button.dataset.category;
      reviewExpanded = false;
      updateVisibleReviews();
    });
  });

  reviewMoreBtn?.addEventListener("click", () => {
    reviewExpanded = !reviewExpanded;
    updateVisibleReviews();
  });

  // 리뷰 작성일 → n일 전 방문
  document.querySelectorAll(".r-meta").forEach((meta) => {
    const text = meta.textContent.trim();
    const match = text.match(/(\d{4}\.\d{2}\.\d{2})/);
    if (!match) return;

    const dateStr = match[1];
    const [y, m, d] = dateStr.split(".").map(Number);
    const date = new Date(y, m - 1, d);
    const now = new Date();
    const diff = Math.floor((now - date) / (1000 * 60 * 60 * 24));

    const visit = diff === 0 ? "오늘" : diff === 1 ? "어제" : `${diff}일 전`;
    meta.textContent = text.replace(dateStr, `${dateStr} · ${visit}`);
  });

  // 리뷰 작성 페이지 이동
  document.querySelector(".r-photo-btn")?.addEventListener("click", () => {
    window.location.href = "/review-write";
  });

  // 정렬
  document.querySelector(".r-sort")?.addEventListener("change", (e) => {
    const order = e.target.value;
    const items = Array.from(rList.children);

    items.sort((a, b) => {
      const aScore = parseFloat(a.querySelector(".r-rating")?.textContent.match(/\d+(\.\d+)?/)?.[0] || "0");
      const bScore = parseFloat(b.querySelector(".r-rating")?.textContent.match(/\d+(\.\d+)?/)?.[0] || "0");
      return order === "high" ? bScore - aScore : aScore - bScore;
    });

    rList.innerHTML = "";
    items.forEach((el) => rList.appendChild(el));
  });



  // 초기 상태 적용
  updateVisibleReviews();
});
