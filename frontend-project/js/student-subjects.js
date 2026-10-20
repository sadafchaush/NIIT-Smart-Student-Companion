
console.log("student-subjects.js loaded successfully");

const API_URL = "http://localhost:8080/subjects";

let allSubjects = [];

document.addEventListener("DOMContentLoaded", function () {
    const semesterFilter = document.getElementById("semesterFilter");

    semesterFilter.addEventListener("change", filterSubjects);

    loadSubjects();
});

async function loadSubjects() {
    const message = document.getElementById("message");
    const table = document.getElementById("subjectsTable");

    try {
        message.hidden = false;
        message.textContent = "Loading subjects...";

        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error("Unable to fetch subjects");
        }

        const subjects = await response.json();

        if (!Array.isArray(subjects)) {
            throw new Error("Invalid subjects data");
        }

        allSubjects = subjects;

        filterSubjects();

    } catch (error) {
        console.error("Error loading subjects:", error);

        message.hidden = false;
        message.textContent =
            "Unable to load subjects. Please check whether Spring Boot is running.";

        message.className = "error";
        table.hidden = true;
    }
}

function filterSubjects() {
    const semesterFilter = document.getElementById("semesterFilter");
    const selectedSemester = semesterFilter.value;

    let filteredSubjects = allSubjects;

    if (selectedSemester !== "all") {
        filteredSubjects = allSubjects.filter(function (subject) {
            return String(subject.semester) === selectedSemester;
        });
    }

    displaySubjects(filteredSubjects);
}

function displaySubjects(subjects) {
    const message = document.getElementById("message");
    const table = document.getElementById("subjectsTable");
    const tbody = document.getElementById("subjectsBody");

    tbody.innerHTML = "";

    if (subjects.length === 0) {
        message.hidden = false;
        message.textContent = "No subjects found for this semester.";
        message.className = "";
        table.hidden = true;
        return;
    }

    subjects.forEach(function (subject) {
        const row = document.createElement("tr");

        const values = [
            subject.subjectId ?? subject.id ?? "",
            subject.subjectName ?? subject.name ?? "",
            subject.subjectCode ?? subject.code ?? "",
            subject.semester ?? "",
            subject.faculty?.fullName ?? "Not assigned"
        ];

        values.forEach(function (value) {
            const cell = document.createElement("td");
            cell.textContent = value;
            row.appendChild(cell);
        });

        tbody.appendChild(row);
    });

    message.hidden = true;
    table.hidden = false;
}

function goBack() {
    window.location.href = "student-dashboard.html";
}
