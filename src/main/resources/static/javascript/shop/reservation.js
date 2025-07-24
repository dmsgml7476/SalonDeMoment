// reservation.js

document.addEventListener("DOMContentLoaded", () => {
    initDesignerSection(); // 디자이너 섹션 초기화
    initDateTimeSection();
    initReservationSubmit();

    const selectedDesignerInput = document.querySelector("#selectedDesignerInput");
    const shopId = document.querySelector('input[name="shopId"]').value;

    // 1. 매장 전체 서비스는 항상 로드
    fetchServicesForShop(shopId);

    // 2. 날짜 그리드는 항상 그립니다.
    generateSlidingDateGrid();

    // ⭐ 3. 페이지 로드 시 시간 가져오기 로직 변경 (핵심 변경 부분) ⭐
    // 디자이너 ID가 URL 파라미터로 넘어왔다면 해당 디자이너의 시간을 로드
    // 그렇지 않으면 "디자이너를 선택해주세요" 메시지 표시
    let designerIdFromUrlParam = selectedDesignerInput.value;

    if (designerIdFromUrlParam) {
        // URL 파라미터로 디자이너가 넘어왔다면 해당 디자이너의 오늘 날짜 시간 표시
        // initialDateFromHtml (HTML에서 넘어온 날짜)가 있다면 그 날짜를 우선 사용
        const initialDateFromHtml = document.querySelector("#initialDate")?.value;
        let dateToFetchTimes = initialDateFromHtml || new Date().toISOString().split("T")[0]; // 오늘 날짜 (YYYY-MM-DD)

        fetchAvailableTimes(dateToFetchTimes, designerIdFromUrlParam);
    } else {
        // 디자이너가 미선택 상태일 때는 메시지를 보여줍니다.
        document.querySelector(".time-grid").innerHTML = ""; // 시간 그리드 비우기
        document.querySelector(".reservation-warning-message").innerHTML = `<span>디자이너를 선택하시면 예약 가능한 시간을 확인할 수 있습니다.</span>`;
    }

    // 4. 초기 디자이너 요약 정보 표시 (기존 로직과 동일)
    // 이 부분은 selectedDesignerInput.value (즉, URL 파라미터로 넘어온 디자이너 ID)가 있을 때만 유효
    if (selectedDesignerInput.value) {
        const designerId = selectedDesignerInput.value;
        const selectedDesignerCard = document.querySelector(`.designer-card[data-id="${designerId}"]`);
        if (selectedDesignerCard) {
            const designerName = selectedDesignerCard.querySelector("h3")?.textContent;
            document.querySelector(".selected-designer-name").textContent = designerName;
            document.querySelector(".designer-selected-summary").style.display = "block";
            document.querySelector(".designer-card-list").style.display = "none";
            document.querySelector(".designer-more").style.display = "none";
        }
    }

    updateSummary(); // 요약 정보 업데이트 (초기 로드 시)
});


