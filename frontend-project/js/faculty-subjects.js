
console.log("faculty-subjects.js loaded successfully");


const API_URL = `${window.NIIT_API_BASE_URL}/subjects`;


document.addEventListener("DOMContentLoaded", function () {

    console.log("Faculty Subjects page loaded");

    loadSubjects();

});


/* =========================================
   LOAD SUBJECTS FOR LOGGED-IN FACULTY
========================================= */

async function loadSubjects() {

    const message =
        document.getElementById("message");

    const table =
        document.getElementById("subjectsTable");

    const tbody =
        document.getElementById("subjectsBody");


    try {

        /* =====================================
           GET LOGGED-IN USER
        ===================================== */

        const loggedInUser =
            localStorage.getItem("loggedInUser");


        console.log(
            "Logged-in user:",
            loggedInUser
        );


        if (!loggedInUser) {

            message.textContent =
                "Faculty login information not found. Please login again.";

            message.className =
                "message error";

            table.style.display =
                "none";

            return;
        }


        let user;


        try {

            user =
                JSON.parse(loggedInUser);

        } catch (error) {

            console.error(
                "Invalid loggedInUser data:",
                error
            );

            message.textContent =
                "Invalid faculty login information.";

            message.className =
                "message error";

            table.style.display =
                "none";

            return;
        }


        console.log(
            "Parsed logged-in user:",
            user
        );


        /* =====================================
           GET FACULTY EMAIL
        ===================================== */

        const facultyEmail =
            user.email;


        console.log(
            "Logged-in Faculty Email:",
            facultyEmail
        );


        if (!facultyEmail) {

            message.textContent =
                "Faculty email not found. Please login again.";

            message.className =
                "message error";

            table.style.display =
                "none";

            return;
        }


        /* =====================================
           CALL SUBJECT API
        ===================================== */

        console.log(
            "Calling Subjects API:",
            API_URL
        );


        const response =
            await fetch(API_URL);


        console.log(
            "Response status:",
            response.status
        );


        if (!response.ok) {

            throw new Error(
                "Failed to load subjects"
            );

        }


        const subjects =
            await response.json();


        console.log(
            "All subjects received:",
            subjects
        );


        /* =====================================
           FILTER BY FACULTY EMAIL
        ===================================== */

        const facultySubjects =
            subjects.filter(function (subject) {

                const subjectFaculty =
                    subject.faculty;


                if (!subjectFaculty) {

                    return false;

                }


                const subjectFacultyEmail =
                    subjectFaculty.email;


                return subjectFacultyEmail &&
                       subjectFacultyEmail.toLowerCase() ===
                       facultyEmail.toLowerCase();

            });


        console.log(
            "Subjects assigned to Faculty:",
            facultySubjects
        );


        /* =====================================
           NO SUBJECTS
        ===================================== */

        if (facultySubjects.length === 0) {

            message.textContent =
                "No subjects are assigned to you.";

            message.className =
                "message no-data";

            table.style.display =
                "none";

            return;
        }


        /* =====================================
           CLEAR OLD DATA
        ===================================== */

        tbody.replaceChildren();


        /* =====================================
           DISPLAY SUBJECTS
        ===================================== */

        facultySubjects.forEach(function (subject) {

            const row =
                document.createElement("tr");


            /* Subject ID */

            const id =
                subject.subjectId ??
                subject.id ??
                "";


            /* Subject Name */

            const name =
                subject.subjectName ??
                subject.name ??
                "";


            /* Subject Code */

            const code =
                subject.subjectCode ??
                subject.code ??
                "";


            /* Course */

            const course =
                subject.course ??
                subject.courseName ??
                "B.Sc. Computer Science";


            /* Semester */

            const semester =
                subject.semester ??
                "";


            [id, name, code, course, semester].forEach(value => {
                const cell = document.createElement("td");
                cell.textContent = value == null ? "" : String(value);
                row.appendChild(cell);
            });

            const actionCell = document.createElement("td");
            const viewButton = document.createElement("button");
            viewButton.type = "button";
            viewButton.className = "view-button";
            viewButton.textContent = "👁 View Details";
            viewButton.addEventListener("click", () => viewSubject(id));
            actionCell.appendChild(viewButton);
            row.appendChild(actionCell);


            tbody.appendChild(row);

        });


        /* =====================================
           SHOW TABLE
        ===================================== */

        message.style.display =
            "none";

        table.style.display =
            "table";


    }

    catch (error) {

        console.error(
            "Error loading subjects:",
            error
        );


        message.textContent =
            "Unable to load subjects. Please check whether Spring Boot is running.";

        message.className =
            "message error";


        table.style.display =
            "none";

    }

}


/* =========================================
   VIEW SUBJECT
========================================= */

function viewSubject(id) {

    console.log(
        "Selected Subject ID:",
        id
    );


    alert(
        "Subject ID: " + id
    );

}


/* =========================================
   BACK TO DASHBOARD
========================================= */

function goBack() {

    window.location.href =
        "faculty-dashboard.html";

}
