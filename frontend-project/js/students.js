// ======================================================
// ADMIN STUDENT MANAGEMENT - students.js
// ======================================================

const API_BASE_URL = `${window.NIIT_API_BASE_URL}/students`;

console.log("students.js loaded successfully");


// ======================================================
// ADMIN ACCESS CHECK
// ======================================================

function checkAdminAccess() {

    const userData = localStorage.getItem("loggedInUser");

    if (!userData) {

        console.log("No logged-in user.");

        window.location.href = "login.html";

        return false;
    }

    try {

        const user = JSON.parse(userData);

        console.log("Admin user:", user);

        const role = String(user.role || "").toUpperCase();

        console.log("User role:", role);

        if (role !== "ADMIN") {

            alert("Access denied. Admin login required.");

            if (role === "STUDENT") {

                window.location.href = "student-dashboard.html";

            } else if (role === "FACULTY") {

                window.location.href = "faculty-dashboard.html";

            } else {

                window.location.href = "login.html";

            }

            return false;
        }

        return true;

    } catch (error) {

        console.error("Login data error:", error);

        localStorage.removeItem("loggedInUser");

        window.location.href = "login.html";

        return false;
    }
}


// ======================================================
// PAGE LOAD
// ======================================================

document.addEventListener("DOMContentLoaded", function () {

    console.log("Manage Students page loaded");

    if (!checkAdminAccess()) {
        return;
    }

    getStudents();
    getPendingStudents();


    const studentForm =
        document.getElementById("studentForm");

    if (studentForm) {

        studentForm.addEventListener(
            "submit",
            function (event) {

                event.preventDefault();

                saveStudent();

            }
        );
    }

});


// ======================================================
// SAVE STUDENT
// ======================================================

async function saveStudent() {

    if (!checkAdminAccess()) {
        return;
    }


    const studentIdElement =
        document.getElementById("studentId");

    const nameElement =
        document.getElementById("name");

    const emailElement =
        document.getElementById("email");

    const mobileElement =
        document.getElementById("mobile");

    const courseElement =
        document.getElementById("course");

    const passwordElement =
        document.getElementById("password");


    const studentId =
        studentIdElement ?
        studentIdElement.value.trim() :
        "";

    const name =
        nameElement ?
        nameElement.value.trim() :
        "";

    const email =
        emailElement ?
        emailElement.value.trim() :
        "";

    const mobile =
        mobileElement ?
        mobileElement.value.trim() :
        "";

    const course =
        courseElement ?
        courseElement.value.trim() :
        "";

    const password =
        passwordElement ?
        passwordElement.value :
        "";

    const passwordLength = Array.from(password).length;
    if ((!studentId && passwordLength !== 6)
            || (studentId && password && passwordLength !== 6)) {
        alert("Password must be exactly 6 characters.");
        return;
    }

    if (!name || !email || !mobile || !course) {

        alert("Please fill all required fields.");

        return;
    }


    const studentData = {

        name: name,

        email: email,

        mobile: mobile,

        course: course
    };


    // Only send password when it has a value.
    if (password) {
        studentData.password = password;
    }



    try {

        let response;

        // ==================================================
        // UPDATE STUDENT
        // ==================================================


        if (studentId) {

            console.log(
                "Updating student:",
                studentId
            );

            response = await fetch(
                `${API_BASE_URL}/${studentId}`,
                {
                    method: "PUT",

                    credentials: "include",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(studentData)
                }
            );

        }


        }


        // ==================================================
        // ADD STUDENT
        // ==================================================

        else {

            console.log(
                "Adding new student..."
            );


            response = await fetch(
                API_BASE_URL,
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(studentData)
                }
            );

        }


        console.log(
            "Response status:",
            response.status
        );


        if (!response.ok) {

            const errorText =
                await response.text();

            console.error(
                "Backend error:",
                errorText
            );

            throw new Error(
                errorText ||
                "HTTP " + response.status
            );
        }


        if (studentId) {

            alert(
                "Student updated successfully!"
            );

        } else {

            alert(
                "Student added successfully!"
            );

        }


        resetForm();

        await getStudents();


    } catch (error) {

        console.error(
            "Save student error:",
            error
        );

        alert(
            "Unable to save student:\n" +
            error.message
        );

    }

}


