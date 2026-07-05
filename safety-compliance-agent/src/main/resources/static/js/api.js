// const BASE_URL = "http://localhost:8080/api";
const BASE_URL = window.location.origin + "/api";
/* =========================================
   PAGE ROUTER — runs the right function 
   based on which page is currently loaded
========================================= */
window.onload = () => {
    checkLoginOrRedirect();

    const page = window.location.pathname;

    if (page.includes("chat.html")) {
        // Chat page ready
    }
    if (page.includes("incident.html")) {
        loadMachinesForDropdown();
    }
    if (page.includes("dashboard.html")) {
        loadDashboard();
    }
    if (page.includes("documents.html")) {
        loadDocuments();
    }
};

/* =========================================
   CHAT ASSISTANT (chat.html)
========================================= */
async function sendMessage() {
    const input = document.getElementById("questionInput");
    const question = input.value.trim();
    if (!question) return;

    const companyName = sessionStorage.getItem("companyName");
    const chatWindow = document.getElementById("chatWindow");

    chatWindow.innerHTML += `<div class="message user">${question}</div>`;
    input.value = "";

    const loadingId = "loading-" + Date.now();
    chatWindow.innerHTML += `<div class="message ai loading" id="${loadingId}">Thinking...</div>`;
    chatWindow.scrollTop = chatWindow.scrollHeight;

    try {
        const response = await fetch(`${BASE_URL}/chat`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ question, companyName })
        });
        const data = await response.json();

        document.getElementById(loadingId).remove();

        const sourcesHtml = data.sources && data.sources.length > 0
            ? `<div class="sources">📄 Source: ${data.sources[0].substring(0, 80)}...</div>`
            : "";

        chatWindow.innerHTML += `<div class="message ai">${data.answer}${sourcesHtml}</div>`;
        chatWindow.scrollTop = chatWindow.scrollHeight;
    } catch (err) {
        document.getElementById(loadingId).innerText = "⚠️ Error getting response. Please try again.";
    }
}

function askQuick(question) {
    if (!question) return;
    document.getElementById("questionInput").value = question;
    sendMessage();
    document.getElementById("quickQuestionSelect").value = "";
}

/* =========================================
   INCIDENT REPORTING (incident.html)
========================================= */
let currentMachinePPE = [];

async function loadMachinesForDropdown() {
    const companyName = sessionStorage.getItem("companyName");
    if (!companyName) return;

    try {
        const response = await fetch(`${BASE_URL}/machines?companyName=${encodeURIComponent(companyName)}`);
        const machines = await response.json();

        const select = document.getElementById("machineSelect");
        machines.forEach(m => {
            const option = document.createElement("option");
            option.value = m.id;
            option.textContent = `${m.name} (${m.location || "N/A"})`;
            option.dataset.notes = m.safetyNotes || "";
            select.appendChild(option);
        });

        // Whenever a machine is selected, render its PPE checklist
        select.addEventListener("change", () => {
            const selectedOption = select.options[select.selectedIndex];
            const notes = selectedOption ? selectedOption.dataset.notes : "";
            renderPPEChecklist(notes);
        });
    } catch (err) {
        console.error("Failed to load machines", err);
    }
}

// Renders the PPE checklist for the currently selected machine
function renderPPEChecklist(notes) {
    const ppeList = document.getElementById("ppeList");
    const ppeItems = document.getElementById("ppeItems");
    if (!ppeList || !ppeItems) return;

    ppeItems.innerHTML = "";

    const items = (notes || "").split(",").map(s => s.trim()).filter(Boolean);
    if (items.length === 0) {
        currentMachinePPE = [];
        ppeList.style.display = "none";
        return;
    }

    currentMachinePPE = items;
    items.forEach((item, idx) => {
        ppeItems.innerHTML += `
            <div class="ppe-item">
                <input type="checkbox" id="ppe-${idx}" class="ppe-checkbox">
                <label for="ppe-${idx}" style="margin:0;">${item}</label>
            </div>`;
    });
    ppeList.style.display = "block";
}

function allPPEChecked() {
    const checkboxes = document.querySelectorAll(".ppe-checkbox");
    if (checkboxes.length === 0) return true; // no PPE required for this machine
    return Array.from(checkboxes).every(cb => cb.checked);
}