function initDesignerSection() {
    const maxVisible = 3;
    let expanded = false;
    let selectedDesigner = null; // 선택된 디자이너 카드 엘리먼트

    const designerSection = document.getElementById("designer-section");
    const designerCards = document.querySelectorAll(".designer-card");
    const designerMoreWrapper = designerSection.querySelector(".designer-more");
    const hiddenDesignerIdInput = document.querySelector("#selectedDesignerInput"); // 실제 폼 제출에 사용될 hidden input

    const summaryBox = document.querySelector(".designer-selected-summary");
    const summaryName = summaryBox.querySelector(".selected-designer-name");
    const changeBtn = summaryBox.querySelector(".btn-reset-designer");

    const moreBtn = document.createElement("button");
    moreBtn.type = "button";
    moreBtn.className = "btn-more";
    moreBtn.textContent = "더보기";
    designerMoreWrapper.innerHTML = "";
    designerMoreWrapper.appendChild(moreBtn);

    moreBtn.addEventListener("click", () => {
        expanded = !expanded;
        designerCards.forEach((card, i) => {
            if (!selectedDesigner) { // 디자이너가 선택되지 않았을 때만 확장/축소 로직 적용
                card.style.display = expanded || i < maxVisible ? "block" : "none";
            }
        });
        moreBtn.textContent = expanded ? "닫기" : "더보기";
    });

    // 초기 로드 시 selectedDesignerId가 있는 경우 처리 (URL 파라미터로 넘어온 경우)
    const initialDesignerId = hiddenDesignerIdInput.value;
    if (initialDesignerId) {
        const initialSelectedCard = document.querySelector(`.designer-card[data-id="${initialDesignerId}"]`);
        if (initialSelectedCard) {
            selectedDesigner = initialSelectedCard; // 선택된 카드 참조 저장
            designerCards.forEach(c => c.style.display = "none"); // 모든 카드 숨기기
            designerMoreWrapper.style.display = "none"; // 더보기 버튼 숨기기
            summaryBox.style.display = "block"; // 요약 박스 보여주기
            summaryName.textContent = initialSelectedCard.dataset.designerName || initialSelectedCard.querySelector("h3")?.textContent; // 이름 설정
            // 해당 라디오 버튼도 checked 상태로 만듭니다. (Thymeleaf th:checked와 동일)
            const initialRadio = document.getElementById(`designerRadio-${initialDesignerId}`);
            if (initialRadio) {
                initialRadio.checked = true;
            }
        }
    } else {
        // 초기 디자이너가 선택되지 않았으면 처음 3개만 보이기
        designerCards.forEach((card, i) => {
            card.style.display = i < maxVisible ? "block" : "none";
        });
    }

    designerCards.forEach(card => {
        card.addEventListener("click", () => {
            // 이미 디자이너가 선택된 상태라면 클릭 무시 (변경 버튼을 통해 변경하도록)
            if (selectedDesigner && selectedDesigner === card) { // 이미 선택된 디자이너를 다시 클릭한 경우
                return;
            } else if (selectedDesigner && selectedDesigner !== card) { // 다른 디자이너를 클릭한 경우
                // 이미 선택된 디자이너가 있다면 해당 라디오 버튼 선택 해제
                const prevSelectedRadio = document.getElementById(`designerRadio-${selectedDesigner.dataset.id}`);
                if (prevSelectedRadio) {
                    prevSelectedRadio.checked = false;
                }
                selectedDesigner = null; // 선택된 디자이너 초기화
            }

            const id = card.dataset.id;
            const name = card.dataset.designerName || card.querySelector("h3")?.textContent; // data-designer-name 속성 우선 사용

            selectedDesigner = card; // 현재 클릭한 카드 저장
            hiddenDesignerIdInput.value = id; // 실제 폼 필드 업데이트
            summaryName.textContent = name; // 요약 이름 업데이트

            // 숨겨진 라디오 버튼을 찾아 checked 상태로 만듭니다.
            const selectedRadio = document.getElementById(`designerRadio-${id}`);
            if (selectedRadio) {
                selectedRadio.checked = true;
            }

            designerCards.forEach(c => c.style.display = "none"); // 모든 카드 숨기기
            designerMoreWrapper.style.display = "none"; // 더보기 숨기기
            summaryBox.style.display = "block"; // 요약 보여주기

            resetServiceSection();
            resetDateTimeSection(); // 날짜/시간 섹션 초기화 (시간 그리드 포함)
            updateServiceAndDateTimeForDesigner(id); // AJAX 호출 (서비스 섹션 업데이트)

            // ⭐ 디자이너 클릭 시 현재 선택된 날짜에 대한 가용 시간 바로 불러오기 ⭐
            const selectedDateElement = document.querySelector(".date-box.selected");
            let dateToFetch = new Date().toISOString().split("T")[0]; // 기본은 오늘 날짜
            if (selectedDateElement && selectedDateElement.dataset.isoDate) {
                dateToFetch = selectedDateElement.dataset.isoDate;
            }
            fetchAvailableTimes(dateToFetch, id); // 새로 선택된 디자이너 ID와 선택된 날짜로 시간 요청

            updateSummary();
        });
    });

    changeBtn.onclick = () => {
        // 이전에 선택된 라디오 버튼이 있다면 checked 해제
        if (selectedDesigner) {
            const prevRadio = document.getElementById(`designerRadio-${selectedDesigner.dataset.id}`);
            if (prevRadio) {
                prevRadio.checked = false;
            }
        }

        selectedDesigner = null;
        hiddenDesignerIdInput.value = ""; // 폼 필드 값 초기화
        summaryName.textContent = "이름 없음";

        summaryBox.style.display = "none";

        designerCards.forEach((c, i) => {
            c.classList.remove("selected", "disabled");
            c.style.display = i < maxVisible ? "block" : "none";
        });

        designerMoreWrapper.style.display = "block";
        moreBtn.textContent = "더보기";
        designerMoreWrapper.innerHTML = "";
        designerMoreWrapper.appendChild(moreBtn);

        resetServiceSection();
        resetDateTimeSection(); // 날짜/시간 섹션 초기화 (여기서 경고 메시지 설정)
        const shopId = document.querySelector('input[name="shopId"]').value;
        fetchServicesForShop(shopId);

        // ⭐ 디자이너 초기화 시 시간 그리드 비우고 메시지 출력 ⭐
        document.querySelector(".time-grid").innerHTML = "";
        document.querySelector(".reservation-warning-message").innerHTML = `<span>디자이너를 선택하시면 예약 가능한 시간을 확인할 수 있습니다.</span>`;

        updateSummary();
    };
}

