// ======================================================
// ADMIN STUDENT MANAGEMENT - students.js
// ======================================================

const API_BASE_URL = "http://localhost:8080/students";

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
        passwordElement.value.trim() :
        "";


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


    console.log("Student data:", studentData);


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

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(studentData)
                }
            );

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


        tableBody.innerHTML = "";


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


            row.innerHTML = `

                <td>
                    ${student.studentId ?? ""}
                </td>

                <td>
                    ${student.name ?? ""}
                </td>

                <td>
                    ${student.email ?? ""}
                </td>

                <td>
                    ${student.mobile ?? ""}
                </td>

                <td>
                    ${student.course ?? ""}
                </td>

                <td>

                    <button
                        type="button"
                        class="edit-btn"
                        onclick="editStudent(${student.studentId})">
                        Edit
                    </button>

                    <button
                        type="button"
                        class="delete-btn"
                        onclick="deleteStudent(${student.studentId})">
                        Delete
                    </button>

                </td>

            `;


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


        tableBody.innerHTML = `
            <tr>
                <td colspan="6"
                    style="color:red;text-align:center;">

                    Unable to load students.

                    <br><br>

                    ${error.message}

                </td>
            </tr>
        `;

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