
const API_BASE_URL = "http://localhost:8080";

let editingAssignmentId = null;

let subjects = [];

console.log("faculty-assignments.js loaded successfully");


document.addEventListener("DOMContentLoaded", function () {

    loadSubjects();

    loadAssignments();

});


// ========================================
// LOAD SUBJECTS
// ========================================

async function loadSubjects() {

    const subjectSelect =
        document.getElementById("subjectId");

    try {
        const user = JSON.parse(localStorage.getItem("loggedInUser") || "null");
        const facultyResponse = await fetch(`${API_BASE_URL}/faculty`);
        const facultyList = facultyResponse.ok ? await facultyResponse.json() : [];
        const faculty = facultyList.find(function (item) {
            return item && item.email && user && item.email.toLowerCase() === user.email.toLowerCase();
        });
        const facultyId = faculty ? Number(faculty.facultyId) : null;

        const response =
            await fetch(
                `${API_BASE_URL}/subjects`
            );

        if (!response.ok) {

            throw new Error(
                "Failed to load subjects"
            );

        }

        subjects =
            await response.json();

        subjectSelect.innerHTML = `
            <option value="">
                Select Subject
            </option>
        `;

        const filteredSubjects = facultyId == null
            ? subjects
            : subjects.filter(function (subject) {
                const assignedFaculty = subject.faculty || subject.facultyId;
                const assignedFacultyId = assignedFaculty && typeof assignedFaculty === "object"
                    ? assignedFaculty.facultyId
                    : assignedFaculty;
                return Number(assignedFacultyId) === facultyId;
            });

        filteredSubjects.forEach(function (subject) {

            const option =
                document.createElement("option");

            option.value =
                subject.subjectId;

            option.textContent =
                subject.subjectName;

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


// ========================================
// CREATE / UPDATE ASSIGNMENT
// ========================================

document
    .getElementById("assignmentForm")
    .addEventListener(
        "submit",
        async function (event) {

            event.preventDefault();


            const title =
                document.getElementById("title")
                    .value.trim();


            const description =
                document.getElementById("description")
                    .value.trim();


            const dueDate =
                document.getElementById("dueDate")
                    .value;


            const subjectId =
                document.getElementById("subjectId")
                    .value;


            if (
                !title ||
                !description ||
                !dueDate ||
                !subjectId
            ) {

                alert(
                    "Please fill all fields."
                );

                return;
            }


            const user = JSON.parse(localStorage.getItem("loggedInUser") || "null");
            const facultyResponse = await fetch(`${API_BASE_URL}/faculty`);
            const facultyList = facultyResponse.ok ? await facultyResponse.json() : [];
            const faculty = facultyList.find(function (item) {
                return item && item.email && user && item.email.toLowerCase() === user.email.toLowerCase();
            });
            const facultyId = faculty ? Number(faculty.facultyId) : null;

            const assignment = {

                title: title,

                description: description,

                dueDate: dueDate,

                subjectId:
                    Number(subjectId),

                facultyId: facultyId

            };


            try {

                let response;


                // ============================
                // UPDATE
                // ============================

                if (
                    editingAssignmentId !== null
                ) {

                    response =
                        await fetch(
                            `${API_BASE_URL}/assignments/${editingAssignmentId}`,
                            {
                                method: "PUT",

                                headers: {
                                    "Content-Type":
                                        "application/json"
                                },

                                body:
                                    JSON.stringify(
                                        assignment
                                    )
                            }
                        );

                }

                // ============================
                // CREATE
                // ============================

                else {

                    response =
                        await fetch(
                            `${API_BASE_URL}/assignments`,
                            {
                                method: "POST",

                                headers: {
                                    "Content-Type":
                                        "application/json"
                                },

                                body:
                                    JSON.stringify(
                                        assignment
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


                alert(
                    editingAssignmentId !== null
                        ? "Assignment updated successfully!"
                        : "Assignment created successfully!"
                );


                resetForm();

                loadAssignments();


            } catch (error) {

                console.error(
                    "Assignment error:",
                    error
                );

                alert(
                    "Unable to save assignment: "
                    + error.message
                );
            }

        }
    );


// ========================================
// LOAD ALL ASSIGNMENTS
// ========================================

async function loadAssignments() {

    const container =
        document.getElementById(
            "assignmentsContainer"
        );


    try {

        const response =
            await fetch(
                `${API_BASE_URL}/assignments`
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load assignments"
            );
        }


        const assignments =
            await response.json();


        container.innerHTML = "";


        if (
            !assignments ||
            assignments.length === 0
        ) {

            container.innerHTML = `
                <div class="loading">
                    No assignments found.
                </div>
            `;

            return;
        }


        assignments.forEach(
            function (assignment) {

                const subjectName =
                    getSubjectName(
                        assignment.subjectId
                    );


                const card =
                    document.createElement(
                        "div"
                    );

                card.className =
                    "assignment-card";


                card.innerHTML = `

                    <h4>
                        ${assignment.title}
                    </h4>

                    <p>
                        <strong>
                            Description:
                        </strong>
                        ${assignment.description}
                    </p>

                    <p>
                        <strong>
                            Subject:
                        </strong>
                        ${subjectName}
                    </p>

                    <p>
                        <strong>
                            Due Date:
                        </strong>
                        ${assignment.dueDate}
                    </p>

                    <p>
                        <strong>
                            Assignment ID:
                        </strong>
                        ${assignment.id}
                    </p>

                    <button
                        class="edit-button"
                        onclick="editAssignment(${assignment.id})">

                        Edit

                    </button>

                    <button
                        class="delete-button"
                        onclick="deleteAssignment(${assignment.id})">

                        Delete

                    </button>

                `;


                container.appendChild(card);

            }
        );


    } catch (error) {

        console.error(
            "Assignment loading error:",
            error
        );


        container.innerHTML = `
            <div class="error">
                Unable to load assignments.
                <br><br>
                ${error.message}
            </div>
        `;
    }
}


// ========================================
// GET SUBJECT NAME
// ========================================

function getSubjectName(subjectId) {

    const subject =
        subjects.find(
            function (item) {

                return Number(
                    item.subjectId
                ) === Number(
                    subjectId
                );

            }
        );


    if (subject) {

        return subject.subjectName;

    }


    return "Unknown Subject";
}


// ========================================
// EDIT ASSIGNMENT
// ========================================

async function editAssignment(id) {

    try {

        const response =
            await fetch(
                `${API_BASE_URL}/assignments/${id}`
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load assignment"
            );
        }


        const assignment =
            await response.json();


        editingAssignmentId =
            assignment.id;


        document.getElementById(
            "title"
        ).value =
            assignment.title;


        document.getElementById(
            "description"
        ).value =
            assignment.description;


        document.getElementById(
            "dueDate"
        ).value =
            assignment.dueDate;


        document.getElementById(
            "subjectId"
        ).value =
            assignment.subjectId;


        document.getElementById(
            "formHeading"
        ).textContent =
            "✏️ Edit Assignment";


        document.getElementById(
            "saveButton"
        ).textContent =
            "Update Assignment";


        document.getElementById(
            "cancelButton"
        ).style.display =
            "inline-block";


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
            "Unable to edit assignment."
        );
    }
}


// ========================================
// DELETE ASSIGNMENT
// ========================================

async function deleteAssignment(id) {

    const confirmed =
        confirm(
            "Are you sure you want to delete this assignment?"
        );


    if (!confirmed) {

        return;

    }


    try {

        const response =
            await fetch(
                `${API_BASE_URL}/assignments/${id}`,
                {
                    method: "DELETE"
                }
            );


        if (!response.ok) {

            throw new Error(
                "Failed to delete assignment"
            );
        }


        alert(
            "Assignment deleted successfully!"
        );


        loadAssignments();


    } catch (error) {

        console.error(
            "Delete error:",
            error
        );


        alert(
            "Unable to delete assignment."
        );
    }
}


// ========================================
// CANCEL EDIT
// ========================================

function cancelEdit() {

    resetForm();

}


// ========================================
// RESET FORM
// ========================================

function resetForm() {

    editingAssignmentId = null;


    document
        .getElementById("assignmentForm")
        .reset();


    document.getElementById(
        "formHeading"
    ).textContent =
        "➕ Add New Assignment";


    document.getElementById(
        "saveButton"
    ).textContent =
        "Save Assignment";


    document.getElementById(
        "cancelButton"
    ).style.display =
        "none";

}


// ========================================
// BACK TO DASHBOARD
// ========================================

function goBack() {

    window.location.href =
        "faculty-dashboard.html";

}