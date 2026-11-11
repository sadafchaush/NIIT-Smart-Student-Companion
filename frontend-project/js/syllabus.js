const API_BASE_URL = window.NIIT_API_BASE_URL;

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


        container.replaceChildren();


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


                const heading = document.createElement("h3");
                heading.textContent = subjectName;
                card.appendChild(heading);

                [
                    ["Subject Code:", subjectCode],
                    ["Semester:", semester]
                ].forEach(([label, value]) => {
                    const info = document.createElement("div");
                    info.className = "subject-info";
                    const strong = document.createElement("strong");
                    strong.textContent = label + " ";
                    info.appendChild(strong);
                    info.appendChild(
                        document.createTextNode(value == null ? "" : String(value))
                    );
                    card.appendChild(info);
                });

                const description = document.createElement("p");
                description.className = "description";
                description.textContent =
                    syllabus.description || "No description available.";
                card.appendChild(description);

                if (syllabus.fileUrl) {
                    try {
                        const fileUrl = new URL(
                            syllabus.fileUrl,
                            API_BASE_URL
                        );
                        if (["http:", "https:"].includes(fileUrl.protocol)) {
                            const link = document.createElement("a");
                            link.href = fileUrl.href;
                            link.target = "_blank";
                            link.rel = "noopener noreferrer";
                            link.className = "view-btn";
                            link.textContent = "View Syllabus";
                            card.appendChild(link);
                        }
                    } catch (error) {
                        console.error("Invalid syllabus URL:", error);
                    }
                }


                container.appendChild(card);

            }
        );


    } catch (error) {

        console.error(
            "Syllabus Error:",
            error
        );


        const errorMessage = document.createElement("div");
        errorMessage.className = "error";
        errorMessage.textContent =
            `Unable to load syllabus. ${error.message}`;
        container.replaceChildren(errorMessage);

    }

}


function goBack() {

    window.location.href =
        "student-dashboard.html";

}