let startOffset = 0; // 전역 변수 유지

function resetDateTimeSection() {
    document.querySelector("#selectedDateTime").value = "";
    document.getElementById("dateList").innerHTML = "";
    document.querySelector(".time-grid").innerHTML = "";
    // 디자이너 미선택 시 메시지 설정 (여기서 변경)
    document.querySelector(".reservation-warning-message").innerHTML = `<span>디자이너를 선택하시면 예약 가능한 시간을 확인할 수 있습니다.</span>`;

    startOffset = 0; // 날짜 그리드 오프셋 초기화
    generateSlidingDateGrid(); // 날짜 그리드 재생성
}

function generateSlidingDateGrid() {
    const dateList = document.getElementById("dateList");
    const monthTitle = document.getElementById("currentMonth");
    const hiddenDateInput = document.querySelector("#selectedDateTime"); // hidden input 유지
    const today = new Date();
    dateList.innerHTML = "";

    const firstDate = new Date(today);
    firstDate.setDate(today.getDate() + startOffset);
    const displayMonth = firstDate.getMonth() + 1;
    const displayYear = firstDate.getFullYear();

    if (monthTitle) {
        monthTitle.textContent = `${displayYear}년 ${displayMonth}월`;
    }

    let dateSelectedInGrid = false; // 그리드 내에서 초기 선택 날짜가 설정되었는지 여부
    const initialDateFromHtml = document.querySelector("#initialDate")?.value;

    for (let i = 0; i < 14; i++) {
        const date = new Date(today);
        date.setDate(today.getDate() + startOffset + i);

        const dayNum = date.getDate();
        const dayOfWeek = date.getDay();
        const isoDate = date.toISOString().split("T")[0];

        const dateBox = document.createElement("div");
        dateBox.classList.add("date-box");
        dateBox.dataset.isoDate = isoDate; // isoDate를 data 속성으로 저장

        const isToday = (new Date()).toDateString() === date.toDateString();

        // 페이지 로드 시 또는 날짜 그리드 재생성 시 초기 날짜 선택 로직
        // URL에서 날짜가 넘어왔다면 그 날짜를 우선 선택
        // 그렇지 않고 오늘 날짜가 현재 그리드에 있다면 오늘 날짜 선택
        if (!dateSelectedInGrid && ((initialDateFromHtml && isoDate === initialDateFromHtml) || (!initialDateFromHtml && isToday))) {
            dateBox.classList.add("selected");
            hiddenDateInput.value = isoDate; // 숨겨진 날짜 input 값도 업데이트
            dateSelectedInGrid = true; // 플래그를 true로 설정하여 중복 선택 방지
        }

        dateBox.innerHTML = `
            <div class="date-day">${["일", "월", "화", "수", "목", "금", "토"][dayOfWeek]}</div>
            <div class="date-number">${dayNum}</div>
            <div class="date-label">${isToday ? '오늘' : ''}</div>
        `;

        dateBox.addEventListener("click", () => {
            document.querySelectorAll(".date-box").forEach(d => d.classList.remove("selected"));
            dateBox.classList.add("selected");
            hiddenDateInput.value = isoDate;

            // ⭐ 날짜 클릭 시 fetchAvailableTimes 호출 로직 ⭐
            // 현재 selectedDesignerInput에 값이 있어야만 시간 요청 (디자이너 미선택 시 요청 안함)
            let currentDesignerId = document.querySelector("#selectedDesignerInput").value;

            if (currentDesignerId) {
                fetchAvailableTimes(isoDate, currentDesignerId); // 디자이너 ID를 인자로 넘김
            } else {
                // 디자이너 미선택 상태에서 날짜 클릭 시 시간 그리드 비우고 메시지 표시
                document.querySelector(".time-grid").innerHTML = "";
                document.querySelector(".reservation-warning-message").innerHTML = `<span>디자이너를 선택하시면 예약 가능한 시간을 확인할 수 있습니다.</span>`;
            }
            updateSummary();
        });

        dateList.appendChild(dateBox);
    }

    // 만약 초기 날짜가 지정되지 않았고, 오늘 날짜도 그리드에 포함되지 않았다면
    // (예: startOffset이 매우 커서 오늘 날짜가 그리드 범위 밖에 있는 경우)
    // 첫 번째 날짜 박스를 기본으로 선택 (선택 해제된 상태 방지)
    if (!dateSelectedInGrid && dateList.firstChild) {
        dateList.firstChild.classList.add("selected");
        // 이 때 hiddenDateInput.value도 첫 번째 날짜로 설정
        const firstDateIso = new Date(today);
        firstDateIso.setDate(today.getDate() + startOffset);
        hiddenDateInput.value = firstDateIso.toISOString().split("T")[0];
    }
}

