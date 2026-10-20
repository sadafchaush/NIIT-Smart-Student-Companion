
console.log("faculty-profile.js loaded successfully");

document.addEventListener("DOMContentLoaded", function () {


    // ==============================
    // BACK TO DASHBOARD
    // ==============================

    window.goBack = function () {

        window.location.href =
            "faculty-dashboard.html";

    };


    // ==============================
    // GET LOGGED-IN FACULTY
    // ==============================

    const userData =
        localStorage.getItem("loggedInUser");


    if (!userData) {

        window.location.href =
            "login.html";

        return;
    }


    try {

        const faculty =
            JSON.parse(userData);


        console.log(
            "Logged-in faculty:",
            faculty
        );


        // ==============================
        // FACULTY ID
        // ==============================

        document.getElementById("facultyId")
            .textContent =
            faculty.facultyId ||
            faculty.id ||
            "-";


        // ==============================
        // NAME
        // ==============================

        document.getElementById("facultyName")
            .textContent =
            faculty.fullName ||
            faculty.name ||
            "Faculty";


        // ==============================
        // EMAIL
        // ==============================

        document.getElementById("facultyEmail")
            .textContent =
            faculty.email ||
            "-";


        // ==============================
        // PHONE
        // ==============================

        document.getElementById("facultyPhone")
            .textContent =
            faculty.phone ||
            faculty.mobile ||
            "-";


        // ==============================
        // DEPARTMENT
        // ==============================

        document.getElementById("facultyDepartment")
            .textContent =
            faculty.department ||
            "-";


        // ==============================
        // DESIGNATION
        // ==============================

        document.getElementById("facultyDesignation")
            .textContent =
            faculty.designation ||
            "-";


    } catch (error) {

        console.error(
            "Error reading faculty data:",
            error
        );

    }

});