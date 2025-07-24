import config from "../apikey.js";
import { setStoredLocation, setGuestLocationConsent } from "./locationUtil.js";

// 주소 선택 시 처리
function handleAddressSelect(addr, lat, lon, onSelectAddress, setMode, addressInput, suggestionList) {
  if (!lat || !lon) {
    alert("❗ 좌표 정보가 없습니다. 도로명 주소로 다시 시도해 주세요.");
    return;
  }

  fetch(`https://dapi.kakao.com/v2/local/geo/coord2address.json?x=${lon}&y=${lat}`, {
    headers: { Authorization: `KakaoAK ${config.restKey}` },
  })
    .then(res => res.json())
    .then(addressData => {
      const detail = addressData.documents?.[0]?.address;
      let region1 = detail?.region_1depth_name || "미확인";

      //광역시 빼는 코드
      region1 = region1.replace(/(광역시|특별시|자치시|시|도)$/, "");
      const region2 = detail?.region_2depth_name || "미확인";

      console.log("📍 상세 지역 정보:", region1, region2);

      // 로그인 비로그인 분리
      const userId = window.currentUserId ?? null;

      const locationData = {
              userLatitude: Number(lat),
              userLongitude: Number(lon),
              region1depth: region1,
              region2depth: region2,
              userAddress: addr,
      };

      setStoredLocation(userId, locationData);

      if (!userId) {
              // 비회원이면 위치 제공 동의 여부도 저장
              setGuestLocationConsent(true);
      }

      onSelectAddress(locationData);
      setMode("initial");
    })
    .catch(e => {
      console.error("❌ 상세 주소 조회 실패", e);
      alert("주소 상세 정보 조회에 실패했습니다.");
    });
}

// 자동완성 설정
function setupAddressAutocomplete(addressInput, suggestionList, onSelectAddress, setMode) {
  addressInput?.addEventListener("input", async () => {
    const query = addressInput.value.trim();
    if (!query) {
      suggestionList.innerHTML = "";
      suggestionList.style.display = "none";
      return;
    }

    try {
      const res = await fetch(
        `https://dapi.kakao.com/v2/local/search/keyword.json?query=${encodeURIComponent(query)}`,
        {
          headers: { Authorization: `KakaoAK ${config.restKey}` },
        }
      );
      const data = await res.json();
      suggestionList.innerHTML = "";

      if (Array.isArray(data.documents) && data.documents.length > 0) {
        suggestionList.style.display = "block";
        data.documents.forEach(doc => {
          const addr = doc.address_name;
          const lat = doc.y;
          const lon = doc.x;
          if (!addr) return;

          const li = document.createElement("li");
          li.textContent = addr;
          li.addEventListener("click", () =>
            handleAddressSelect(addr, lat, lon, onSelectAddress, setMode, addressInput, suggestionList)
          );
          suggestionList.appendChild(li);
        });
      } else {
        suggestionList.style.display = "block";
        const li = document.createElement("li");
        li.textContent = "검색 결과가 없습니다.";
        suggestionList.appendChild(li);
      }
    } catch (err) {
      console.error("❌ 주소 검색 실패:", err);
    }
  });
}

// 모드 전환 핸들링
function setupAddressSearchModeHandlers(setMode) {
  document.getElementById("initialShopSearchBox")?.addEventListener("click", () => setMode("search"));
  document.getElementById("initialLocationSearchArea")?.addEventListener("click", () => setMode("location"));
  document.querySelector(".close-search-mode-button")?.addEventListener("click", () => setMode("initial"));
  document.querySelector(".close-location-mode-button")?.addEventListener("click", () => setMode("initial"));
}

// 초기화
export function initAddressSearchToggle({
  onSelectAddress = (address, lat, lon) => {},
} = {}) {
  const initialDisplay = document.querySelector(".initial-display");
  const expandedShopSearchArea = document.getElementById("expandedShopSearchArea");
  const expandedAddressInputArea = document.getElementById("expandedAddressInputArea");
  const addressInput = expandedAddressInputArea?.querySelector(".address-search-input");
  const suggestionList = expandedAddressInputArea?.querySelector(".address-suggestion-list");

  const setMode = (mode) => {
    initialDisplay?.classList.add("hidden");
    initialDisplay?.classList.remove("active");
    expandedShopSearchArea?.classList.add("hidden");
    expandedAddressInputArea?.classList.add("hidden");

    if (mode === "initial") {
      initialDisplay?.classList.remove("hidden");
      initialDisplay?.classList.add("active");
    } else if (mode === "search") {
      expandedShopSearchArea?.classList.remove("hidden");
      expandedShopSearchArea?.querySelector(".shop-search-input-expanded")?.focus();
    } else if (mode === "location") {
      expandedAddressInputArea?.classList.remove("hidden");
      setTimeout(() => addressInput?.focus(), 50);
    }
  };

  setupAddressSearchModeHandlers(setMode);
  setupAddressAutocomplete(addressInput, suggestionList, onSelectAddress, setMode);
}
