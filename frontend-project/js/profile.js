const API_BASE_URL = "http://localhost:8080";

console.log("profile.js loaded successfully");

document.addEventListener("DOMContentLoaded", function () {
    loadProfile();
});

async function loadProfile() {

    try {

        const userData =
            localStorage.getItem("loggedInUser");

        console.log("loggedInUser:", userData);

        if (!userData) {
            throw new Error("No logged-in user found");
        }

        const user = JSON.parse(userData);

        console.log("Logged-in user:", user);

        const email = user.email;

        if (!email) {
            throw new Error("Email not found");
        }

        const response = await fetch(
            `${API_BASE_URL}/students/email/${encodeURIComponent(email)}`
        );

        console.log(
            "Student API status:",
            response.status
        );

        if (!response.ok) {
            throw new Error(
                "Student not found: " + response.status
            );
        }

        const student = await response.json();

        console.log(
            "Student details:",
            student
        );

        document.getElementById("studentName").textContent =
            student.name || "Not available";

        document.getElementById("studentEmail").textContent =
            student.email || "Not available";

        document.getElementById("studentMobile").textContent =
            student.mobile || "Not available";

        document.getElementById("studentCourse").textContent =
            student.course || "Not available";

    } catch (error) {

        console.error(
            "Profile Error:",
            error
        );

        document.getElementById("studentName").textContent =
            "Unable to load";

        document.getElementById("studentEmail").textContent =
            "Unable to load";

        document.getElementById("studentMobile").textContent =
            "Unable to load";

        document.getElementById("studentCourse").textContent =
            "Unable to load";
    }
}


function goBack() {

    window.location.href =
        "student-dashboard.html";
}