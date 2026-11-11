
console.log("student-registration.js loaded successfully");

document.addEventListener("DOMContentLoaded", function () {

    const form = document.getElementById("registrationForm");
    const message = document.getElementById("message");

    // =====================================
    // SHOW / HIDE PASSWORD
    // =====================================

    document.querySelectorAll(".toggle-password").forEach(function (button) {

        button.addEventListener("click", function () {

            const targetId = button.dataset.target;
            const passwordInput = document.getElementById(targetId);
            const icon = button.querySelector("i");

            if (!passwordInput || !icon) {
                return;
            }

            const showPassword = passwordInput.type === "password";

            passwordInput.type = showPassword ? "text" : "password";

            icon.classList.toggle("fa-eye", !showPassword);
            icon.classList.toggle("fa-eye-slash", showPassword);

            const fieldName =
                targetId === "confirmPassword"
                    ? "confirm password"
                    : "password";

            button.setAttribute(
                "aria-label",
                `${showPassword ? "Hide" : "Show"} ${fieldName}`
            );

            button.setAttribute(
                "aria-pressed",
                String(showPassword)
            );

        });

    });

    // =====================================
    // REGISTRATION FORM
    // =====================================

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

        const password =
            document.getElementById("password").value;

        const confirmPassword =
            document.getElementById("confirmPassword").value;

        // =====================================
        // PASSWORD VALIDATION
        // =====================================

        if (Array.from(password).length !== 6) {
            showMessage(
                "Password must be exactly 6 characters.",
                "error"
            );
            return;
        }

        // =====================================
        // CONFIRM PASSWORD VALIDATION
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
            password: password
        };

        try {

            // =====================================
            // CALL SPRING BOOT REGISTRATION API
            // =====================================

            if (
                typeof window.NIIT_API_BASE_URL !== "string" ||
                !window.NIIT_API_BASE_URL.trim()
            ) {
                throw new Error(
                    "API base URL is not configured. Check js/api.js."
                );
            }

            const response = await fetch(
                `${window.NIIT_API_BASE_URL}/students`,
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify(studentData)
                }
            );

            console.log(
                "Registration status:",
                response.status
            );

            // =====================================
            // REGISTRATION REQUEST ACCEPTED
            // =====================================

            if (response.status === 202) {

                showMessage(
                    "If the information can be registered, your request is pending admin review. If you already have an account, sign in.",
                    "success"
                );

                form.reset();

                // Reset the eye icons and hide both passwords.
                document.querySelectorAll(".toggle-password").forEach(
                    function (button) {

                        const input = document.getElementById(
                            button.dataset.target
                        );

                        const icon = button.querySelector("i");

                        if (input) {
                            input.type = "password";
                        }

                        if (icon) {
                            icon.classList.remove("fa-eye-slash");
                            icon.classList.add("fa-eye");
                        }

                        const fieldName =
                            button.dataset.target === "confirmPassword"
                                ? "confirm password"
                                : "password";

                        button.setAttribute(
                            "aria-label",
                            `Show ${fieldName}`
                        );

                        button.setAttribute("aria-pressed", "false");
                    }
                );

                return;
            }

            // =====================================
            // OTHER SERVER RESPONSES
            // =====================================

            const errorText = await response.text();

            console.error(
                "Registration error:",
                errorText
            );

            showMessage(
                errorText || "Registration failed. Please try again.",
                "error"
            );

        } catch (error) {

            console.error(
                "Registration Error:",
                error
            );

            showMessage(
                error.message ===
                    "API base URL is not configured. Check js/api.js."
                    ? error.message
                    : "Unable to connect to Spring Boot. Check that the backend is running and the API URL is correct.",
                "error"
            );

        }

    });

    // =====================================
    // MESSAGE FUNCTION
    // =====================================

    function showMessage(text, type) {

        message.textContent = text;

        message.className = "message " + type;

        message.style.display = "block";
    }

});
