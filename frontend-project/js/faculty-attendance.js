
const API_BASE_URL = "http://localhost:8080";

console.log("faculty-attendance.js loaded successfully");


let editingAttendanceId = null;


// =========================================
// PAGE LOAD
// =========================================

document.addEventListener("DOMContentLoaded", function () {

    setToday();

    loadStudents();

    loadSubjects();

    loadAttendance();

});


// =========================================
// SET TODAY'S DATE
// =========================================

function setToday() {

    const dateInput =
        document.getElementById("attendanceDate");

    const today =
        new Date().toISOString().split("T")[0];

    dateInput.value = today;

}


// =========================================
// LOAD STUDENTS
// =========================================

async function loadStudents() {

    const studentSelect =
        document.getElementById("student");

    try {

        const response =
            await fetch(`${API_BASE_URL}/students`);

        if (!response.ok) {
            throw new Error("Unable to load students");
        }

        const students =
            await response.json();

        studentSelect.innerHTML = `
            <option value="">
                Select Student
            </option>
        `;

        students.forEach(function (student) {

            const option =
                document.createElement("option");

            option.value =
                student.studentId;

            option.textContent =
                `${student.name} - ${student.email}`;

            studentSelect.appendChild(option);

        });

    } catch (error) {

        console.error(
            "Student loading error:",
            error
        );

        studentSelect.innerHTML = `
            <option value="">
                Unable to load students
            </option>
        `;
    }
}


// =========================================
// LOAD SUBJECTS
// =========================================

async function loadSubjects() {

    const subjectSelect =
        document.getElementById("subject");

    try {

        const response =
            await fetch(`${API_BASE_URL}/subjects`);

        if (!response.ok) {
            throw new Error("Unable to load subjects");
        }

        const subjects =
            await response.json();

        subjectSelect.innerHTML = `
            <option value="">
                Select Subject
            </option>
        `;

        subjects.forEach(function (subject) {

            const option =
                document.createElement("option");

            option.value =
                subject.subjectId;

            option.textContent =
                `${subject.subjectName} (${subject.subjectCode})`;

            subjectSelect.appendChild(option);

        });

    } catch (error) {

        console.error(
            "Subject loading error:",
            error
        );

        subjectSelect.innerHTML = `
            <option value="">
                Unable to load subjects
            </option>
        `;
    }
}


// =========================================
// SUBMIT FORM
// =========================================

document
    .getElementById("attendanceForm")
    .addEventListener(
        "submit",
        async function (event) {

            event.preventDefault();

            const studentId =
                document.getElementById("student").value;

            const subjectId =
                document.getElementById("subject").value;

            const attendanceDate =
                document.getElementById("attendanceDate").value;

            const status =
                document.getElementById("status").value;

            const message =
                document.getElementById("message");


            if (
                !studentId ||
                !subjectId ||
                !attendanceDate ||
                !status
            ) {

                message.className =
                    "message error";

                message.textContent =
                    "Please fill all fields.";

                return;
            }


            const attendanceData = {

                student: {
                    studentId: Number(studentId)
                },

                subject: {
                    subjectId: Number(subjectId)
                },

                attendanceDate: attendanceDate,

                status: status

            };


            try {

                let response;


                // =================================
                // UPDATE
                // =================================

                if (editingAttendanceId !== null) {

                    response =
                        await fetch(
                            `${API_BASE_URL}/attendance/${editingAttendanceId}`,
                            {
                                method: "PUT",

                                headers: {
                                    "Content-Type":
                                        "application/json"
                                },

                                body:
                                    JSON.stringify(
                                        attendanceData
                                    )
                            }
                        );

                }


                // =================================
                // CREATE
                // =================================

                else {

                    response =
                        await fetch(
                            `${API_BASE_URL}/attendance`,
                            {
                                method: "POST",

                                headers: {
                                    "Content-Type":
                                        "application/json"
                                },

                                body:
                                    JSON.stringify(
                                        attendanceData
                                    )
                            }
                        );

                }


                if (!response.ok) {

                    const errorText =
                        await response.text();

                    throw new Error(
                        errorText ||
                        "Operation failed"
                    );
                }


                if (editingAttendanceId !== null) {

                    message.className =
                        "message success";

                    message.textContent =
                        "Attendance updated successfully!";

                } else {

                    message.className =
                        "message success";

                    message.textContent =
                        "Attendance marked successfully!";

                }


                resetForm();

                loadAttendance();


            } catch (error) {

                console.error(
                    "Attendance error:",
                    error
                );

                message.className =
                    "message error";

                message.textContent =
                    "Error: " + error.message;

            }

        }
    );


// =========================================
// LOAD ALL ATTENDANCE
// =========================================

