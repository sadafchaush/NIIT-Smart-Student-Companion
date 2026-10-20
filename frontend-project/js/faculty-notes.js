const NOTES_API = "http://localhost:8080/notes";
const SUBJECTS_API = "http://localhost:8080/subjects";

let subjectsList = [];
let editingNoteId = null;
let editingFileUrl = "";

console.log("faculty-notes.js loaded successfully");

async function getLoggedInFacultyId() {
    const loggedInUser = localStorage.getItem("loggedInUser");
    if (!loggedInUser) return null;

    try {
        const user = JSON.parse(loggedInUser);
        if (!user || !user.email) return null;

        const response = await fetch("http://localhost:8080/faculty");
        if (!response.ok) return null;

        const facultyList = await response.json();
        const matchedFaculty = facultyList.find(function (item) {
            return item && item.email && item.email.toLowerCase() === user.email.toLowerCase();
        });

        return matchedFaculty ? Number(matchedFaculty.facultyId) : null;
    } catch (error) {
        console.error("Unable to resolve logged-in faculty ID:", error);
        return null;
    }
}

// ======================================
// PAGE LOADED
// ======================================

document.addEventListener("DOMContentLoaded", async function () {
    console.log("Faculty Notes page loaded");

    await loadSubjects();
    await loadNotes();

    const saveButton = document.getElementById("saveNoteBtn");

    if (saveButton) {
        saveButton.addEventListener("click", saveNote);
    }
});

// ======================================
// LOAD SUBJECTS
// ======================================

async function loadSubjects() {
    console.log("Loading subjects...");

    const subjectSelect = document.getElementById("subjectId");

    if (!subjectSelect) {
        console.error("ERROR: subjectId dropdown not found!");
        return;
    }

    try {
        const response = await fetch(SUBJECTS_API);

        console.log("Subject API Status:", response.status);

        if (!response.ok) {
            throw new Error("Subject API Error: " + response.status);
        }

        const subjects = await response.json();
        const facultyId = await getLoggedInFacultyId();

        console.log("Subjects received:", subjects, "facultyId:", facultyId);

        subjectsList = subjects;
        subjectSelect.innerHTML = "";

        const defaultOption = document.createElement("option");
        defaultOption.value = "";
        defaultOption.textContent = "Select Subject";

        subjectSelect.appendChild(defaultOption);

        const filteredSubjects = facultyId == null
            ? subjects
            : subjects.filter(function (subject) {
                const assignedFaculty = subject.faculty || subject.facultyId;
                const facultyValue = assignedFaculty && typeof assignedFaculty === "object"
                    ? assignedFaculty.facultyId
                    : assignedFaculty;
                return Number(facultyValue) === facultyId;
            });

        filteredSubjects.forEach(function (subject) {
            const option = document.createElement("option");

            option.value = subject.subjectId;
            option.textContent =
                subject.subjectName + " - " + subject.subjectCode;

            subjectSelect.appendChild(option);
        });

        if (filteredSubjects.length === 0) {
            const noneOption = document.createElement("option");
            noneOption.value = "";
            noneOption.textContent = "No assigned subjects found";
            subjectSelect.appendChild(noneOption);
        }

        console.log("Subjects added successfully");

    } catch (error) {
        console.error("Error loading subjects:", error);

        subjectSelect.innerHTML = "";

        const errorOption = document.createElement("option");
        errorOption.value = "";
        errorOption.textContent = "Unable to load subjects";

        subjectSelect.appendChild(errorOption);
    }
}

// ======================================
// GET SUBJECT NAME
// ======================================

function getSubjectName(subjectId) {
    const subject = subjectsList.find(function (item) {
        return Number(item.subjectId) === Number(subjectId);
    });

    if (subject) {
        return subject.subjectName + " - " + subject.subjectCode;
    }

    return "No Subject";
}

// ======================================
// GET COMPLETE FILE URL
// ======================================