function moveDateWindow(direction) {
    if (direction === 'prev') {
        startOffset = Math.max(0, startOffset - 14);
    } else if (direction === 'next') {
        startOffset += 14;
    }
    generateSlidingDateGrid();

    // 날짜 이동 시 디자이너가 선택된 상태라면 해당 날짜의 시간 다시 불러오기
    const currentDesignerId = document.querySelector("#selectedDesignerInput").value;
    if (currentDesignerId) {
        const selectedDate = document.querySelector("#selectedDateTime").value.split("T")[0];
        fetchAvailableTimes(selectedDate, currentDesignerId);
    } else {
        // 디자이너 미선택 상태에서는 날짜 이동 시 시간 그리드 비우고 메시지 표시
        document.querySelector(".time-grid").innerHTML = "";
        document.querySelector(".reservation-warning-message").innerHTML = `<span>디자이너를 선택하시면 예약 가능한 시간을 확인할 수 있습니다.</span>`;
    }
}

function initDateTimeSection() {
    document.querySelector("#btn-prev")?.addEventListener("click", () => moveDateWindow("prev"));
    document.querySelector("#btn-next")?.addEventListener("click", () => moveDateWindow("next"));
}

function fetchAvailableTimes(dateStr, explicitDesignerId = null) {
    const designerId = explicitDesignerId || document.querySelector("#selectedDesignerInput").value;

    const timeGrid = document.querySelector(".time-grid");
    const warning = document.querySelector(".reservation-warning-message");
    timeGrid.innerHTML = ""; // 시간 그리드를 먼저 비웁니다.

    if (!designerId) {
        // 디자이너 ID가 없으면 경고 메시지 표시하고 함수 종료
        warning.innerHTML = `<span>디자이너를 선택하시면 예약 가능한 시간을 확인할 수 있습니다.</span>`;
        return;
    }

    warning.innerHTML = ``; // 디자이너 ID가 있으면 경고 메시지 초기화

    fetch(`/reservation/designers/${designerId}/available-times?date=${dateStr}`)
        .then(res => {
            if (!res.ok) {
                throw new Error(`HTTP error! status: ${res.status}`);
            }
            return res.json();
        })
        .then(data => {
            if (data.holiday) {
                warning.innerHTML = `<span>휴무일입니다</span>`;
                return;
            }

            if (data.availableTimes && data.availableTimes.length > 0) {
                 warning.innerHTML = `<span>선택 가능한 시간입니다.</span>`;
            } else {
                 warning.innerHTML = `<span>해당 날짜에 예약 가능한 시간이 없습니다.</span>`;
            }

            data.availableTimes.forEach(timeStr => {
                const btn = document.createElement("button");
                btn.className = "time-btn";
                btn.dataset.time = timeStr;

                const displayTime = timeStr.substring(0, 5); // HH:mm:SS -> HH:mm
                btn.textContent = displayTime;

                btn.addEventListener("click", () => {
                    document.querySelectorAll(".time-btn").forEach(b => b.classList.remove("selected"));
                    btn.classList.add("selected");

                    const selectedDate = document.querySelector("#selectedDateTime").value.split("T")[0];
                    document.querySelector("#selectedDateTime").value = `${selectedDate}T${timeStr}`;
                    updateSummary();
                });

                timeGrid.appendChild(btn);
            });
        })
        .catch(error => {
            console.error('Error fetching available times:', error);
            warning.innerHTML = `<span>시간 정보를 불러오는 데 실패했습니다.</span>`;
        });
}

