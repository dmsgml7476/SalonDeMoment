document.addEventListener('DOMContentLoaded', () => {
    const openServiceModalBtn = document.getElementById('openServiceModal');
    const serviceModal = document.getElementById('serviceModal');
    const closeServiceModalBtn = document.getElementById('closeServiceModal');
    const serviceForm = document.getElementById('serviceForm');
    const saveServiceBtn = document.getElementById('saveServiceBtn');
    const cancelServiceBtn = document.getElementById('cancelServiceBtn');
    const serviceListContainer = document.getElementById('serviceList');
    const modalTitle = document.getElementById('modalTitle');

    // 폼 필드 요소들
    const serviceIdInput = document.getElementById('serviceId');
    const serviceNameInput = document.getElementById('serviceName');
    const descriptionInput = document.getElementById('description');
    const priceInput = document.getElementById('price');
    const categorySelect = document.getElementById('category');
    const imgFileInput = document.getElementById('imgFile');
    const recommendedCheckbox = document.getElementById('recommended');

    // 이미지 관련 숨김 필드
    const originalImgNameInput = document.getElementById('originalImgName');
    const imgNameInput = document.getElementById('imgName');
    const imgUrlInput = document.getElementById('imgUrl');

    // 이미지 미리보기 요소
    const imagePreview = document.getElementById('imagePreview');
    const imageFileName = document.getElementById('imageFileName');
    const currentImagePreviewDiv = document.getElementById('currentImagePreview');
    const deleteCurrentImageBtn = document.getElementById('deleteCurrentImageBtn');


    // CSRF 토큰 및 헤더 가져오기 (필요하다면)
    const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    const csrfToken = csrfTokenMeta ? csrfTokenMeta.getAttribute('content') : null;
    const csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.getAttribute('content') : null;

    // 모달 초기화 함수
    const resetServiceModal = () => {
        serviceForm.reset(); // 폼 필드 초기화
        serviceIdInput.value = ''; // ID 필드 초기화
        originalImgNameInput.value = '';
        imgNameInput.value = '';
        imgUrlInput.value = '';
        modalTitle.textContent = '시술 추가'; // 제목 변경
        imagePreview.src = '#';
        imagePreview.style.display = 'none'; // 이미지 미리보기 숨김
        imageFileName.textContent = ''; // 파일명 초기화
        deleteCurrentImageBtn.style.display = 'none'; // 삭제 버튼 숨김
        recommendedCheckbox.checked = false; // 추천 체크박스 초기화
    };

    // 모달 열기
    openServiceModalBtn.addEventListener('click', (e) => {
        e.preventDefault();
        resetServiceModal(); // 모달 열기 전에 초기화
        serviceModal.style.display = 'flex';
    });

    // 모달 닫기
    closeServiceModalBtn.addEventListener('click', () => {
        serviceModal.style.display = 'none';
    });

    cancelServiceBtn.addEventListener('click', () => {
        serviceModal.style.display = 'none';
    });

    // 모달 외부 클릭 시 닫기
    window.addEventListener('click', (event) => {
        if (event.target === serviceModal) {
            serviceModal.style.display = 'none';
        }
    });

    // 이미지 파일 선택 시 미리보기
    imgFileInput.addEventListener('change', (event) => {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = (e) => {
                imagePreview.src = e.target.result;
                imagePreview.style.display = 'block';
            };
            reader.readAsDataURL(file);
            imageFileName.textContent = file.name;
            deleteCurrentImageBtn.style.display = 'none'; // 새 파일 선택 시 기존 이미지 삭제 버튼 숨김
        } else {
            // 파일이 선택되지 않은 경우, 기존 이미지 정보가 있다면 그것을 보여줌
            if (imgUrlInput.value) {
                imagePreview.src = imgUrlInput.value;
                imagePreview.style.display = 'block';
                imageFileName.textContent = originalImgNameInput.value;
                deleteCurrentImageBtn.style.display = 'block';
            } else {
                imagePreview.src = '#';
                imagePreview.style.display = 'none';
                imageFileName.textContent = '';
                deleteCurrentImageBtn.style.display = 'none';
            }
        }
    });

    // 현재 이미지 삭제 버튼 클릭 시
    deleteCurrentImageBtn.addEventListener('click', () => {
        // 숨김 필드 초기화
        originalImgNameInput.value = '';
        imgNameInput.value = '';
        imgUrlInput.value = '';
        imgFileInput.value = ''; // 파일 input도 초기화
        imagePreview.src = '#';
        imagePreview.style.display = 'none';
        imageFileName.textContent = '';
        deleteCurrentImageBtn.style.display = 'none';
    });


    // 시술 등록 및 수정 처리
    serviceForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const serviceId = serviceIdInput.value;
        const serviceName = serviceNameInput.value.trim();
        const description = descriptionInput.value.trim();
        const price = priceInput.value;
        const category = categorySelect.value; // 선택된 카테고리 값
        const recommended = recommendedCheckbox.checked; // 추천 시술 체크 여부

        if (!serviceName || !price) {
            alert('시술명과 가격은 필수 입력 항목입니다.');
            return;
        }

        const method = serviceId ? 'PUT' : 'POST';
        const url = serviceId ? `/master/services/${serviceId}` : '/master/services';

        // FormData 객체 생성 (파일 업로드 포함)
        const formData = new FormData();
        if (serviceId) formData.append('id', serviceId); // 수정 시 ID 추가
        formData.append('name', serviceName);
        formData.append('description', description);
        formData.append('price', price);
        formData.append('category', category); // 카테고리 추가
        formData.append('recommended', recommended); // 추천 여부 추가

        // 이미지 파일이 선택된 경우 추가
        if (imgFileInput.files.length > 0) {
            formData.append('imgFile', imgFileInput.files[0]);
        } else {
            // 파일이 선택되지 않았을 경우 기존 이미지 정보 유지
            formData.append('originalImgName', originalImgNameInput.value);
            formData.append('imgName', imgNameInput.value);
            formData.append('imgUrl', imgUrlInput.value);
            // 만약 기존 이미지를 삭제했는데 새로운 이미지를 선택하지 않았다면, 서버에서 이미지 삭제 처리해야 함
            // isImageDeleted 플래그를 추가로 보내는 등의 로직 필요
            if (originalImgNameInput.value === '' && imgNameInput.value === '' && imgUrlInput.value === '') {
                // 이미지가 완전히 삭제된 상태 (새로운 파일도 없고, 기존 파일 정보도 없음)
                formData.append('isImageDeleted', 'true');
            }
        }


        try {
            const response = await fetch(url, {
                method: method,
                headers: {
                    // FormData 사용 시 Content-Type 헤더를 설정하지 않습니다. 브라우저가 자동으로 multipart/form-data로 설정합니다.
                    ...(csrfHeader && csrfToken && { [csrfHeader]: csrfToken }),
                },
                body: formData // FormData 객체 전송
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`Failed to save service: ${response.status} - ${errorText}`);
            }

            alert(`시술이 성공적으로 ${serviceId ? '수정' : '등록'}되었습니다.`);
            serviceModal.style.display = 'none';
            location.reload(); // 페이지 새로고침하여 목록 업데이트

        } catch (error) {
            console.error('시술 저장 실패:', error);
            alert('시술 저장에 실패했습니다. 오류: ' + error.message);
        }
    });

    // 시술 삭제 처리 (이벤트 위임 사용)
    serviceListContainer.addEventListener('click', async (e) => {
        if (e.target.classList.contains('btn-delete')) {
            const serviceId = e.target.dataset.serviceId;
            if (!serviceId) {
                alert('삭제할 시술 ID를 찾을 수 없습니다.');
                return;
            }

            if (confirm('정말로 이 시술을 삭제하시겠습니까?')) {
                try {
                    const response = await fetch(`/master/services/${serviceId}`, {
                        method: 'DELETE',
                        headers: {
                            ...(csrfHeader && csrfToken && { [csrfHeader]: csrfToken }),
                        }
                    });

                    if (!response.ok) {
                        const errorText = await response.text();
                        throw new Error(`Failed to delete service: ${response.status} - ${errorText}`);
                    }

                    alert('시술이 성공적으로 삭제되었습니다.');
                    e.target.closest('.service-item').remove(); // DOM에서 해당 아이템 제거

                    // 만약 모든 시술이 삭제되면 "등록된 시술이 없습니다" 메시지 표시
                    const remainingServices = serviceListContainer.querySelectorAll('.service-item').length;
                    if (remainingServices === 0) {
                        const noServicesMessage = document.createElement('div');
                        noServicesMessage.classList.add('no-services');
                        noServicesMessage.innerHTML = '<p>등록된 시술이 없습니다.</p>';
                        serviceListContainer.appendChild(noServicesMessage);
                    }
                } catch (error) {
                    console.error('시술 삭제 실패:', error);
                    alert('시술 삭제에 실패했습니다. 오류: ' + error.message);
                }
            }
        }
    });

    // 시술 수정 모달 열기 (이벤트 위임 사용) - '수정' 버튼 클릭 시
    serviceListContainer.addEventListener('click', async (e) => {
        if (e.target.classList.contains('btn-edit')) { // 'btn-edit' 클릭 시
            const serviceId = e.target.dataset.serviceId;
            if (!serviceId) return;

            resetServiceModal(); // 모달 열기 전에 초기화

            try {
                // 특정 시술의 상세 정보를 백엔드에서 가져옵니다.
                const response = await fetch(`/master/services/${serviceId}`);
                if (!response.ok) {
                    throw new Error(`Failed to fetch service details: ${response.status}`);
                }
                const serviceData = await response.json();

                // 모달 폼에 데이터 채우기
                serviceIdInput.value = serviceData.id;
                serviceNameInput.value = serviceData.name; // DTO 필드명 'name'
                descriptionInput.value = serviceData.description;
                priceInput.value = serviceData.price;
                categorySelect.value = serviceData.category; // 카테고리 설정
                recommendedCheckbox.checked = serviceData.recommended; // 추천 체크박스 설정

                // 이미지 정보 설정
                originalImgNameInput.value = serviceData.originalImgName || '';
                imgNameInput.value = serviceData.imgName || '';
                imgUrlInput.value = serviceData.imgUrl || '';

                if (serviceData.imgUrl) {
                    imagePreview.src = serviceData.imgUrl;
                    imagePreview.style.display = 'block';
                    imageFileName.textContent = serviceData.originalImgName || '이미지 파일';
                    deleteCurrentImageBtn.style.display = 'block';
                } else {
                    imagePreview.src = '#';
                    imagePreview.style.display = 'none';
                    imageFileName.textContent = '';
                    deleteCurrentImageBtn.style.display = 'none';
                }

                modalTitle.textContent = '시술 수정'; // 제목 변경
                serviceModal.style.display = 'flex';

            } catch (error) {
                console.error('시술 정보 불러오기 실패:', error);
                alert('시술 정보를 불러오는 데 실패했습니다.');
            }
        }
    });

    // 시술 수정 모달 열기 (이벤트 위임 사용) - '수정' 버튼 클릭 시
    serviceListContainer.addEventListener('click', async (e) => {
        if (e.target.classList.contains('btn-edit')) { // 'btn-edit' 클릭 시
            const serviceId = e.target.dataset.serviceId;
            if (!serviceId) return;

            resetServiceModal(); // 모달 열기 전에 초기화

            try {
                // 특정 시술의 상세 정보를 백엔드에서 가져옵니다.
                const response = await fetch(`/master/services/${serviceId}`);
                if (!response.ok) {
                    throw new Error(`Failed to fetch service details: ${response.status}`);
                }
                const serviceData = await response.json();

                // 모달 폼에 데이터 채우기 (여기에 시술 상세 정보가 채워집니다)
                serviceIdInput.value = serviceData.id;
                serviceNameInput.value = serviceData.name;
                descriptionInput.value = serviceData.description;
                priceInput.value = serviceData.price;
                categorySelect.value = serviceData.category;
                recommendedCheckbox.checked = serviceData.recommended;

                // 이미지 정보 설정
                originalImgNameInput.value = serviceData.originalImgName || '';
                imgNameInput.value = serviceData.imgName || '';
                imgUrlInput.value = serviceData.imgUrl || '';

                if (serviceData.imgUrl) {
                    imagePreview.src = serviceData.imgUrl;
                    imagePreview.style.display = 'block';
                    imageFileName.textContent = serviceData.originalImgName || '이미지 파일';
                    deleteCurrentImageBtn.style.display = 'block';
                } else {
                    imagePreview.src = '#';
                    imagePreview.style.display = 'none';
                    imageFileName.textContent = '';
                    deleteCurrentImageBtn.style.display = 'none';
                }

                modalTitle.textContent = '시술 수정'; // 모달 제목을 "시술 수정"으로 변경
                serviceModal.style.display = 'flex'; // 모달 표시

            } catch (error) {
                console.error('시술 정보 불러오기 실패:', error);
                alert('시술 정보를 불러오는 데 실패했습니다.');
            }
        }
    });
});