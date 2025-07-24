// locationUitl.js

const DEFAULT_LOCATION = {
  userLatitude: 36.3504396,
  userLongitude: 127.3849508,
  region1depth: "대전 광역시청",
  region2depth: "서구",
  userAddress: "대전광역시 서구 둔산동 123"
};

const buildKey = (userId) => userId ? `user_${userId}_location` : `guest_location`;

//로컬 스트리지에서 불러오기
export function getStoredLocation(userId, allowDefault = false) {
  const key = buildKey(userId);
  const raw = localStorage.getItem(key);
  if (!raw) return allowDefault ? DEFAULT_LOCATION : null;

  try {
    const parsed = JSON.parse(raw);
    const { userLatitude, userLongitude, region1depth, region2depth, userAddress } = parsed;

    if (
      userLatitude === undefined ||
      userLongitude === undefined ||
      !region1depth ||
      !region2depth ||
      !userAddress
    ) {
      return allowDefault ? DEFAULT_LOCATION : null;
    }

    return parsed;
  } catch (e) {
    console.error("위치 정보 파싱 오류:", e);
    return allowDefault ? DEFAULT_LOCATION : null;
  }
}

// 저장 함수
export function setStoredLocation(userId, locationData) {
  const key = buildKey(userId);
  try {
    localStorage.setItem(key, JSON.stringify(locationData));
  } catch (e) {
    console.error("위치 정보 저장 실패:", e);
  }
}

// 비회원 위치 제공 동의 여부 저장
export function setGuestLocationConsent(consent) {
  localStorage.setItem("guest_location_agree", consent ? "true" : "false");
}

// 비회원 위치 제공 동의 여부 조회
export function getGuestLocationConsent() {
  return localStorage.getItem("guest_location_agree") === "true";
}