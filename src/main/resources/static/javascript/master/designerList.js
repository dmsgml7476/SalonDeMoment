document.addEventListener('DOMContentLoaded', () => {
  const openDesignerModalBtn = document.getElementById('openDesignerModal');
  const designerModal = document.getElementById('designerModal');
  const closeButton = designerModal.querySelector('.close-button');
  const designerSearchInput = document.getElementById('designerSearchInput');
  const searchDesignerBtn = document.getElementById('searchDesignerBtn');
  const designerSearchResults = document.getElementById('designerSearchResults');
  const addSelectedDesignerBtn = document.getElementById('addSelectedDesignerBtn');
  const designerCardList = document.getElementById('designerCardList');

  let selectedDesigner = null; // 현재 모달에서 선택된 디자이너 (DesignerResultDto 형태)

  // --- AJAX 호출로 디자이너를 가져오는 함수 (DesignerResultDto 반환) ---
  const fetchDesignersFromBackend = async (searchName = '', searchTel = '') => {
    try {
      const queryParams = new URLSearchParams();
      if (searchName) {
        queryParams.append('name', searchName);
      }
      if (searchTel) {
        queryParams.append('tel', searchTel);
      }

      const response = await fetch(`/master/designer-search?${queryParams.toString()}`);
      console.log('Backend response status:', response.status); // 응답 상태 코드 확인
      console.log('Backend response OK:', response.ok);       // 응답 성공 여부 확인

      if (!response.ok) {
        const errorText = await response.text(); // 오류 메시지 확인
        console.error('HTTP error response body:', errorText);
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      console.log('Received data from backend:', data); // 받은 데이터 확인
      return data;
    } catch (error) {
      console.error('디자이너 검색 정보를 가져오는 데 실패했습니다:', error);
      return [];
    }
  };

  // 모달 열기
  openDesignerModalBtn.addEventListener('click', (e) => {
    e.preventDefault();
    designerModal.style.display = 'flex';
    designerSearchResults.innerHTML = '<p class="no-results">검색어를 입력해주세요.</p>';
    designerSearchInput.value = '';
    selectedDesigner = null;
    addSelectedDesignerBtn.style.display = 'none'; // 모달 열 때 버튼 숨기기
  });

  // 모달 닫기
  closeButton.addEventListener('click', () => {
    designerModal.style.display = 'none';
  });

  // 모달 콘텐츠 외부 클릭 시 모달 닫기
  window.addEventListener('click', (event) => {
    if (event.target === designerModal) {
      designerModal.style.display = 'none';
    }
  });

  // 검색 기능 (버튼 클릭 및 Enter 키)
  searchDesignerBtn.addEventListener('click', () => {
    performSearch();
  });

  designerSearchInput.addEventListener('keyup', (event) => {
    if (event.key === 'Enter') {
      performSearch();
    }
  });

  // 디자이너 검색 및 결과 표시 함수
  const performSearch = async () => {
      const searchTerm = designerSearchInput.value.trim();
      designerSearchResults.innerHTML = '';
      selectedDesigner = null; // 검색 전에 선택된 디자이너 초기화
      addSelectedDesignerBtn.style.display = 'none'; // 검색 전에 버튼 숨기기

      if (searchTerm === '') {
        designerSearchResults.innerHTML = '<p class="no-results">검색어를 입력해주세요.</p>';
        return;
      }

      const searchName = searchTerm;
      const searchTel = searchTerm;

      const foundDesigners = await fetchDesignersFromBackend(searchName, searchTel);

      if (foundDesigners.length > 0) {
        foundDesigners.forEach(designer => {
          const designerItem = document.createElement('div');
          designerItem.classList.add('search-result-item');
          designerItem.setAttribute('data-designer-id', designer.id);

          let statusMessage = ''; // 소속 여부 메시지
          let isDisabled = false; // 선택 및 추가 버튼 활성화 여부를 위한 플래그

          // 소속 여부에 따라 UI 변경 및 선택 가능 여부 설정
          if (designer.affiliation) {
              statusMessage = '<p class="affiliation-status affiliated">이미 소속된 디자이너입니다</p>'; // 빨간 폰트 메시지
              designerItem.classList.add('affiliated-designer'); // CSS 스타일링을 위한 클래스 추가
              isDisabled = true; // 소속된 디자이너는 선택 불가
          }

          designerItem.innerHTML = `
            <img src="${designer.imgUrl || '/images/default_designer.jpg'}" alt="${designer.designerName} 사진" />
            <div class="search-result-info">
              <p class="name">${designer.designerName}</p>
              <p>경력: ${designer.workingYears}년차</p>
              ${statusMessage}
            </div>
          `;

          // 소속되지 않은 디자이너만 클릭 가능하게 설정
          if (!isDisabled) {
              designerItem.addEventListener('click', () => {
                // 기존 선택 해제
                const currentSelected = designerSearchResults.querySelector('.search-result-item.selected');
                if (currentSelected) {
                  currentSelected.classList.remove('selected');
                }
                // 새 항목 선택
                designerItem.classList.add('selected');
                selectedDesigner = designer; // 선택된 디자이너 DTO 객체 저장
                addSelectedDesignerBtn.style.display = 'block'; // 선택하면 버튼 활성화
              });
          } else {
              // 소속된 디자이너는 클릭 불가 스타일 적용 (선택적)
              designerItem.style.cursor = 'not-allowed';
              designerItem.style.opacity = '0.7';
          }

          designerSearchResults.appendChild(designerItem);
        });
      } else {
        designerSearchResults.innerHTML = '<p class="no-results">검색 결과가 없습니다.</p>';
      }
    };

    // 선택된 디자이너를 목록에 추가
    addSelectedDesignerBtn.addEventListener('click', async () => {
     if (selectedDesigner) {
       // 다시 한번 소속 여부 확인 (혹시 모를 UI 로직 오류 방지)
       if (selectedDesigner.affiliation) {
           alert('선택된 디자이너는 이미 다른 미용실에 소속되어 있습니다.');
           return; // 이미 소속된 디자이너는 추가 방지
       }

       // 현재 미용실 디자이너 목록 내에서 이름 중복 체크
       const existingDesignerTitles = Array.from(designerCardList.querySelectorAll('.designer-card .designer-title'))
         .map(titleElement => titleElement.textContent.trim());

       if (existingDesignerTitles.includes(selectedDesigner.designerName)) {
         alert('이미 추가된 디자이너입니다.');
         return;
       }

       // --- 백엔드로 디자이너 추가 요청 보내기 ---
       try {
         const addParams = new URLSearchParams();
         addParams.append('designerId', selectedDesigner.id); // Designer 엔티티의 ID 사용

         const response = await fetch(`/master/add-designer?${addParams.toString()}`, {
           method: 'POST',
           headers: {
             // CSRF 토큰 사용 시 필요:
             // 'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
           },
         });

         if (response.status === 400) {
             const errorText = await response.text(); // 오류 메시지를 텍스트로 받음
             console.error("디자이너 추가 400 오류:", errorText);
             alert('디자이너 추가 실패: ' + (errorText || '이미 소속되었거나 유효하지 않은 디자이너입니다.'));
             return;
         }

         if (!response.ok) {
           const errorText = await response.text();
           console.error('디자이너 추가 실패:', response.status, errorText);
           alert('디자이너 추가에 실패했습니다. 서버 오류: ' + (errorText || '알 수 없는 오류'));
           return;
         }

         const newDesignerData = await response.json(); // 새로 추가된 디자이너 정보 받기

         // 성공 시, 새로운 디자이너 카드를 DOM에 추가
         const newDesignerCard = document.createElement('div');
         newDesignerCard.classList.add('designer-card');
         // Thymeleaf의 ${designer.name} 대신 newDesignerData.name을 사용해야 함
         newDesignerCard.innerHTML = `
           <img src="${newDesignerData.imgUrl || '/images/default_designer.jpg'}" alt="${newDesignerData.name} 사진" class="designer-photo" />
           <div class="designer-info">
             <p class="designer-title">${newDesignerData.name}</p>
             <p class="designer-desc">${newDesignerData.profileSummary}</p>
             <div class="designer-stats">
               <span>🤍 ${newDesignerData.likeCount}</span>
               <span>💬 ${newDesignerData.reviewCount}</span>
             </div>
           </div>
           <a href="/shop/designer/${newDesignerData.id}" class="btn-book">관리</a>
         `;
         designerCardList.appendChild(newDesignerCard);

         // 모달 닫기 및 초기화
         designerModal.style.display = 'none';
         selectedDesigner = null;
         addSelectedDesignerBtn.style.display = 'none';
         designerSearchInput.value = '';
         designerSearchResults.innerHTML = '<p class="no-results">검색 결과가 없습니다.</p>';

       } catch (error) {
         console.error('디자이너 추가 중 네트워크 또는 파싱 오류 발생:', error);
         alert('디자이너 추가 중 예상치 못한 오류가 발생했습니다.');
       }
     } else {
       alert('디자이너를 선택해주세요.');
     }
});
});