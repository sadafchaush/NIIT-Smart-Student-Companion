
// ======================================================
// FACULTY QUESTION PAPERS
// ======================================================

console.log("faculty-question-papers.js loaded");

document.addEventListener("DOMContentLoaded", async function () {

    const user = getLoggedInUser();

    const subjectSelect = document.getElementById("subjectId");
    const form = document.getElementById("questionPaperForm");
    const fileInput = document.getElementById("file");
    const uploadButton = document.getElementById("uploadButton");
    const message = document.getElementById("message");


    // --------------------------------------------------
    // CHECK LOGIN AND ROLE
    // --------------------------------------------------

    if (!user || getUserRole() !== "FACULTY") {
        alert("Please log in with a faculty account.");

        window.location.replace("login.html");
        return;
    }

    // Get the actual faculty ID from the login response.
const facultyId = Number(user.facultyId);

if (!Number.isInteger(facultyId) || facultyId <= 0) {
    message.textContent =
        "Faculty ID was not found in your login response.";
    return;
}
    // --------------------------------------------------
    // LOAD SUBJECTS
    // --------------------------------------------------

    async function loadSubjects() {

        subjectSelect.innerHTML =
            '<option value="">Loading subjects...</option>';

        try {

            const response = await fetch(
                "http://localhost:8080/subjects"
            );

            if (!response.ok) {
                throw new Error("Unable to load subjects.");
            }

            const subjects = await response.json();

            subjectSelect.innerHTML =
                '<option value="">Select your subject</option>';

            // Show only subjects assigned to this faculty.
            const assignedSubjects = subjects.filter(function (subject) {

                const assignedFaculty =
                    subject.faculty || subject.facultyId;

                const assignedFacultyId =
                    assignedFaculty && typeof assignedFaculty === "object"
                        ? assignedFaculty.facultyId
                        : assignedFaculty;

                return Number(assignedFacultyId) === facultyId;
            });

            if (assignedSubjects.length === 0) {

                subjectSelect.innerHTML =
                    '<option value="">No assigned subjects found</option>';

                message.textContent =
                    "No subjects assigned to your faculty account were found. " +
                    "Check the faculty ID and subject assignments in the database.";

                return;
            }

            assignedSubjects.forEach(function (subject) {

                const option = document.createElement("option");

                option.value = subject.subjectId;

                option.textContent =
                    (subject.subjectName || "Unnamed subject") +
                    (subject.subjectCode
                        ? " (" + subject.subjectCode + ")"
                        : "");

                subjectSelect.appendChild(option);
            });

            message.textContent = "";

        } catch (error) {

            console.error("SUBJECT LOADING ERROR:", error);

            subjectSelect.innerHTML =
                '<option value="">Could not load subjects</option>';

            message.textContent =
                "Could not load subjects. Make sure Spring Boot is running " +
                "and the /subjects API works.";
        }
    }

    await loadSubjects();

    // --------------------------------------------------
    // UPLOAD QUESTION PAPER
    // --------------------------------------------------

    form.addEventListener("submit", async function (event) {

        event.preventDefault();

        message.textContent = "";

        const subjectId = subjectSelect.value;
        const examYear = Number(
            document.getElementById("examYear").value
        );
        const examType = document.getElementById("examType").value;
        const file = fileInput.files[0];

        if (!subjectId || !examType || !file) {
            message.textContent =
                "Please complete all fields and select a PDF file.";
            return;
        }

        if (!Number.isInteger(examYear) ||
            examYear < 1900 || examYear > 2100) {

            message.textContent =
                "Please enter a valid examination year.";
            return;
        }

        if (!file.name.toLowerCase().endsWith(".pdf")) {
            message.textContent = "Only PDF files are allowed.";
            return;
        }

        if (file.size === 0) {
            message.textContent = "The selected file is empty.";
            return;
        }

        if (file.size > 10 * 1024 * 1024) {
            message.textContent =
                "The maximum allowed file size is 10 MB.";
            return;
        }

        const formData = new FormData();

        formData.append("file", file);
        formData.append("subjectId", subjectId);
        formData.append("facultyId", String(facultyId));
        formData.append("examYear", String(examYear));
        formData.append("examType", examType);

        uploadButton.disabled = true;
        uploadButton.textContent = "Uploading...";

        try {

            const response = await fetch(
                "http://localhost:8080/question-papers/upload",
                {
                    method: "POST",
                    body: formData
                }
            );

            const result = await response.json().catch(function () {
                return {};
            });

            if (!response.ok) {

                throw new Error(
                    result.message ||
                    result.detail ||
                    "Upload failed. Check the Spring Boot Console."
                );
            }

            message.textContent =
                "Success: " + (result.message ||
                "Question paper uploaded successfully.") +
                " File: " + file.name;

            form.reset();

        } catch (error) {

            console.error("QUESTION PAPER UPLOAD ERROR:", error);

            message.textContent =
                "Upload failed: " + error.message;

        } finally {

            uploadButton.disabled = false;
            uploadButton.innerHTML =
                '<i class="fa-solid fa-upload"></i> Upload Question Paper';
        }
    });

});
