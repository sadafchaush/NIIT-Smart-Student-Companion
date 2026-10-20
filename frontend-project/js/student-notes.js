// =====================================================
// STUDENT STUDY NOTES
// =====================================================

const NOTES_API = "http://localhost:8080/notes";
const SUBJECTS_API = "http://localhost:8080/subjects";

let allNotes = [];
let allSubjects = [];


// =====================================================
// PAGE LOAD
// =====================================================

document.addEventListener("DOMContentLoaded", function () {

    console.log("=================================");
    console.log("Student Study Notes JS Loaded");
    console.log("=================================");

    loadSubjects();
    loadNotes();


    // Search
    const searchInput =
        document.getElementById("searchInput");

    if (searchInput) {

        searchInput.addEventListener(
            "input",
            function () {

                applyFilters();

            }
        );

    }


    // Class filter
    const classFilter =
        document.getElementById("classFilter");

    if (classFilter) {

        classFilter.addEventListener(
            "change",
            function () {

                applyFilters();

            }
        );

    }


    // Semester filter
    const semesterFilter =
        document.getElementById("semesterFilter");

    if (semesterFilter) {

        semesterFilter.addEventListener(
            "change",
            function () {

                applyFilters();

            }
        );

    }


    // Subject filter
    const subjectFilter =
        document.getElementById("subjectFilter");

    if (subjectFilter) {

        subjectFilter.addEventListener(
            "change",
            function () {

                applyFilters();

            }
        );

    }


    // Refresh
    const refreshButton =
        document.getElementById("refreshButton");

    if (refreshButton) {

        refreshButton.addEventListener(
            "click",
            function () {

                loadSubjects();
                loadNotes();

            }
        );

    }


    // Back
    const backButton =
        document.getElementById("backButton");

    if (backButton) {

        backButton.addEventListener(
            "click",
            function () {

                window.location.href =
                    "student-dashboard.html";

            }
        );

    }

});


// =====================================================
// LOAD SUBJECTS
// =====================================================

async function loadSubjects() {

    const subjectFilter =
        document.getElementById("subjectFilter");


    if (!subjectFilter) {

        return;

    }


    try {

        console.log(
            "Calling Subjects API:",
            SUBJECTS_API
        );


        const response =
            await fetch(SUBJECTS_API);


        console.log(
            "Subjects API status:",
            response.status
        );


        if (!response.ok) {

            throw new Error(
                "HTTP Error: " +
                response.status
            );

        }


        const data =
            await response.json();


        if (!Array.isArray(data)) {

            throw new Error(
                "Invalid subjects response"
            );

        }


        allSubjects = data;


        populateSubjectFilter();

    }

    catch (error) {

        console.error(
            "Subjects loading error:",
            error
        );

        subjectFilter.innerHTML = `
            <option value="">
                All Subjects
            </option>
        `;

    }

}


// =====================================================
// POPULATE SUBJECT FILTER
// =====================================================

function populateSubjectFilter() {

    const subjectFilter =
        document.getElementById("subjectFilter");


    if (!subjectFilter) {

        return;

    }


    subjectFilter.innerHTML = `
        <option value="">
            All Subjects
        </option>
    `;


    allSubjects.forEach(function (subject) {

        const option =
            document.createElement("option");


        option.value =
            subject.subjectId ??
            subject.id ??
            "";


        option.textContent =
            subject.subjectName ??
            subject.name ??
            subject.subjectCode ??
            "Unknown Subject";


        if (option.value !== "") {

            subjectFilter.appendChild(option);

        }

    });

}


// =====================================================
// LOAD NOTES
// =====================================================

async function loadNotes() {

    const container =
        document.getElementById("notesContainer");


    if (!container) {

        console.error(
            "notesContainer not found"
        );

        return;

    }


    container.innerHTML = `
        <div class="loading">

            <div class="loader"></div>

            <p>Loading notes...</p>

        </div>
    `;


    try {
        let semester = 1;
        const storedUser = localStorage.getItem("loggedInUser");

        if (storedUser) {
            const user = JSON.parse(storedUser);
            const email = user.email;

            if (email) {
                const studentResponse = await fetch(
                    `http://localhost:8080/students/email/${encodeURIComponent(email)}`
                );

                if (studentResponse.ok) {
                    const student = await studentResponse.json();
                    semester = Number(student.semester ?? 1);
                }
            }
        }

        const notesUrl = `${NOTES_API}?semester=${semester}`;

        console.log(
            "Calling Notes API:",
            notesUrl
        );


        const response =
            await fetch(notesUrl);


        console.log(
            "Notes API status:",
            response.status
        );


        if (!response.ok) {

            throw new Error(
                "HTTP Error: " +
                response.status
            );

        }


        const data =
            await response.json();


        console.log(
            "Notes received:",
            data
        );


        if (!Array.isArray(data)) {

            throw new Error(
                "Invalid notes response"
            );

        }


        allNotes = data;


        applyFilters();

    }

    catch (error) {

        console.error(
            "Notes loading error:",
            error
        );


        container.innerHTML = `
            <div class="error">

                ❌ Unable to load notes.

                <br><br>

                Make sure Spring Boot is running
                on port 8080.

                <br><br>

                <button
                    onclick="loadNotes()"
                    style="
                        background:#1e3a8a;
                        color:white;
                        border:none;
                        padding:10px 18px;
                        border-radius:6px;
                        cursor:pointer;
                        font-weight:bold;
                    "
                >
                    🔄 Try Again
                </button>

            </div>
        `;

    }

}


// =====================================================
// APPLY ALL FILTERS
// =====================================================

