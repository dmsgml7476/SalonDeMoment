import { initAddressSearchToggle } from '/javascript/user/addressSearchUtil.js';
import { setStoredLocation, getStoredLocation } from '/javascript/user/locationUtil.js';
import { renderStars } from '/javascript/ratingStarUtil.js';


let sortOption = "distance";
let toggleState = 1; // 1: 기본, 2: 드롭다운, 3: 가게리스트




document.addEventListener("DOMContentLoaded", function () {
  console.log("안녕 헤어샵 페이지 나야 js");

    // 토글



  const toggle = document.getElementById("floatingToggle");
  const dropdown = document.getElementById("toggleDropdown");
  const shopList = document.getElementById("shopListToggle");

  if (toggle) {
    toggle.addEventListener("click", () => {
      if (toggleState === 1) {
        dropdown.style.display = "block";
        shopList.classList.remove("visible");
        toggle.classList.add("visible");
        toggleState = 2;
      }
    });
  }

  if (dropdown) {
    dropdown.querySelectorAll("div").forEach((el) => {
      el.addEventListener("click", () => {
        dropdown.style.display = "none";
        shopList.style.display = "flex";
        shopList.classList.add("visible");
        toggleState = 3;
      });
    });
  }

  function resetToggle() {
    dropdown.style.display = "none";
    shopList.style.display = "none";
    toggleState = 1;
  }


    // 정렬
    document.getElementById("sort-select")?.addEventListener("change", (e) => {
      const selected = e.target.value;
      if (!selected) return;

      sortOption = selected;
      page = 0;
      endOfList = false;
      allShops = [];
      document.querySelector("#shop-list").innerHTML = "";
      getShopList();
    });

    console.log("🌟 DOMContentLoaded 실행됨");

    const stars = document.querySelectorAll(".rating-stars");
    console.log("⭐ .rating-stars 찾은 개수:", stars.length);

    stars.forEach(el => {
      const ratingStr = el.dataset.rating;
      const rating = parseFloat(ratingStr);
      console.log("➡️ 대상:", el, " | data-rating:", ratingStr, " | 해석:", rating);

      if (isNaN(rating)) {
        console.warn("❗ rating이 숫자가 아님, 무시됨:", ratingStr);
      } else {
        renderStars(rating, el);
        console.log("✅ 별 렌더링 완료");
      }
    });


  document.querySelectorAll(".rating-stars").forEach(el => {
    const rating = parseFloat(el.dataset.rating || "0");
    renderStars(rating, el);
  });

  /* ─────── 전역 변수 ─────── */
  const currentUserId = window.currentUserId ?? null;
  const isGuest = !currentUserId;

  let agreeLocation = false;
  if (!isGuest) {
    const raw = window.userAgreeLocation;
    agreeLocation = raw === true || raw === "true" || raw === 1 || raw === "1";
  }

  const guestAgreed = localStorage.getItem("guestLocationConsent") === "true";
  const needLocationConsent = () => (isGuest ? !guestAgreed : !agreeLocation);

  let region = "";
  let userLat = null;
  let userLon = null;
  let allShops = [];
  let selectedShops = [];


  function updateSelectedShopUI() {
      const nameSpans = document.querySelectorAll(".selected-shop-name");
      const boxes = document.querySelectorAll(".shop-list-box .name-box");

      boxes.forEach((box, index) => {
        if (selectedShops[index]) {
          box.style.display = "flex";
          nameSpans[index].textContent = selectedShops[index].name;
        } else {
          box.style.display = "none";
          nameSpans[index].textContent = "";
        }
      });
    }

    function removeShop(index) {
      selectedShops.splice(index, 1);
      updateSelectedShopUI();
      updateToggleDropdownUI();
    }

//    function updateToggleDropdownUI() {
//      const toggleDropdown = document.getElementById("toggleDropdown");
//      if (!toggleDropdown) return;
//
//      toggleDropdown.innerHTML = ""; // 초기화
//
//      selectedShops.forEach((shop, index) => {
//        const div = document.createElement("div");
//        div.className = "toggle-option";
//        div.textContent = `${index + 1}`;
//        div.addEventListener("click", openShopListToggle);
//        toggleDropdown.appendChild(div);
//      });
//    }

  let page = 0;
  const size = 10;
  let isLoading = false;
  let endOfList = false;

  /* ─────── 위치 동의 모달 처리 ─────── */
  const locationModal = document.querySelector("#location-agree-modal");
  const confirmBtn = document.querySelector("#location-agree-accept");
  const cancelBtn = document.querySelector("#location-agree-cancel");

  document.addEventListener("click", (e) => {
    const btn = e.target.closest(".location-now-text");
    if (!btn) return;

    if (needLocationConsent()) {
      locationModal.classList.remove("hidden");
      locationModal.style.display = "flex";
    } else {
      detectAndConvertLocation(applyDetectedLocation);
    }
  });

  confirmBtn?.addEventListener("click", () => {
    locationModal.style.display = "none";

    if (isGuest) {
      localStorage.setItem("guestLocationConsent", "true");
    } else {
      const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
      const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

      fetch('/api/member/location-consent', {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json', [csrfHeader]: csrfToken }
      })
        .then(r => {
          if (!r.ok) throw new Error();
          agreeLocation = true;
          window.userAgreeLocation = true;
        })
        .catch(() => alert('동의 처리 실패'));
    }

    detectAndConvertLocation(applyDetectedLocation);
  });

  cancelBtn?.addEventListener("click", () => {
    locationModal.style.display = "none";
  });

  /* ─────── 위치 감지 및 주소 변환 ─────── */
  function detectAndConvertLocation(callback) {
    navigator.geolocation.getCurrentPosition(
      position => {
        const lat = position.coords.latitude;
        const lon = position.coords.longitude;

        fetch(`/api/coord-to-address?x=${lon}&y=${lat}`)
          .then(res => res.json())
          .then(data => {
            if (data.userAddress) {
              callback({
                lat,
                lon,
                userAddress: data.userAddress,
                region1depth: data.region1depth,
                region2depth: data.region2depth
              });
            } else {
              alert("주소 정보를 불러올 수 없습니다.");
            }
          })
          .catch(err => {
            alert("주소 변환 실패");
            console.error(err);
          });
      },
      err => {
        alert("위치 정보 접근이 거부되었습니다.");
        console.error(err);
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    );
  }

  /* ─────── 위치 정보 적용 및 샵 리스트 초기화 ─────── */
  function applyDetectedLocation({ lat, lon, userAddress, region1depth, region2depth }) {
    document.getElementById("user-region").textContent = `${region1depth} ${region2depth}`;
    region = region1depth;
    userLat = lat;
    userLon = lon;
    page = 0;
    endOfList = false;
    allShops = [];

    setStoredLocation(currentUserId, {
      userAddress,
      userLatitude: lat,
      userLongitude: lon,
      region1depth,
      region2depth,
    });

    document.querySelector("#shop-list").innerHTML = "";
    getShopList();

    document.querySelector(".initial-display")?.classList.remove("hidden");
    document.querySelector(".initial-display")?.classList.add("active");
    document.getElementById("expandedShopSearchArea")?.classList.add("hidden");
    document.getElementById("expandedAddressInputArea")?.classList.add("hidden");
  }

  /* ─────── 주소 검색창 이벤트 등록 ─────── */
  initAddressSearchToggle({
    onSelectAddress: ({ userAddress, userLatitude, userLongitude, region1depth, region2depth }) => {
      applyDetectedLocation({
        lat: userLatitude,
        lon: userLongitude,
        userAddress,
        region1depth,
        region2depth
      });
    }
  });

  /* ─────── 선택된 샵 로딩 및 초기 지역 셋업 ─────── */
  function loadSelectedShopsFromSession() {
    const saved = sessionStorage.getItem("selectedShops");
    if (saved) selectedShops = JSON.parse(saved);
  }

  function saveSelectedShopsToSession() {
    sessionStorage.setItem("selectedShops", JSON.stringify(selectedShops));
  }

  loadSelectedShopsFromSession();

  const storedLocation = getStoredLocation(currentUserId);
  region = storedLocation?.region1depth || "";
  userLat = storedLocation?.userLatitude || null;
  userLon = storedLocation?.userLongitude || null;

  if (storedLocation && region && userLat !== null && userLon !== null) {
    document.getElementById("user-region").textContent = `${storedLocation.region1depth} ${storedLocation.region2depth}`;
    getShopList();
  } else {
    // 기본 위치: 대전 시청
    const defaultLocation = {
      lat: 36.3504, // 위도
      lon: 127.3845, // 경도
      userAddress: "대전 광역시청", // 표현용 주소
      region1depth: "대전",
      region2depth: "광역시청"
    };

    applyDetectedLocation(defaultLocation);
  }

  /* ─────── 샵 리스트 API 호출 ─────── */
  function getShopList() {
    if (isLoading || endOfList) return;
    isLoading = true;

    console.log("🔍 호출 region:", region, "위도:", userLat, "경도:", userLon);

    fetch(`/api/shop-list?region=${region}&lat=${userLat}&lon=${userLon}&page=${page}&size=${size}&sort=${sortOption}`)
      .then(res => res.json())
      .then(shopList => {
        if (shopList.length === 0) {
          endOfList = true;
          if (page === 0) {
            document.querySelector("#shop-list").innerHTML = "<p>해당 지역에 등록된 샵이 없습니다.</p>";
          }
          return;
        }

        allShops.push(...shopList);
        renderShopList(shopList, true);
        page++;
      })
      .catch(err => console.error("샵 리스트 불러오기 실패: ", err))
      .finally(() => isLoading = false);
  }

  /* ─────── 샵 카드 렌더링 ─────── */
  function renderShopList(shopList, append = false) {
    const container = document.querySelector("#shop-list");
    if (!container) return;
    if (!append) container.innerHTML = "";

    shopList.forEach(shop => {
      const card = document.createElement("div");
      card.className = "shop-card";
      card.dataset.id = shop.id;
      card.dataset.name = shop.shopName;

      const couponHtml = shop.hasCoupon ? `<div class="shop-coupon"><img src="/images/coupon.png" alt="쿠폰" /></div>` : "";

       let statusHtml = "";
        switch (shop.openStatus) {
          case "OPEN":
            statusHtml = `<div class="shop-status"><span class="dot-open"></span><span class="status-text">영업중</span></div>`;
            break;
          case "CLOSED":
            statusHtml = `<div class="shop-status"><span class="dot-closed"></span><span class="status-text">영업종료</span></div>`;
            break;
          case "DAYOFF":
            statusHtml = `<div class="shop-status"><span class="dot-holiday"></span><span class="status-text">휴무일</span></div>`;
            break;
          default:
            statusHtml = `<div class="shop-status"><span class="status-text">정보 없음</span></div>`;
        }


      const designersHtml = (shop.designerList || []).map(d => `
        <div class="icon-circle">

            <img src="${d.imgUrl ? d.imgUrl : '/images/default_profile.jpg'}" alt="디자이너 이미지" />

        </div>
      `).join("");

      card.innerHTML = `
        <div class="shop-img" style="background-image: url('${shop.shopImageDto && shop.shopImageDto.imgUrl ? shop.shopImageDto.imgUrl : '/images/default.png'}'); background-size: cover;"></div>

        <div class="shop-info-area">
          <div class="shop-info">
            <div class="shop-header">
              <h3 class="shop-name">${shop.shopName}</h3>
              ${couponHtml}
              <div class="select-box ${selectedShops.includes(String(shop.id)) ? 'selected' : ''}"></div>
            </div>
            <div class="shop-info-content">
              <p class="shop-rating">
                        <span class="rating-stars" data-rating="${shop.rating}"></span>
                        <span class="rating-count">${shop.rating} (${shop.reviewCount})</span>
               </p>
              <p class="shop-address">주소 : ${shop.address} ${shop.addressDetail} (${formatDistance(shop.distance)})</p>
              ${statusHtml}
              <div class="shop-time-area">
                <p class="shop-dayoff">${shop.dayOffShowDto.dayOffText}</p>
                <p class="shop-time">영업시간 : ${shop.openTime.substring(0,5)} ~ ${shop.closeTime.substring(0,5)}</p>
              </div>
            </div>
          </div>
          <div class="profile-icons-wrapper">
            <div class="profile-icons" data-drag-scroll>${designersHtml}</div>
          </div>
        </div>
      `;


      card.addEventListener("click", (e) => {
        if (e.defaultPrevented) return;

        if (!e.target.closest(".select-box")) {
          location.href = `/shop/${shop.id}`;
        }
      });

      container.appendChild(card);

      const ratingContainer = card.querySelector(".shop-rating .rating-stars");
      if (ratingContainer) {
        const rating = parseFloat(ratingContainer.dataset.rating || '0');
        renderStars(rating, ratingContainer);
      }


      bindShopCardProfileIconScroll();
    });

    document.querySelectorAll(".icon-circle img").forEach(img => {
      if (img.complete) applyClass(img);
      else img.addEventListener("load", () => applyClass(img));
    });

    function applyClass(img) {
      img.classList.toggle("landscape", img.naturalWidth < img.naturalHeight);
    }

    updateSelectedShopUI();

    document.querySelectorAll(".rating-stars").forEach(star => {
      const rating = parseFloat(star.dataset.rating || "0");
      renderStars(rating, star);
    });
  }

  function formatDistance(distance) {
    return distance >= 1000 ? (distance / 1000).toFixed(1) + "km" : Math.round(distance) + "m";
  }

  /* ─────── 무한 스크롤 ─────── */
  window.addEventListener("scroll", () => {

    const floatToggle = document.getElementById("floatingToggle");
      if (!floatToggle) return;

      // 특정 위치 이상에서 floatingToggle 보이게
      if (window.scrollY > 400) {
        floatToggle.classList.add("visible");
      } else {
        floatToggle.classList.remove("visible");
      }

    if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 100) {
      getShopList();
    }
  });

  /* ─────── 미용실 선택 UI 및 비교 버튼 처리 ─────── */
  document.addEventListener("click", function (e) {
    if (e.target.classList.contains("select-box")) {
      const shopCard = e.target.closest(".shop-card");
      const shopId = shopCard.dataset.id;

      if (selectedShops.includes(shopId)) {
        selectedShops = selectedShops.filter(id => id !== shopId);
        e.target.classList.remove("selected");
      } else {
        if (selectedShops.length >= 3) {
          alert("최대 3개의 미용실만 선택할 수 있습니다.");
          return;
        }
        selectedShops.push(shopId);
        e.target.classList.add("selected");
      }

      saveSelectedShopsToSession();
      updateSelectedShopUI();
    }

    if (e.target.classList.contains("remove-btn")) {
      const parent = e.target.closest(".selected-shop-one, .selected-shop-two, .selected-shop-three");
      const span = parent.querySelector("span");
      const shopToRemove = allShops.find(shop => shop.shopName === span.textContent.trim());
      if (!shopToRemove) return;

      selectedShops = selectedShops.filter(id => id !== String(shopToRemove.id));
      saveSelectedShopsToSession();
      updateSelectedShopUI();

      document.querySelectorAll(".shop-card").forEach(card => {
        if (card.dataset.id === String(shopToRemove.id)) {
          card.querySelector(".select-box")?.classList.remove("selected");
        }
      });
    }
  });


   const scissorsBox = document.querySelector("#floatingToggle .scissors-box");
    if (scissorsBox) {
      scissorsBox.addEventListener("click", () => {
        if (selectedShops.length < 2) {
          alert("비교할 미용실을 2개 이상 선택하세요.");
          return;
        }

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

        fetch("/api/saveSelectedShops", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            [csrfHeader]: csrfToken,
          },
          body: JSON.stringify(selectedShops)
        })
          .then(res => {
            if (res.ok) {
              location.href = "/compare";
            } else {
              alert("서버에 선택 정보 저장 실패");
            }
          })
          .catch(err => {
            console.error("비교 페이지 이동 오류", err);
            alert("오류 발생");
          });
      });
    }


