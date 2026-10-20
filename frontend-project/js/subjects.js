console.log("subjects.js loaded successfully");


const SUBJECTS_API =
    "http://localhost:8080/subjects";


document.addEventListener(
    "DOMContentLoaded",
    function () {

        console.log("Subjects page loaded");

        loadSubjects();


        const subjectForm =
            document.getElementById(
                "subjectForm"
            );


        if (subjectForm) {

            subjectForm.addEventListener(
                "submit",
                saveSubject
            );

        }

    }
);


// =====================================================
// LOAD SUBJECTS
// =====================================================

async function loadSubjects() {

    const tableBody =
        document.getElementById(
            "subjectTableBody"
        );


    if (!tableBody) {

        console.error(
            "subjectTableBody not found!"
        );

        return;
    }


    tableBody.innerHTML = `
        <tr>
            <td colspan="5">
                Loading subjects...
            </td>
        </tr>
    `;


    try {

        const response =
            await fetch(
                SUBJECTS_API
            );


        console.log(
            "Subjects API status:",
            response.status
        );


        if (!response.ok) {

            throw new Error(
                "Failed to load subjects"
            );

        }


        const subjects =
            await response.json();


        console.log(
            "Subjects received:",
            subjects
        );


        if (
            !subjects ||
            subjects.length === 0
        ) {

            tableBody.innerHTML = `
                <tr>
                    <td colspan="5">
                        No subjects found.
                    </td>
                </tr>
            `;

            return;
        }


        tableBody.innerHTML = "";


        subjects.forEach(
            function (subject) {

                const row =
                    document.createElement(
                        "tr"
                    );


                let facultyId = "-";


                if (
                    subject.faculty &&
                    subject.faculty.facultyId
                ) {

                    facultyId =
                        subject.faculty.facultyId;

                }


                row.innerHTML = `

                    <td>
                        ${subject.subjectId ?? "-"}
                    </td>

                    <td>
                        ${subject.subjectName ?? "-"}
                    </td>

                    <td>
                        ${subject.subjectCode ?? "-"}
                    </td>

                    <td>
                        ${subject.semester ?? "-"}
                    </td>

                    <td>
                        ${facultyId}
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
            "Error loading subjects:",
            error
        );


        tableBody.innerHTML = `
            <tr>
                <td colspan="5">
                    Unable to load subjects.
                </td>
            </tr>
        `;

    }

}


// =====================================================
// ADD SUBJECT
// =====================================================

async function saveSubject(event) {

    event.preventDefault();


    const subjectName =
        document
            .getElementById(
                "subjectName"
            )
            .value
            .trim();


    const subjectCode =
        document
            .getElementById(
                "subjectCode"
            )
            .value
            .trim();


    const semester =
        document
            .getElementById(
                "semester"
            )
            .value;


    const facultyId =
        document
            .getElementById(
                "facultyId"
            )
            .value;


    const message =
        document.getElementById(
            "message"
        );


    // ================================
    // VALIDATION
    // ================================

    if (
        !subjectName ||
        !subjectCode ||
        !semester ||
        !facultyId
    ) {

        message.textContent =
            "Please fill all fields.";

        message.className =
            "message error";

        return;

    }


    const subjectData = {

        subjectName:
            subjectName,

        subjectCode:
            subjectCode,

        semester:
            Number(semester),

        faculty: {

            facultyId:
                Number(facultyId)

        }

    };


    console.log(
        "Sending subject:",
        subjectData
    );


    try {

        const response =
            await fetch(
                SUBJECTS_API,
                {

                    method: "POST",

                    headers: {

                        "Content-Type":
                            "application/json"

                    },

                    body:
                        JSON.stringify(
                            subjectData
                        )

                }
            );


        console.log(
            "Add subject status:",
            response.status
        );


        if (!response.ok) {

            const errorText =
                await response.text();


            throw new Error(
                errorText ||
                "Unable to add subject."
            );

        }


        const savedSubject =
            await response.json();


        console.log(
            "Subject saved:",
            savedSubject
        );


        message.textContent =
            "Subject added successfully.";

        message.className =
            "message success";


        // Clear form

        document
            .getElementById(
                "subjectForm"
            )
            .reset();


        // Reload subject table

        await loadSubjects();

    }

    catch (error) {

        console.error(
            "Add subject error:",
            error
        );


        message.textContent =
            error.message ||
            "Unable to add subject.";

        message.className =
            "message error";

    }

}