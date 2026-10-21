const QUESTION_PAPERS_API =
    `http://${window.location.hostname}:8080/question-papers`;

let questionPapers = [];

document.addEventListener("DOMContentLoaded", loadQuestionPapers);

async function loadQuestionPapers() {
    const container = document.getElementById("papersContainer");
    try {
        const response = await fetch(QUESTION_PAPERS_API, {
            credentials: "include"
        });
        if (response.status === 401) {
            window.location.replace("login.html");
            return;
        }
        if (!response.ok) {
            throw new Error(`Could not load question papers (${response.status}).`);
        }
        questionPapers = await response.json();
        populateFilters();
        filterPapers();
    } catch (error) {
        console.error("Question-paper loading failed:", error);
        container.textContent = "Could not load question papers. Please try again later.";
    }
}

function populateFilters() {
    const semesterFilter = document.getElementById("semesterFilter");
    const subjectFilter = document.getElementById("subjectFilter");
    const yearFilter = document.getElementById("yearFilter");
    const semesters = [...new Set(questionPapers
        .map(paper => paper.semester)
        .filter(value => value !== null && value !== undefined))].sort((a, b) => a - b);
    const subjects = [...new Set(questionPapers
        .map(paper => paper.subject)
        .filter(Boolean))].sort();
    const years = [...new Set(questionPapers
        .map(paper => paper.year)
        .filter(Boolean))].sort((a, b) => b - a);

    addFilterOptions(semesterFilter, semesters, value => `Semester ${value}`);
    addFilterOptions(subjectFilter, subjects, value => value);
    addFilterOptions(yearFilter, years, value => value);
}

function addFilterOptions(select, values, label) {
    select.replaceChildren(select.options[0]);
    values.forEach(value => {
        const option = document.createElement("option");
        option.value = String(value);
        option.textContent = label(value);
        select.appendChild(option);
    });
}

function filterPapers() {
    const semester = document.getElementById("semesterFilter").value;
    const subject = document.getElementById("subjectFilter").value;
    const year = document.getElementById("yearFilter").value;
    displayPapers(questionPapers.filter(paper =>
        (semester === "ALL" || String(paper.semester) === semester)
        && (subject === "ALL" || paper.subject === subject)
        && (year === "ALL" || String(paper.year) === year)));
}

function displayPapers(papers) {
    const container = document.getElementById("papersContainer");
    container.replaceChildren();
    if (papers.length === 0) {
        const empty = document.createElement("div");
        empty.className = "no-results";
        empty.textContent = "No question papers found.";
        container.appendChild(empty);
        return;
    }

    papers.forEach(paper => {
        const card = document.createElement("article");
        card.className = "paper-card";
        const heading = document.createElement("h3");
        heading.textContent = paper.subject || "Question paper";
        card.appendChild(heading);
        [
            ["Semester", paper.semester ?? "N/A"],
            ["Year", paper.year],
            ["Exam", paper.exam]
        ].forEach(([label, value]) => {
            const detail = document.createElement("p");
            const strong = document.createElement("strong");
            strong.textContent = `${label}: `;
            detail.append(strong, document.createTextNode(String(value ?? "")));
            card.appendChild(detail);
        });
        const download = document.createElement("a");
        download.className = "download-btn";
        download.href = `${QUESTION_PAPERS_API}/${encodeURIComponent(paper.id)}/download`;
        download.textContent = "View / Download";
        download.rel = "noopener";
        card.appendChild(download);
        container.appendChild(card);
    });
}

function goBack() {
    window.location.href = "student-dashboard.html";
}
