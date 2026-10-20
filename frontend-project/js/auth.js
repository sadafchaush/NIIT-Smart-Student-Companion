// ======================================================
// AUTH.JS
// Common Login / Session / Role Handling
// ======================================================

const AUTH_KEY = "loggedInUser";


// ======================================================
// GET LOGGED-IN USER
// ======================================================

function getLoggedInUser() {

    const userData =
        localStorage.getItem(AUTH_KEY);

    if (!userData) {
        return null;
    }

    try {

        return JSON.parse(userData);

    } catch (error) {

        console.error(
            "Invalid session data:",
            error
        );

        localStorage.removeItem(AUTH_KEY);

        return null;
    }
}


// ======================================================
// GET USER ROLE
// ======================================================

function getUserRole() {

    const user =
        getLoggedInUser();

    if (!user || !user.role) {
        return null;
    }

    return String(user.role)
        .trim()
        .toUpperCase();
}


// ======================================================
// CHECK LOGIN
// ======================================================

function requireLogin() {

    const user =
        getLoggedInUser();

    if (!user) {

        console.warn(
            "No logged-in user."
        );

        window.location.replace(
            "login.html"
        );

        return false;
    }

    return true;
}


// ======================================================
// CHECK SPECIFIC ROLE
// ======================================================

function requireRole(requiredRole) {

    const user =
        getLoggedInUser();


    // No user logged in

    if (!user) {

        console.warn(
            "No logged-in user."
        );

        window.location.replace(
            "login.html"
        );

        return false;
    }


    // Get actual role

    const actualRole =
        String(user.role || "")
            .trim()
            .toUpperCase();


    // Required role

    const expectedRole =
        String(requiredRole || "")
            .trim()
            .toUpperCase();


    console.log(
        "Required role:",
        expectedRole
    );

    console.log(
        "Logged-in role:",
        actualRole
    );


    // Invalid role

    if (
        actualRole !== "ADMIN" &&
        actualRole !== "FACULTY" &&
        actualRole !== "STUDENT"
    ) {

        console.warn(
            "Invalid user role."
        );

        logout();

        return false;
    }


    // ==================================================
    // WRONG ROLE
    // ==================================================

    if (actualRole !== expectedRole) {

        console.warn(
            "Access denied."
        );

        console.warn(
            "Required:",
            expectedRole
        );

        console.warn(
            "Actual:",
            actualRole
        );


        alert(
            "Access denied.\n\n" +
            "This page is only available for " +
            expectedRole +
            " users."
        );


        // Send user to their OWN dashboard

        redirectAccordingToRole(
            actualRole
        );

        return false;
    }


    // ==================================================
    // ACCESS GRANTED
    // ==================================================

    console.log(
        "Access granted for:",
        actualRole
    );

    return true;
}


// ======================================================
// REDIRECT USER TO THEIR OWN DASHBOARD
// ======================================================

function redirectAccordingToRole(role) {

    const userRole =
        String(role || "")
            .trim()
            .toUpperCase();


    switch (userRole) {

        case "ADMIN":

            window.location.replace(
                "admin-dashboard.html"
            );

            break;


        case "FACULTY":

            window.location.replace(
                "faculty-dashboard.html"
            );

            break;


        case "STUDENT":

            window.location.replace(
                "student-dashboard.html"
            );

            break;


        default:

            logout();

            break;
    }
}


// ======================================================
// LOGOUT
// ======================================================

async function logout() {

    console.log(
        "Logging out..."
    );

    try {
        const response = await fetch(
            `http://${window.location.hostname}:8080/auth/logout`,
            {
                method: "POST",
                credentials: "include"
            }
        );
        if (!response.ok) {
            throw new Error(
                `Server logout failed with status ${response.status}`
            );
        }
    } catch (error) {
        console.error("Unable to end server session:", error);
        alert(
            "The server session could not be confirmed as ended. " +
            "Please close this browser if you are using a shared device."
        );
    }

    localStorage.removeItem(
        "loggedInUser"
    );

    localStorage.removeItem(
        "userRole"
    );


    window.location.replace(
        "login.html"
    );
}


// ======================================================
// PREVENT BACK BUTTON AFTER LOGOUT
// ======================================================

window.addEventListener(
    "pageshow",
    function (event) {

        if (event.persisted) {

            const user =
                getLoggedInUser();


            if (!user) {

                window.location.replace(
                    "login.html"
                );
            }
        }
    }
);