// ======================================================
// GET ALL STUDENTS
// ======================================================

async function getStudents() {

    if (!checkAdminAccess()) {
        return;
    }


    const tableBody =
        document.getElementById(
            "studentTableBody"
        );


    if (!tableBody) {

        console.error(
            "studentTableBody not found!"
        );

        return;
    }


    tableBody.innerHTML = `
        <tr>
            <td colspan="6" style="text-align:center;">
                Loading students...
            </td>
        </tr>
    `;


    try {

        console.log(
            "Loading students from:",
            API_BASE_URL
        );


        const response =
            await fetch(API_BASE_URL);


        console.log(
            "GET students status:",
            response.status
        );


        if (!response.ok) {

            throw new Error(
                "HTTP " +
                response.status
            );
        }


        const students =
            await response.json();


        console.log(
            "Students received:",
            students
        );


        tableBody.replaceChildren();


        if (
            !students ||
            students.length === 0
        ) {

            tableBody.innerHTML = `
                <tr>
                    <td colspan="6"
                        style="text-align:center;">
                        No students found.
                    </td>
                </tr>
            `;

            return;
        }


        // ==================================================
        // DISPLAY STUDENTS
        // ==================================================

        students.forEach(function (student) {

            const row =
                document.createElement("tr");

            [
                student.studentId,
                student.name,
                student.email,
                student.mobile,
                student.course
            ].forEach(value => {
                const cell = document.createElement("td");
                cell.textContent = value == null ? "" : String(value);
                row.appendChild(cell);
            });

            const actions = document.createElement("td");
            const editButton = document.createElement("button");
            editButton.type = "button";
            editButton.className = "edit-btn";
            editButton.textContent = "Edit";
            editButton.addEventListener("click", () => {
                editStudent(student.studentId);
            });
            actions.appendChild(editButton);

            const deleteButton = document.createElement("button");
            deleteButton.type = "button";
            deleteButton.className = "delete-btn";
            deleteButton.textContent = "Delete";
            deleteButton.addEventListener("click", () => {
                deleteStudent(student.studentId);
            });
            actions.appendChild(deleteButton);
            row.appendChild(actions);


            tableBody.appendChild(row);

        });


        console.log(
            "Student table updated."
        );


    } catch (error) {

        console.error(
            "Load students error:",
            error
        );


        const row = document.createElement("tr");
        const cell = document.createElement("td");
        cell.colSpan = 6;
        cell.style.color = "red";
        cell.style.textAlign = "center";
        cell.textContent = `Unable to load students. ${error.message}`;
        row.appendChild(cell);
        tableBody.replaceChildren(row);

    }

}

async function getPendingStudents() {
    if (!checkAdminAccess()) {
        return;
    }

    const tableBody = document.getElementById("pendingStudentTableBody");
    if (!tableBody) {
        return;
    }

    tableBody.replaceChildren();

    try {
        const response = await fetch(`${API_BASE_URL}/pending`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const students = await response.json();
        if (students.length === 0) {
            const row = document.createElement("tr");
            const cell = document.createElement("td");
            cell.colSpan = 6;
            cell.textContent = "No students are awaiting approval.";
            row.appendChild(cell);
            tableBody.appendChild(row);
            return;
        }

        students.forEach(student => {
            const row = document.createElement("tr");
            [
                student.studentId,
                student.name,
                student.email,
                student.course
            ].forEach(value => {
                const cell = document.createElement("td");
                cell.textContent = value == null ? "" : String(value);
                row.appendChild(cell);
            });

            const semesterCell = document.createElement("td");
            const semesterSelect = document.createElement("select");
            semesterSelect.setAttribute(
                "aria-label",
                `Assign semester for ${student.name || "student"}`
            );
            for (let semester = 1; semester <= 6; semester += 1) {
                const option = document.createElement("option");
                option.value = String(semester);
                option.textContent = `Semester ${semester}`;
                semesterSelect.appendChild(option);
            }
            semesterCell.appendChild(semesterSelect);
            row.appendChild(semesterCell);

            const actionCell = document.createElement("td");
            const approveButton = document.createElement("button");
            approveButton.type = "button";
            approveButton.className = "approve-btn";
            approveButton.textContent = "Approve";
            approveButton.addEventListener("click", () => {
                approveStudent(student.studentId, semesterSelect.value);
            });
            actionCell.appendChild(approveButton);
            row.appendChild(actionCell);
            tableBody.appendChild(row);
        });
    } catch (error) {
        console.error("Load pending students error:", error);
        const row = document.createElement("tr");
        const cell = document.createElement("td");
        cell.colSpan = 6;
        cell.textContent = `Unable to load pending students: ${error.message}`;
        row.appendChild(cell);
        tableBody.appendChild(row);
    }
}

async function approveStudent(studentId, semester) {
    if (!checkAdminAccess()) {
        return;
    }

    try {
        const response = await fetch(
            `${API_BASE_URL}/${encodeURIComponent(studentId)}/approval`,
            {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ semester: Number(semester) })
            }
        );

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        await Promise.all([getStudents(), getPendingStudents()]);
    } catch (error) {
        console.error("Approve student error:", error);
        alert(`Unable to approve student:\n${error.message}`);
    }
}