// Voice Input using Web Speech API
// Voice Input using Web Speech API
function startVoiceInput() {
    if (!('webkitSpeechRecognition' in window)) {
        showToast("Not Supported", "Voice input is not supported in this browser. Try Chrome.", "error");
        return;
    }

    const lang = document.getElementById("voiceLang").value; // ta-IN or en-IN
    const micBtn = document.getElementById("micBtn");

    const recognition = new webkitSpeechRecognition();
    recognition.lang = lang;
    recognition.interimResults = false;

    recognition.onstart = () => {
        micBtn.classList.add("listening");
        micBtn.innerText = "🔴";
        document.getElementById("incidentDesc").placeholder = "🎤 Listening...";
    };

    recognition.onresult = function (event) {
        const transcript = event.results[0][0].transcript;
        document.getElementById("incidentDesc").value = transcript;
    };

    recognition.onerror = function (event) {
        if (event.error === "no-speech") {
            showToast("Not Recorded", "No speech detected. Please try again.", "error");
        } else if (event.error === "not-allowed" || event.error === "permission-denied") {
            showToast("Mic Access Denied", "Please allow microphone access and try again.", "error");
        } else {
            showToast("Voice Input Error", "Please try again or type manually.", "error");
        }
    };

    recognition.onend = () => {
        micBtn.classList.remove("listening");
        micBtn.innerText = "🎤";
        document.getElementById("incidentDesc").placeholder = "Describe what happened...";
    };

    recognition.start();
}

