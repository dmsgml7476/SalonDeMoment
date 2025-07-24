document.addEventListener("DOMContentLoaded", function () {
  const memberNameInput = document.getElementById("memberName");
  const memberIdInput = document.getElementById("memberId");
  const memberSearchResultsDiv = document.getElementById("memberSearchResults"); // 이름 변경

  const serviceNameInput = document.getElementById("serviceName");
  const serviceIdInput = document.getElementById("serviceId");
  const servicePriceInput = document.getElementById("servicePrice");
  const serviceSearchResultsDiv = document.getElementById("serviceSearchResults"); // 추가된 시술 검색 결과 div

  const couponSelect = document.getElementById("couponSelect");
  const couponDiscountInput = document.getElementById("couponDiscount");
  const useTicketCheckbox = document.getElementById("useTicket");
  const ticketAmountInput = document.getElementById("ticketAmount");
  const ticketAmountRow = document.getElementById("ticketAmountRow"); // ID로 직접 참조
  const finalPriceInput = document.getElementById("finalPrice");
  const statusSelect = document.getElementById("status");
  const reservationDateInput = document.getElementById("reservationDate");
  const commentInput = document.getElementById("comment"); // 추가된 메모 필드


  // --- 페이지 로드 시 기존 예약 정보 불러오기 및 채우기 ---
  const pathSegments = window.location.pathname.split('/');
  const lastSegment = pathSegments[pathSegments.length - 1]; // URL의 마지막 세그먼트

  // URL이 /manage/reservations/edit/{id} 형태일 때만 로드
  if (pathSegments.includes('edit') && !isNaN(lastSegment)) {
      const reservationId = lastSegment;
      console.log('수정 모드. 예약 ID:', reservationId);
      loadReservationData(reservationId);
  } else {
      console.log('등록 모드.');
      // 등록 모드일 때는 초기 계산 한 번 수행 (초기값이 있을 경우)
      calculateFinalPrice();
  }

  async function loadReservationData(id) {
      try {
          // 백엔드 API 엔드포인트에 맞게 수정
          const response = await fetch(`/manage/reservations/${id}`);
          if (!response.ok) {
              const errorText = await response.text();
              console.error('예약 정보를 불러오는 데 실패했습니다:', response.status, errorText);
              alert('예약 정보를 불러오는 중 오류가 발생했습니다.');
              return;
          }
          const reservationData = await response.json();
          console.log('불러온 예약 데이터:', reservationData);

          // 폼 필드에 데이터 채우기
          // hidden ID 필드는 Thymeleaf에서 이미 채워져 있을 수 있지만, JS에서도 명시적으로
          const hiddenIdInput = document.querySelector('input[name="id"][type="hidden"]');
          if (hiddenIdInput) {
              hiddenIdInput.value = reservationData.id;
          }

          memberIdInput.value = reservationData.memberId;
          memberNameInput.value = reservationData.memberName;

          serviceIdInput.value = reservationData.serviceId;
          serviceNameInput.value = reservationData.serviceName;
          servicePriceInput.value = reservationData.servicePrice;

          // 예약 날짜/시간 설정
          if (reservationData.reservationDate) {
            const formattedDate = reservationData.reservationDate.substring(0, 16); // YYYY-MM-DDTHH:MM 형식으로 자름
            reservationDateInput.value = formattedDate;
          }

          // 쿠폰 정보 불러오기 및 설정
          // 회원 ID를 기준으로 쿠폰 목록을 다시 불러와야 합니다.
          await loadMemberCouponsAndSetSelected(reservationData.memberId, reservationData.couponId);
          couponDiscountInput.value = reservationData.couponDiscount || 0;


          // 정액권 정보 설정 (회원의 총 정액권 잔액을 불러와야 함)
          useTicketCheckbox.checked = reservationData.ticketIsUsed;
          ticketAmountRow.style.display = useTicketCheckbox.checked ? "block" : "none";
          ticketAmountInput.value = reservationData.ticketUsedAmount || 0;

          if (reservationData.memberId) {
             const memberRes = await fetch(`/manage/members/${reservationData.memberId}/coupons`); // 정액권 잔액을 위해 동일 API 호출
             const memberData = await memberRes.json();
             const prepaid = memberData.ticketBalance || 0;
             ticketAmountInput.setAttribute("data-max", prepaid); // 정액권 입력 필드의 최대값 설정
          }

          finalPriceInput.value = reservationData.finalPrice;
          statusSelect.value = reservationData.status; // 예약 상태 설정
          commentInput.value = reservationData.comment; // 메모 설정


          // 모든 데이터가 채워진 후 최종 금액 재계산
          calculateFinalPrice();

      } catch (error) {
          console.error('예약 정보를 불러오는 중 오류 발생:', error);
          alert('예약 정보를 불러오는 중 오류가 발생했습니다. 콘솔을 확인해주세요.');
      }
  }

  // 회원 ID를 기반으로 쿠폰 목록을 로드하고, 선택된 쿠폰이 있다면 설정
  async function loadMemberCouponsAndSetSelected(memberId, selectedCouponId) {
      couponSelect.innerHTML = `<option value="" data-discount-type="" data-discount-value="0" data-minimum-amount="0">선택 안 함</option>`;

      if (!memberId) return; // memberId가 없으면 쿠폰 로드하지 않음

      try {
          const res = await fetch(`/manage/members/${memberId}/coupons`);
          const data = await res.json();

          data.coupons.forEach(coupon => {
              const option = document.createElement("option");
              option.value = coupon.memberCouponId ?? "";
              option.dataset.discountType = coupon.discountType;
              option.dataset.discountValue = coupon.discountValue;
              option.dataset.minimumAmount = coupon.minimumAmount;
              option.textContent = `${coupon.name} (${coupon.discountType === 'PERCENT' ? coupon.discountValue + '%' : coupon.discountValue.toLocaleString() + '원'})`;
              couponSelect.appendChild(option);
          });

          // 선택된 쿠폰이 있다면 설정
          if (selectedCouponId) {
              couponSelect.value = selectedCouponId;
          }
      } catch (e) {
          console.error("쿠폰 목록을 불러오는 데 실패했습니다:", e);
      }
  }


  // --- 기존의 회원 검색 및 쿠폰/정액권 로직 유지 ---
  memberNameInput.addEventListener("input", async function () {
    const keyword = this.value.trim();
    if (keyword.length < 2) {
      memberSearchResultsDiv.innerHTML = '';
      return;
    }

    try {
      const res = await fetch(`/manage/members/search?keyword=${encodeURIComponent(keyword)}`);
      const members = await res.json();

      memberSearchResultsDiv.innerHTML = '';
      if (members.length === 0) {
        const none = document.createElement("div");
        none.classList.add("autocomplete-item");
        none.textContent = "일치하는 회원이 없습니다.";
        memberSearchResultsDiv.appendChild(none);
        return;
      }

      members.forEach(member => {
        const div = document.createElement("div");
        div.classList.add("autocomplete-item");
        div.textContent = `${member.name} (${member.tel})`;
        div.dataset.id = member.id;
        div.dataset.name = member.name;
        memberSearchResultsDiv.appendChild(div);
      });
    } catch (e) {
      console.error("회원 검색 오류:", e);
    }
  });

  memberSearchResultsDiv.addEventListener("click", async function (e) {
    const target = e.target;
    if (!target.classList.contains("autocomplete-item")) return;

    const memberId = target.dataset.id;
    const memberName = target.dataset.name;

    memberIdInput.value = memberId;
    memberNameInput.value = memberName;
    memberSearchResultsDiv.innerHTML = '';

    // 회원 선택 시 쿠폰/정액권 다시 로드
    try {
        await loadMemberCouponsAndSetSelected(memberId, null); // 신규 회원이므로 선택된 쿠폰 ID는 없으므로 null
        const res = await fetch(`/manage/members/${memberId}/coupons`); // 정액권 잔액을 위해 다시 호출
        const data = await res.json();
        const prepaid = data.ticketBalance || 0;
        ticketAmountInput.setAttribute("data-max", prepaid);
        ticketAmountInput.value = 0; // 회원 변경 시 정액권 금액 초기화
        calculateFinalPrice();
    } catch (e) {
        console.error("회원 변경 시 쿠폰/정액권 로드 오류:", e);
    }
  });

  // 시술명 자동완성 (이전에 추가된 로직)
  serviceNameInput.addEventListener("input", async function() {
    const keyword = this.value.trim();
    if (keyword.length < 2) {
      serviceSearchResultsDiv.innerHTML = '';
      return;
    }

    try {
      const res = await fetch(`/manage/services/search?keyword=${encodeURIComponent(keyword)}`);
      const services = await res.json();

      serviceSearchResultsDiv.innerHTML = '';

      if (services.length === 0) {
        const none = document.createElement("div");
        none.classList.add("autocomplete-item");
        none.textContent = "일치하는 시술이 없습니다.";
        serviceSearchResultsDiv.appendChild(none);
        return;
      }

      services.forEach(service => {
        const div = document.createElement("div");
        div.classList.add("autocomplete-item");
        div.textContent = `${service.name} (${service.price.toLocaleString()}원)`;
        div.dataset.id = service.id;
        div.dataset.name = service.name;
        div.dataset.price = service.price;
        serviceSearchResultsDiv.appendChild(div);
      });
    } catch (e) {
      console.error("시술 검색 오류:", e);
    }
  });

  // 시술 자동완성 결과 클릭 시
  if (serviceSearchResultsDiv) { // 서비스 검색 결과 div가 존재할 때만 리스너 추가
      serviceSearchResultsDiv.addEventListener("click", function(e) {
          const target = e.target;
          if (!target.classList.contains("autocomplete-item")) return;

          serviceIdInput.value = target.dataset.id;
          serviceNameInput.value = target.dataset.name;
          servicePriceInput.value = target.dataset.price;
          serviceSearchResultsDiv.innerHTML = ''; // 검색 결과 창 닫기
          calculateFinalPrice(); // 시술 금액 변경됐으니 최종 금액 재계산
      });
  }


  // --- 나머지 기존 이벤트 리스너들 (변수 참조로 변경) ---
  couponSelect.addEventListener("change", function () {
    const selected = this.options[this.selectedIndex];
    const servicePrice = parseInt(servicePriceInput.value || 0);
    const minAmount = parseInt(selected.dataset.minimumAmount || 0);

    if (selected.value !== "" && servicePrice < minAmount) {
      alert("이 쿠폰은 최소 " + minAmount.toLocaleString() + "원 이상 시술 금액에서만 사용 가능합니다.");
      this.value = ""; // "선택 안 함"으로 초기화
      couponDiscountInput.value = 0;
      calculateFinalPrice();
      return;
    }

    const discountType = selected.dataset.discountType;
    const discountValue = parseInt(selected.dataset.discountValue || 0);
    const discount = discountType === 'PERCENT'
      ? Math.floor(servicePrice * (discountValue / 100))
      : discountValue;

    couponDiscountInput.value = discount;
    calculateFinalPrice();
  });

  servicePriceInput.addEventListener("input", function () {
    const couponSelect = document.getElementById("couponSelect");
    const selected = couponSelect.options[couponSelect.selectedIndex];
    const minAmount = parseInt(selected.dataset.minimumAmount || 0);
    const servicePrice = parseInt(this.value || 0);

    if (selected.value !== "") {
      if (servicePrice < minAmount) {
        alert("이 쿠폰은 최소 " + minAmount.toLocaleString() + "원 이상 시술 금액에서만 사용 가능합니다.");
        couponSelect.value = "";
        couponDiscountInput.value = 0;
        calculateFinalPrice();
        return;
      }

      const discountType = selected.dataset.discountType;
      const discountValue = parseInt(selected.dataset.discountValue || 0);
      const discount = discountType === 'PERCENT'
        ? Math.floor(servicePrice * (discountValue / 100))
        : discountValue;

      couponDiscountInput.value = discount;
    } else {
      couponDiscountInput.value = 0;
    }

    calculateFinalPrice();
  });

  // 정액권 사용 체크박스 토글
  useTicketCheckbox.addEventListener("change", function () {
    const servicePrice = parseInt(servicePriceInput.value || 0);
    const couponDiscount = parseInt(couponDiscountInput.value || 0);
    const ticketBalance = parseInt(ticketAmountInput.getAttribute("data-max") || 0);

    const maxTicketUse = servicePrice - couponDiscount;
    const maxUse = Math.min(maxTicketUse, ticketBalance);

    if (this.checked) {
      ticketAmountRow.style.display = "block";
      ticketAmountInput.value = maxUse > 0 ? maxUse : 0;
    } else {
      ticketAmountRow.style.display = "none";
      ticketAmountInput.value = 0;
    }

    calculateFinalPrice();
  });

  // 정액권 사용 금액 직접 입력 시
  ticketAmountInput.addEventListener("input", function () {
    const max = parseInt(this.getAttribute("data-max") || 0);
    let val = parseInt(this.value || 0);
    if (val > max) {
      val = max;
      this.value = max;
    }
    if (val < 0) {
      this.value = 0;
    }
    calculateFinalPrice();
  });

  // 최종 결제금액 계산 함수
  function calculateFinalPrice() {
    const servicePrice = parseInt(servicePriceInput.value || 0);
    const couponDiscount = parseInt(couponDiscountInput.value || 0);
    const ticketUsed = useTicketCheckbox.checked;
    const ticketBalance = parseInt(ticketAmountInput.getAttribute("data-max") || 0);

    const maxTicketUse = servicePrice - couponDiscount;
    let effectiveTicketAmount = 0;

    if (ticketUsed) {
        const currentTicketAmount = parseInt(ticketAmountInput.value || 0);
        effectiveTicketAmount = Math.min(currentTicketAmount, maxTicketUse, ticketBalance);
        ticketAmountInput.value = effectiveTicketAmount < 0 ? 0 : effectiveTicketAmount;
    } else {
        ticketAmountInput.value = 0;
    }

    const finalPrice = servicePrice - couponDiscount - effectiveTicketAmount;
    finalPriceInput.value = finalPrice < 0 ? 0 : finalPrice;
  }

  // 시술 금액, 쿠폰, 정액권 관련 input 변경 시 최종 금액 재계산 이벤트 리스너 추가
  servicePriceInput.addEventListener("input", calculateFinalPrice);
  couponSelect.addEventListener("change", calculateFinalPrice);
  ticketAmountInput.addEventListener("input", calculateFinalPrice);
  useTicketCheckbox.addEventListener("change", calculateFinalPrice);
});