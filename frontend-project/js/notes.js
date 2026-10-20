
// =====================================================
// NOTES.JS - STUDENT STUDY NOTES
// =====================================================

const API_BASE_URL = "http://localhost:8080";

console.log("notes.js loaded successfully");

document.addEventListener("DOMContentLoaded", loadNotes);


// =====================================================
// LOAD NOTES
// =====================================================

async function loadNotes() {

    try {

        // ==============================
        // GET LOGGED-IN USER
        // ==============================

        const userData =
            localStorage.getItem("loggedInUser");

        console.log("loggedInUser:", userData);

        if (!userData) {

            console.error("No logged-in user found");

            alert("Please login first.");

            window.location.href = "login.html";

            return;
        }


        // ==============================
        // PARSE USER
        // ==============================

        const user = JSON.parse(userData);

        console.log("Logged-in user:", user);


        // ==============================
        // CHECK STUDENT ROLE
        // ==============================

        if (
            user.role &&
            user.role.toUpperCase() !== "STUDENT"
        ) {

            alert("Access denied. Student access only.");

            window.location.href = "login.html";

            return;
        }


        // ==============================
        // GET STUDENT EMAIL
        // ==============================

        const email = user.email;

        if (!email) {

            throw new Error(
                "Student email not found in loggedInUser"
            );
        }

        console.log("Student email:", email);


        // ==============================
        // GET STUDENT DATA
        // ==============================

        const studentUrl =
            `${API_BASE_URL}/students/email/${encodeURIComponent(email)}`;

        console.log("Calling student API:", studentUrl);

        const studentResponse = await fetch(studentUrl);

        console.log(
            "Student API status:",
            studentResponse.status
        );

        if (!studentResponse.ok) {

            throw new Error(
                `Student API returned ${studentResponse.status}`
            );
        }

        const student = await studentResponse.json();

        console.log("Student data:", student);


        // ==============================
        // GET ALL NOTES
        // ==============================

        const notesUrl = `${API_BASE_URL}/notes`;

        console.log("Calling notes API:", notesUrl);

        const notesResponse = await fetch(notesUrl);

        console.log(
            "Notes API status:",
            notesResponse.status
        );

        if (!notesResponse.ok) {

            throw new Error(
                `Notes API returned ${notesResponse.status}`
            );
        }

        const notes = await notesResponse.json();

        console.log("Notes data:", notes);


        // ==============================
        // DISPLAY NOTES
        // ==============================

        displayNotes(notes);

    } catch (error) {

        console.error("NOTES ERROR:", error);

        const container =
            document.getElementById("notesContainer");

        if (container) {

            container.innerHTML = `
                <p class="error">
                    Unable to load notes.
                    Please check the server and try again.
                </p>
            `;
        }
    }
}


// =====================================================
// DISPLAY NOTES
// =====================================================

function displayNotes(notes) {

    const container =
        document.getElementById("notesContainer");

    if (!container) {

        console.error("notesContainer not found");

        return;
    }


    // ==============================
    // CHECK NOTES
    // ==============================

    if (!Array.isArray(notes) || notes.length === 0) {

        container.innerHTML = `
            <p class="no-notes">
                No notes available.
            </p>
        `;

        return;
    }


    // ==============================
    // CLEAR OLD NOTE CARDS
    // ==============================

    container.innerHTML = "";


    // ==============================
    // CREATE NOTE CARDS
    // ==============================

    notes.forEach(note => {

        const card = document.createElement("div");

        card.className = "note-card";


        // ------------------------------
        // NOTE TITLE
        // ------------------------------

        const title = document.createElement("h3");

        title.textContent =
            note.title ||
            note.noteTitle ||
            "Untitled Note";


        // ------------------------------
        // NOTE DESCRIPTION
        // ------------------------------

        const description = document.createElement("p");

        description.textContent =
            note.description ||
            note.content ||
            "No description available.";


        // ------------------------------
        // NOTE SUBJECT
        // ------------------------------

        const info = document.createElement("div");

        info.className = "note-info";

        const subjectName =
            note.subjectName ||
            note.subject?.subjectName ||
            note.subject ||
            "Not available";

        const subjectLabel =
            document.createElement("strong");

        subjectLabel.textContent = "Subject: ";

        info.appendChild(subjectLabel);

        info.appendChild(
            document.createTextNode(
                typeof subjectName === "string"
                    ? subjectName
                    : "Not available"
            )
        );


        // ------------------------------
        // DOWNLOAD BUTTON
        // ------------------------------

        const fileAvailable =
            note.fileUrl ||
            note.filePath ||
            note.fileName;

        if (fileAvailable) {

            const downloadButton =
                document.createElement("a");

            downloadButton.className = "download-btn";

            downloadButton.textContent =
                "Open / Download Note";

            downloadButton.target = "_blank";

            downloadButton.rel =
                "noopener noreferrer";


            // IMPORTANT:
            // Uploaded files use the Spring Boot backend.
            // Do not use the Live Server URL (port 5500).

            if (note.id != null) {

                downloadButton.href =
                    `${API_BASE_URL}/notes/${note.id}/download`;

            } else {

                // Fallback for older notes without an ID.
                // Relative paths are not treated as backend URLs.

                const fileUrl =
                    note.fileUrl || note.filePath;

                if (
                    typeof fileUrl === "string" &&
                    /^https?:\/\//i.test(fileUrl)
                ) {

                    downloadButton.href = fileUrl;

                } else {

                    downloadButton.href = "#";

                    downloadButton.addEventListener(
                        "click",
                        function (event) {

                            event.preventDefault();

                            alert(
                                "The download link is unavailable for this note."
                            );
                        }
                    );
                }
            }

            card.appendChild(title);

            card.appendChild(description);

            card.appendChild(info);

            card.appendChild(downloadButton);

        } else {

            card.appendChild(title);

            card.appendChild(description);

            card.appendChild(info);
        }


        // ------------------------------
        // ADD CARD TO PAGE
        // ------------------------------

        container.appendChild(card);

    });
}


// =====================================================
// BACK TO STUDENT DASHBOARD
// =====================================================

function goBack() {

    window.location.href = "student-dashboard.html";
}