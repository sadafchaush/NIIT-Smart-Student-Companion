const API_URL = `${window.NIIT_API_BASE_URL}/announcements`;

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
                    `${window.NIIT_API_BASE_URL}/students/email/${encodeURIComponent(email)}`
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

        container.replaceChildren();

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

                const title = document.createElement("h3");
                title.textContent = `📢 ${announcement.title ?? ""}`;
                card.appendChild(title);

                const message = document.createElement("p");
                message.textContent = announcement.message ?? "";
                card.appendChild(message);

                const date = document.createElement("p");
                date.className = "announcement-date";
                date.textContent =
                    `📅 Date: ${announcement.announcementDate ?? ""}`;
                card.appendChild(date);

                const faculty = document.createElement("p");
                faculty.textContent =
                    `Faculty ID: ${announcement.facultyId ?? ""}`;
                card.appendChild(faculty);

                container.appendChild(card);
            }
        );

    }

    catch (error) {

        console.error(
            "Error loading announcements:",
            error
        );

        const errorMessage = document.createElement("p");
        errorMessage.className = "error";
        errorMessage.textContent =
            `Unable to load announcements. ${error.message}`;
        container.replaceChildren(errorMessage);
    }
}

function goBack() {

    window.location.href =
        "student-dashboard.html";
}