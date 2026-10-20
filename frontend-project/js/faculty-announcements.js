
console.log("faculty-announcements.js loaded successfully");

const API_BASE_URL = "http://localhost:8080";

async function getCurrentFaculty() {
    const userString = localStorage.getItem("loggedInUser");
    if (!userString) return null;

    try {
        const user = JSON.parse(userString);
        const facultyResponse = await fetch(`${API_BASE_URL}/faculty`);
        if (!facultyResponse.ok) return null;

        const facultyList = await facultyResponse.json();
        return facultyList.find(function (faculty) {
            return faculty && faculty.email && user.email &&
                faculty.email.toLowerCase() === user.email.toLowerCase();
        }) || null;
    } catch (error) {
        console.error("Failed to resolve faculty:", error);
        return null;
    }
}

async function getAssignedSubjects(facultyId) {
    const response = await fetch(`${API_BASE_URL}/subjects`);
    if (!response.ok) return [];

    const subjects = await response.json();
    return subjects.filter(function (subject) {
        const assignedFaculty = subject.faculty || subject.facultyId;
        const assignedFacultyId = assignedFaculty && typeof assignedFaculty === "object"
            ? assignedFaculty.facultyId
            : assignedFaculty;
        return Number(assignedFacultyId) === Number(facultyId);
    });
}

async function loadAnnouncements() {
    const container = document.getElementById("announcementsContainer");
    const faculty = await getCurrentFaculty();

    if (!container) return;
    if (!faculty) {
        container.innerHTML = "<p class='error'>Faculty information not found.</p>";
        return;
    }

    const facultyId = Number(faculty.facultyId);
    const subjectList = await getAssignedSubjects(facultyId);
    const subjectIds = subjectList.map(function (subject) {
        return Number(subject.subjectId);
    });

    const response = await fetch(`${API_BASE_URL}/announcements`);
    if (!response.ok) {
        container.innerHTML = "<p class='error'>Unable to load announcements.</p>";
        return;
    }

    const announcements = await response.json();
    const visibleAnnouncements = announcements.filter(function (announcement) {
        if (Number(announcement.facultyId) !== facultyId) return false;
        if (subjectIds.length > 0 && announcement.subjectId != null) {
            return subjectIds.includes(Number(announcement.subjectId));
        }
        return true;
    });

    if (!visibleAnnouncements.length) {
        container.innerHTML = "<div class='announcement-card'><h4>No announcements yet</h4></div>";
        return;
    }

    container.innerHTML = "";
    visibleAnnouncements.forEach(function (item) {
        const card = document.createElement("div");
        card.className = "announcement-card";
        card.innerHTML = `
            <h4>${item.title || "Announcement"}</h4>
            <p>${item.message || ""}</p>
            <small>${item.announcementDate || ""}</small>
        `;
        container.appendChild(card);
    });
}

document.addEventListener("DOMContentLoaded", async function () {
    const form = document.getElementById("announcementForm");
    const container = document.getElementById("announcementsContainer");
    const message = document.getElementById("message");
    const subjectSelect = document.getElementById("subjectId");

    window.goBack = function () {
        window.location.href = "faculty-dashboard.html";
    };

    const faculty = await getCurrentFaculty();
    if (faculty) {
        const assignedSubjects = await getAssignedSubjects(Number(faculty.facultyId));

        if (subjectSelect) {
            subjectSelect.innerHTML = '<option value="">Select Subject</option>';
            assignedSubjects.forEach(function (subject) {
                const option = document.createElement("option");
                option.value = subject.subjectId;
                option.textContent = subject.subjectName + " - " + subject.subjectCode;
                subjectSelect.appendChild(option);
            });
        }
    }

    form.addEventListener("submit", async function (event) {
        event.preventDefault();

        if (!faculty) {
            if (message) {
                message.textContent = "Faculty information not found.";
                message.className = "message error";
            }
            return;
        }

        const title = document.getElementById("title").value.trim();
        const messageText = document.getElementById("messageText").value.trim();
        const subjectId = subjectSelect ? subjectSelect.value : "";

        if (!title || !messageText || !subjectId) {
            if (message) {
                message.textContent = "Please complete all announcement fields.";
                message.className = "message error";
            }
            return;
        }

        const subjectResponse = await fetch(`${API_BASE_URL}/subjects/${subjectId}`);
        const subject = subjectResponse.ok ? await subjectResponse.json() : null;
        const payload = {
            title: title,
            message: messageText,
            announcementDate: new Date().toISOString().slice(0, 10),
            subjectId: Number(subjectId),
            semester: subject ? Number(subject.semester) : 1,
            facultyId: Number(faculty.facultyId)
        };

        const response = await fetch(`${API_BASE_URL}/announcements`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const text = await response.text();
            if (message) {
                message.textContent = text || "Announcement could not be created.";
                message.className = "message error";
            }
            return;
        }

        form.reset();
        if (message) {
            message.textContent = "Announcement created successfully.";
            message.className = "message success";
        }
        await loadAnnouncements();
    });

    if (container) {
        container.innerHTML = "<div class='announcement-card'><h4>Loading announcements...</h4></div>";
    }

    await loadAnnouncements();
});
