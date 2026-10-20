// ======================================================
// LOGIN.JS
// ======================================================

console.log("login.js loaded successfully");

document.addEventListener("DOMContentLoaded", function () {

    const loginForm = document.getElementById("loginForm");

    if (!loginForm) {
        console.error("loginForm not found!");
        return;
    }

    loginForm.addEventListener("submit", async function (event) {

        event.preventDefault();

        console.log("LOGIN BUTTON CLICKED");

        const email = document
            .getElementById("email")
            .value
            .trim();

        const password = document
            .getElementById("password")
            .value
            .trim();

        if (!email || !password) {
            alert("Please enter email and password.");
            return;
        }

        try {

            const response = await fetch(
                `http://${window.location.hostname}:8080/auth/login`,
                {
                    method: "POST",
                    credentials: "include",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify({
                        email: email,
                        password: password
                    })
                }
            );

            console.log("Login response:", response.status);

            let data;

            try {
                data = await response.json();
            } catch (error) {
                console.error("Invalid JSON response.");
                alert("Invalid response from server.");
                return;
            }

            console.log("Login data:", data);

            if (!response.ok) {

                alert(
                    data.message ||
                    "Login failed. Please check your email and password."
                );

                return;
            }

            // ------------------------------------------------
            // CHECK ROLE
            // ------------------------------------------------

            if (!data.role) {

                console.error("Role missing from login response:", data);

                alert(
                    "Login successful, but user role was not received."
                );

                return;
            }

            const role = String(data.role)
                .trim()
                .toUpperCase();

            console.log("USER ROLE:", role);

            // ------------------------------------------------
            // SAVE SESSION
            // ------------------------------------------------

            localStorage.setItem(
                "loggedInUser",
                JSON.stringify(data)
            );

            localStorage.setItem(
                "userRole",
                role
            );

            console.log(
                "Session saved:",
                localStorage.getItem("loggedInUser")
            );

            alert("Login successful!");

            // ------------------------------------------------
            // ROLE BASED NAVIGATION
            // ------------------------------------------------

            switch (role) {

                case "ADMIN":

                    console.log("Redirecting to Admin Dashboard");

                    window.location.replace(
                        "admin-dashboard.html"
                    );

                    break;

                case "FACULTY":

                    console.log("Redirecting to Faculty Dashboard");

                    window.location.replace(
                        "faculty-dashboard.html"
                    );

                    break;

                case "STUDENT":

                    console.log("Redirecting to Student Dashboard");

                    window.location.replace(
                        "student-dashboard.html"
                    );

                    break;

                default:

                    console.error(
                        "Unknown role:",
                        role
                    );

                    localStorage.removeItem("loggedInUser");
                    localStorage.removeItem("userRole");

                    alert(
                        "Unknown user role: " +
                        data.role
                    );

                    break;
            }

        } catch (error) {

            console.error("LOGIN ERROR:", error);

            alert(
                "Unable to connect to Spring Boot.\n\n" +
                "Make sure the backend is running on port 8080."
            );
        }
    });
});