function applyFilters() {

    const classFilter =
        document.getElementById("classFilter");

    const semesterFilter =
        document.getElementById("semesterFilter");

    const subjectFilter =
        document.getElementById("subjectFilter");

    const searchInput =
        document.getElementById("searchInput");


    const selectedClass =
        classFilter
            ? classFilter.value
            : "";


    const selectedSemester =
        semesterFilter
            ? semesterFilter.value
            : "";


    const selectedSubject =
        subjectFilter
            ? subjectFilter.value
            : "";


    const searchText =
        searchInput
            ? searchInput.value
                .toLowerCase()
                .trim()
            : "";


    const filteredNotes =
        allNotes.filter(function (note) {


            // ================================
            // CLASS FILTER
            // ================================

            if (
                selectedClass !== "" &&
                note.className !== selectedClass
            ) {

                return false;

            }


            // ================================
            // SEMESTER FILTER
            // ================================

            if (
                selectedSemester !== "" &&
                String(note.semester) !==
                String(selectedSemester)
            ) {

                return false;

            }


            // ================================
            // SUBJECT FILTER
            // ================================

            if (
                selectedSubject !== "" &&
                String(note.subjectId) !==
                String(selectedSubject)
            ) {

                return false;

            }


            // ================================
            // SEARCH FILTER
            // ================================

            if (searchText !== "") {

                const title =
                    (note.title || "")
                    .toLowerCase();


                const description =
                    (note.description || "")
                    .toLowerCase();


                const subjectName =
                    getSubjectName(note.subjectId)
                    .toLowerCase();


                const id =
                    String(note.id || "")
                    .toLowerCase();


                const className =
                    (note.className || "")
                    .toLowerCase();


                const semester =
                    String(note.semester || "")
                    .toLowerCase();


                const matchesSearch =
                    title.includes(searchText) ||
                    description.includes(searchText) ||
                    subjectName.includes(searchText) ||
                    id.includes(searchText) ||
                    className.includes(searchText) ||
                    semester.includes(searchText);


                if (!matchesSearch) {

                    return false;

                }

            }


            return true;

        });


    displayNotes(filteredNotes);

}


// =====================================================
// GET SUBJECT NAME
// =====================================================

function getSubjectName(subjectId) {

    if (
        subjectId === null ||
        subjectId === undefined
    ) {

        return "Not assigned";

    }


    const subject =
        allSubjects.find(function (item) {

            const id =
                item.subjectId ??
                item.id;

            return String(id) ===
                String(subjectId);

        });


    if (!subject) {

        return "Subject ID: " + subjectId;

    }


    return (
        subject.subjectName ??
        subject.name ??
        subject.subjectCode ??
        "Unknown Subject"
    );

}


// =====================================================
// DISPLAY NOTES
// =====================================================

function displayNotes(notes) {

    const container =
        document.getElementById("notesContainer");


    if (!container) {

        return;

    }


    container.innerHTML = "";


    if (
        !notes ||
        notes.length === 0
    ) {

        container.innerHTML = `
            <div class="no-notes">

                📚 No notes found for the selected
                class, semester or subject.

            </div>
        `;

        return;

    }


    notes.forEach(function (note) {

        const card =
            document.createElement("div");


        card.className =
            "note-card";


        const title =
            note.title ||
            "Untitled Note";


        const description =
            note.description ||
            "No description available";


        const className =
            note.className ||
            "Not assigned";


        const semester =
            note.semester ||
            "Not assigned";


        const subjectName =
            getSubjectName(note.subjectId);


        const fileUrl =
            getFileUrl(note);


        card.innerHTML = `

            <h3>
                📘 ${escapeHtml(title)}
            </h3>

            <p>
                <strong>Description:</strong>
                <br>
                ${escapeHtml(description)}
            </p>

            <p>
                <strong>Class:</strong>
                ${escapeHtml(className)}
            </p>

            <p>
                <strong>Semester:</strong>
                ${escapeHtml(semester)}
            </p>

            <p>
                <strong>Subject:</strong>
                ${escapeHtml(subjectName)}
            </p>

        `;


        // ================================
        // DOWNLOAD / OPEN BUTTON
        // ================================

        if (fileUrl !== "") {

            const button =
                document.createElement("a");


            button.className =
                "open-button";


            button.href =
                fileUrl;

                console.log("Note title:", title);
console.log("Download URL:", button.href);


            button.target =
                "_blank";


            button.rel =
                "noopener noreferrer";


            button.textContent =
                "📥 Open / Download Note";


            card.appendChild(button);

        }

        else {

            const unavailable =
                document.createElement("p");


            unavailable.innerHTML =
                "⚠️ File not available";


            card.appendChild(unavailable);

        }


        container.appendChild(card);

    });

}


// =====================================================
// GET FILE URL
// =====================================================

function getFileUrl(note) {

    // If backend already provides a URL
    if (
        note.fileUrl &&
        note.fileUrl.trim() !== ""
    ) {

        let url =
            note.fileUrl.trim();

            console.log("Original fileUrl:", url);


        // Relative backend URL
        if (url.startsWith("/")) {

            return "http://localhost:8080" + url;

        }


        return url;

    }


    // Uploaded file
    // Backend download endpoint
    if (note.id !== null &&
        note.id !== undefined) {

        return (
            "http://localhost:8080/notes/" +
            note.id +
            "/download"
        );

    }


    return "";

}


// =====================================================
// ESCAPE HTML
// =====================================================

function escapeHtml(value) {

    return String(value)

        .replace(/&/g, "&amp;")

        .replace(/</g, "&lt;")

        .replace(/>/g, "&gt;")

        .replace(/"/g, "&quot;")

        .replace(/'/g, "&#039;");

}