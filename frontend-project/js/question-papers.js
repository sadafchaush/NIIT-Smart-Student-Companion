const questionPapers = [];

function displayPapers(papers) {


const container = document.getElementById("papersContainer");

container.innerHTML = "";

if (papers.length === 0) {

    container.innerHTML = `
        <div class="no-results">
            No question papers found.
        </div>
    `;

    return;
}


papers.forEach(paper => {

    const card = document.createElement("div");

    card.className = "paper-card";

    card.innerHTML = `

        <h3>${paper.subject}</h3>

        <p>
            <strong>Semester:</strong>
            ${paper.semester}
        </p>

        <p>
            <strong>Year:</strong>
            ${paper.year}
        </p>

        <p>
            <strong>Exam:</strong>
            ${paper.exam}
        </p>

        <a href="${paper.file}"
           class="download-btn"
           target="_blank"
           onclick="downloadPaper(event, '${paper.file}')">
            View / Download
        </a>

    `;

    container.appendChild(card);

});


}

function filterPapers() {

const semester =
    document.getElementById("semesterFilter").value;

const subject =
    document.getElementById("subjectFilter").value;

const year =
    document.getElementById("yearFilter").value;


const filteredPapers = questionPapers.filter(paper => {

    const semesterMatch =
        semester === "ALL" ||
        paper.semester === semester;

    const subjectMatch =
        subject === "ALL" ||
        paper.subject === subject;

    const yearMatch =
        year === "ALL" ||
        paper.year === year;

    return semesterMatch &&
           subjectMatch &&
           yearMatch;

});


displayPapers(filteredPapers);


}

function downloadPaper(event, file) {


if (file === "#") {

    event.preventDefault();

    alert(
        "Question paper file will be connected to the backend in the next step."
    );
}


}

function goBack() {


window.location.href = "student-dashboard.html";

}

displayPapers(questionPapers);