function getFileUrl(fileUrl) {
    if (!fileUrl) {
        return null;
    }

    if (
        fileUrl.startsWith("http://") ||
        fileUrl.startsWith("https://")
    ) {
        return fileUrl;
    }

    if (fileUrl.startsWith("/")) {
        return "http://localhost:8080" + fileUrl;
    }

    return "http://localhost:8080/" + fileUrl;
}

// ======================================
// ESCAPE HTML
// ======================================

function escapeHtml(value) {
    if (value === null || value === undefined) {
        return "";
    }

    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// ======================================
// LOAD ALL NOTES
// ======================================

async function loadNotes() {
    const container = document.getElementById("notesContainer");

    if (!container) {
        console.error("notesContainer not found!");
        return;
    }

    try {
        console.log("Loading notes...");

        const response = await fetch(NOTES_API);

        console.log("Notes API Status:", response.status);

        if (!response.ok) {
            throw new Error("Notes API Error: " + response.status);
        }

        const notes = await response.json();

        console.log("Notes received:", notes);

        container.innerHTML = "";

        if (!notes || notes.length === 0) {
            container.innerHTML = "<p>No notes available.</p>";
            return;
        }

        notes.forEach(function (note) {
            const card = document.createElement("div");
            card.className = "note-card";

            const subjectName = getSubjectName(note.subjectId);
            const fileUrl = getFileUrl(note.fileUrl);

            let fileSection = "";

            if (fileUrl) {
                fileSection = `
                    <p>
                        <a
                            href="${fileUrl}"
                            target="_blank"
                            rel="noopener noreferrer">
                            📥 Open / Download File
                        </a>
                    </p>
                `;
            } else {
                fileSection = `<p>No file available</p>`;
            }

            card.innerHTML = `
                <h4>
                    ${escapeHtml(note.title || "Untitled")}
                </h4>

                <p>
                    <strong>Description:</strong>
                    ${escapeHtml(note.description || "No description")}
                </p>

                <p>
                    <strong>Class:</strong>
                    ${escapeHtml(note.className || "Not assigned")}
                </p>

                <p>
                    <strong>Semester:</strong>
                    ${
                        note.semester
                            ? "Semester " + escapeHtml(note.semester)
                            : "Not assigned"
                    }
                </p>

                <p>
                    <strong>Subject:</strong>
                    ${escapeHtml(subjectName)}
                </p>

                <p>
                    <strong>Faculty ID:</strong>
                    ${escapeHtml(note.facultyId || "Not assigned")}
                </p>

                ${fileSection}

                <div style="
                    margin-top: 15px;
                    display: flex;
                    gap: 10px;
                ">
                    <button
                        type="button"
                        onclick="editNote(${note.id})"
                        style="
                            background: #2563eb;
                            color: white;
                            border: none;
                            padding: 8px 15px;
                            border-radius: 5px;
                            cursor: pointer;
                        ">
                        ✏️ Edit
                    </button>

                    <button
                        type="button"
                        onclick="deleteNote(${note.id})"
                        style="
                            background: #dc2626;
                            color: white;
                            border: none;
                            padding: 8px 15px;
                            border-radius: 5px;
                            cursor: pointer;
                        ">
                        🗑️ Delete
                    </button>
                </div>
            `;

            container.appendChild(card);
        });

    } catch (error) {
        console.error("Error loading notes:", error);

        container.innerHTML = `
            <p class="error">
                Unable to load notes.
                <br>
                Please check whether Spring Boot is running.
            </p>
        `;
    }
}

// ======================================
// EDIT NOTE
// ======================================

async function editNote(id) {
    console.log("Editing note ID:", id);

    try {
        const response = await fetch(`${NOTES_API}/${id}`);

        if (!response.ok) {
            throw new Error("Unable to get note");
        }

        const note = await response.json();

        console.log("Note to edit:", note);

        editingNoteId = id;
        editingFileUrl = note.fileUrl || "";

        const titleInput = document.getElementById("title");

        if (titleInput) {
            titleInput.value = note.title || "";
        }

        const descriptionInput = document.getElementById("description");

        if (descriptionInput) {
            descriptionInput.value = note.description || "";
        }

        const classInput = document.getElementById("className");

        if (classInput) {
            classInput.value = note.className || "";
        }

        const semesterInput = document.getElementById("semester");

        if (semesterInput) {
            semesterInput.value = note.semester || "";
        }

        const subjectInput = document.getElementById("subjectId");

        if (subjectInput) {
            subjectInput.value = note.subjectId || "";
        }

        const fileInput = document.getElementById("file");

        if (fileInput) {
            fileInput.value = "";
            fileInput.required = false;
        }

        const saveButton = document.getElementById("saveNoteBtn");

        if (saveButton) {
            saveButton.textContent = "Update Note";
        }

        addCancelButton();

        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });

    } catch (error) {
        console.error("Error loading note for edit:", error);

        alert("Unable to load note for editing.");
    }
}