//
//  function updateSelectedShopUI() {
//    const boxes = [
//      document.querySelector(".selected-shop-one"),
//      document.querySelector(".selected-shop-two"),
//      document.querySelector(".selected-shop-three"),
//    ];
//
//    boxes.forEach((box, idx) => {
//      const shopId = selectedShops[idx];
//      if (shopId) {
//        const shop = allShops.find(s => s.id == shopId);
//        box.style.display = "flex";
//
//        const nameSpan = box.querySelector(".selected-shop-name");
//        const removeSpan = box.querySelector(".remove-btn");
//
//        nameSpan.textContent = shop ? shop.shopName : "알 수 없음";
//
//        // X 버튼 클릭 시 제거
//        removeSpan.onclick = () => {
//          selectedShops = selectedShops.filter(id => id !== String(shop.id));
//          saveSelectedShopsToSession();
//          updateSelectedShopUI();
//
//          // 카드에서도 선택 해제 UI 반영
//          document.querySelectorAll(".shop-card").forEach(card => {
//            if (card.dataset.id === String(shop.id)) {
//              card.querySelector(".select-box")?.classList.remove("selected");
//            }
//          });
//        };
//
//      } else {
//        box.style.display = "none";
//      }
//    });
//
//    updateToggleDropdownUI(); // 드롭다운 이름 표시 업데이트
//  }

  const compareBtn = document.getElementById("compare-btn");

  if (compareBtn) {
    compareBtn.addEventListener("click", () => {
      if (selectedShops.length < 2) {
        alert("비교할 미용실을 2개 이상 선택하세요.");
        return;
      }

      const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
      const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

      fetch("/api/saveSelectedShops", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          [csrfHeader]: csrfToken,
        },
        body: JSON.stringify(selectedShops)
      })
        .then(res => {
          if (res.ok) {
            location.href = "/compare";
          } else {
            alert("서버에 선택 정보 저장 실패");
          }
        })
        .catch(err => {
          console.error("세션 저장 중 오류 발생: ", err);
          alert("오류 발생");
        });
    });
  }



  //검색 (추가)

  const searchInput = document.querySelector(".shop-search-input-expanded");

    if (searchInput) {
      searchInput.addEventListener("input", debounce(async (e) => {
        const keyword = e.target.value.trim();

        if (keyword === "") {
          page = 0;
          endOfList = false;
          document.querySelector("#shop-list").innerHTML = "";
          getShopList();
          return;
        }
        try {
          const res = await fetch(`/api/shop-list/search?keyword=${encodeURIComponent(keyword)}`);
          const shops = await res.json();
          renderShopList(shops, false); // 기존 리스트 비우고 새로 렌더링
          page = 0; // 검색일 경우 페이징 초기화 (필요 시)
          endOfList = true; // 무한스크롤 막기 (검색 결과만 보여줄 경우)
        } catch (err) {
          console.error("검색 실패", err);
        }
      }, 300));
    }

    // 토글

    function updateToggleDropdownUI() {
      const dropdown = document.getElementById("toggleDropdown");
      dropdown.innerHTML = "";

      selectedShops.forEach((_, i) => {
        const div = document.createElement("div");
        div.className = "toggle-option";
        div.textContent = i + 1;
        div.addEventListener("click", openShopListToggle);
        dropdown.appendChild(div);
      });
    }

    // 기존 updateSelectedShopUI 안에서 호출 추가
    function updateSelectedShopUI() {
      const compareBoxes = [
        document.querySelector(".compare-panel.selected-shop-one"),
        document.querySelector(".compare-panel.selected-shop-two"),
        document.querySelector(".compare-panel.selected-shop-three"),
      ];

      const toggleBoxes = [
        document.querySelector(".toggle-panel.selected-shop-one"),
        document.querySelector(".toggle-panel.selected-shop-two"),
        document.querySelector(".toggle-panel.selected-shop-three"),
      ];

      [compareBoxes, toggleBoxes].forEach(boxSet => {
        boxSet.forEach((box, idx) => {
          const shopId = selectedShops[idx];
          if (shopId) {
            const shop = allShops.find(s => s.id == shopId);
            box.style.display = "flex";
            const nameEl = box.querySelector(".selected-shop-name");
            if (nameEl) nameEl.textContent = shop ? shop.shopName : "알 수 없음";

            const removeBtn = box.querySelector(".remove-btn");
            if (removeBtn) {
              removeBtn.onclick = () => {
                selectedShops = selectedShops.filter(id => id !== String(shop.id));
                saveSelectedShopsToSession();
                updateSelectedShopUI();
                document.querySelectorAll(".shop-card").forEach(card => {
                  if (card.dataset.id === String(shop.id)) {
                    card.querySelector(".select-box")?.classList.remove("selected");
                  }
                });
              };
            }

          } else {
            box.style.display = "none";
          }
        });
      });

      updateToggleDropdownUI();
    }


  window.closeShopListToggle = function () {
    const toggle = document.getElementById("floatingToggle");
    const dropdown = document.getElementById("toggleDropdown");
    const shopList = document.getElementById("shopListToggle");

    shopList.style.display = "none";
    toggle.style.display = "flex";
    dropdown.style.display = "none";

    toggleState = 1;
  };

  /* ─────── THE END ─────── */

});

  function debounce(fn, delay) {
    let timeout;
    return (...args) => {
      clearTimeout(timeout);
      timeout = setTimeout(() => fn(...args), delay);
    };
  }

  // 드래그

  function enableShopCardProfileIconDragScroll(target, { multiplier = 1.2, clickThreshold = 5 } = {}) {
    if (!target || target.classList.contains("drag-bound")) return;

    target.classList.add("drag-bound"); // ✅ 중복 방지

    let isDown = false;
    let startX = 0;
    let scrollX = 0;
    let moved = false;

    function removeDragging() {
      isDown = false;
      document.body.style.userSelect = "auto";
      if (moved) {
        // 드래그된 경우 클릭 한 번 차단
        const cancelClick = (e) => {
          e.stopImmediatePropagation();
          e.preventDefault();
          target.removeEventListener("click", cancelClick, true);
        };
        target.addEventListener("click", cancelClick, true);
      }
      moved = false;
      target.classList.remove("dragging");
    }

    target.addEventListener("mousedown", (e) => {
      isDown = true;
      moved = false;
      startX = e.pageX;
      scrollX = target.scrollLeft;
      document.body.style.userSelect = "none";
    });

    target.addEventListener("mousemove", (e) => {
      if (!isDown) return;
      const dx = e.pageX - startX;
      if (Math.abs(dx) > clickThreshold) {
        moved = true;
        target.classList.add("dragging");
      }
      e.preventDefault();
      target.scrollLeft = scrollX - dx * multiplier;
    });

    window.addEventListener("mouseup", removeDragging);
    target.addEventListener("mouseleave", removeDragging);
    window.addEventListener("blur", removeDragging);
    document.addEventListener("visibilitychange", () => {
      if (document.visibilityState === "hidden") removeDragging();
    });

    // 터치 드래그
    let touchStartX = 0;
    target.addEventListener("touchstart", (e) => {
      touchStartX = e.touches[0].pageX;
      scrollX = target.scrollLeft;
    }, { passive: true });

    target.addEventListener("touchmove", (e) => {
      const dx = e.touches[0].pageX - touchStartX;
      target.scrollLeft = scrollX - dx * multiplier;
    }, { passive: true });
  }

  // shop 카드들에 개별 적용
  function bindShopCardProfileIconScroll() {
    document.querySelectorAll(".shop-card .profile-icons").forEach(el => {
      enableShopCardProfileIconDragScroll(el);
    });
  }

   window.openDropdown = function () {
      const dropdown = document.getElementById("toggleDropdown");
      if (!dropdown) return;
      dropdown.style.display = dropdown.style.display === "block" ? "none" : "block";
    };


  // shop-list-toggle 열기
   window.openShopListToggle = function () {
      document.getElementById("floatingToggle").style.display = "none";
      document.getElementById("shopListToggle").style.display = "flex";
    };


  const scissorsBox = document.querySelector("#floatingToggle .scissors-box");
    if (scissorsBox) {
      scissorsBox.addEventListener("click", () => {
        if (selectedShops.length < 2) {
          alert("비교할 미용실을 2개 이상 선택하세요.");
          return;
        }

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

        fetch("/api/saveSelectedShops", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            [csrfHeader]: csrfToken,
          },
          body: JSON.stringify(selectedShops),
        })
          .then((res) => {
            if (res.ok) {
              location.href = "/compare";
            } else {
              alert("서버에 선택 정보 저장 실패");
            }
          })
          .catch((err) => {
            console.error("비교 페이지 이동 오류", err);
            alert("오류 발생");
          });
      });
    }

     document.addEventListener("click", function (e) {
        if (e.target.classList.contains("remove-btn")) {
          const box = e.target.closest(".name-box");
          const index = Array.from(box.parentElement.children).indexOf(box);
          removeShop(index);
        }
      });

      window.selectShop = function (name) {
        if (selectedShops.length >= 3) return;
        selectedShops.push({ name });
        updateSelectedShopUI();
        updateToggleDropdownUI();
      };

  // 가게 리스트 열기
window.openShopList = function () {
  const floatToggle = document.getElementById("floatingToggle");
  const shopListToggle = document.getElementById("shopListToggle");
  if (!floatToggle || !shopListToggle) return;

  floatToggle.classList.remove("visible");
  shopListToggle.style.display = "flex"; // ← 추가 필요!
  shopListToggle.classList.add("visible");
};

  window.backToMain = function () {
    const floatToggle = document.getElementById("floatingToggle");
    const shopListToggle = document.getElementById("shopListToggle");
    if (!floatToggle || !shopListToggle) return;

    shopListToggle.style.display = "none";
    floatToggle.classList.add("visible");
    shopListToggle.classList.remove("visible");
  };







