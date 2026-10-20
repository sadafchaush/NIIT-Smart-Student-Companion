const MESSAGES_API =
    `http://${window.location.hostname}:8080/messages`;

document.addEventListener("DOMContentLoaded", () => {
    const user = getLoggedInUser();
    const role = String(user?.role || "").trim().toUpperCase();

    if (!user || !["STUDENT", "FACULTY"].includes(role)) {
        window.location.replace("login.html");
        return;
    }

    const contactSelect = document.getElementById("contactSelect");
    const contactHint = document.getElementById("contactHint");
    const conversationHeader = document.getElementById("conversationHeader");
    const messageList = document.getElementById("messageList");
    const statusMessage = document.getElementById("statusMessage");
    const messageNotification =
        document.getElementById("messageNotification");
    const unreadMessageCount =
        document.getElementById("unreadMessageCount");
    const messageForm = document.getElementById("messageForm");
    const messageInput = document.getElementById("messageInput");
    const sendButton = document.getElementById("sendButton");
    const refreshButton = document.getElementById("refreshButton");
    const dashboardLink = document.getElementById("dashboardLink");
    const dashboard = role === "STUDENT"
        ? "student-dashboard.html"
        : "faculty-dashboard.html";
    dashboardLink.href = dashboard;

    let selectedContactId = null;
    let refreshInProgress = false;
    let notificationRefreshInProgress = false;

    async function request(url, options = {}) {
        const response = await fetch(url, {
            ...options,
            credentials: "include",
            headers: {
                ...(options.body ? { "Content-Type": "application/json" } : {}),
                ...options.headers
            }
        });

        if (response.status === 401) {
            localStorage.removeItem("loggedInUser");
            localStorage.removeItem("userRole");
            window.location.replace("login.html");
            throw new Error("Your sign-in has expired. Please sign in again.");
        }

        if (!response.ok) {
            let message = `Request failed (${response.status})`;
            try {
                const errorData = await response.json();
                message = errorData.detail || errorData.message || message;
            } catch (error) {
                console.error("Could not read messaging error response:", error);
            }
            throw new Error(message);
        }

        if (response.status === 204) {
            return null;
        }
        return response.json();
    }

    function showStatus(message = "") {
        statusMessage.textContent = message;
    }

    function setConversationEnabled(enabled) {
        messageInput.disabled = !enabled;
        sendButton.disabled = !enabled;
        refreshButton.disabled = !enabled;
    }

    function renderMessages(messages) {
        messageList.replaceChildren();
        if (messages.length === 0) {
            const empty = document.createElement("p");
            empty.className = "empty-state";
            empty.textContent = "No messages yet. Send the first message.";
            messageList.append(empty);
            return;
        }

        for (const message of messages) {
            const item = document.createElement("article");
            const ownMessage = Number(message.senderId) === Number(user.id);
            item.className = ownMessage
                ? "message own-message"
                : "message";

            const author = document.createElement("p");
            author.className = "message-author";
            author.textContent = ownMessage ? "You" : message.senderName;

            const content = document.createElement("p");
            content.className = "message-content";
            content.textContent = message.content;

            const time = document.createElement("time");
            time.className = "message-time";
            const parsedDate = new Date(message.sentAt);
            time.textContent = Number.isNaN(parsedDate.getTime())
                ? ""
                : parsedDate.toLocaleString();

            item.append(author, content, time);
            messageList.append(item);
        }
        messageList.scrollTop = messageList.scrollHeight;
    }

    async function loadConversation() {
        if (!selectedContactId || refreshInProgress) {
            return;
        }
        refreshInProgress = true;
        try {
            const messages = await request(
                `${MESSAGES_API}/${encodeURIComponent(selectedContactId)}`
            );
            renderMessages(messages);
            showStatus();
            await refreshUnreadCount();
        } catch (error) {
            console.error("Unable to load conversation:", error);
            showStatus(error.message);
        } finally {
            refreshInProgress = false;
        }
    }

    async function refreshUnreadCount() {
        if (notificationRefreshInProgress) {
            return;
        }

        notificationRefreshInProgress = true;
        try {
            const count = await request(`${MESSAGES_API}/unread-count`);
            const unreadCount = Number(count);
            if (!Number.isSafeInteger(unreadCount) || unreadCount < 0) {
                throw new Error("The server returned an invalid unread message count.");
            }

            messageNotification.hidden = unreadCount === 0;
            unreadMessageCount.textContent =
                `${unreadCount} unread message${unreadCount === 1 ? "" : "s"}`;
        } catch (error) {
            console.error("Unable to refresh unread message notifications:", error);
            showStatus(error.message);
        } finally {
            notificationRefreshInProgress = false;
        }
    }

    async function loadContacts() {
        try {
            const contacts = await request(`${MESSAGES_API}/contacts`);
            contactSelect.replaceChildren();

            if (contacts.length === 0) {
                contactSelect.add(new Option("No contacts available", ""));
                contactHint.textContent =
                    "There are no users available for messaging yet.";
                setConversationEnabled(false);
                return;
            }

            contactSelect.add(new Option("Choose a contact", ""));
            for (const contact of contacts) {
                contactSelect.add(new Option(
                    `${contact.name} (${contact.role.toLowerCase()})`,
                    contact.id
                ));
            }
            contactSelect.disabled = false;
            contactHint.textContent =
                role === "STUDENT"
                    ? "Start or continue a conversation with faculty."
                    : "Start or continue a conversation with students.";
        } catch (error) {
            console.error("Unable to load message contacts:", error);
            showStatus(error.message);
            contactSelect.replaceChildren(
                new Option("Could not load contacts", "")
            );
        }
    }

    contactSelect.addEventListener("change", async () => {
        selectedContactId = contactSelect.value || null;
        const contact = contactSelect.selectedOptions[0];
        conversationHeader.querySelector("h2").textContent =
            selectedContactId ? contact.textContent : "Select a contact";
        setConversationEnabled(Boolean(selectedContactId));
        if (selectedContactId) {
            await loadConversation();
        } else {
            renderMessages([]);
        }
    });

    refreshButton.addEventListener("click", loadConversation);

    messageForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const content = messageInput.value.trim();
        if (!selectedContactId || !content) {
            return;
        }

        sendButton.disabled = true;
        showStatus();
        try {
            await request(MESSAGES_API, {
                method: "POST",
                body: JSON.stringify({
                    recipientId: Number(selectedContactId),
                    content
                })
            });
            messageInput.value = "";
            await loadConversation();
        } catch (error) {
            console.error("Unable to send message:", error);
            showStatus(error.message);
        } finally {
            sendButton.disabled = !selectedContactId;
        }
    });

    void loadContacts();
    void refreshUnreadCount();
    window.setInterval(() => {
        if (document.visibilityState === "visible") {
            void loadConversation();
            void refreshUnreadCount();
        }
    }, 5000);
});
