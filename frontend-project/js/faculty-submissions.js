const API_BASE_URL = window.NIIT_API_BASE_URL;


console.log(
    "faculty-submissions.js loaded successfully"
);


// ======================================================
// PAGE LOAD
// ======================================================

document.addEventListener(
    "DOMContentLoaded",
    function () {

        loadSubmissions();

    }
);


// ======================================================
// LOAD SUBMISSIONS
// ======================================================

async function loadSubmissions() {

    const tableBody =
        document.getElementById(
            "submissionsTableBody"
        );


    try {

        console.log(
            "Loading submissions..."
        );


        // ==================================================
        // 1. GET ALL SUBMISSIONS
        // ==================================================

        const submissionsResponse =
            await fetch(
                `${API_BASE_URL}/submissions`
            );


        console.log(
            "Submissions status:",
            submissionsResponse.status
        );


        if (!submissionsResponse.ok) {

            throw new Error(
                "Unable to load submissions"
            );

        }


        const submissions =
            await submissionsResponse.json();


        console.log(
            "Submissions:",
            submissions
        );


        // ==================================================
        // NO SUBMISSIONS
        // ==================================================

        if (
            !submissions ||
            submissions.length === 0
        ) {

            tableBody.innerHTML = `

                <tr>

                    <td
                        colspan="4"
                        class="no-data">

                        No student submissions found.

                    </td>

                </tr>

            `;

            return;

        }


        // ==================================================
        // 2. GET ALL STUDENTS
        // ==================================================

        const studentsResponse =
            await fetch(
                `${API_BASE_URL}/students`
            );


        console.log(
            "Students status:",
            studentsResponse.status
        );


        if (!studentsResponse.ok) {

            throw new Error(
                "Unable to load students"
            );

        }


        const students =
            await studentsResponse.json();


        console.log(
            "Students:",
            students
        );


        // ==================================================
        // 3. GET ALL ASSIGNMENTS
        // ==================================================

        const assignmentsResponse =
            await fetch(
                `${API_BASE_URL}/assignments`
            );


        console.log(
            "Assignments status:",
            assignmentsResponse.status
        );


        if (!assignmentsResponse.ok) {

            throw new Error(
                "Unable to load assignments"
            );

        }


        const assignments =
            await assignmentsResponse.json();


        console.log(
            "Assignments:",
            assignments
        );


        // ==================================================
        // CLEAR TABLE
        // ==================================================

        tableBody.innerHTML = "";


        // ==================================================
        // 4. DISPLAY EACH SUBMISSION
        // ==================================================

        submissions.forEach(
            function (submission) {


                // ------------------------------------------
                // FIND STUDENT
                // ------------------------------------------

                const student =
                    students.find(
                        function (student) {

                            const studentId =
                                student.studentId ||
                                student.id;

                            return Number(
                                studentId
                            ) === Number(
                                submission.studentId
                            );

                        }
                    );


                // ------------------------------------------
                // FIND ASSIGNMENT
                // ------------------------------------------

                const assignment =
                    assignments.find(
                        function (assignment) {

                            const assignmentId =
                                assignment.id ||
                                assignment.assignmentId;

                            return Number(
                                assignmentId
                            ) === Number(
                                submission.assignmentId
                            );

                        }
                    );


                // ------------------------------------------
                // STUDENT NAME
                // ------------------------------------------

                const studentName =
                    student
                        ? student.name
                        : "Unknown Student";


                // ------------------------------------------
                // ASSIGNMENT TITLE
                // ------------------------------------------

                const assignmentTitle =
                    assignment
                        ? assignment.title
                        : "Unknown Assignment";


                // ------------------------------------------
                // SUBMITTED DATE
                // ------------------------------------------

                let submittedDate =
                    "N/A";


                if (
                    submission.submittedAt
                ) {

                    const date =
                        new Date(
                            submission.submittedAt
                        );


                    if (
                        !isNaN(
                            date.getTime()
                        )
                    ) {

                        submittedDate =
                            date.toLocaleString();

                    }

                }


                // ------------------------------------------
                // FILE URL
                // ------------------------------------------

                const fileUrl =
                    safeHttpUrl(submission.fileUrl);


                // ------------------------------------------
                // CREATE TABLE ROW
                // ------------------------------------------

                const row =
                    document.createElement(
                        "tr"
                    );


                [studentName, assignmentTitle].forEach(value => {
                    const cell = document.createElement("td");
                    cell.textContent = value == null ? "" : String(value);
                    row.appendChild(cell);
                });

                const fileCell = document.createElement("td");
                if (fileUrl) {
                    const link = document.createElement("a");
                    link.href = fileUrl;
                    link.target = "_blank";
                    link.rel = "noopener noreferrer";
                    link.className = "file-button";
                    link.textContent = "View File";
                    fileCell.appendChild(link);
                } else {
                    fileCell.textContent = "No File";
                }
                row.appendChild(fileCell);

                const dateCell = document.createElement("td");
                dateCell.textContent = submittedDate;
                row.appendChild(dateCell);


                tableBody.appendChild(
                    row
                );

            }
        );

    }


    catch (error) {

        console.error(
            "Error loading submissions:",
            error
        );


        const row = document.createElement("tr");
        const cell = document.createElement("td");
        cell.colSpan = 4;
        cell.className = "error";
        cell.textContent =
            `Unable to load student submissions. ${error.message} Please make sure Spring Boot is running.`;
        row.replaceChildren(cell);
        tableBody.replaceChildren(row);

    }

}


// ======================================================
// ESCAPE HTML
// ======================================================

function safeHttpUrl(value) {
    if (!value) {
        return null;
    }

    try {
        const url = new URL(value, window.location.href);
        return url.protocol === "http:" || url.protocol === "https:"
            ? url.href
            : null;
    } catch {
        return null;
    }
}

// ======================================================
// BACK TO FACULTY DASHBOARD
// ======================================================

function goBack() {

    window.location.href =
        "faculty-dashboard.html";

}