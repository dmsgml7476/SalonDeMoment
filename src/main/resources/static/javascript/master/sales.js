document.addEventListener('DOMContentLoaded', () => {
    const totalSalesAmountSpan = document.getElementById('totalSalesAmount');
    const totalCategorySalesList = document.getElementById('totalCategorySalesList');
    const designerSalesCards = document.getElementById('designerSalesCards');
    const salesMonthFilter = document.getElementById('salesMonthFilter');
    const applySalesFilterBtn = document.getElementById('applySalesFilterBtn');

    // CSRF 토큰 및 헤더 가져오기 (Thymeleaf 스크립트에서 설정된 경우)
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    // 현재 날짜로 기본 필터 값 설정
    const today = new Date();
    const currentYear = today.getFullYear();
    const currentMonth = (today.getMonth() + 1).toString().padStart(2, '0');
    salesMonthFilter.value = `${currentYear}-${currentMonth}`;

    // 콤마 포맷팅 함수
    const formatNumberWithCommas = (number) => {
        return number.toLocaleString('ko-KR'); // 한국어 형식으로 콤마 추가
    };

    // 데이터 가져오기 및 렌더링 함수
    const fetchAndRenderSalesData = async (year, month) => {
        const url = `/master/sales-dashboard?year=${year}&month=${month}`;

        let salesData;

        try {
            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    // CSRF 토큰이 필요한 경우 주석 해제:
                    // [csrfHeader]: csrfToken,
                },
            });

            if (!response.ok) {
                const errorText = await response.text();
                console.error('매출 데이터를 가져오는 데 실패했습니다:', response.status, errorText);
                alert('매출 데이터를 가져오는 중 오류가 발생했습니다.');
                // 에러 발생 시 UI 초기화
                totalSalesAmountSpan.textContent = '0';
                totalCategorySalesList.innerHTML = '<li class="no-data">데이터를 불러올 수 없습니다.</li>';
                designerSalesCards.innerHTML = '<p class="no-data">데이터를 불러올 수 없습니다.</p>';
                return;
            }

            salesData = await response.json();
            console.log('Received sales data:', salesData);

        } catch (error) {
            console.error('매출 데이터를 가져오는 중 네트워크 오류:', error);
            alert('네트워크 오류로 매출 데이터를 가져올 수 없습니다.');
            // 에러 발생 시 UI 초기화
            totalSalesAmountSpan.textContent = '0';
            totalCategorySalesList.innerHTML = '<li class="no-data">데이터를 불러올 수 없습니다.</li>';
            designerSalesCards.innerHTML = '<p class="no-data">데이터를 불러올 수 없습니다.</p>';
            return;
        }

        // --- 총 매출 렌더링 ---
        // 백엔드에서 totalSales로 오기 때문에 수정
        totalSalesAmountSpan.textContent = formatNumberWithCommas(salesData.totalSales || 0);

        totalCategorySalesList.innerHTML = '';
        // 백엔드에서 categorySalesDtoList로 오기 때문에 수정 (또는 categorySales로)
        // console.log에서 categorySales로 찍혔으니 categorySales로 접근합니다.
        if (salesData.categorySales && salesData.categorySales.length > 0) {
            salesData.categorySales.forEach(category => {
                const li = document.createElement('li');
                li.innerHTML = `${category.label}:<span>${formatNumberWithCommas(category.amount)}원</span>`;
                totalCategorySalesList.appendChild(li);
            });
        } else {
            totalCategorySalesList.innerHTML = '<li class="no-data">카테고리별 매출 데이터가 없습니다.</li>';
        }

        // --- 디자이너별 매출 렌더링 ---
        designerSalesCards.innerHTML = '';
        // 백엔드에서 designerSalesDtoList로 오기 때문에 수정 (또는 designerSales로)
        // console.log에서 designerSales로 찍혔으니 designerSales로 접근합니다.
        if (salesData.designerSales && salesData.designerSales.length > 0) {
            salesData.designerSales.forEach(designer => {
                const designerCard = document.createElement('div');
                designerCard.classList.add('designer-sales-card');

                // 디자이너별 카테고리 매출 리스트도 백엔드의 필드명과 일치시켜야 합니다.
                // SalesPageDto의 designerSalesDtoList 안의 DesignerSalesDto는 categorySales라는 필드를 가지고 있습니다.
                const categoryListHtml = designer.categorySales && designer.categorySales.length > 0
                    ? designer.categorySales.map(cat => `
                        <li>${cat.label}: <span>${formatNumberWithCommas(cat.amount)}원</span></li>
                      `).join('')
                    : '<li>카테고리별 매출 데이터가 없습니다.</li>';

                designerCard.innerHTML = `
                    <p class="designer-name">${designer.designerName || '이름 없음'}</p>
                    <p class="designer-total-amount">총 매출: ₩ <span>${formatNumberWithCommas(designer.totalSales || 0)}</span></p>
                    <div class="category-breakdown">
                        <h5>카테고리별 매출</h5>
                        <ul>
                            ${categoryListHtml}
                        </ul>
                    </div>
                `;
                designerSalesCards.appendChild(designerCard);
            });
        } else {
            designerSalesCards.innerHTML = '<p class="no-data">디자이너별 매출 데이터가 없습니다.</p>';
        }
    };

    // 초기 로딩 시 현재 월의 데이터 렌더링
    const initialDate = salesMonthFilter.value.split('-');
    fetchAndRenderSalesData(parseInt(initialDate[0]), parseInt(initialDate[1]));

    // 필터 버튼 클릭 이벤트
    applySalesFilterBtn.addEventListener('click', () => {
        const selectedMonthValue = salesMonthFilter.value; // "YYYY-MM" 형식
        if (selectedMonthValue) {
            const [year, month] = selectedMonthValue.split('-').map(Number);
            fetchAndRenderSalesData(year, month);
        } else {
            alert('조회할 년/월을 선택해주세요.');
        }
    });
});