// ======================================================
// EDIT STUDENT
// ======================================================

async function editStudent(id) {

    if (!checkAdminAccess()) {
        return;
    }


    try {

        console.log(
            "Editing student:",
            id
        );


        const response =
            await fetch(
                `${API_BASE_URL}/${id}`
            );


        if (!response.ok) {

            throw new Error(
                "Unable to load student"
            );
        }


        const student =
            await response.json();


        document.getElementById(
            "studentId"
        ).value =
            student.studentId || "";


        document.getElementById(
            "name"
        ).value =
            student.name || "";


        document.getElementById(
            "email"
        ).value =
            student.email || "";


        document.getElementById(
            "mobile"
        ).value =
            student.mobile || "";


        document.getElementById(
            "course"
        ).value =
            student.course || "";


        const passwordField =
            document.getElementById(
                "password"
            );


        if (passwordField) {
            passwordField.value = "";
            passwordField.required = false;
            passwordField.placeholder =
                "Leave blank to keep the current password";
        }


        const submitButton =
            document.getElementById(
                "submitButton"
            );


        if (submitButton) {

            submitButton.textContent =
                "Update Student";

        }


        const cancelButton =
            document.getElementById(
                "cancelButton"
            );


        if (cancelButton) {

            cancelButton.style.display =
                "inline-block";

        }


        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });


    } catch (error) {

        console.error(
            "Edit error:",
            error
        );


        alert(
            "Unable to load student."
        );

    }

}


// ======================================================
// DELETE STUDENT
// ======================================================

async function deleteStudent(id) {

    if (!checkAdminAccess()) {
        return;
    }


    const confirmation =
        confirm(
            "Are you sure you want to delete this student?"
        );


    if (!confirmation) {
        return;
    }


    try {

        const response =
            await fetch(
                `${API_BASE_URL}/${id}`,
                {
                    method: "DELETE"
                }
            );


        if (!response.ok) {

            throw new Error(
                "Delete failed: " +
                response.status
            );
        }


        alert(
            "Student deleted successfully!"
        );


        await getStudents();


    } catch (error) {

        console.error(
            "Delete error:",
            error
        );


        alert(
            "Unable to delete student:\n" +
            error.message
        );

    }

}


// ======================================================
// RESET FORM
// ======================================================

function resetForm() {

    const form =
        document.getElementById(
            "studentForm"
        );


    if (form) {
        form.reset();
    }


    const studentId =
        document.getElementById(
            "studentId"
        );


    if (studentId) {
        studentId.value = "";
    }

    const passwordField =
        document.getElementById("password");
    if (passwordField) {
        passwordField.required = true;
        passwordField.placeholder = "Exactly 6 characters";
    }

    const submitButton =
        document.getElementById(
            "submitButton"
        );


    if (submitButton) {

        submitButton.textContent =
            "Add Student";

    }


    const cancelButton =
        document.getElementById(
            "cancelButton"
        );


    if (cancelButton) {

        cancelButton.style.display =
            "none";

    }

}


// ======================================================
// CANCEL EDIT
// ======================================================

function cancelEdit() {

    resetForm();

}


// ======================================================
// BACK TO ADMIN DASHBOARD
// ======================================================

function goBack() {

    console.log(
        "Back to Admin Dashboard"
    );

    window.location.href =
        "admin-dashboard.html";

}