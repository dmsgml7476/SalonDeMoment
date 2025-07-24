document.addEventListener("DOMContentLoaded", () => {

    // 로그인 인풋 클리어 기능
    const clearButtons = document.querySelectorAll(".clear-btn");

          clearButtons.forEach((btn) => {
            const input = btn.parentElement.querySelector("input");

            // 초기 상태: 값이 있으면 보여주고 없으면 숨김
            if (input.value === "") {
              btn.style.display = "none";
            } else {
              btn.style.display = "inline";
            }

            // x 버튼 클릭 시 input 비우고 버튼 숨김
            btn.addEventListener("click", () => {
              input.value = "";
              input.focus();
              btn.style.display = "none";
            });

            // input 입력 시 버튼 다시 표시
            input.addEventListener("input", () => {
              if (input.value === "") {
                btn.style.display = "none";
              } else {
                btn.style.display = "inline";
              }
            });
          });

    // find id modal start

    const findIdModal = document.getElementById("findIdModal");
    const step1 = document.getElementById("findIdStep1");
    const step2 = document.getElementById("findIdStep2");

    const emailInput = document.getElementById("emailInput");
    const emailMsg = document.getElementById("chkEmailMsg");

    const codeInput = document.getElementById("emailCodeInput");
    const codeMsg = document.getElementById("chkCodeMsg");

    const sendBtn = document.getElementById("sendChkNum");
    const verifyBtn = document.getElementById("chkNumCheck");
    const nextBtn = document.getElementById("nextBtn");

   const csrfToken = document.getElementById("csrfToken").value;
   const csrfHeader = document.getElementById("csrfHeader").value;


    document.querySelector(".find-id").addEventListener("click", openFindIdModal);

    function openFindIdModal() {
        findIdModal.style.display = "block";
        resetFindIdModal();
    }

    window.closeFindIdModal = function () {
        findIdModal.style.display= "none";
    };

    function resetFindIdModal() {
        step1.style.display = "block";
        step2.style.display = "none";
        emailInput.value = "";
        codeInput.value = "";
        emailMsg.style.display = "none";
        codeMsg.style.display = "none";
        nextBtn.disabled = true;
    }

    sendBtn.addEventListener("click", async function(e) {
        e.preventDefault();

        const email = emailInput.value.trim();
        emailMsg.style.display = "none";


        if(!email) {
            emailMsg.textContent = "이메일을 입력하세요";
            emailMsg.style.display = "block";
            return;
        }

        try {
            const res = await fetch("/auth/email/check", {
                method: "post",
                headers: {"Content-Type": "application/json",
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify({email})
            });

            const result = await res.json();

            if(!result.exists) {
                emailMsg.textContent = "일치하는 이메일이 없습니다";
                emailMsg.style.color = "red";
                emailMsg.style.display = "block";
                return;
            }

            emailMsg.textContent = "인증번호가 전송되었습니다.";
            emailMsg.style.color = "green";
            emailMsg.style.display = "block";
            codeInput.focus();


            verifyBtn.disabled = false;

            fetch("/auth/email/send", {
              method: "POST",
              headers: {
                "Content-Type" : "application/json",
                [csrfHeader]: csrfToken
              },
              body: JSON.stringify({email, context: "find"})
            })
              .then(res => res.json())
              .then(data => {
                if(data.status !== "sent") {
                    emailMsg.textContent = "이메일 전송에 실패했습니다";
                    emailMsg.style.color = "red";
                    }
              })
              .catch(() => {
                emailMsg.textContent = "서버 오류가 발생했습니다.";
                emailMsg.style.color = "red";
              });

        } catch (err) {
            emailMsg.textContent = "서버 오류가 발생했습니다.";
            emailMsg.style.color = "red";
            emailMsg.style.display = "block";
        }
    });

// check send number

    verifyBtn.addEventListener("click", function() {
        const code = codeInput.value.trim();
        codeMsg.style.display = "none";

        if(!code) {
            codeMsg.textContent = "인증번호를 입력해주세요.";
            codeMsg.style.color = "red";
            codeMsg.style.display = "block";
            return;
        }

        fetch("/auth/email/verify", {
            method:"POST",
            headers: {"Content-Type":"application/json"},
            credentials: "include",
            body: JSON.stringify({code})
        })
            .then(res => res.json())
            .then(data => {
                if(data.success) {
                    codeMsg.textContent="인증되었습니다.";
                    codeMsg.style.color = "green";
                    nextBtn.disabled = false;


                    verifyBtn.disabled = true;
                } else {
                    codeMsg.textContent = "인증번호가 일치하지 않습니다";
                    codeMsg.style.color = "red";
                    nextBtn.disabled=true;
                }
                codeMsg.style.display = "block";
            })
            .catch(() => {
               codeMsg.textContent = "서버 오류가 발생했습니다.";
               codeMsg.style.color = "red";
                codeMsg.style.display = "block";
                nextBtn.disabled = true;
            });
    });

//    step2

    window.goToIdPrintStep = async function () {
        step1.style.display = "none";
        step2.style.display = "block";

        try {
         const res = await fetch("/auth/email/find-id", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [csrfHeader]: csrfToken
            },
            credentials: "include"
         });

         const data = await res.json();

         if (data.loginId) {
            document.getElementById("foundId").textContent = data.loginId;
         } else {
            document.getElementById("foundId").textContent = "알 수 없음";
         }
       } catch (err) {
            document.getElementById("foundId").textContent = "서버 오류";
       }
    };

// findPw

  const pwModal = document.getElementById("findPwModal");
  const pwStep1 = document.getElementById("findPwStep1");
  const pwStep2 = document.getElementById("findPwStep2");

  const pwEmailInput = document.getElementById("pwEmailInput");
  const pwEmailMsg = document.getElementById("pwEmailMsg");

  const pwCodeInput = document.getElementById("pwEmailCodeInput");
  const pwCodeMsg = document.getElementById("chkPwCodeMsg");

  const pwSendBtn = document.getElementById("pwSendChkNum");
  const pwVerifyBtn = document.getElementById("pwNumCheck");
  const pwNextBtn = document.getElementById("pwNextBtn");

  const changePwEmailInput = document.getElementById("changePwEmail");


  	document.querySelector(".find-pw").addEventListener("click", openFindPwModal);

    function openFindPwModal() {
      pwModal.style.display = "block";
//      resetPwModal();
    }

    function resetPwModal() {
      pwStep1.style.display = "block";
      pwStep2.style.display = "none";
      pwEmailInput.value = "";
      pwCodeInput.value = "";
      pwEmailMsg.style.display = "none";
      pwCodeMsg.style.display = "none";
      pwNextBtn.disabled = true;

      const changePwResult = document.getElementById("changePwResult");
      if (changePwResult) {
        changePwResult.style.display = "none";
      }
    }


// 비밀번호 찾기때문에 이메일 보내는 코드

  pwSendBtn.addEventListener("click", async function(e) {
    e.preventDefault();

    const email = pwEmailInput.value.trim();
    pwEmailMsg.style.display = "none";


    if (!email) {
        pwEmailMsg.textContent = "이메일을 입력하세요.";
        pwEmailMsg.style.display = "block";
        return;
    }

    try {
        const res = await fetch("/auth/email/check", {
            method: "post",
            headers: {"Content-Type": "application/json",
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({email})
        });

        const result = await res.json();

        if(!result.exists) {
            pwEmailMsg.textContent="일치하는 이메일이 없습니다.";
            pwEmailMsg.style.color = "red";
            pwEmailMsg.style.display = "block";

            return;
        }

        pwEmailMsg.textContent="인증번호가 전송되었습니다.";
        pwEmailMsg.style.color = "green";
        pwEmailMsg.style.display = "block";
        pwCodeInput.focus();


        pwVerifyBtn.disabled = false;

        fetch("/auth/email/send", {
            method: "POST",
            headers: {
                "Content-Type" : "application/json",
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({email, context:"pw"})
        })
          .then(res => res.json())
          .then(data => {
            if(data.status !== "sent") {
                pwEmailMsg.textContent = "이메일 전송에 실패했습니다.";
                pwEmailMsg.style.color = "red";
            }
          })
          .catch(()=> {
            pwEmailMsg.textContent = "서버 오류가 발생했습니다.";
            pwEmailMsg.style.color = "red";
          });
    } catch (err) {
        pwEmailMsg.textContent="서버 오류가 발생했습니다.";
        pwEmailMsg.style.color = "red";
        pwEmailMsg.style.display = "block";
    }

  });


    pwVerifyBtn.addEventListener("click", async () => {
        const code = pwCodeInput.value.trim();
        pwCodeMsg.style.display = "none";

        if (!code) {
          pwCodeMsg.textContent = "인증번호를 입력해주세요.";
          pwCodeMsg.style.display = "block";
          return;
        }

        try {
          const res = await fetch("/auth/email/verify", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({ code }),
          });

          const data = await res.json();
          if (data.success) {
            pwCodeMsg.textContent = "인증되었습니다.";
            pwCodeMsg.style.color = "green";
            pwCodeMsg.style.display = "block";
            pwNextBtn.disabled = false;

            pwVerifyBtn.disabled = true;
          } else {
            pwCodeMsg.textContent = "인증번호가 일치하지 않습니다.";
            pwCodeMsg.style.color = "red";
            pwCodeMsg.style.display = "block";
            pwNextBtn.disabled = true;
          }
        } catch {
          pwCodeMsg.textContent = "서버 오류가 발생했습니다.";
          pwCodeMsg.style.color = "red";
          pwCodeMsg.style.display = "block";
        }
    });

    window.goToPwChange = function () {
      pwStep1.style.display = "none";
      pwStep2.style.display = "block";
      changePwEmailInput.value = pwEmailInput.value;
    };


//    change pw


    const newPwInput = document.getElementById("newPassword");
    const confirmPwInput = document.getElementById("confirmPassword");

    const pwConfirmMsg = document.getElementById("pwConfirmMsg");
    const pwMismatchMsg = document.getElementById("pwMismatchMsg");

    const changePwBtn = document.getElementById("changePwBtn");

    let isPwValid = false;
    let isPwMatch = false;

    // 새비밀번호 유효성

    newPwInput.addEventListener("input", function() {
        const pw = newPwInput.value.trim();

        if (pw === "") {
            pwConfirmMsg.textContent="";
            pwConfirmMsg.style.display = "none";
                isPwValid = false;
                checkPwMatch();
            return;
        }

        const lengthValid = pw.length >= 8 && pw.length <= 30;
        const containsLetter = /[A-Za-z]/.test(pw);
        const containsDigit = /\d/.test(pw);
        const allowedCharsOnly = /^[A-Za-z\d!@#$%^&*]+$/.test(pw);


        if(!allowedCharsOnly) {
            pwConfirmMsg.textContent="특수문자는 !,@,#,$,%,^,&,* 만 사용할 수 있습니다.";
            pwConfirmMsg.style.display="block";
            pwConfirmMsg.style.color = "red";
            isPwValid = false;
            checkPwMatch();
            return;
        }

        if(!lengthValid) {
            pwConfirmMsg.textContent="비밀번호는 8자 이상 30자 이하로 입력하세요.";
            pwConfirmMsg.style.display="block";
            pwConfirmMsg.style.color = "red";
            isPwValid = false;
            checkPwMatch();
            return;
        } else if (!(containsLetter && containsDigit)) {
            pwConfirmMsg.textContent="영문자와 숫자가 최소 하나씩은 포함되어있어야 합니다.";
            pwConfirmMsg.style.display="block";
            pwConfirmMsg.style.color = "red";
            isPwValid = false;
            checkPwMatch();
            return;
        }

        pwConfirmMsg.textContent = "사용할 수 있는 비밀번호입니다.";
        pwConfirmMsg.style.color = "green";
        pwConfirmMsg.style.display = "block";
        isPwValid = true;
        checkPwMatch();

    });


    // 비밀번호 확인 코드


    confirmPwInput.addEventListener("input", checkPwMatch);

    function checkPwMatch() {
        const pw = newPwInput.value.trim();
        const pwChk = confirmPwInput.value.trim();

        if(pw && pwChk && pw === pwChk) {
            pwMismatchMsg.textContent="비밀번호가 일치합니다.";
            pwMismatchMsg.style.color = "green";
            pwMismatchMsg.style.display = "block";
            isPwMatch = true;
        } else if (pwChk.length > 0) {
            pwMismatchMsg.textContent = "비밀번호가 일치하지 않습니다.";
            pwMismatchMsg.style.color = "red";
            pwMismatchMsg.style.display = "block";
            isPwMatch = false;
        } else {
            pwMismatchMsg.textContent = "";
            pwMismatchMsg.style.display = "none";
            isPwMatch = false;
        }

        checkValidState();
    }

  	function checkValidState() {
  			changePwBtn.disabled = !(isPwValid && isPwMatch);
  	}

document.getElementById("changePwForm").addEventListener("submit", async function(e) {
  e.preventDefault();

  const email = document.getElementById("changePwEmail").value;
  const newPassword = document.getElementById("newPassword").value;

  const csrfToken = document.getElementById("csrfToken").value;
  const csrfHeader = document.getElementById("csrfHeader").value;

  document.getElementById("findPwStep2").style.display = "none";
  document.getElementById("changePwResult").style.display = "block";

  try {
    const response = await fetch("/auth/email/editPw", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        [csrfHeader]: csrfToken
      },
      body: JSON.stringify({ email, newPassword })
    });

    const result = await response.text();

    // 실패했다면 사용자에게 알림만 띄움 (결과 화면은 그대로 유지)
    if (result !== "success") {
      alert("비밀번호 변경에 실패했습니다. 다시 시도해 주세요.");
    }

  } catch (err) {
    alert("서버 오류가 발생했습니다.");
  }
});

window.closeFindPwModal = function () {
  const pwModal = document.getElementById("findPwModal");
  pwModal.style.display = "none";
  resetPwModal(); // 선택 사항
};





// document end
});