// ======================================
// ADD CANCEL BUTTON
// ======================================

function addCancelButton() {
    if (document.getElementById("cancelEditButton")) {
        return;
    }

    const saveButton = document.getElementById("saveNoteBtn");

    if (!saveButton) {
        return;
    }

    const cancelButton = document.createElement("button");

    cancelButton.type = "button";
    cancelButton.id = "cancelEditButton";
    cancelButton.textContent = "Cancel Edit";
    cancelButton.style.marginLeft = "10px";
    cancelButton.style.padding = "12px 25px";
    cancelButton.style.border = "none";
    cancelButton.style.borderRadius = "5px";
    cancelButton.style.cursor = "pointer";
    cancelButton.style.background = "#6b7280";
    cancelButton.style.color = "white";
    cancelButton.onclick = cancelEdit;

    saveButton.parentNode.appendChild(cancelButton);
}

// ======================================
// CANCEL EDIT
// ======================================

function cancelEdit() {
    console.log("Edit cancelled");

    editingNoteId = null;
    editingFileUrl = "";

    const noteForm = document.getElementById("noteForm");

    if (noteForm) {
        noteForm.reset();
    }

    const fileInput = document.getElementById("file");

    if (fileInput) {
        fileInput.required = true;
    }

    const saveButton = document.getElementById("saveNoteBtn");

    if (saveButton) {
        saveButton.textContent = "Upload Note";
    }

    const cancelButton = document.getElementById("cancelEditButton");

    if (cancelButton) {
        cancelButton.remove();
    }
}

// ======================================
// VALIDATE YEAR AND SEMESTER
// ======================================

function isValidYearSemester(classValue, semesterValue) {
    const match = String(classValue).match(
        /\b(1st|2nd|3rd)\s*Year\b/i
    );

    if (!match) {
        return false;
    }

    const allowedSemesters = {
        "1st": ["1", "2"],
        "2nd": ["3", "4"],
        "3rd": ["5", "6"]
    };

    return allowedSemesters[match[1].toLowerCase()]
        .includes(String(semesterValue));
}

// ======================================
// SAVE OR UPDATE NOTE
// ======================================

