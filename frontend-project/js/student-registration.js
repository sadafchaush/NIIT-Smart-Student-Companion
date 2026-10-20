console.log("student-registration.js loaded successfully");

document.addEventListener("DOMContentLoaded", function () {

    const form =
        document.getElementById("registrationForm");

    const message =
        document.getElementById("message");


    form.addEventListener("submit", async function (event) {

        event.preventDefault();


        console.log("REGISTER BUTTON CLICKED");


        const name =
            document.getElementById("name").value.trim();

        const email =
            document.getElementById("email").value.trim();

        const mobile =
            document.getElementById("mobile").value.trim();

        const course =
            document.getElementById("course").value;

            const year =
    document.getElementById("year").value;

const semester =
    Number(document.getElementById("semester").value);

        const password =
            document.getElementById("password").value;

        const confirmPassword =
            document.getElementById("confirmPassword").value;


        // =====================================
        // PASSWORD VALIDATION
        // =====================================

        if (password !== confirmPassword) {

            showMessage(
                "Passwords do not match.",
                "error"
            );

            return;
        }


        // =====================================
        // CREATE STUDENT OBJECT
        // =====================================

const studentData = {
    name: name,
    email: email,
    mobile: mobile,
    course: course,
    year: year,
    semester: semester,
    password: password
};


        console.log(
            "Registration data:",
            studentData
        );


        try {

            // =====================================
            // CALL SPRING BOOT REGISTRATION API
            // =====================================

            const response = await fetch(
    "http://localhost:8080/students",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(studentData)
                }
            );


            console.log(
                "Registration status:",
                response.status
            );


            // =====================================
            // NEW USER
            // =====================================

            if (response.status === 201) {

                showMessage(
                    "Registration successful! Redirecting to Login...",
                    "success"
                );


                form.reset();


                setTimeout(function () {

                    window.location.href =
                        "login.html";

                }, 1800);


                return;
            }


            // =====================================
            // ALREADY REGISTERED
            // =====================================

            if (response.status === 409) {

                showMessage(
                    "This email is already registered. Please Login.",
                    "error"
                );


                setTimeout(function () {

                    window.location.href =
                        "login.html";

                }, 2000);


                return;
            }


            // =====================================
            // OTHER ERROR
            // =====================================

            const errorText =
                await response.text();

            console.error(
                "Registration error:",
                errorText
            );


            showMessage(
                errorText ||
                "Registration failed. Please try again.",
                "error"
            );


        } catch (error) {

            console.error(
                "Registration Error:",
                error
            );


            showMessage(
                "Unable to connect to Spring Boot.",
                "error"
            );

        }

    });


    // =====================================
    // MESSAGE FUNCTION
    // =====================================

    function showMessage(text, type) {

        message.textContent = text;

        message.className =
            "message " + type;

        message.style.display = "block";
    }

});