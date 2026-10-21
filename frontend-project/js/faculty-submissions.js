const API_BASE_URL =
    "http://localhost:8080";


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


                row.innerHTML = `

                    <td>
                        ${escapeHTML(
                            studentName
                        )}
                    </td>


                    <td>
                        ${escapeHTML(
                            assignmentTitle
                        )}
                    </td>


                    <td>

                        ${
                            fileUrl
                                ? `
                                    <a
                                        href="${escapeAttribute(
                                            fileUrl
                                        )}"
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        class="file-button">

                                        View File

                                    </a>
                                  `
                                : "No File"
                        }

                    </td>


                    <td>
                        ${escapeHTML(
                            submittedDate
                        )}
                    </td>

                `;


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


        tableBody.innerHTML = `

            <tr>

                <td
                    colspan="4"
                    class="error">

                    Unable to load student submissions.

                    <br><br>

                    ${escapeHTML(
                        error.message
                    )}

                    <br><br>

                    Please make sure Spring Boot
                    is running.

                </td>

            </tr>

        `;

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

function escapeHTML(value) {

    if (
        value === null ||
        value === undefined
    ) {

        return "";

    }


    return String(value)
        .replace(
            /&/g,
            "&amp;"
        )
        .replace(
            /</g,
            "&lt;"
        )
        .replace(
            />/g,
            "&gt;"
        )
        .replace(
            /"/g,
            "&quot;"
        )
        .replace(
            /'/g,
            "&#039;"
        );

}


// ======================================================
// ESCAPE URL ATTRIBUTE
// ======================================================

function escapeAttribute(value) {

    if (
        value === null ||
        value === undefined
    ) {

        return "";

    }


    return String(value)
        .replace(
            /"/g,
            "&quot;"
        )
        .replace(
            /'/g,
            "&#039;"
        );

}


// ======================================================
// BACK TO FACULTY DASHBOARD
// ======================================================

function goBack() {

    window.location.href =
        "faculty-dashboard.html";

}