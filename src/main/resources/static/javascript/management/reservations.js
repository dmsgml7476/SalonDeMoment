document.addEventListener('DOMContentLoaded', () => {
    // 모든 수정 버튼을 가져옵니다.
    const editButtons = document.querySelectorAll('.btn-edit');

    editButtons.forEach(button => {
        button.addEventListener('click', (event) => {
            // 이벤트 버블링을 막아 혹시 모를 부모 요소의 다른 이벤트 발생을 방지합니다.
            event.stopPropagation();

            const reservationId = button.dataset.reservationId;
            const parentRow = button.closest('tr[data-type="reservation-row"]');
            const reservationStatus = parentRow ? parentRow.dataset.reservationStatus : null;
            const isPaid = parentRow ? parentRow.dataset.paid === 'true' : false;


            if (!reservationId) {
                console.error("수정할 예약 ID를 찾을 수 없습니다.");
                alert("수정 정보를 가져오는 데 문제가 발생했습니다.");
                return;
            }

            // '수정' 버튼 클릭 시, 예약 상태가 'COMPLETED' 이면서 아직 '결제되지 않은' 경우에만 결제 제안
            if (reservationStatus === 'COMPLETED' && !isPaid) {
                const confirmPayment = confirm('완료된 예약이지만, 아직 결제가 진행되지 않았습니다.\n결제 등록 페이지로 이동하시겠습니까?\n(취소 시 예약 수정 페이지로 이동합니다.)');
                if (confirmPayment) {
                    window.location.href = `/manage/sales/new?reservationId=${reservationId}`;
                } else {
                    // 사용자가 '취소'를 누르면 예약 수정 페이지로 이동
                    window.location.href = `/manage/reservations/edit/${reservationId}`;
                }
            } else {
                // 그 외의 모든 경우 (COMPLETED가 아니거나, COMPLETED이면서 이미 결제된 경우)
                // 항상 예약 수정 페이지로 이동합니다.
                window.location.href = `/manage/reservations/edit/${reservationId}`;
            }
        });
    });
});