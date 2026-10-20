const API_BASE_URL =
    "http://localhost:8080";

console.log(
    "syllabus.js loaded successfully"
);


document.addEventListener(
    "DOMContentLoaded",
    function () {

        loadSyllabus();

    }
);


async function loadSyllabus() {

    const container =
        document.getElementById(
            "syllabusContainer"
        );

    try {

        const response =
            await fetch(
                `${API_BASE_URL}/syllabus`
            );


        console.log(
            "Syllabus API status:",
            response.status
        );


        if (!response.ok) {

            throw new Error(
                "Unable to load syllabus"
            );
        }


        const syllabusList =
            await response.json();


        console.log(
            "Syllabus data:",
            syllabusList
        );


        container.innerHTML = "";


        if (
            !syllabusList ||
            syllabusList.length === 0
        ) {

            container.innerHTML = `
                <div class="no-data">
                    No syllabus available.
                </div>
            `;

            return;
        }


        syllabusList.forEach(
            function (syllabus) {

                const card =
                    document.createElement(
                        "div"
                    );

                card.className =
                    "syllabus-card";


                const subject =
                    syllabus.subject;


                const subjectName =
                    subject &&
                    subject.subjectName
                        ? subject.subjectName
                        : "Unknown Subject";


                const subjectCode =
                    subject &&
                    subject.subjectCode
                        ? subject.subjectCode
                        : "";


                const semester =
                    subject &&
                    subject.semester
                        ? subject.semester
                        : "";


                let fileButton = "";


                if (syllabus.fileUrl) {

                    fileButton = `
                        <a
                            href="${syllabus.fileUrl}"
                            target="_blank"
                            class="view-btn">

                            View Syllabus

                        </a>
                    `;

                }


                card.innerHTML = `

                    <h3>
                        ${subjectName}
                    </h3>

                    <div class="subject-info">

                        <strong>
                            Subject Code:
                        </strong>

                        ${subjectCode}

                    </div>

                    <div class="subject-info">

                        <strong>
                            Semester:
                        </strong>

                        ${semester}

                    </div>

                    <p class="description">

                        ${
                            syllabus.description
                            || "No description available."
                        }

                    </p>

                    ${fileButton}

                `;


                container.appendChild(card);

            }
        );


    } catch (error) {

        console.error(
            "Syllabus Error:",
            error
        );


        container.innerHTML = `

            <div class="error">

                Unable to load syllabus.

                <br><br>

                ${error.message}

            </div>

        `;

    }

}


function goBack() {

    window.location.href =
        "student-dashboard.html";

}