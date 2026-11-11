const API_URL = `${window.NIIT_API_BASE_URL}/assignments`;

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

        tableBody.replaceChildren();

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

            [
                assignment.title || "No Title",
                assignment.description || "No Description",
                assignment.dueDate || "No Deadline",
                assignment.subjectId || "N/A",
                assignment.facultyId || "N/A"
            ].forEach(value => {
                const cell = document.createElement("td");
                cell.textContent = String(value);
                row.appendChild(cell);
            });

            tableBody.appendChild(row);
        });

    }

    catch (error) {

        console.error(
            "Error loading assignments:",
            error
        );

        const row = document.createElement("tr");
        const cell = document.createElement("td");
        cell.colSpan = 5;
        cell.className = "error";
        cell.textContent =
            "Unable to load assignments. Please make sure Spring Boot is running.";
        row.appendChild(cell);
        tableBody.replaceChildren(row);
    }
}


// Back to Student Dashboard
function goBack() {

    window.location.href =
        "student-dashboard.html";
}