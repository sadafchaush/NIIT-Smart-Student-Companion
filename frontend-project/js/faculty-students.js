
const API_URL = "http://localhost:8080/students";

console.log("faculty-students.js loaded successfully");


// =====================================
// LOAD STUDENTS WHEN PAGE OPENS
// =====================================

document.addEventListener("DOMContentLoaded", function () {

    loadStudents();

});


// =====================================
// LOAD ALL STUDENTS
// =====================================

async function loadStudents() {

    const message =
        document.getElementById("message");

    const table =
        document.getElementById("studentsTable");

    const tbody =
        document.getElementById("studentsBody");


    try {

        console.log("Calling API:", API_URL);


        const response =
            await fetch(API_URL);


        console.log(
            "Response status:",
            response.status
        );


        if (!response.ok) {

            throw new Error(
                "Failed to load students. HTTP Status: "
                + response.status
            );

        }


        const students =
            await response.json();


        console.log(
            "Students received:",
            students
        );


        // Clear old rows

        tbody.innerHTML = "";


        // Check if students exist

        if (
            !students ||
            students.length === 0
        ) {

            message.textContent =
                "No students found.";

            table.style.display = "none";

            return;

        }


        // Add students to table

        students.forEach(function (student) {

            const row =
                document.createElement("tr");


            row.innerHTML = `

                <td>
                    ${student.id ?? "N/A"}
                </td>

                <td>
                    ${student.name ?? "N/A"}
                </td>

                <td>
                    ${student.email ?? "N/A"}
                </td>

                <td>
                    ${student.mobile ?? "N/A"}
                </td>

                <td>
                    ${student.course ?? "N/A"}
                </td>

            `;


            tbody.appendChild(row);

        });


        // Hide loading message

        message.style.display = "none";


        // Show table

        table.style.display = "table";


    } catch (error) {

        console.error(
            "Error loading students:",
            error
        );


        message.className =
            "message error";


        message.textContent =
            "Unable to load students. " +
            "Please make sure Spring Boot is running.";


        table.style.display = "none";

    }

}


// =====================================
// BACK TO FACULTY DASHBOARD
// =====================================

function goBack() {

    window.location.href =
        "faculty-dashboard.html";

}