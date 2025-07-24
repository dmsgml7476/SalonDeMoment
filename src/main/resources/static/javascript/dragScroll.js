export function enableDragScroll(
  target,
  { multiplier = 1.2, clickThreshold = 5 } = {}
) {
  if (!target) return;

  let isDown = false;
  let startX = 0;
  let scrollX = 0;
  let moved = false;

  function removeDragging() {
    isDown = false;
    moved = false;
    target.classList.remove("dragging");
    document.body.style.userSelect = "auto";
  }

  // PC: 마우스 시작
  target.addEventListener("mousedown", (e) => {
    isDown = true;
    moved = false;
    startX = e.pageX;
    scrollX = target.scrollLeft;
    document.body.style.userSelect = "none";
  });

  // PC: 마우스 이동
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

  // PC: 마우스 해제
  target.addEventListener("mousemove", (e) => {
      if (!isDown) return;

      const dx = e.pageX - startX;
      if (!moved && Math.abs(dx) > clickThreshold) {
        moved = true;
        target.classList.add("dragging");
      }

      e.preventDefault();
      target.scrollLeft = scrollX - dx * multiplier;
    });


    const onMouseUp = (e) => {
      if (!isDown) return;

      removeDragging();


      if (moved) {

        const preventClick = (clickEvent) => {
          clickEvent.stopImmediatePropagation();
          clickEvent.preventDefault();
          target.removeEventListener("click", preventClick, true);
        };

        setTimeout(() => {
          target.addEventListener("click", preventClick, true);
        }, 0);
      }
    };


  window.addEventListener("mouseup", onMouseUp);
  target.addEventListener("mouseleave", removeDragging);
  window.addEventListener("blur", removeDragging);
  document.addEventListener("visibilitychange", () => {
    if (document.visibilityState === "hidden") removeDragging();
  });

  // 📱 모바일 터치
  let touchStartX = 0;
  target.addEventListener(
    "touchstart",
    (e) => {
      touchStartX = e.touches[0].pageX;
      scrollX = target.scrollLeft;
    },
    { passive: true }
  );

  target.addEventListener(
    "touchmove",
    (e) => {
      const dx = e.touches[0].pageX - touchStartX;
      target.scrollLeft = scrollX - dx * multiplier;
    },
    { passive: true }
  );
}

// 자동 바인딩
export function autoBindDragScroll(selector = "[data-drag-scroll]") {
  document.querySelectorAll(selector).forEach((el) => {
    el.classList.remove("dragging");
    enableDragScroll(el);
  });
}


export function resetAllDragScrollState() {
  document.querySelectorAll('[data-drag-scroll]').forEach(el => {
    el.classList.remove('dragging');
    el.style.cursor = ''; // 💡 추가: 커서 초기화
  });
  document.body.style.userSelect = 'auto';
  document.body.style.cursor = ''; // 💡 추가: body 커서도 초기화
}
