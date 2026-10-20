const API_URL = "http://localhost:8080/announcements";

console.log("announcement.js loaded successfully");

document.addEventListener("DOMContentLoaded", function () {
    loadAnnouncements();
});

async function loadAnnouncements() {

    const container =
        document.getElementById("announcementsContainer");

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

        const apiUrl = `${API_URL}?semester=${semester}`;

        console.log("Calling API:", apiUrl);

        const response =
            await fetch(apiUrl);

        console.log(
            "Response status:",
            response.status
        );

        if (!response.ok) {

            throw new Error(
                "Failed to load announcements. Status: " +
                response.status
            );
        }

        const announcements =
            await response.json();

        console.log(
            "Announcements received:",
            announcements
        );

        container.innerHTML = "";

        if (
            !announcements ||
            announcements.length === 0
        ) {

            container.innerHTML = `
                <p>
                    No announcements available.
                </p>
            `;

            return;
        }

        announcements.forEach(
            function (announcement) {

                const card =
                    document.createElement("div");

                card.className =
                    "announcement-card";

                card.innerHTML = `

                    <h3>
                        📢 ${announcement.title}
                    </h3>

                    <p>
                        ${announcement.message}
                    </p>

                    <p class="announcement-date">

                        📅 Date:
                        ${announcement.announcementDate}

                    </p>

                    <p>

                        Faculty ID:
                        ${announcement.facultyId}

                    </p>

                `;

                container.appendChild(card);
            }
        );

    }

    catch (error) {

        console.error(
            "Error loading announcements:",
            error
        );

        container.innerHTML = `

            <p class="error">

                Unable to load announcements.

                <br><br>

                ${error.message}

            </p>

        `;
    }
}

function goBack() {

    window.location.href =
        "student-dashboard.html";
}