async function submitIncident() {
    const description = document.getElementById("incidentDesc").value.trim();
    const reporterName = document.getElementById("reporterName").value.trim();
    const machineSelect = document.getElementById("machineSelect");
    const machineId = machineSelect.value;
    const ppeWarning = document.getElementById("ppeWarning");
    const companyName = sessionStorage.getItem("companyName");
    const reporterPhone = sessionStorage.getItem("userPhone");
    const reporterEmail = sessionStorage.getItem("userEmail");

    if (!description || !reporterName) {
        showToast("Missing Fields", "Please fill in the description and your name.", "error");
        return;
    }

    if (!allPPEChecked()) {
        ppeWarning.style.display = "block";
        return;
    }
    ppeWarning.style.display = "none";

    // NOTE: only ONE payload declaration — this was duplicated before and caused a SyntaxError
    const payload = { description, reporterName, companyName, reporterPhone, reporterEmail };
    if (machineId) {
        payload.machine = { id: parseInt(machineId) };
    }

    try {
        const response = await fetch(`${BASE_URL}/incidents`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        const data = await response.json();

        document.getElementById("resultSeverity").innerHTML =
            `<span class="badge badge-${data.severity}">${data.severity}</span>`;
        document.getElementById("resultCategory").innerText = data.category;
        document.getElementById("resultAction").innerText = data.recommendedAction;
        document.getElementById("resultBox").style.display = "block";

        showToast("Incident Submitted", "AI classification complete.");

        document.getElementById("incidentDesc").value = "";
        document.getElementById("reporterName").value = "";
    } catch (err) {
        showToast("Error", "Could not submit incident. Please try again.", "error");
        console.error(err);
    }
}

/* =========================================
   DASHBOARD (dashboard.html)
========================================= */
async function loadDashboard() {
    const companyName = sessionStorage.getItem("companyName");
    if (!companyName) return;

    try {
        const [incidentsRes, machinesRes, documentsRes] = await Promise.all([
            fetch(`${BASE_URL}/incidents?companyName=${encodeURIComponent(companyName)}`),
            fetch(`${BASE_URL}/machines?companyName=${encodeURIComponent(companyName)}`),
            fetch(`${BASE_URL}/documents?companyName=${encodeURIComponent(companyName)}`)
        ]);

        const incidents = await incidentsRes.json();
        const machines = await machinesRes.json();
        const documents = await documentsRes.json();

        // Stats
        document.getElementById("totalIncidents").innerText = incidents.length;
        document.getElementById("highSeverityCount").innerText =
            incidents.filter(i => i.severity === "high").length;
        document.getElementById("totalMachines").innerText = machines.length;
        document.getElementById("totalDocuments").innerText = documents.length;

        // Severity chart
        const severityCounts = { low: 0, medium: 0, high: 0 };
        incidents.forEach(i => {
            if (severityCounts[i.severity] !== undefined) severityCounts[i.severity]++;
        });

        new Chart(document.getElementById("severityChart"), {
            type: 'bar',
            data: {
                labels: ['Low', 'Medium', 'High'],
                datasets: [{
                    label: 'Incidents',
                    data: [severityCounts.low, severityCounts.medium, severityCounts.high],
                    backgroundColor: ['#22c55e', '#f59e0b', '#ef4444']
                }]
            },
            options: { plugins: { legend: { display: false } } }
        });

        // Category chart
        const categoryCounts = {};
        incidents.forEach(i => {
            const cat = i.category || "other";
            categoryCounts[cat] = (categoryCounts[cat] || 0) + 1;
        });

        new Chart(document.getElementById("categoryChart"), {
            type: 'pie',
            data: {
                labels: Object.keys(categoryCounts),
                datasets: [{
                    data: Object.values(categoryCounts),
                    backgroundColor: ['#7c3aed', '#22d3ee', '#f59e0b', '#ef4444', '#22c55e', '#eab308']
                }]
            }
        });

        // Machine-wise risk ranking (top 5)
        const machineCounts = {};
        incidents.forEach(i => {
            const machineName = (i.machine && i.machine.name) ? i.machine.name : "Unassigned";
            machineCounts[machineName] = (machineCounts[machineName] || 0) + 1;
        });

        const topMachines = Object.entries(machineCounts)
            .sort((a, b) => b[1] - a[1])
            .slice(0, 5);

        new Chart(document.getElementById("machineRiskChart"), {
            type: 'bar',
            data: {
                labels: topMachines.map(m => m[0]),
                datasets: [{
                    label: 'Incidents',
                    data: topMachines.map(m => m[1]),
                    backgroundColor: '#ef4444'
                }]
            },
            options: {
                indexAxis: 'y',
                plugins: { legend: { display: false } },
                scales: { x: { ticks: { precision: 0 } } }
            }
        });

        // High-severity live alerts
        const highSeverityIncidents = incidents.filter(i => i.severity === "high").slice(-5).reverse();
        const alertBox = document.getElementById("highSeverityAlerts");
        alertBox.innerHTML = "";

        if (highSeverityIncidents.length === 0) {
            alertBox.innerHTML = `<p class="no-alerts">✅ No active high-severity incidents right now.</p>`;
        } else {
            highSeverityIncidents.forEach(i => {
                const machineName = (i.machine && i.machine.name) ? i.machine.name : "N/A";
                alertBox.innerHTML += `
                    <div class="alert-item">
                        <span class="badge badge-high">HIGH</span>
                        <span class="alert-desc">${(i.description || "").substring(0, 60)}...</span>
                        <span class="alert-machine">🛠️ ${machineName}</span>
                        <span class="alert-reporter">👤 ${i.reporterName || "N/A"}</span>
                    </div>`;
            });
        }

        // Recent incidents table
        const tableBody = document.getElementById("incidentTableBody");
        tableBody.innerHTML = "";
        incidents.slice(-10).reverse().forEach(i => {
            tableBody.innerHTML += `
                <tr>
                    <td>${(i.description || "").substring(0, 50)}...</td>
                    <td><span class="badge badge-${i.severity}">${i.severity || "N/A"}</span></td>
                    <td>${i.category || "N/A"}</td>
                    <td>${i.reporterName || "N/A"}</td>
                    <td><button class="delete-btn" onclick="deleteIncident(${i.id})">Delete</button></td>
                </tr>`;
        });

    } catch (err) {
        console.error("Failed to load dashboard data", err);
    }
}

function deleteIncident(id) {
    showConfirm(
        "Delete Incident?",
        "This action cannot be undone.",
        async () => {
            try {
                await fetch(`${BASE_URL}/incidents/${id}`, { method: "DELETE" });
                showToast("Incident Deleted", "The incident has been removed successfully.");
                loadDashboard();
            } catch (err) {
                showToast("Error", "Could not delete incident. Please try again.", "error");
                console.error(err);
            }
        }
    );
}

/* =========================================
   DOCUMENTS (documents.html)
========================================= */
async function uploadDocument() {
    const title = document.getElementById("docTitle").value.trim();
    const content = document.getElementById("docContent").value.trim();
    const companyName = sessionStorage.getItem("companyName");

    if (!title || !content) {
        showToast("Missing Fields", "Please fill in both title and content.", "error");
        return;
    }

    try {
        const response = await fetch(`${BASE_URL}/documents/upload`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ title, content, companyName })
        });
        await response.json();

        document.getElementById("docTitle").value = "";
        document.getElementById("docContent").value = "";
        showToast("Document Uploaded", "Your document has been indexed successfully.");
        loadDocuments();
    } catch (err) {
        showToast("Error", "Could not upload document. Please try again.", "error");
        console.error(err);
    }
}

