// ======================================================
// FACULTY MANAGEMENT - faculty.js
// ======================================================

console.log("faculty.js loaded successfully");

const FACULTY_API_URL =
    "http://localhost:8080/faculty";


// ======================================================
// PAGE LOAD
// ======================================================

document.addEventListener(
    "DOMContentLoaded",
    function () {

        console.log(
            "Faculty management page loaded"
        );

        loadFaculty();

        const facultyForm =
            document.getElementById("facultyForm");

        if (facultyForm) {

            facultyForm.addEventListener(
                "submit",
                function (event) {

                    event.preventDefault();

                    saveFaculty();
                }
            );
        }

    }
);


// ======================================================
// LOAD FACULTY
// ======================================================

async function loadFaculty() {

    const tableBody =
        document.getElementById(
            "facultyTableBody"
        );

    if (!tableBody) {
        console.error(
            "facultyTableBody not found"
        );
        return;
    }

    tableBody.innerHTML = `
        <tr>
            <td colspan="7">
                Loading faculty...
            </td>
        </tr>
    `;

    try {

        const response =
            await fetch(FACULTY_API_URL);

        if (!response.ok) {

            throw new Error(
                "HTTP Error: " +
                response.status
            );
        }

        const facultyList =
            await response.json();

        console.log(
            "Faculty data:",
            facultyList
        );

        displayFaculty(facultyList);

    } catch (error) {

        console.error(
            "Faculty loading error:",
            error
        );

        tableBody.innerHTML = `
            <tr>
                <td colspan="7">
                    Unable to load faculty.
                </td>
            </tr>
        `;
    }
}


// ======================================================
// DISPLAY FACULTY
// ======================================================

function displayFaculty(facultyList) {

    const tableBody =
        document.getElementById(
            "facultyTableBody"
        );

    tableBody.innerHTML = "";

    if (
        !facultyList ||
        facultyList.length === 0
    ) {

        tableBody.innerHTML = `
            <tr>
                <td colspan="7">
                    No faculty found.
                </td>
            </tr>
        `;

        return;
    }

    facultyList.forEach(
        function (faculty) {

            const id =
                faculty.facultyId ??
                faculty.id;

            const row =
                document.createElement("tr");

            row.innerHTML = `

                <td>${id}</td>

                <td>
                    ${faculty.fullName ?? ""}
                </td>

                <td>
                    ${faculty.email ?? ""}
                </td>

                <td>
                    ${faculty.phone ?? ""}
                </td>

                <td>
                    ${faculty.department ?? ""}
                </td>

                <td>
                    ${faculty.designation ?? ""}
                </td>

                <td>

                    <button
                        type="button"
                        onclick="editFaculty(${id})"
                    >
                        Edit
                    </button>

                    <button
                        type="button"
                        onclick="deleteFaculty(${id})"
                    >
                        Delete
                    </button>

                </td>
            `;

            tableBody.appendChild(row);
        }
    );
}


// ======================================================
// ADD / UPDATE
// ======================================================

async function saveFaculty() {

    const facultyId =
        document.getElementById(
            "facultyId"
        ).value.trim();

    const facultyData = {

        fullName:
            document.getElementById(
                "fullName"
            ).value.trim(),

        email:
            document.getElementById(
                "email"
            ).value.trim(),

        phone:
            document.getElementById(
                "phone"
            ).value.trim(),

        department:
            document.getElementById(
                "department"
            ).value.trim(),

        designation:
            document.getElementById(
                "designation"
            ).value.trim(),

        password:
            document.getElementById(
                "password"
            ).value
    };


    try {

        let response;

        if (facultyId) {

            response =
                await fetch(
                    `${FACULTY_API_URL}/${facultyId}`,
                    {
                        method: "PUT",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body:
                            JSON.stringify(
                                facultyData
                            )
                    }
                );

        } else {

            response =
                await fetch(
                    FACULTY_API_URL,
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body:
                            JSON.stringify(
                                facultyData
                            )
                    }
                );
        }


        if (!response.ok) {

            throw new Error(
                "HTTP Error: " +
                response.status
            );
        }


        alert(
            facultyId
                ? "Faculty updated successfully!"
                : "Faculty added successfully!"
        );


        resetForm();

        loadFaculty();

    } catch (error) {

        console.error(
            "Save faculty error:",
            error
        );

        alert(
            "Unable to save faculty."
        );
    }
}


// ======================================================
// EDIT
// ======================================================

async function editFaculty(id) {

    try {

        const response =
            await fetch(
                `${FACULTY_API_URL}/${id}`
            );

        if (!response.ok) {
            throw new Error(
                "Unable to load faculty"
            );
        }

        const faculty =
            await response.json();


        document.getElementById(
            "facultyId"
        ).value =
            faculty.facultyId ??
            faculty.id ??
            "";


        document.getElementById(
            "fullName"
        ).value =
            faculty.fullName ?? "";


        document.getElementById(
            "email"
        ).value =
            faculty.email ?? "";


        document.getElementById(
            "phone"
        ).value =
            faculty.phone ?? "";


        document.getElementById(
            "department"
        ).value =
            faculty.department ?? "";


        document.getElementById(
            "designation"
        ).value =
            faculty.designation ?? "";


        document.getElementById(
            "password"
        ).value = "";


        document.getElementById(
            "submitButton"
        ).textContent =
            "Update Faculty";


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
            "Edit faculty error:",
            error
        );

        alert(
            "Unable to load faculty."
        );
    }
}


// ======================================================
// DELETE
// ======================================================

async function deleteFaculty(id) {

    if (!id) {
        alert("Faculty ID is missing.");
        return;
    }

    if (
        !confirm(
            "Are you sure you want to delete this faculty?"
        )
    ) {
        return;
    }


    try {

        const response =
            await fetch(
                `${FACULTY_API_URL}/${id}`,
                {
                    method: "DELETE"
                }
            );


        if (!response.ok) {

            throw new Error(
                "HTTP Error: " +
                response.status
            );
        }


        alert(
            "Faculty deleted successfully!"
        );

        loadFaculty();

    } catch (error) {

        console.error(
            "Delete faculty error:",
            error
        );

        alert(
            "Unable to delete faculty."
        );
    }
}


// ======================================================
// RESET
// ======================================================

function resetForm() {

    const form =
        document.getElementById(
            "facultyForm"
        );

    if (form) {
        form.reset();
    }


    document.getElementById(
        "facultyId"
    ).value = "";


    document.getElementById(
        "submitButton"
    ).textContent =
        "Add Faculty";


    document.getElementById(
        "cancelButton"
    ).style.display =
        "none";
}


// ======================================================
// CANCEL
// ======================================================

function cancelEdit() {

    resetForm();
}


// ======================================================
// BACK TO ADMIN
// ======================================================

function goBack() {

    window.location.replace(
        "admin-dashboard.html"
    );
}