async function saveNote() {
    console.log("SAVE NOTE BUTTON CLICKED");

    const titleInput = document.getElementById("title");
    const descriptionInput = document.getElementById("description");
    const classInput = document.getElementById("className");
    const semesterInput = document.getElementById("semester");
    const subjectInput = document.getElementById("subjectId");
    const fileInput = document.getElementById("file");

    if (
        !titleInput ||
        !descriptionInput ||
        !classInput ||
        !semesterInput ||
        !subjectInput ||
        !fileInput
    ) {
        alert("Required form fields are missing.");

        console.error("One or more form fields were not found.");

        return;
    }

    const title = titleInput.value.trim();
    const description = descriptionInput.value.trim();
    const className = classInput.value;
    const semester = semesterInput.value;
    const subjectId = subjectInput.value;
    const file = fileInput.files[0];

    // Required-field validation

    if (!title) {
        alert("Please enter note title.");
        titleInput.focus();
        return;
    }

    if (!description) {
        alert("Please enter description.");
        descriptionInput.focus();
        return;
    }

    if (!className) {
        alert("Please select a class.");
        classInput.focus();
        return;
    }

    if (!semester) {
        alert("Please select a semester.");
        semesterInput.focus();
        return;
    }

    if (!subjectId) {
        alert("Please select a subject.");
        subjectInput.focus();
        return;
    }

    // YEAR-SEMESTER VALIDATION

    if (!isValidYearSemester(className, semester)) {
        alert(
            "Invalid semester for the selected year.\n\n" +
            "1st Year: Semester 1 or 2\n" +
            "2nd Year: Semester 3 or 4\n" +
            "3rd Year: Semester 5 or 6"
        );

        semesterInput.focus();
        return;
    }

    // ======================================
    // UPDATE EXISTING NOTE
    // ======================================

    if (editingNoteId !== null) {
        console.log("Updating note ID:", editingNoteId);

        const loggedInFacultyId = await getLoggedInFacultyId();
        const noteData = {
            title: title,
            description: description,
            fileUrl: editingFileUrl || "",
            subjectId: Number(subjectId),
            facultyId: loggedInFacultyId,
            className: className,
            semester: Number(semester)
        };

        console.log("Update data:", noteData);

        try {
            const response = await fetch(
                `${NOTES_API}/${editingNoteId}`,
                {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify(noteData)
                }
            );

            console.log("Update response:", response.status);

            if (!response.ok) {
                const errorText = await response.text();

                console.error("Update error:", errorText);

                throw new Error(
                    errorText || "Failed to update note"
                );
            }

            alert("Note updated successfully!");

            cancelEdit();

            await loadNotes();

        } catch (error) {
            console.error("Error updating note:", error);

            alert(
                "Unable to update note.\n\n" +
                error.message
            );
        }

        return;
    }

    // ======================================
    // NEW NOTE UPLOAD
    // ======================================

    if (!file) {
        alert("Please select a file from your computer.");

        fileInput.focus();

        return;
    }

    const formData = new FormData();
    const loggedInFacultyId = await getLoggedInFacultyId();

    formData.append("file", file);
    formData.append("title", title);
    formData.append("description", description);
    formData.append("className", className);
    formData.append("semester", semester);
    formData.append("subjectId", subjectId);
    formData.append("facultyId", String(loggedInFacultyId ?? ""));

    console.log("Uploading new note...");

    try {
        const response = await fetch(
            `${NOTES_API}/upload`,
            {
                method: "POST",
                // Do not manually set Content-Type for FormData.
                body: formData
            }
        );

        console.log("Upload response:", response.status);

        if (!response.ok) {
            const errorText = await response.text();

            console.error("Upload error:", errorText);

            throw new Error(
                errorText || "File upload failed"
            );
        }

        const uploadedNote = await response.json();

        console.log("Uploaded note:", uploadedNote);

        alert("Note uploaded successfully!");

        const noteForm = document.getElementById("noteForm");

        if (noteForm) {
            noteForm.reset();
        }

        fileInput.required = true;

        await loadNotes();

    } catch (error) {
        console.error("Error uploading note:", error);

        alert(
            "Unable to upload note.\n\n" +
            error.message
        );
    }
}

// ======================================
// DELETE NOTE
// ======================================

async function deleteNote(id) {
    console.log("Deleting note ID:", id);

    const confirmed = confirm(
        "Are you sure you want to delete this note?"
    );

    if (!confirmed) {
        return;
    }

    try {
        const response = await fetch(
            `${NOTES_API}/${id}`,
            {
                method: "DELETE"
            }
        );

        console.log("DELETE response:", response.status);

        if (!response.ok) {
            const errorText = await response.text();

            console.error("Delete error:", errorText);

            throw new Error(
                errorText || "Failed to delete note"
            );
        }

        alert("Note deleted successfully!");

        if (editingNoteId === id) {
            cancelEdit();
        }

        await loadNotes();

    } catch (error) {
        console.error("Error deleting note:", error);

        alert(
            "Unable to delete note.\n\n" +
            error.message
        );
    }
}

// ======================================
// BACK TO DASHBOARD
// ======================================

function goBack() {
    window.location.href = "faculty-dashboard.html";
}