function resetServiceSection() {
    document.querySelectorAll("input[name='shopServiceId']").forEach(input => input.checked = false);
    document.querySelector(".service-selected-summary").style.display = "none";
    document.querySelector(".selected-service-info").textContent = "";
    document.querySelector("#selectedServiceId").value = "";

    document.querySelectorAll(".service-category").forEach(cat => cat.remove());
    document.querySelector(".service-filters").innerHTML = "";
}

function updateServiceAndDateTimeForDesigner(designerId) {
    fetch(`/reservation/designers/${designerId}/services`)
        .then(res => {
            if (!res.ok) {
                throw new Error(`HTTP error! status: ${res.status}`);
            }
            return res.json();
        })
        .then(data => {
            renderServiceSections(data);
        })
        .catch(error => console.error('Error fetching designer services:', error));

    generateSlidingDateGrid(); // 날짜 그리드는 디자이너 선택과 무관하게 항상 그림
}

function fetchServicesForShop(shopId) {
    fetch(`/reservation/shop-services?shopId=${shopId}`)
        .then(res => {
            if (!res.ok) {
                throw new Error(`HTTP error! status: ${res.status}`);
            }
            return res.json();
        })
        .then(data => {
            renderServiceSections(data);
        })
        .catch(error => console.error('Error fetching shop services:', error));
}

