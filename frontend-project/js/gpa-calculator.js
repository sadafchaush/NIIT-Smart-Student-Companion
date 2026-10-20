console.log("GPA Calculator JS loaded successfully");


/* Add Subject */

function addSubject() {

    const tableBody = document.getElementById("subjectTableBody");

    const row = document.createElement("tr");

    row.innerHTML = `
        <td>
            <input type="text"
                   class="subject"
                   placeholder="Subject name">
        </td>

        <td>
            <input type="number"
                   class="credits"
                   min="1"
                   max="10"
                   placeholder="Credits">
        </td>

        <td>
            <select class="grade">
                <option value="">Select Grade</option>
                <option value="10">O</option>
                <option value="9">A+</option>
                <option value="8">A</option>
                <option value="7">B+</option>
                <option value="6">B</option>
                <option value="5">C</option>
                <option value="4">D</option>
                <option value="0">F</option>
            </select>
        </td>

        <td>
            <span class="grade-point">-</span>
        </td>

        <td>
            <button class="remove-btn"
                    onclick="removeSubject(this)">
                Remove
            </button>
        </td>
    `;

    tableBody.appendChild(row);

    updateGradePoints();
}


/* Remove Subject */

function removeSubject(button) {

    const row = button.closest("tr");

    const tableBody = document.getElementById("subjectTableBody");

    if (tableBody.rows.length === 1) {

        alert("At least one subject is required.");

        return;
    }

    row.remove();

    updateGradePoints();
}


/* Update Grade Points */

function updateGradePoints() {

    const rows = document.querySelectorAll("#subjectTableBody tr");

    rows.forEach(function(row) {

        const grade = row.querySelector(".grade");

        const gradePoint = row.querySelector(".grade-point");

        if (grade.value === "") {

            gradePoint.textContent = "-";

        } else {

            gradePoint.textContent = grade.value;

        }

    });
}


/* Grade Change Event */

document.addEventListener("change", function(event) {

    if (event.target.classList.contains("grade")) {

        updateGradePoints();

    }

});


/* Calculate GPA */

function calculateGPA() {

    const rows = document.querySelectorAll("#subjectTableBody tr");

    let totalCredits = 0;

    let totalPoints = 0;

    let valid = true;


    rows.forEach(function(row) {

        const subject = row.querySelector(".subject").value.trim();

        const credits = parseFloat(
            row.querySelector(".credits").value
        );

        const grade = row.querySelector(".grade").value;


        if (
            subject === "" ||
            isNaN(credits) ||
            credits <= 0 ||
            grade === ""
        ) {

            valid = false;

        }


        if (valid) {

            totalCredits += credits;

            totalPoints += credits * parseFloat(grade);

        }

    });


    if (!valid) {

        alert(
            "Please enter subject name, valid credits and grade for every subject."
        );

        return;
    }


    if (totalCredits === 0) {

        alert("Please enter valid subject details.");

        return;
    }


    const gpa = totalPoints / totalCredits;

    const finalGPA = gpa.toFixed(2);


    document.getElementById("gpaValue").textContent = finalGPA;

    document.getElementById("result").classList.remove("hidden");


    let message = "";

    if (gpa >= 9) {

        message = "Excellent academic performance.";

    } else if (gpa >= 8) {

        message = "Very good academic performance.";

    } else if (gpa >= 7) {

        message = "Good academic performance.";

    } else if (gpa >= 6) {

        message = "Satisfactory academic performance.";

    } else if (gpa >= 4) {

        message = "You have passed, but there is room for improvement.";

    } else {

        message = "Please review your failed subjects.";

    }


    document.getElementById("gpaMessage").textContent = message;

}


/* Reset */

function resetCalculator() {

    const tableBody = document.getElementById("subjectTableBody");

    tableBody.innerHTML = `
        <tr>
            <td>
                <input type="text"
                       class="subject"
                       placeholder="Subject name">
            </td>

            <td>
                <input type="number"
                       class="credits"
                       min="1"
                       max="10"
                       placeholder="Credits">
            </td>

            <td>
                <select class="grade">
                    <option value="">Select Grade</option>
                    <option value="10">O</option>
                    <option value="9">A+</option>
                    <option value="8">A</option>
                    <option value="7">B+</option>
                    <option value="6">B</option>
                    <option value="5">C</option>
                    <option value="4">D</option>
                    <option value="0">F</option>
                </select>
            </td>

            <td>
                <span class="grade-point">-</span>
            </td>

            <td>
                <button class="remove-btn"
                        onclick="removeSubject(this)">
                    Remove
                </button>
            </td>
        </tr>
    `;


    document.getElementById("result").classList.add("hidden");

    document.getElementById("gpaValue").textContent = "0.00";

    document.getElementById("gpaMessage").textContent = "";

}


/* Back Button */

function goBack() {

    window.history.back();

}