
const API_BASE_URL = "http://localhost:8080";

let assignments = [];
let submissions = [];
let subjects = [];
let loggedInStudentId = null;
let loggedInStudentSemester = null;

// ========================================
// PAGE LOAD
// ========================================

document.addEventListener("DOMContentLoaded", async function () {
    const studentLoaded = await loadLoggedInStudent();

    if (!studentLoaded) {
        return;
    }

    await loadSubjects();
    await loadAssignments();
});

// ========================================
// LOAD LOGGED-IN STUDENT
// ========================================

async function loadLoggedInStudent() {
    const userData = localStorage.getItem("loggedInUser");

    if (!userData) {
        window.location.href = "login.html";
        return false;
    }

    try {
        const user = JSON.parse(userData);
        const email = user.email;

        if (!email) {
            throw new Error("Student email not found");
        }

        const response = await fetch(
            `${API_BASE_URL}/students/email/` +
            encodeURIComponent(email)
        );

        if (!response.ok) {
            throw new Error("Student details not found");
        }

        const student = await response.json();

        console.log("Logged-in student:", student);

        loggedInStudentId = student.studentId || student.id;
        loggedInStudentSemester = student.semester ?? 1;

        if (!loggedInStudentId) {
            throw new Error("Student ID not found");
        }

        localStorage.setItem("studentId", loggedInStudentId);

        return true;

    } catch (error) {
        console.error("Error loading logged-in student:", error);
        showMessage("Unable to identify logged-in student.", "error");
        return false;
    }
}

// ========================================
// LOAD SUBJECTS
// ========================================

async function loadSubjects() {
    try {
        const response = await fetch(`${API_BASE_URL}/subjects`);

        if (!response.ok) {
            throw new Error("Unable to load subjects");
        }

        subjects = await response.json();

    } catch (error) {
        console.error("Subject loading error:", error);
        subjects = [];
    }
}

// ========================================
// GET SUBJECT NAME
// ========================================

function getSubjectName(subjectId) {
    const subject = subjects.find(function (item) {
        return Number(item.subjectId) === Number(subjectId);
    });

    return subject
        ? subject.subjectName
        : "Subject information unavailable";
}

// ========================================
// SAFE HTML TEXT
// ========================================

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

// ========================================
// LOAD ASSIGNMENTS
// ========================================

async function loadAssignments() {
    const container = document.getElementById("assignmentContainer");

    try {
        container.innerHTML = '<p class="loading">Loading assignments...</p>';

        const response = await fetch(
            `${API_BASE_URL}/assignments?semester=${loggedInStudentSemester}`
        );

        if (!response.ok) {
            throw new Error("Unable to load assignments");
        }

        assignments = await response.json();

        await loadSubmissions(loggedInStudentId);

        displayAssignments();

    } catch (error) {
        console.error("Error loading assignments:", error);

        container.innerHTML = `
            <p class="message error">
                Unable to load assignments.
                Please check whether Spring Boot is running.
            </p>
        `;
    }
}

// ========================================
// LOAD STUDENT SUBMISSIONS
// ========================================

async function loadSubmissions(studentId) {
    try {
        const response = await fetch(
            `${API_BASE_URL}/submissions/student/${studentId}`
        );

        if (!response.ok) {
            submissions = [];
            return;
        }

        submissions = await response.json();

    } catch (error) {
        console.error("Unable to load submissions:", error);
        submissions = [];
    }
}

// ========================================
// DISPLAY ASSIGNMENTS
// ========================================