async function loadAttendance() {

    const table =
        document.getElementById("attendanceTable");

    const tbody =
        document.getElementById("attendanceBody");

    const loading =
        document.getElementById("loadingMessage");


    try {

        const response =
            await fetch(
                `${API_BASE_URL}/attendance`
            );


        if (!response.ok) {

            throw new Error(
                "Unable to load attendance"
            );

        }


        const attendanceList =
            await response.json();


        tbody.innerHTML = "";


        if (
            !attendanceList ||
            attendanceList.length === 0
        ) {

            loading.textContent =
                "No attendance records found.";

            table.style.display =
                "none";

            return;

        }


        attendanceList.forEach(
            function (attendance) {

                const row =
                    document.createElement("tr");


                const studentName =
                    attendance.student
                        ? attendance.student.name
                        : "N/A";


                const subjectName =
                    attendance.subject
                        ? attendance.subject.subjectName
                        : "N/A";


                row.innerHTML = `

                    <td>
                        ${attendance.attendanceId ?? "N/A"}
                    </td>

                    <td>
                        ${studentName}
                    </td>

                    <td>
                        ${subjectName}
                    </td>

                    <td>
                        ${attendance.attendanceDate ?? "N/A"}
                    </td>

                    <td>
                        ${attendance.status ?? "N/A"}
                    </td>

                    <td>

                        <button
                            class="edit-button"
                            onclick="editAttendance(
                                ${attendance.attendanceId}
                            )">

                            Edit

                        </button>


                        <button
                            class="delete-button"
                            onclick="deleteAttendance(
                                ${attendance.attendanceId}
                            )">

                            Delete

                        </button>

                    </td>

                `;


                tbody.appendChild(row);

            }
        );


        loading.style.display =
            "none";

        table.style.display =
            "table";


    } catch (error) {

        console.error(
            "Load attendance error:",
            error
        );

        loading.textContent =
            "Unable to load attendance.";

    }
}


// =========================================
// EDIT ATTENDANCE
// =========================================

async function editAttendance(id) {

    try {

        const response =
            await fetch(
                `${API_BASE_URL}/attendance/${id}`
            );


        if (!response.ok) {

            throw new Error(
                "Unable to get attendance record"
            );

        }


        const attendance =
            await response.json();


        console.log(
            "Editing attendance:",
            attendance
        );


        editingAttendanceId =
            attendance.attendanceId;


        // Fill form

        document.getElementById("student").value =
            attendance.student.studentId;


        document.getElementById("subject").value =
            attendance.subject.subjectId;


        document.getElementById("attendanceDate").value =
            attendance.attendanceDate;


        document.getElementById("status").value =
            attendance.status;


        // Change title

        document.getElementById("formTitle").textContent =
            "Update Attendance";


        // Change button

        document.getElementById("submitButton").textContent =
            "Update Attendance";


        document.getElementById("submitButton").className =
            "update-button";


        // Show cancel

        document.getElementById("cancelButton").style.display =
            "inline-block";


        // Scroll to form

        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });


    } catch (error) {

        console.error(
            "Edit attendance error:",
            error
        );

        alert(
            "Unable to edit attendance."
        );

    }

}


// =========================================
// DELETE ATTENDANCE
// =========================================

async function deleteAttendance(id) {

    const confirmed =
        confirm(
            "Are you sure you want to delete this attendance record?"
        );


    if (!confirmed) {
        return;
    }


    try {

        const response =
            await fetch(
                `${API_BASE_URL}/attendance/${id}`,
                {
                    method: "DELETE"
                }
            );


        if (!response.ok) {

            const errorText =
                await response.text();

            throw new Error(
                errorText ||
                "Failed to delete attendance"
            );

        }


        alert(
            "Attendance deleted successfully!"
        );


        loadAttendance();


    } catch (error) {

        console.error(
            "Delete attendance error:",
            error
        );


        alert(
            "Unable to delete attendance: "
            + error.message
        );

    }

}


// =========================================
// CANCEL EDIT
// =========================================

function cancelEdit() {

    resetForm();

}


// =========================================
// RESET FORM
// =========================================

function resetForm() {

    editingAttendanceId = null;


    document.getElementById(
        "attendanceForm"
    ).reset();


    setToday();


    document.getElementById(
        "formTitle"
    ).textContent =
        "Mark Attendance";


    document.getElementById(
        "submitButton"
    ).textContent =
        "Mark Attendance";


    document.getElementById(
        "submitButton"
    ).className =
        "save-button";


    document.getElementById(
        "cancelButton"
    ).style.display =
        "none";


    document.getElementById(
        "message"
    ).textContent = "";

}


// =========================================
// BACK TO FACULTY DASHBOARD
// =========================================

function goBack() {

    window.location.href =
        "faculty-dashboard.html";

}