async function loadDocuments() {
    const companyName = sessionStorage.getItem("companyName");
    if (!companyName) return;

    try {
        const response = await fetch(`${BASE_URL}/documents?companyName=${encodeURIComponent(companyName)}`);
        const documents = await response.json();

        const tableBody = document.getElementById("documentsTableBody");
        tableBody.innerHTML = "";
        documents.forEach(doc => {
            const statusClass = doc.status === "INDEXED" ? "status-indexed" : "status-processing";
            tableBody.innerHTML += `
                <tr>
                    <td>${doc.title}</td>
                    <td class="${statusClass}">${doc.status}</td>
                    <td>${new Date(doc.uploadedAt).toLocaleString()}</td>
                    <td><button class="delete-btn" onclick="deleteDocument(${doc.id})">Delete</button></td>
                </tr>`;
        });
    } catch (err) {
        console.error("Failed to load documents", err);
    }
}

function deleteDocument(id) {
    showConfirm(
        "Delete Document?",
        "This action cannot be undone.",
        async () => {
            try {
                await fetch(`${BASE_URL}/documents/${id}`, { method: "DELETE" });
                showToast("Document Deleted", "The document has been removed successfully.");
                loadDocuments();
            } catch (err) {
                showToast("Error", "Could not delete document. Please try again.", "error");
                console.error(err);
            }
        }
    );
}

/* =========================================
   LOGIN / COMPANY DATA (login.html)
========================================= */
const cityCompanyMap = {
    "Chennai": ["Ashok Leyland", "TVS Motors", "Sundram Fasteners", "Amalgamations Group"],
    "Coimbatore": ["Lakshmi Machine Works", "Roots Industries", "Premier Mills", "Kirloskar Coimbatore"],
    "Madurai": ["TVS Srichakra", "Madurai Steel Industries", "Vela Agro Industries", "SAF Yarns"],
    "Bangalore": ["Toyota Kirloskar", "Bosch Bangalore", "Titan Company", "BEML Limited"]
};

function loadCompanies() {
    const city = document.getElementById("citySelect").value;
    const companySelect = document.getElementById("companySelect");
    companySelect.innerHTML = "";

    if (!city) {
        companySelect.innerHTML = `<option value="">-- Select City First --</option>`;
        return;
    }

    companySelect.innerHTML = `<option value="">-- Select Company --</option>`;
    cityCompanyMap[city].forEach(company => {
        const option = document.createElement("option");
        option.value = company;
        option.textContent = company;
        companySelect.appendChild(option);
    });
}

function login() {
    const name = document.getElementById("userName").value.trim();
    const phone = document.getElementById("userPhone").value.trim();
    const email = document.getElementById("userEmail").value.trim();
    const city = document.getElementById("citySelect").value;
    const company = document.getElementById("companySelect").value;
    const errorText = document.getElementById("loginError");

    // 1) Check all fields filled (email included) — validate BEFORE saving/redirecting
    if (!name || !phone || !email || !city || !company) {
        errorText.innerText = "Please fill in all fields.";
        errorText.style.display = "block";
        return;
    }

    // 2) Check phone number is exactly 10 digits
    const phoneRegex = /^[0-9]{10}$/;
    if (!phoneRegex.test(phone)) {
        errorText.innerText = "Phone number must be exactly 10 digits.";
        errorText.style.display = "block";
        return;
    }

    // 3) Check valid email format
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        errorText.innerText = "Please enter a valid email address.";
        errorText.style.display = "block";
        return;
    }

    errorText.style.display = "none";

    // 4) All valid — NOW save session info and redirect
    sessionStorage.setItem("userName", name);
    sessionStorage.setItem("userPhone", phone);
    sessionStorage.setItem("userEmail", email);
    sessionStorage.setItem("userCity", city);
    sessionStorage.setItem("companyName", company);

    showToast("Login Successful", "Welcome, " + name + "!");
    setTimeout(() => { window.location.href = "index.html"; }, 800);
}

