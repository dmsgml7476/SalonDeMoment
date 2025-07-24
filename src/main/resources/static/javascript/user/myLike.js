document.addEventListener("DOMContentLoaded", function () {
        const sliders = document.querySelectorAll(".scroll-wrapper");

        sliders.forEach((slider) => {
          let isDown = false;
          let startX;
          let scrollLeft;

          slider.addEventListener("mousedown", (e) => {
            if (e.target.tagName === "BUTTON") return;

            isDown = true;
            slider.classList.add("dragging");
            startX = e.pageX;
            scrollLeft = slider.scrollLeft;
            console.log("🟢 드래그 시작", startX);
          });

          document.addEventListener("mouseup", () => {
            if (isDown) console.log("🔴 드래그 종료");
            isDown = false;
            slider.classList.remove("dragging");
          });

          document.addEventListener("mousemove", (e) => {
            if (!isDown) return;

            const x = e.pageX;
            const walk = (x - startX) * 1.5;

            const direction =
              walk > 0
                ? "← 왼쪽으로 드래그 (오른쪽으로 스크롤)"
                : walk < 0
                ? "→ 오른쪽으로 드래그 (왼쪽으로 스크롤)"
                : "정지";

            console.log(
              `🟡 ${direction}, walk: ${walk.toFixed(
                2
              )}, scrollLeft: ${slider.scrollLeft.toFixed(2)}`
            );

            e.preventDefault();
            slider.scrollLeft = scrollLeft - walk;
          });
        });
      });