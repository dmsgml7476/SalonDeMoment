document.addEventListener("DOMContentLoaded", function () {
    const chkLoginIdBtn = document.getElementById("chkLoginIdBtn");  // 중복확인 버튼
    const loginIdInput = document.getElementById("loginId"); // 로그인 인풋
    const idMsg = document.getElementById("loginIdMsg"); // 로그인 에러 메세지


//    아이디 유효성 검사

    let isIdValid=false;  // 아이디 중복 인증 여부

    // 아이디 입력시 인증 리셋

    loginIdInput.addEventListener("input", () => {
        isIdValid=false;
        idMsg.textContent="";
        idMsg.style.display = "none";
    });

    chkLoginIdBtn.addEventListener("click", function() {
        const loginId = loginIdInput.value.trim();
        const regex = /^[a-z0-9_]+$/;

        if(!loginId) {
            idMsg.textContent = "아이디를 입력해주세요";
            idMsg.style.color = "var(--red-msg)";
            idMsg.style.display = "block";
            isIdValid=false;
            return;
        }

        if(loginId.length < 6 || loginId.length > 16) {
            idMsg.textContent = "아이디는 6자 이상 16자 이하로 입력해주세요."
            idMsg.style.color = "var(--red-msg)";
            idMsg.style.display = "block";
            isIdValid=false;
            return;
        }

        if(!regex.test(loginId)) {
            idMsg.textContent = "아이디는 영어 소문자, 숫자, 언더바(_)만 사용할 수 있습니다.";
            idMsg.style.color = "var(--red-msg)";
            idMsg.style.display = "block";
            isIdValid=false;
            return;
        }

        fetch(`/check-id?loginId=${loginId}`)
            .then(res => res.json())
            .then(data => {
                if (data.exists) {
                    idMsg.textContent = "이미 사용 중인 아이디입니다.";
                    idMsg.style.color = "var(--red-msg)";
                    idMsg.style.display = "block";
                    isIdValid=false;
                } else {
                    idMsg.textContent = "사용 가능한 아이디입니다."
                    idMsg.style.color = "green"
                    idMsg.style.display = "block";
                    isIdValid=true;
                }
            })
            .catch(() => {
                idMsg.textContent = "서버 오류가 발생했습니다.";
                idMsg.style.color = "var(--red-msg)";
                idMsg.style.display = "block";
                isIdValid=false;
            });

    });

    // 비밀번호 코드



    const pwInput = document.getElementById("password"); // 비밀번호 입력 인풋
    const pwMsg = document.getElementById("pwMsg"); // 비밀번호 확인 메세지


    let isPwValid =false;

    pwInput.addEventListener("input", function() {
        const pw = pwInput.value.trim();

        if (pw === "") {
            pwMsg.textContent = "";
            pwMsg.style.display = "none";
            isPwValid =false;
            return;
        }

        const lengthValid = pw.length >= 8 && pw.length <= 30;
        const containsLetter = /[A-Za-z]/.test(pw);
        const containsDigit = /\d/.test(pw);
        const allowedCharsOnly = /^[A-Za-z\d!@#$%^&*]+$/.test(pw);

        if(!allowedCharsOnly) {
            pwMsg.textContent="특수문자는 !,@,#,$,%,^,&,* 만 사용할 수 있습니다.";
            pwMsg.style.display="block";
            pwMsg.style.color = "var(--red-msg)";
            isPwValid = false;
            checkPwMatch();
            return;
        }

        if(!lengthValid) {
            pwMsg.textContent="비밀번호는 8자 이상 30자 이하로 입력하세요.";
            pwMsg.style.display="block";
            pwMsg.style.color = "var(--red-msg)";
            isPwValid = false;
            checkPwMatch();
            return;
        } else if (!(containsLetter && containsDigit)) {
            pwMsg.textContent="영문자와 숫자가 최소 하나씩은 포함되어있어야 합니다.";
            pwMsg.style.display="block";
            pwMsg.style.color = "var(--red-msg)";
            isPwValid = false;
            checkPwMatch();
           return;
        }

        pwMsg.textContent = "사용할 수 있는 비밀번호입니다.";
        pwMsg.style.color = "green";
        pwMsg.style.display = "block";
        isPwValid = true;
        checkPwMatch();

    });


    const pwChkInput = document.getElementById("confirmPassword"); // 비밀번호 확인 인풋
    const pwMismatchMsg = document.getElementById("pwConfirmMsg"); // 비밀번호 일치 확인 메세지

    let isPwMatch = false;

    // 비밀번호 확인코드
    pwChkInput.addEventListener("blur", checkPwMatch);

    function checkPwMatch() {
      const pw = pwInput.value.trim();
      const pwChk = pwChkInput.value.trim();

      if(pw && pwChk && pw === pwChk) {
        pwMismatchMsg.textContent="비밀번호가 일치합니다.";
        pwMismatchMsg.style.color = "green";
        pwMismatchMsg.style.display = "block";
        isPwMatch = true;
      } else if (pwChk.length > 0) {
        pwMismatchMsg.textContent = "비밀번호가 일치하지 않습니다.";
        pwMismatchMsg.style.color = "var(--red-msg)";
        pwMismatchMsg.style.display = "block";
        isPwMatch = false;
      } else {
        pwMismatchMsg.textContent = "";
        pwMismatchMsg.style.display = "none";
        isPwMatch = false;
      }
    }


    // 비밀번호 확인 끝


    // 이메일
    const emailInput = document.getElementById("emailInput");
    const emailMsg = document.getElementById("chkEmailMsg");

    const codeInput = document.getElementById("emailCodeInput");
    const codeMsg = document.getElementById("chkCodeMsg");

    const sendBtn = document.getElementById("sendChkBtn");
    const verifyBtn = document.getElementById("chkNumCheck");

    const csrfToken = document.getElementById("csrfToken").value;
    const csrfHeader = document.getElementById("csrfHeader").value;

    let isEmailValid = false;
    let isEmailVerified = false;

    emailInput.addEventListener("input", () => {
        isEmailValid = false;
        isEmailVerified = false;
        emailMsg.textContent = "";
        codeMsg.textContent = "";
        codeMsg.style.display = "none";
        verifyBtn.disabled = false;
    });

    sendBtn.addEventListener("click", async function(e) {
        e.preventDefault();

        const email = emailInput.value.trim();
        emailMsg.style.display = "none";





        if(!email) {
           emailMsg.textContent = "이메일을 입력하세요";
           emailMsg.style.color = "var(--red-msg)";
           emailMsg.style.display = "block";
           isEmailValid = false;
           return;
        }

        const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (!emailPattern.test(email)) {
           emailMsg.textContent = "유효한 이메일 주소를 입력해주세요.";
           emailMsg.style.color = "var(--red-msg)";
           emailMsg.style.display = "block";
           isEmailValid = false;
           return;
        }

        try {
           const res = await fetch("/auth/email/check", {
           method: "POST",
           headers: {"Content-Type": "application/json",
               [csrfHeader]: csrfToken
           },
           body: JSON.stringify({email})
           });

           const result = await res.json();

           if(result.exists) {
              emailMsg.textContent = "이미 존재하는 이메일입니다.";
              emailMsg.style.color = "var(--red-msg)";
              emailMsg.style.display = "block";
              isEmailValid = false;
              return;
           }

           isEmailValid = true;
           emailMsg.textContent = "인증번호가 전송되었습니다.";
           emailMsg.style.color = "green";
           emailMsg.style.display = "block";
           codeInput.focus();


           fetch("/auth/email/send", {
             method: "POST",
             headers: {
                "Content-Type" : "application/json",
                [csrfHeader]: csrfToken
             },
             body: JSON.stringify({email, context: "signUp_chk"})
           })
              .then(res => res.json())
              .then(data => {
                 if(data.status !== "sent") {
                   emailMsg.textContent = "이메일 전송에 실패했습니다";
                   emailMsg.style.color = "var(--red-msg)";
                   isEmailValid = false;
                 }
              })
              .catch(() => {
                 emailMsg.textContent = "서버 오류가 발생했습니다.";
                 emailMsg.style.color = "var(--red-msg)";
              });

        } catch (err) {
            emailMsg.textContent = "서버 오류가 발생했습니다.";
            emailMsg.style.color = "var(--red-msg)";
            emailMsg.style.display = "block";
        }
    });

    // check send number

    verifyBtn.addEventListener("click", function() {
       const code = codeInput.value.trim();
       codeMsg.style.display = "none";

       if(!code) {
          codeMsg.textContent = "인증번호를 입력해주세요.";
          codeMsg.style.color = "var(--red-msg)";
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
                 codeMsg.style.display = "block";
                 verifyBtn.disabled = true;
                 isEmailVerified = true;
              } else {
                 codeMsg.textContent = "인증번호가 일치하지 않습니다";
                 codeMsg.style.color = "var(--red-msg)";
                 codeMsg.style.display = "block";
                 isEmailVerified = false;

              }
              codeMsg.style.display = "block";
           })
           .catch(() => {
              codeMsg.textContent = "서버 오류가 발생했습니다.";
              codeMsg.style.color = "var(--red-msg)";
              codeMsg.style.display = "block";

           });
    });

    // 이메일 인증 끝

    const nameInput = document.getElementById("name");
    const birthDateInput = document.getElementById("birthDate");
    const genderInputs = document.querySelectorAll("input[name='gender']");
    const telInput = document.getElementById("tel");

    const nameErrorMsg = document.getElementById("nameErrorMsg");
    const formGroupMsg = document.getElementById("formGroupMsg");
    const telErrorMsg = document.getElementById("telErrorMsg");


    const form = document.getElementById("signUpDto");
    console.log("폼:", form);


    form.addEventListener("submit", function (e) {

        e.preventDefault();

        if (validateForm()) {
            setTimeout(() => form.submit(), 100);
        }

    });



    function validateForm() {
        const loginId = loginIdInput.value.trim();
        const regex = /^[a-z0-9_]+$/;

        if (!loginId || loginId.length < 6 || loginId.length > 16 || !regex.test(loginId)) {
            idMsg.textContent = "아이디를 형식에 맞게 입력해주세요."
            idMsg.style.color = "red";
            idMsg.style.display = "block";
            loginIdInput.focus();
            console.log("나야 내가 인풋으로 보낸거야");
            return false;
        }

        if (!isIdValid) {
            idMsg.textContent = "아이디 중북 여부를 확인해주세요."
            idMsg.style.color = "var(--red-msg)";
            idMsg.style.display = "block";
            chkLoginIdBtn.focus();
            console.log("중복");
            return false;
        }

        if(!isPwValid) {
            pwMsg.textContent="비밀번호를 확인해주세요.";
            pwMsg.style.display = "block";
            pwMsg.style.color = "var(--red-msg)";
            pwInput.focus();
            console.log("비밀번호");
            return false;
        }

        if(!isPwMatch) {
            pwMismatchMsg.textContent = "비밀번호가 일치하지 않습니다.";
            pwMismatchMsg.style.color = "var(--red-msg)";
            pwMismatchMsg.style.display = "block";
            pwChkInput.focus();
            return false;
        }

        if (!isEmailValid) {
           emailMsg.textContent = "이메일 중복을 확인해주세요";
           emailMsg.style.color = "var(--red-msg)";
           emailMsg.style.display = "block";
           emailInput.focus();
           return false;
        }

        if (!isEmailVerified) {
          codeMsg.textContent = "인증번호를 확인해주세요.";
          codeMsg.style.color = "var(--red-msg)";
          codeMsg.style.display = "block";
          codeInput.focus();
          return false;
        }

        const nameValue = nameInput.value.trim();

        if (!nameValue) {
          nameErrorMsg.textContent = "본명을 입력해주세요.";
          nameErrorMsg.style.color = "var(--red-msg)";
          nameErrorMsg.style.display = "block";
          nameInput.focus();
          return false;
        }

        nameErrorMsg.textContent="";
        nameErrorMsg.style.display = "none";

        if (!birthDateInput.value) {
          formGroupMsg.textContent = "생년월일을 입력해주세요.";
          formGroupMsg.style.color = "var(--red-msg)";
          formGroupMsg.style.display = "block";
          birthDateInput.focus();
          return false;
        }

        formGroupMsg.textContent="";
        formGroupMsg.style.display = "none";

        const selectedGender = Array.from(genderInputs).some(input => input.checked);
        if (!selectedGender) {
          formGroupMsg.textContent = "성별을 입력해주세요.";
          formGroupMsg.style.color = "var(--red-msg)";
          formGroupMsg.style.display = "block";
          genderInputs[0].focus();
          return false;
        }

        formGroupMsg.textContent="";
        formGroupMsg.style.display = "none";

        const telValue = telInput.value.trim();

        if (!telValue) {

          telErrorMsg.textContent = "전화번호를 입력하세요.";
          telErrorMsg.style.color = "var(--red-msg)";
          telErrorMsg.style.display = "block";
          telInput.focus();
          return false;
        }
        telErrorMsg.textContent="";
        telErrorMsg.style.display = "none";

        const termsUseAgree = document.querySelector("input[name='termsUse'][value='agree']:checked");
        const termsPrivacyAgree = document.querySelector("input[name='termsPrivacy'][value='agree']:checked");

        if (!termsUseAgree) {
            alert("이용약관에 동의해주세요.");
            document.querySelector("input[name='termsUse'][value='agree']").focus();
            return false;
        }

        if (!termsPrivacyAgree) {
            alert("개인정보처리방침에 동의해주세요.");
            document.querySelector("input[name='termsPrivacy'][value='agree']").focus();
            return false;
        }

        return true;

    }





//document.addEventListener end
});