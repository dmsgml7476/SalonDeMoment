document.addEventListener("DOMContentLoaded", () => {

  const csrfToken  = document.querySelector('meta[name="_csrf"]')?.content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

  // 변수 이름 통일
  const userId = window.currentUserId;

  if (!userId) {
    console.warn("currentUserId 없음, 알림 기능 건너뜀");
    return;
  }

  /* 웹소켓 연결 - */
  const sock   = new SockJS("/ws");
  const stomp  = Stomp.over(sock);

  stomp.connect({}, () => {
    console.log("웹소켓 연결 성공");

    stomp.subscribe(`/topic/notify/${userId}`, (msg) => {
      const data = JSON.parse(msg.body);   // { message, webTarget, targetId, unreadTotal }
        console.log("알림 수신:", msg.body);
      /* 배지 숫자를 서버가 보내준 unreadTotal 로 갱신 */
      const badge = document.getElementById("notification-badge");
      if (badge) {
        if (data.unreadTotal > 0) {
          badge.textContent  = data.unreadTotal;
          badge.dataset.count= data.unreadTotal;
          badge.style.display = "inline-block";
        } else {
          badge.style.display = "none";
          badge.dataset.count = 0;
        }
      }

      /* ------ 사이드바 알림 카드 렌더링 ------ */
      const container = document.getElementById("sidebar-alert-container");
            if (!container) return;

            const card = document.createElement("div");
            card.className      = "sidebar-alert";
            card.dataset.target = data.webTarget;
            card.dataset.id     = data.targetId;

            // (2) 내용 span
            const content = document.createElement("span");
            content.className   = "alert-content";
            content.textContent = data.message;

            // (3) 날짜 span (필요하면 createAt 포맷팅)
            const date = document.createElement("span");
            date.className   = "alert-date";
            date.textContent = data.createAt || "방금 전"

            card.append(content, date);

            card.addEventListener("click", (e) => {
              console.log("클릭 잡힙", e.target);
              /* 읽음 처리 */
              fetch("/api/notification/read", {
                method: "POST",
                headers: {
                  "Content-Type": "application/json",
                  [csrfHeader]: csrfToken,
                },
                body: JSON.stringify({
                  webTarget: data.webTarget,
                  targetId : data.targetId
                })
              })
              .then(res => {
                if(!res.ok) throw new Error ("HTTP " + res.status);

                console.log("지우려 시도는 함");

                if(["DESAPPLY", "BANNER"].includes(data.webTarget)) {
                    card.remove();
                    const unreadLeft =
                      container.querySelectorAll(".sidebar-alert").length;

                    badge.dataset.count = unreadLeft;
                    badge.textContent   = unreadLeft;
                    badge.style.display = unreadLeft > 0 ? "inline-block" : "none";

                }

                let redirectUrl = null;

                console.log("웹 타겟 타입" + data.webTarget);

                switch (data.webTarget) {
                    case "CS":
                        redirectUrl = `/myPage/myQuestionList`;
                        break;

                    case "RESER_USER":
                        redirectUrl = "/myPage/reservation";
                        break;
                      case "RESER_DES":
                        redirectUrl = card.dataset.date
                          ? `/manage/reservations?date=${encodeURIComponent(card.dataset.date)}`
                          : "/manage/reservations";
                        break;

                    case "RESER_USER":
                        redirectUrl = `/myPage/reservation`;
                        break;

                    case "SHOPAPPROVE":
                        redirectUrl = `/master/shop-edit`;
                        break;

                    case "SHOPREJECT":
                        redirectUrl = `/manage`;
                        break;
                }

                if(redirectUrl) {
                    window.location.href = redirectUrl;
                }
              })
              .catch(err => console.error("읽음 처리 실패:", err));



        /* 필요 시 페이지 이동 로직 */
      });


      container.prepend(card);
      while (container.children.length > 3) container.removeChild(container.lastChild);
    });
  }, err => console.error("웹소켓 연결 실패:", err));
});
