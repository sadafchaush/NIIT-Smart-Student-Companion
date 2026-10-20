const API_BASE_URL = "http://localhost:8080";

console.log("attendance.js loaded successfully");

document.addEventListener("DOMContentLoaded", function () {

    console.log("Attendance page loaded");

    loadAttendance();

});

async function loadAttendance() {

    const tableBody =
        document.getElementById("attendanceTableBody");

    try {

        // Check logged-in student
        const userData =
            localStorage.getItem("loggedInUser");

        if (!userData) {
            window.location.href = "login.html";
            return;
        }

        const user = JSON.parse(userData);

        console.log("Logged-in user:", user);

        const email = user.email;

        if (!email) {
            throw new Error("Student email not found.");
        }

        // Get student details
        const studentResponse = await fetch(
            `${API_BASE_URL}/students/email/${encodeURIComponent(email)}`
        );

        console.log(
            "Student API status:",
            studentResponse.status
        );

        if (!studentResponse.ok) {
            throw new Error("Unable to find student.");
        }

        const student =
            await studentResponse.json();

        console.log("Student details:", student);

        // Get student ID
        const studentId =
            student.studentId || student.id;

        if (!studentId) {
            throw new Error("Student ID not found.");
        }

        console.log(
            "Student ID:",
            studentId
        );

        // Attendance API
        const attendanceUrl =
            `${API_BASE_URL}/attendance/student/${studentId}`;

        console.log(
            "Calling Attendance API:",
            attendanceUrl
        );

        const response =
            await fetch(attendanceUrl);

        console.log(
            "Attendance API status:",
            response.status
        );

        if (!response.ok) {
            throw new Error(
                "Unable to load attendance. Status: " +
                response.status
            );
        }

        const attendanceList =
            await response.json();

        console.log(
            "Attendance data:",
            attendanceList
        );

        tableBody.innerHTML = "";

        // No records
        if (
            !Array.isArray(attendanceList) ||
            attendanceList.length === 0
        ) {

            tableBody.innerHTML = `
                <tr>
                    <td colspan="2">
                        No attendance records available.
                    </td>
                </tr>
            `;

            return;
        }

        // Group attendance by subject
        const subjectData = {};

        attendanceList.forEach(function (attendance) {

            // IMPORTANT:
            // Subject comes from attendance.subject
            const subject =
                attendance.subject;

            const subjectName =
                subject &&
                subject.subjectName
                    ? subject.subjectName
                    : "Unknown Subject";

            if (!subjectData[subjectName]) {

                subjectData[subjectName] = {
                    total: 0,
                    present: 0
                };

            }

            // Count total classes
            subjectData[subjectName].total++;

            // Count present classes
            if (
                attendance.status &&
                attendance.status.toUpperCase() === "PRESENT"
            ) {

                subjectData[subjectName].present++;

            }

        });

        // Display results
        Object.keys(subjectData).forEach(
            function (subjectName) {

                const data =
                    subjectData[subjectName];

                const percentage =
                    data.total > 0
                        ? (data.present / data.total) * 100
                        : 0;

                const row =
                    document.createElement("tr");

                row.innerHTML = `
                    <td>
                        ${subjectName}
                    </td>

                    <td>
                        ${percentage.toFixed(0)}%
                    </td>
                `;

                tableBody.appendChild(row);

            }
        );

    } catch (error) {

        console.error(
            "Attendance Error:",
            error
        );

        tableBody.innerHTML = `
            <tr>
                <td colspan="2" class="error">
                    Unable to load attendance.
                    <br><br>
                    ${error.message}
                </td>
            </tr>
        `;
    }
}


function goBack() {

    window.location.href =
        "student-dashboard.html";

}