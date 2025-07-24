document.addEventListener("DOMContentLoaded", () => {
    const originalTotalAmountSpan = document.getElementById("original-total-amount");
    const discountedAmountSpan = document.getElementById("discounted-amount");
    const ticketAppliedAmountSpan = document.getElementById("ticket-applied-amount");
    const finalPaymentAmountSpan = document.getElementById("final-payment-amount");
    const appliedDiscountSpan = document.getElementById("applied-discount");
    const ticketUsedAmountSpan = document.getElementById("ticket-used-amount");

    const customerCommentTextarea = document.getElementById("customerComment");
    const commentApplyBtn = document.getElementById("commentApplyBtn");
    const hiddenRequestMemo = document.getElementById("hiddenRequestMemo");

    const couponApplyBtns = document.querySelectorAll(".btn-coupon-apply");
    const btnCouponCancel = document.getElementById("btn-coupon-cancel");
    const selectedCouponIdInput = document.getElementById("selectedCouponId");

    const ticketApplyBtns = document.querySelectorAll(".btn-ticket-apply");
    const btnTicketCancel = document.getElementById("btn-ticket-cancel");
    const selectedTicketIdInput = document.getElementById("selectedTicketId");

    // 초기 시술 금액을 숫자로 파싱하여 저장
    let originalTotalAmount = parseFloat(originalTotalAmountSpan.textContent || '0');
    let currentDiscountAmount = 0;
    let currentTicketUsedAmount = 0;

    // --- 초기 설정 ---
    // hiddenRequestMemo에 textarea의 초기값 설정 (Thymeleaf로 받아온 값)
    hiddenRequestMemo.value = customerCommentTextarea.value;
    updateFinalAmount(); // 초기 최종 결제 금액 업데이트 (이미 서버에서 계산된 값 표시)


    // --- 1. 고객 요청사항 처리 ---
    commentApplyBtn.addEventListener("click", () => {
        hiddenRequestMemo.value = customerCommentTextarea.value;
        alert("요청사항이 적용되었습니다.");
    });

    // --- 2. 쿠폰 처리 ---
    couponApplyBtns.forEach(button => {
        button.addEventListener("click", () => {
            // 다른 쿠폰 비활성화 및 현재 쿠폰만 활성화
            couponApplyBtns.forEach(btn => btn.disabled = false);
            button.disabled = true;

            // 정액권 선택 해제 (쿠폰과 정액권은 동시에 사용하지 않는다고 가정)
            resetTicketSelection();

            const couponId = button.dataset.couponId;
            const discountAmount = parseFloat(button.dataset.discountAmount || '0');
            const discountRate = parseFloat(button.dataset.discountRate || '0');
            const minOrderAmount = parseFloat(button.dataset.minAmount || '0');

            // 최소 주문 금액 체크
            if (originalTotalAmount < minOrderAmount) {
                alert(`이 쿠폰은 ${new Intl.NumberFormat('ko-KR').format(minOrderAmount)}원 이상 결제 시 사용 가능합니다.`);
                button.disabled = false; // 다시 활성화
                resetCouponSelection(); // 쿠폰 선택 초기화
                return;
            }

            // 할인 금액 계산
            if (discountRate > 0) {
                currentDiscountAmount = originalTotalAmount * (discountRate / 100);
            } else {
                currentDiscountAmount = discountAmount;
            }
            // 최종 할인 금액은 총 시술 금액을 넘지 않도록
            currentDiscountAmount = Math.min(currentDiscountAmount, originalTotalAmount);


            selectedCouponIdInput.value = couponId;
            appliedDiscountSpan.textContent = `총 ${new Intl.NumberFormat('ko-KR').format(currentDiscountAmount)}원 할인`;
            btnCouponCancel.style.display = "inline-block"; // 취소 버튼 보이기

            updateFinalAmount();
        });
    });

    btnCouponCancel.addEventListener("click", () => {
        resetCouponSelection();
        updateFinalAmount();
    });

    function resetCouponSelection() {
        couponApplyBtns.forEach(btn => btn.disabled = false);
        selectedCouponIdInput.value = "";
        currentDiscountAmount = 0;
        appliedDiscountSpan.textContent = "총 0원 할인";
        btnCouponCancel.style.display = "none";
    }

    // --- 3. 정액권 처리 ---
    ticketApplyBtns.forEach(button => {
        button.addEventListener("click", () => {
            // 다른 정액권 비활성화 및 현재 정액권만 활성화
            ticketApplyBtns.forEach(btn => btn.disabled = false);
            button.disabled = true;

            // 쿠폰 선택 해제 (쿠폰과 정액권은 동시에 사용하지 않는다고 가정)
            resetCouponSelection();

            const ticketId = button.dataset.ticketId;
            const remainingAmount = parseFloat(button.dataset.remainingAmount || '0');

            // 정액권 사용 금액은 최종 결제 금액 또는 잔액 중 적은 값
            // 이때 finalPaymentAmount는 쿠폰 적용 전 금액으로 생각 (복잡성 줄이기 위함)
            // 즉, originalTotalAmount - currentDiscountAmount가 현재 시점에서 정액권으로 결제될 금액
            let amountToPayWithTicket = Math.max(0, originalTotalAmount - currentDiscountAmount); // 이미 적용된 쿠폰 할인이 있다면 적용 후 금액

            currentTicketUsedAmount = Math.min(remainingAmount, amountToPayWithTicket);
            
            selectedTicketIdInput.value = ticketId;
            ticketUsedAmountSpan.textContent = new Intl.NumberFormat('ko-KR').format(currentTicketUsedAmount);
            btnTicketCancel.style.display = "inline-block";

            updateFinalAmount();
        });
    });

    btnTicketCancel.addEventListener("click", () => {
        resetTicketSelection();
        updateFinalAmount();
    });

    function resetTicketSelection() {
        ticketApplyBtns.forEach(btn => btn.disabled = false);
        selectedTicketIdInput.value = "";
        currentTicketUsedAmount = 0;
        ticketUsedAmountSpan.textContent = "0";
        btnTicketCancel.style.display = "none";
    }

    // --- 4. 최종 금액 업데이트 함수 ---
    function updateFinalAmount() {
        let finalAmount = originalTotalAmount;

        // 1. 쿠폰 할인 적용
        finalAmount -= currentDiscountAmount;
        discountedAmountSpan.textContent = new Intl.NumberFormat('ko-KR').format(currentDiscountAmount);

        // 2. 정액권 적용
        // 정액권은 할인 후 금액에서 차감 (만약 쿠폰과 정액권 동시 사용 가능하다면)
        // 현재는 동시 사용 불가이므로, 정액권이 선택되면 할인 금액은 0
        if (currentTicketUsedAmount > 0) {
            finalAmount = originalTotalAmount - currentTicketUsedAmount;
            discountedAmountSpan.textContent = new Intl.NumberFormat('ko-KR').format(0); // 정액권 사용 시 할인 금액 0으로 표시
        }
        ticketAppliedAmountSpan.textContent = new Intl.NumberFormat('ko-KR').format(currentTicketUsedAmount);

        // 최종 금액이 0원 미만이 되지 않도록
        finalAmount = Math.max(0, finalAmount);
        
        finalPaymentAmountSpan.textContent = new Intl.NumberFormat('ko-KR').format(finalAmount);
    }

    // 초기 로드 시 총 시술 금액이 있다면 parse하여 변수에 저장
    if (originalTotalAmountSpan.textContent) {
        originalTotalAmount = parseFloat(originalTotalAmountSpan.textContent);
        updateFinalAmount(); // 초기 로드 시 한 번 계산하여 최종 금액 표시
    }
});