function renderServiceSections(serviceCategoriesData) {
    console.log("renderServiceSections: 받아온 시술 및 카테고리 데이터:", serviceCategoriesData);

    const serviceListContainer = document.querySelector(".service-list");
    const serviceFilters = serviceListContainer.querySelector(".service-filters");

    document.querySelectorAll(".service-category").forEach(el => el.remove());
    serviceFilters.innerHTML = '';

    const allBtn = document.createElement('button');
    allBtn.className = 'category-btn active';
    allBtn.dataset.tab = 'all';
    allBtn.textContent = '전체';
    serviceFilters.appendChild(allBtn);
    allBtn.addEventListener("click", () => {
        document.querySelectorAll(".category-btn").forEach(t => t.classList.remove("active"));
        allBtn.classList.add("active");
        document.querySelectorAll(".service-category").forEach(section => {
            section.style.display = "block";
        });
    });

    serviceCategoriesData.forEach(categoryDto => {
        let categoryLabel = '알 수 없음';
        let categoryTabValue = '';

        if (categoryDto.category && typeof categoryDto.category === 'object') {
            const enumName = categoryDto.category.name;
            const label = categoryDto.category.label;

            categoryTabValue = (enumName || label || 'unknown_category').toLowerCase().replace(/\s/g, '');
            categoryLabel = label || enumName || '알 수 없음';

        } else if (typeof categoryDto.category === 'string') {
            categoryLabel = categoryDto.category;
            categoryTabValue = categoryLabel.toLowerCase().replace(/\s/g, '');
        } else {
            console.warn("renderServiceSections: 예상치 못한 categoryDto.category 형식:", categoryDto.category);
            return;
        }

        const categoryBtn = document.createElement('button');
        categoryBtn.className = 'category-btn';
        categoryBtn.dataset.tab = categoryTabValue;
        categoryBtn.textContent = categoryLabel;
        serviceFilters.appendChild(categoryBtn);

        categoryBtn.addEventListener("click", () => {
            document.querySelectorAll(".category-btn").forEach(t => t.classList.remove("active"));
            categoryBtn.classList.add("active");
            document.querySelectorAll(".service-category").forEach(section => {
                const matched = categoryTabValue === section.dataset.category;
                section.style.display = matched ? "block" : "none";
            });
        });

        const serviceCategoryDiv = document.createElement("div");
        serviceCategoryDiv.className = "service-category";
        serviceCategoryDiv.dataset.category = categoryTabValue;

        serviceCategoryDiv.innerHTML = `
            <h3>${categoryLabel}</h3>
            <ul class="service-items">
                ${categoryDto.services.map(service => `
                    <li>
                        <input type="radio" name="shopServiceId" value="${service.id}" />
                        <div>
                            <span class="service-name">${service.name}</span>
                            <span class="service-price">${new Intl.NumberFormat('ko-KR').format(service.price)}원</span>
                        </div>
                    </li>
                `).join('')}
            </ul>
        `;
        serviceListContainer.appendChild(serviceCategoryDiv);
    });

    document.querySelectorAll("input[name='shopServiceId']").forEach(input => {
        input.addEventListener("change", () => {
            const li = input.closest("li");
            const serviceName = li.querySelector(".service-name").textContent;
            const servicePrice = li.querySelector(".service-price").textContent;

            document.querySelector("#selectedServiceId").value = input.value;
            document.querySelector(".selected-service-info").textContent = `${serviceName} (${servicePrice})`;
            document.querySelector(".service-selected-summary").style.display = "block";

            document.querySelectorAll(".service-category").forEach(cat => {
                cat.style.display = "none";
            });

            const changeBtn = document.querySelector(".service-selected-summary .change-service-btn");
            changeBtn.onclick = () => {
                document.querySelector("#selectedServiceId").value = "";
                document.querySelector(".service-selected-summary").style.display = "none";
                document.querySelector(".category-btn[data-tab='all']").click();
                document.querySelectorAll(".service-category").forEach(cat => cat.style.display = "block");
            };
            updateSummary();
        });
    });

    document.querySelector(".category-btn[data-tab='all']")?.click();
}

function initReservationSubmit() {
    const reservationForm = document.getElementById("reservationForm");
    if (reservationForm) {
        reservationForm.addEventListener("submit", (event) => {
            const designerId = document.querySelector("#selectedDesignerInput").value;
            const serviceId = document.querySelector("#selectedServiceId").value;
            const dateTime = document.querySelector("#selectedDateTime").value;
            const warningMessageArea = document.querySelector(".reservation-warning-message");

            if (!designerId) {
                warningMessageArea.textContent = "디자이너를 선택해주세요.";
                event.preventDefault();
                return;
            }
            if (!serviceId) {
                warningMessageArea.textContent = "시술을 선택해주세요.";
                event.preventDefault();
                return;
            }
            if (!dateTime) {
                warningMessageArea.textContent = "날짜와 시간을 선택해주세요.";
                event.preventDefault();
                return;
            }

            warningMessageArea.textContent = "";
        });
    }
}

function updateSummary() {
    const designerNameElement = document.querySelector(".selected-designer-name");
    const serviceInfoElement = document.querySelector(".selected-service-info");
    const dateTimeElement = document.querySelector("#selectedDateTime");
    const summaryArea = document.querySelector(".reservation-summary");

    const designerName = designerNameElement ? designerNameElement.textContent : "디자이너 미선택";
    const serviceInfo = serviceInfoElement ? serviceInfoElement.textContent : "시술 미선택";
    const dateTimeValue = dateTimeElement ? dateTimeElement.value : "";

    let formattedDateTime = "날짜/시간 미선택";
    if (dateTimeValue) {
        try {
            const dateObj = new Date(dateTimeValue);
            formattedDateTime = new Intl.DateTimeFormat('ko-KR', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
                hour12: false
            }).format(dateObj);
        } catch (e) {
            console.error("날짜/시간 포맷 오류:", e);
            formattedDateTime = "유효하지 않은 날짜/시간";
        }
    }

    summaryArea.innerHTML = `
        <p><strong>${designerName}</strong> 디자이너</p>
        <p>시술: ${serviceInfo}</p>
        <p>일정: ${formattedDateTime}</p>
    `;
}