function displayAssignments() {
    const container = document.getElementById("assignmentContainer");

    if (!assignments || assignments.length === 0) {
        container.innerHTML = `
            <p class="loading">No assignments available.</p>
        `;
        return;
    }

    container.innerHTML = "";

    assignments.forEach(function (assignment) {
        const submission = submissions.find(function (sub) {
            return Number(sub.assignmentId) === Number(assignment.id);
        });

        const isSubmitted = submission !== undefined;

        const title = assignment.title || "Untitled Assignment";
        const description = assignment.description || "No description";
        const dueDate = assignment.dueDate || "No due date";
        const className = assignment.className || "Not specified";
        const semester = assignment.semester;
        const subjectName = getSubjectName(assignment.subjectId);

        const card = document.createElement("div");
        card.className = "assignment-card";

        card.innerHTML = `
            <h2>${escapeHtml(title)}</h2>

            <p>${escapeHtml(description)}</p>

            <p>
                <strong>Class / Year:</strong>
                ${escapeHtml(className)}
            </p>

            <p>
                <strong>Semester:</strong>
                ${semester
                    ? escapeHtml(semester)
                    : "Not specified"}
            </p>

            <p>
                <strong>Subject:</strong>
                ${escapeHtml(subjectName)}
            </p>

            <p class="due-date">
                <strong>Deadline:</strong>
                ${escapeHtml(dueDate)}
            </p>

            <div class="
                status
                ${isSubmitted ? "submitted" : "pending"}
            ">
                Status:
                ${isSubmitted ? "Submitted" : "Not Submitted"}
            </div>

            <button
                type="button"
                class="submit-work-btn"
                ${isSubmitted ? "disabled" : ""}
            >
                ${isSubmitted
                    ? "Already Submitted"
                    : "Submit Assignment"}
            </button>
        `;

        const button = card.querySelector(".submit-work-btn");

        if (!isSubmitted) {
            button.addEventListener("click", function () {
                openSubmitModal(assignment.id, title);
            });
        }

        container.appendChild(card);
    });
}

// ========================================
// OPEN SUBMIT MODAL
// ========================================

function openSubmitModal(assignmentId, assignmentTitle) {
    document.getElementById("submitModal").style.display = "block";
    document.getElementById("assignmentId").value = assignmentId;
    document.getElementById("studentId").value = loggedInStudentId;
    document.getElementById("selectedAssignment").innerText =
        "Assignment: " + assignmentTitle;
    document.getElementById("fileUrl").value = "";
}

// ========================================
// CLOSE MODAL
// ========================================

function closeModal() {
    document.getElementById("submitModal").style.display = "none";
}

// ========================================
// SUBMIT ASSIGNMENT
// ========================================

async function submitAssignment() {
    const assignmentId =
        document.getElementById("assignmentId").value;

    const fileUrl =
        document.getElementById("fileUrl").value.trim();

    if (!assignmentId || !loggedInStudentId || !fileUrl) {
        showMessage("Please enter your assignment file URL.", "error");
        return;
    }

    const submission = {
        assignmentId: Number(assignmentId),
        studentId: Number(loggedInStudentId),
        fileUrl: fileUrl
    };

    try {
        const response = await fetch(
            `${API_BASE_URL}/submissions`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(submission)
            }
        );

        const text = await response.text();

        let result;

        try {
            result = JSON.parse(text);
        } catch {
            result = text;
        }

        if (!response.ok) {
            showMessage(
                typeof result === "string"
                    ? result
                    : "Unable to submit assignment.",
                "error"
            );
            return;
        }

        showMessage("Assignment submitted successfully!", "success");
        closeModal();

        await loadAssignments();

    } catch (error) {
        console.error("Submission error:", error);

        showMessage(
            "Unable to submit assignment. Check whether Spring Boot is running.",
            "error"
        );
    }
}

// ========================================
// SHOW MESSAGE
// ========================================

function showMessage(text, type) {
    const message = document.getElementById("message");

    message.innerText = text;
    message.className = "message " + type;

    setTimeout(function () {
        message.className = "message";
    }, 4000);
}

// ========================================
// CLOSE MODAL WHEN CLICKING OUTSIDE
// ========================================

window.addEventListener("click", function (event) {
    const modal = document.getElementById("submitModal");

    if (event.target === modal) {
        closeModal();
    }
});

// ========================================
// BACK TO DASHBOARD
// ========================================

function goBack() {
    window.location.href = "student-dashboard.html";
}
