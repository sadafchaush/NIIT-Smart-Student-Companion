const API_URL = "http://localhost:8080/assignments";

console.log("assignment.js loaded successfully");

document.addEventListener("DOMContentLoaded", function () {
    loadAssignments();
});


async function loadAssignments() {

    const tableBody =
        document.getElementById("assignmentsTableBody");

    if (!tableBody) {
        console.error(
            "assignmentsTableBody element not found"
        );
        return;
    }

    try {

        console.log(
            "Calling API:",
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
                "Failed to load assignments. HTTP Status: "
                + response.status
            );
        }

        const assignments =
            await response.json();

        console.log(
            "Assignments received:",
            assignments
        );

        tableBody.innerHTML = "";

        if (
            !assignments ||
            assignments.length === 0
        ) {

            tableBody.innerHTML = `
                <tr>
                    <td colspan="5">
                        No assignments available.
                    </td>
                </tr>
            `;

            return;
        }


        assignments.forEach(function (assignment) {

            const row =
                document.createElement("tr");

            row.innerHTML = `
                <td>
                    ${assignment.title || "No Title"}
                </td>

                <td>
                    ${assignment.description || "No Description"}
                </td>

                <td>
                    ${assignment.dueDate || "No Deadline"}
                </td>

                <td>
                    ${assignment.subjectId || "N/A"}
                </td>

                <td>
                    ${assignment.facultyId || "N/A"}
                </td>
            `;

            tableBody.appendChild(row);
        });

    }

    catch (error) {

        console.error(
            "Error loading assignments:",
            error
        );

        tableBody.innerHTML = `
            <tr>
                <td colspan="5" class="error">
                    Unable to load assignments.
                    Please make sure Spring Boot
                    is running.
                </td>
            </tr>
        `;
    }
}


// Back to Student Dashboard
function goBack() {

    window.location.href =
        "student-dashboard.html";
}