function logout() {
    showToast("Logged Out", "You have been logged out successfully.");
    sessionStorage.clear();
    setTimeout(() => { window.location.href = "login.html"; }, 800);
}

function checkLoginOrRedirect() {
    const company = sessionStorage.getItem("companyName");
    if (!company && !window.location.pathname.includes("login.html")) {
        window.location.href = "login.html";
    }
}

/* ===== Toast Notification Helper ===== */
function showToast(title, message, type = "success") {
    const existing = document.querySelector(".toast");
    if (existing) existing.remove();

    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <div class="toast-title">${type === "success" ? "✅" : "⚠️"} ${title}</div>
        <div class="toast-message">${message}</div>
    `;
    document.body.appendChild(toast);

    setTimeout(() => toast.classList.add("show"), 10);
    setTimeout(() => {
        toast.classList.remove("show");
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

/* ===== Custom Confirm Modal ===== */
function showConfirm(title, message, onConfirm) {
    const existing = document.querySelector(".modal-overlay");
    if (existing) existing.remove();

    const overlay = document.createElement("div");
    overlay.className = "modal-overlay";
    overlay.innerHTML = `
        <div class="modal-box">
            <h3>${title}</h3>
            <p>${message}</p>
            <div class="modal-actions">
                <button class="btn btn-secondary" id="modalCancelBtn">Cancel</button>
                <button class="btn-danger" id="modalConfirmBtn">Delete</button>
            </div>
        </div>
    `;
    document.body.appendChild(overlay);

    setTimeout(() => overlay.classList.add("show"), 10);

    const close = () => {
        overlay.classList.remove("show");
        setTimeout(() => overlay.remove(), 200);
    };

    document.getElementById("modalCancelBtn").onclick = close;
    document.getElementById("modalConfirmBtn").onclick = () => {
        close();
        onConfirm();
    };
}
/* =========================================
   GET STARTED CHECKLIST (index.html)
========================================= */
async function loadGetStartedChecklist() {
    const container = document.getElementById("checklistContainer");
    if (!container) return;

    const companyName = sessionStorage.getItem("companyName");
    if (!companyName) return;

    try {
        const [docsRes, machinesRes, incidentsRes] = await Promise.all([
            fetch(`${BASE_URL}/documents?companyName=${encodeURIComponent(companyName)}`),
            fetch(`${BASE_URL}/machines?companyName=${encodeURIComponent(companyName)}`),
            fetch(`${BASE_URL}/incidents?companyName=${encodeURIComponent(companyName)}`)
        ]);

        const docs = await docsRes.json();
        const machines = await machinesRes.json();
        const incidents = await incidentsRes.json();

        const steps = [
            {
                label: "Upload your first safety document",
                done: docs.length > 0,
                link: "documents.html",
                linkText: "Upload now →"
            },
            {
                label: "Add a machine to your company",
                done: machines.length > 0,
                link: "incident.html",
                linkText: "Add machine →"
            },
            {
                label: "Ask a question in Chat Assistant",
                done: false, // no easy way to track this, always show as a suggestion
                link: "chat.html",
                linkText: "Try it →"
            },
            {
                label: "Report your first incident",
                done: incidents.length > 0,
                link: "incident.html",
                linkText: "Report now →"
            }
        ];

        container.innerHTML = steps.map(step => `
            <div class="checklist-item">
                <div class="checklist-icon ${step.done ? 'done' : 'pending'}">
                    ${step.done ? '✓' : '○'}
                </div>
                <div class="checklist-text ${step.done ? 'done' : ''}">${step.label}</div>
                ${!step.done ? `<a href="${step.link}" class="checklist-action">${step.linkText}</a>` : ''}
            </div>
        `).join("");

    } catch (err) {
        console.error("Failed to load checklist", err);
        container.innerHTML = `<p style="color:var(--text-muted); font-size:13px;">Could not load progress.</p>`;
    }
}

