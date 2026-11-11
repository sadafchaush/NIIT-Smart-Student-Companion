(() => {
    const allowedTargets = new Set([
        "notes.html",
        "assignment.html",
        "attendance.html",
        "announcement.html",
        "messages.html",
        "faculty-notes.html",
        "faculty-assignments.html",
        "faculty-attendance.html",
        "faculty-announcements.html",
        "faculty-submissions.html"
    ]);

    document.addEventListener("DOMContentLoaded", () => {
        if (typeof getUserRole !== "function"
                || !["STUDENT", "FACULTY"].includes(getUserRole())) {
            return;
        }

        const toggle = document.getElementById("notificationToggle");
        const dropdown = document.getElementById("notificationDropdown");
        const list = document.getElementById("notificationList");
        const count = document.getElementById("notificationCount");
        if (!toggle || !dropdown || !list || !count) {
            return;
        }

        let notifications = [];

        function setOpen(open) {
            dropdown.hidden = !open;
            toggle.setAttribute("aria-expanded", String(open));
        }

        function updateCount() {
            const unread = notifications.filter(item => !item.read).length;
            count.hidden = unread === 0;
            count.textContent = unread > 99 ? "99+" : String(unread);
            toggle.setAttribute(
                "aria-label",
                unread ? `Notifications, ${unread} unread` : "Notifications"
            );
        }

        function showMessage(message) {
            list.replaceChildren();
            const item = document.createElement("li");
            item.className = "notification-empty";
            item.textContent = message;
            list.append(item);
        }

        function renderNotifications() {
            list.replaceChildren();
            if (notifications.length === 0) {
                showMessage("You're all caught up.");
                return;
            }

            for (const notification of notifications) {
                const item = document.createElement("li");
                const button = document.createElement("button");
                button.type = "button";
                button.className = `notification-item${notification.read ? "" : " unread"}`;

                const title = document.createElement("strong");
                title.textContent = notification.title;
                const message = document.createElement("span");
                message.textContent = notification.message;
                button.append(title, message);

                if (notification.createdAt) {
                    const time = document.createElement("time");
                    time.dateTime = notification.createdAt;
                    const date = new Date(notification.createdAt);
                    time.textContent = Number.isNaN(date.getTime())
                        ? ""
                        : date.toLocaleString();
                    button.append(time);
                }

                button.addEventListener("click", async () => {
                    if (!allowedTargets.has(notification.targetUrl)) {
                        showMessage("This notification has an invalid destination.");
                        return;
                    }
                    if (!notification.read) {
                        button.disabled = true;
                        try {
                            const response = await fetch(
                                `${window.NIIT_API_BASE_URL}/notifications/${notification.id}/read`,
                                { method: "PATCH" }
                            );
                            if (!response.ok) {
                                throw new Error(
                                    `Could not mark notification as read (HTTP ${response.status}).`
                                );
                            }
                            notification.read = true;
                            updateCount();
                        } catch (error) {
                            console.error("Could not update notification:", error);
                            button.disabled = false;
                            showMessage(
                                error.message || "Could not update notification. Please try again."
                            );
                            return;
                        }
                    }
                    window.location.href = notification.targetUrl;
                });

                item.append(button);
                list.append(item);
            }
        }

        async function loadNotifications() {
            showMessage("Loading notifications...");
            try {
                const response = await fetch(
                    `${window.NIIT_API_BASE_URL}/notifications`
                );
                if (!response.ok) {
                    throw new Error(`Could not load notifications (HTTP ${response.status}).`);
                }
                notifications = await response.json();
                if (!Array.isArray(notifications)) {
                    throw new Error("The notifications response was invalid.");
                }
                updateCount();
                renderNotifications();
            } catch (error) {
                console.error("Could not load notifications:", error);
                showMessage(
                    error.message || "Could not load notifications. Please refresh the page."
                );
            }
        }

        toggle.addEventListener("click", () => {
            const open = dropdown.hidden;
            setOpen(open);
            if (open) {
                loadNotifications();
            }
        });

        document.addEventListener("click", event => {
            if (!event.target.closest(".notification-wrap")) {
                setOpen(false);
            }
        });

        document.addEventListener("keydown", event => {
            if (event.key === "Escape") {
                setOpen(false);
                toggle.focus();
            }
        });

        loadNotifications();
    });
})();
