
console.log("login.js loaded successfully");

document.addEventListener("DOMContentLoaded", function () {

    const loginForm = document.getElementById("loginForm");

    if (!loginForm) {
        console.error("loginForm not found!");
        return;
    }

    // Read selected role from landing-page URL
    const params = new URLSearchParams(window.location.search);

    let role = (params.get("role") || "STUDENT")
        .trim()
        .toUpperCase();

    const allowedRoles = ["STUDENT", "FACULTY", "ADMIN"];

    if (!allowedRoles.includes(role)) {
        role = "STUDENT";
    }

    // Display selected portal
    const selectedRole = document.getElementById("selectedRole");

    if (selectedRole) {
        selectedRole.textContent = "Login as " + role;
    }

    loginForm.addEventListener("submit", async function (event) {

        event.preventDefault();

        console.log("LOGIN BUTTON CLICKED");
        console.log("Selected role:", role);

        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;

        if (!email || !password) {
            alert("Please enter email and password.");
            return;
        }

        try {

            // Get CSRF token first
            const csrfResponse = await fetch(
                `${window.NIIT_API_BASE_URL}/auth/csrf`,
                {
                    method: "GET",
                    credentials: "include"
                }
            );

            if (!csrfResponse.ok) {
                throw new Error("Unable to obtain CSRF token.");
            }

            const csrfData = await csrfResponse.json();

            const csrfToken = csrfData.token;

            if (!csrfToken) {
                throw new Error("CSRF token was not received.");
            }

            // Send login request with selected role
            const response = await fetch(
                `${window.NIIT_API_BASE_URL}/auth/login`,
                {
                    method: "POST",
                    credentials: "include",

                    headers: {
                        "Content-Type": "application/json",
                        "X-CSRF-TOKEN": csrfToken
                    },

                    body: JSON.stringify({
                        email: email,
                        password: password,
                        role: role
                    })
                }
            );

            console.log("Login response:", response.status);

            const data = await response.json();

            if (!response.ok) {
                alert(
                    data.message ||
                    "Login failed. Check your email, password and selected portal."
                );
                return;
            }

            if (!data.role) {
                console.error("Role missing:", data);
                alert("Login successful, but role was not received.");
                return;
            }

            const actualRole = String(data.role)
                .trim()
                .toUpperCase();

            if (actualRole !== role) {
                alert("The account role does not match the selected portal.");
                return;
            }

            // Save session
            localStorage.setItem("loggedInUser", JSON.stringify(data));
            localStorage.setItem("userRole", actualRole);

            alert("Login successful!");

            // Redirect according to actual role
            switch (actualRole) {

                case "ADMIN":
                    window.location.replace("admin-dashboard.html");
                    break;

                case "FACULTY":
                    window.location.replace("faculty-dashboard.html");
                    break;

                case "STUDENT":
                    window.location.replace("student-dashboard.html");
                    break;

                default:
                    localStorage.removeItem("loggedInUser");
                    localStorage.removeItem("userRole");
                    alert("Unknown user role.");
            }

        } catch (error) {

            console.error("LOGIN ERROR:", error);

            alert(
                error.message ||
                "Unable to connect to backend. Check whether Spring Boot is running."
            );
        }
    });
});
