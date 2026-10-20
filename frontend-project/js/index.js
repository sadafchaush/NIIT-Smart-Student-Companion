// =========================================
// NIIT SMART STUDENT COMPANION
// SPLASH PAGE JAVASCRIPT
// =========================================


document.addEventListener("DOMContentLoaded", function () {

    console.log("NIIT Smart Student Companion splash page loaded successfully");


    const loginButton =
        document.getElementById("loginButton");


    if (!loginButton) {

        console.error(
            "Login button not found!"
        );

        return;
    }


    loginButton.addEventListener(
        "click",
        function () {

            console.log(
                "Opening login page..."
            );


            window.location.href =
                "login.html";

        }
    );

});