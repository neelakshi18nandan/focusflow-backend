/* ========================================
   API CONFIGURATION
======================================== */
const BASE_URL = "https://focusflow-backend-mg1p.onrender.com";

/* ========================================
   CLOCK FUNCTIONALITY
======================================== */
function updateClock() {
    let d = new Date();
    let h = d.getHours();
    let m = d.getMinutes();
    let ap = h >= 12 ? "pm" : "am";
    h = h % 12 || 12;
    m = m < 10 ? "0" + m : m;
    document.getElementById('clock').innerText = `${h}:${m} ${ap}`;
}

updateClock();
setInterval(updateClock, 60000);

/* ========================================
   ALERT TONE
======================================== */
function playAlertTone(type = 'study') {
    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    const now = ctx.currentTime;

    function playNote(freq, startAt, duration, volume = 0.4, oscType = 'sine') {
        const osc  = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.type = oscType;
        osc.frequency.value = freq;
        gain.gain.setValueAtTime(0, now + startAt);
        gain.gain.linearRampToValueAtTime(volume, now + startAt + 0.06);
        gain.gain.setValueAtTime(volume, now + startAt + duration - 0.15);
        gain.gain.linearRampToValueAtTime(0, now + startAt + duration);
        osc.start(now + startAt);
        osc.stop(now + startAt + duration + 0.05);
    }

    if (type === 'study') {
        const studyNotes = [523, 659, 784, 1047];
        studyNotes.forEach((freq, i) => playNote(freq, i * 0.35, 0.6, 0.38));
        studyNotes.forEach((freq, i) => playNote(freq, 1.4 + i * 0.35, 0.6, 0.22));
        playNote(1047, 2.85, 1.4, 0.28);
    } else if (type === 'break') {
        const breakNotes = [784, 659, 523, 392];
        breakNotes.forEach((freq, i) => playNote(freq, i * 0.4, 0.7, 0.38));
        breakNotes.forEach((freq, i) => playNote(freq, 1.65 + i * 0.4, 0.7, 0.20));
        playNote(392, 3.3, 1.4, 0.25);
    }
}

/* ========================================
   BRAIN FOG / FOCUS TONE (852Hz)
======================================== */
let brainFogAudio = null;
let brainFogInterval = null;
let brainFogRunning = false;
const BRAIN_DURATION = 60;

function toggleBrainFog() {
    if (brainFogRunning) stopBrainFog();
    else startBrainFog();
}

function startBrainFog() {
    brainFogRunning = true;

    const btn = document.getElementById('brainPlayBtn');
    const wave = document.getElementById('brainWave');
    const progressWrap = document.getElementById('brainProgressWrap');
    const barFill = document.getElementById('brainBarFill');
    const countdown = document.getElementById('brainCountdown');

    btn.innerHTML = '⏹ &nbsp;Stop';
    wave.classList.add('active');
    progressWrap.classList.add('visible');
    barFill.style.width = '100%';

    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    const osc = ctx.createOscillator();
    const gainNode = ctx.createGain();
    osc.connect(gainNode);
    gainNode.connect(ctx.destination);
    osc.type = 'sine';
    osc.frequency.value = 852;
    gainNode.gain.setValueAtTime(0, ctx.currentTime);
    gainNode.gain.linearRampToValueAtTime(0.3, ctx.currentTime + 0.5);
    osc.start(ctx.currentTime);
    brainFogAudio = { osc, gainNode, ctx };

    let secondsLeft = BRAIN_DURATION;
    brainFogInterval = setInterval(() => {
        secondsLeft--;
        const m = Math.floor(secondsLeft / 60);
        const s = secondsLeft % 60;
        countdown.innerText = `${m}:${s < 10 ? '0' + s : s}`;
        barFill.style.width = ((secondsLeft / BRAIN_DURATION) * 100) + '%';
        if (secondsLeft <= 0) {
            stopBrainFog();
            showToast('🧠 Focus Boost complete! You\'re ready to study.', 4000, 'info');
        }
    }, 1000);
}

function stopBrainFog() {
    brainFogRunning = false;
    clearInterval(brainFogInterval);
    brainFogInterval = null;

    if (brainFogAudio) {
        const { osc, gainNode, ctx } = brainFogAudio;
        gainNode.gain.linearRampToValueAtTime(0, ctx.currentTime + 0.5);
        osc.stop(ctx.currentTime + 0.5);
        brainFogAudio = null;
    }

    const btn = document.getElementById('brainPlayBtn');
    const wave = document.getElementById('brainWave');
    const progressWrap = document.getElementById('brainProgressWrap');
    const barFill = document.getElementById('brainBarFill');
    const countdown = document.getElementById('brainCountdown');

    btn.innerHTML = '▶ &nbsp;Start Focus Tone';
    wave.classList.remove('active');
    progressWrap.classList.remove('visible');
    barFill.style.width = '100%';
    countdown.innerText = '1:00';
}

/* ========================================
   TOAST NOTIFICATION SYSTEM
======================================== */
function showToast(message, duration = 4000, type = 'info') {
    const existing = document.getElementById('focusToast');
    if (existing) existing.remove();

    const colors = {
        info:    { bg: 'linear-gradient(135deg, #667eea, #764ba2)', border: 'rgba(102,126,234,0.5)' },
        break:   { bg: 'linear-gradient(135deg, #11998e, #38ef7d)', border: 'rgba(56,239,125,0.5)' },
        study:   { bg: 'linear-gradient(135deg, #f093fb, #f5576c)', border: 'rgba(240,147,251,0.5)' },
        warning: { bg: 'linear-gradient(135deg, #f7971e, #ffd200)', border: 'rgba(255,210,0,0.5)' },
    };
    const c = colors[type] || colors.info;

    const toast = document.createElement('div');
    toast.id = 'focusToast';
    toast.style.cssText = `
        position: fixed; top: 90px; left: 50%;
        transform: translateX(-50%) translateY(-20px);
        background: ${c.bg}; border: 1.5px solid ${c.border};
        color: #fff; padding: 14px 28px; border-radius: 50px;
        font-size: 15px; font-weight: 600; font-family: "Segoe UI", sans-serif;
        box-shadow: 0 8px 32px rgba(0,0,0,0.3); z-index: 9999;
        opacity: 0; transition: opacity 0.4s ease, transform 0.4s ease;
        white-space: nowrap; pointer-events: none; text-align: center; letter-spacing: 0.02em;
    `;
    toast.innerText = message;
    document.body.appendChild(toast);

    requestAnimationFrame(() => requestAnimationFrame(() => {
        toast.style.opacity = '1';
        toast.style.transform = 'translateX(-50%) translateY(0)';
    }));

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(-50%) translateY(-20px)';
        setTimeout(() => toast.remove(), 400);
    }, duration);
}

/* ========================================
   COUNTDOWN TOAST
======================================== */
function showCountdownToast(message, type = 'warning') {
    const colors = {
        warning: { bg: 'linear-gradient(135deg, #f7971e, #ffd200)', border: 'rgba(255,210,0,0.5)' },
        break:   { bg: 'linear-gradient(135deg, #11998e, #38ef7d)', border: 'rgba(56,239,125,0.5)' },
    };
    const c = colors[type] || colors.warning;
    let toast = document.getElementById('countdownToast');

    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'countdownToast';
        toast.style.cssText = `
            position: fixed; top: 90px; left: 50%;
            transform: translateX(-50%) translateY(0);
            background: ${c.bg}; border: 1.5px solid ${c.border};
            color: #fff; padding: 14px 28px; border-radius: 50px;
            font-size: 15px; font-weight: 600; font-family: "Segoe UI", sans-serif;
            box-shadow: 0 8px 32px rgba(0,0,0,0.3); z-index: 9999;
            opacity: 0; transition: opacity 0.4s ease, transform 0.4s ease;
            white-space: nowrap; pointer-events: none; text-align: center; letter-spacing: 0.02em;
        `;
        document.body.appendChild(toast);
        requestAnimationFrame(() => requestAnimationFrame(() => {
            toast.style.opacity = '1';
            toast.style.transform = 'translateX(-50%) translateY(0)';
        }));
    }
    toast.innerText = message;
}

function removeCountdownToast() {
    const toast = document.getElementById('countdownToast');
    if (toast) {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(-50%) translateY(-20px)';
        setTimeout(() => toast.remove(), 400);
    }
}

/* ========================================
   BREAK DELAY COUNTDOWN
======================================== */
function showBreakDelayToast(secondsLeft) {
    const colors = { bg: 'linear-gradient(135deg, #11998e, #38ef7d)', border: 'rgba(56,239,125,0.5)' };
    let toast = document.getElementById('breakDelayToast');

    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'breakDelayToast';
        toast.style.cssText = `
            position: fixed; top: 90px; left: 50%;
            transform: translateX(-50%) translateY(0);
            background: ${colors.bg}; border: 1.5px solid ${colors.border};
            color: #fff; padding: 14px 28px; border-radius: 50px;
            font-size: 15px; font-weight: 600; font-family: "Segoe UI", sans-serif;
            box-shadow: 0 8px 32px rgba(0,0,0,0.3); z-index: 9999;
            opacity: 0; transition: opacity 0.4s ease, transform 0.4s ease;
            white-space: nowrap; pointer-events: none; text-align: center; letter-spacing: 0.02em;
        `;
        document.body.appendChild(toast);
        requestAnimationFrame(() => requestAnimationFrame(() => {
            toast.style.opacity = '1';
            toast.style.transform = 'translateX(-50%) translateY(0)';
        }));
    }
    toast.innerText = `☕ Break starts in ${secondsLeft} second${secondsLeft === 1 ? '' : 's'}...`;
}

function removeBreakDelayToast() {
    const toast = document.getElementById('breakDelayToast');
    if (toast) {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(-50%) translateY(-20px)';
        setTimeout(() => toast.remove(), 400);
    }
}

/* ========================================
   DELAY THEN START BREAK
======================================== */
function delayedStartBreak(delaySeconds) {
    let remaining = delaySeconds;
    showBreakDelayToast(remaining);
    remaining--;

    const delayInterval = setInterval(() => {
        if (remaining <= 0) {
            clearInterval(delayInterval);
            removeBreakDelayToast();
            isBreak = true;
            time = breakDuration;
            initialTime = breakDuration;
            updateTimer();
            startTimer();
        } else {
            showBreakDelayToast(remaining);
            remaining--;
        }
    }, 1000);
}

/* ========================================
   DURATION MODAL
======================================== */
function openDurationModal() {
    document.getElementById('durationModal').style.display = "flex";
}

function closeDurationModal() {
    document.getElementById('durationModal').style.display = "none";
}

function setCustomDuration() {
    let totalHours = parseFloat(document.getElementById('totalHours').value) || 0;
    let studyH = parseInt(document.getElementById('studyHours').value) || 0;
    let studyM = parseInt(document.getElementById('studyMins').value) || 25;
    let study = studyH * 60 + studyM;
    if (study <= 0) study = 25;
    let breakMin = parseInt(document.getElementById('breakTime').value) || 0;
    let hasBreak = !document.getElementById('noBreak').checked && breakMin > 0;

    pauseTimer();
    // If no total goal set, default to the study session length
    totalStudyTime = totalHours > 0 ? totalHours * 3600 : study * 60;
    studyDuration = study * 60;
    breakDuration = hasBreak ? breakMin * 60 : 0;
    time = studyDuration;
    initialTime = studyDuration;
    isBreak = false;
    timeSpentStudying = 0;
    updateTimer();
    closeDurationModal();
}

document.getElementById('noBreak').onchange = function () {
    document.getElementById('breakTime').disabled = this.checked;
    if (this.checked) document.getElementById('breakTime').value = 0;
};

/* ========================================
   WELCOME MODAL
======================================== */
function showGoalForm() {
    document.getElementById('welcomeSection').style.display = 'none';
    document.getElementById('goalFormSection').style.display = 'block';
}

function showWelcomeSection() {
    document.getElementById('goalFormSection').style.display = 'none';
    document.getElementById('welcomeSection').style.display = 'block';
}

function closeWelcomeModal() {
    document.getElementById('welcomeModal').style.display = 'none';
    document.getElementById('welcomeSection').style.display = 'block';
    document.getElementById('goalFormSection').style.display = 'none';
}

function setWelcomeGoal() {
    let totalHours = parseFloat(document.getElementById('welcomeTotalHours').value) || 0;
    let studyH = parseInt(document.getElementById('welcomeStudyHours').value) || 0;
    let studyM = parseInt(document.getElementById('welcomeStudyMins').value) || 25;
    let study = studyH * 60 + studyM;
    if (study <= 0) study = 25;
    let breakMin = parseInt(document.getElementById('welcomeBreakTime').value) || 0;
    let hasBreak = !document.getElementById('welcomeNoBreak').checked && breakMin > 0;

    pauseTimer();
    totalStudyTime = totalHours > 0 ? totalHours * 3600 : study * 60;
    studyDuration = study * 60;
    breakDuration = hasBreak ? breakMin * 60 : 0;
    time = studyDuration;
    initialTime = studyDuration;
    isBreak = false;
    timeSpentStudying = 0;
    updateTimer();

    document.getElementById('welcomeModal').style.display = 'none';
    document.getElementById('welcomeSection').style.display = 'block';
    document.getElementById('goalFormSection').style.display = 'none';
}

document.getElementById('welcomeNoBreak').onchange = function () {
    document.getElementById('welcomeBreakTime').disabled = this.checked;
    if (this.checked) document.getElementById('welcomeBreakTime').value = 0;
};

/* ========================================
   LOGIN FUNCTIONALITY
======================================== */
let currentUserId = null;
let currentUser = null;
let currentUsername = null;

function openLogin() {
    document.getElementById('loginModal').style.display = "flex";
    showSignIn();
}

function closeLogin() {
    document.getElementById('loginModal').style.display = "none";
}

function showSignIn() {
    document.getElementById('signInView').style.display = 'block';
    document.getElementById('signUpView').style.display = 'none';
}

function showSignUp() {
    document.getElementById('signInView').style.display = 'none';
    document.getElementById('signUpView').style.display = 'block';
}

/* ========================================
   SIGN IN
======================================== */
function handleSignIn() {
    let username = document.getElementById('loginEmail').value.trim();
    let password = document.getElementById('loginPassword').value;

    if (!username) return alert("Please enter your username");
    if (!password) return alert("Please enter your password");

    fetch(`${BASE_URL}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password })
    })
    .then(res => res.json())
    .then(data => {
        if (!data.success) {
            alert(data.message || "Login failed. Please try again.");
            return;
        }
        currentUserId = data.userId;
        currentUsername = data.username;
        currentUser = data.email;

        localStorage.setItem("currentUserId", data.userId);
        localStorage.setItem("currentUsername", data.username);
        localStorage.setItem("currentUser", data.email);

        document.getElementById('loginBtn').innerText = "👋 Hi, " + data.username;
        document.getElementById('loginBtn').style.display = 'none';
        document.getElementById('logoutBtn').style.display = 'block';
        loadTodos();
        closeLogin();
        document.getElementById('welcomeModal').style.display = "flex";
    })
    .catch(err => {
        console.error("Sign in error:", err);
        alert("Could not connect to server. Please wait 60 seconds and try again (server may be waking up).");
    });
}

/* ========================================
   LOGOUT CONFIRMATION MODAL
======================================== */
function openLogoutModal() {
    const name = currentUsername || 'there';
    document.getElementById('logoutConfirmName').textContent = 'Hey, ' + name + '!';
    document.getElementById('logoutModal').style.display = 'flex';
}

function closeLogoutModal() {
    document.getElementById('logoutModal').style.display = 'none';
}

function confirmLogout() {
    closeLogoutModal();
    handleLogout();
}

/* ========================================
   LOGOUT
======================================== */
function handleLogout() {
    currentUserId = null;
    currentUsername = null;
    currentUser = null;
    localStorage.removeItem("currentUserId");
    localStorage.removeItem("currentUsername");
    localStorage.removeItem("currentUser");
    localStorage.removeItem("focusFlow_User");

    document.getElementById('loginBtn').innerText = "🔐 Sign In";
    document.getElementById('loginBtn').style.display = 'block';
    document.getElementById('logoutBtn').style.display = 'none';

    loadTodos();
    showToast("👋 Logged out successfully!", 3000, 'info');
}

/* ========================================
   SIGN UP
======================================== */
function handleSignUp() {
    let name = document.getElementById('signupName').value.trim();
    let email = document.getElementById('signupEmail').value.trim();
    let password = document.getElementById('signupPassword').value;
    let confirmPassword = document.getElementById('signupConfirmPassword').value;

    if (!name) return alert("Please enter your username");
    if (!email) return alert("Please enter your email");

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) return alert("Please enter a valid email address");

    if (!password) return alert("Please enter a password");
    if (password !== confirmPassword) return alert("Passwords don't match");
    if (password.length < 6) return alert("Password must be at least 6 characters");

    fetch(`${BASE_URL}/api/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: name, email, password })
    })
    .then(res => res.json())
    .then(data => {
        if (!data.success) {
            alert(data.message || "Registration failed. Please try again.");
            return;
        }
        currentUserId = data.userId;
        currentUsername = data.username;
        currentUser = data.email;

        localStorage.setItem("currentUserId", data.userId);
        localStorage.setItem("currentUsername", data.username);
        localStorage.setItem("currentUser", data.email);

        document.getElementById('loginBtn').innerText = "👋 Hi, " + data.username;
        document.getElementById('loginBtn').style.display = 'none';
        document.getElementById('logoutBtn').style.display = 'block';

        document.getElementById('signupName').value = '';
        document.getElementById('signupEmail').value = '';
        document.getElementById('signupPassword').value = '';
        document.getElementById('signupConfirmPassword').value = '';

        loadTodos();
        closeLogin();
        document.getElementById('welcomeModal').style.display = "flex";
    })
    .catch(err => {
        console.error("Sign up error:", err);
        alert("Could not connect to server. Please wait 60 seconds and try again (server may be waking up).");
    });
}

/* ========================================
   TODO LIST — localStorage-based
======================================== */
document.getElementById('todoInput').onkeydown = (e) => {
    if (e.key === "Enter") addTodo();
};

function addTodo() {
    if (!currentUser) return alert("Login first");

    let todoText = document.getElementById('todoInput').value.trim();
    if (!todoText) return;

    let todos = JSON.parse(localStorage.getItem("todos_" + currentUser)) || [];
    todos.push({ id: Date.now(), text: todoText, completed: false });
    localStorage.setItem("todos_" + currentUser, JSON.stringify(todos));
    document.getElementById('todoInput').value = "";
    loadTodos();
}

function loadTodos() {
    document.getElementById('todoList').innerHTML = "";
    if (!currentUser) return;

    let todos = JSON.parse(localStorage.getItem("todos_" + currentUser)) || [];
    todos.forEach((task) => {
        let li = document.createElement("li");
        if (task.completed) li.classList.add("completed");

        let checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.checked = task.completed;
        checkbox.onchange = () => toggleTask(task.id);

        let span = document.createElement("span");
        span.className = "task-text";
        span.textContent = task.text;

        let deleteBtn = document.createElement("button");
        deleteBtn.className = "delete-btn";
        deleteBtn.textContent = "×";
        deleteBtn.onclick = () => deleteTask(task.id);

        li.appendChild(checkbox);
        li.appendChild(span);
        li.appendChild(deleteBtn);
        document.getElementById('todoList').appendChild(li);
    });
}

function toggleTask(taskId) {
    let todos = JSON.parse(localStorage.getItem("todos_" + currentUser)) || [];
    todos = todos.map(t => t.id === taskId ? { ...t, completed: !t.completed } : t);
    localStorage.setItem("todos_" + currentUser, JSON.stringify(todos));
    loadTodos();
}

function deleteTask(taskId) {
    let todos = JSON.parse(localStorage.getItem("todos_" + currentUser)) || [];
    todos = todos.filter(t => t.id !== taskId);
    localStorage.setItem("todos_" + currentUser, JSON.stringify(todos));
    loadTodos();
}

/* ========================================
   SAVE SINGLE SESSION TO BACKEND
   Called each time a pomodoro cycle completes
======================================== */
let sessionSaveInProgress = false; // FIXED: guard against double-save from 500ms interval

function saveSessionToBackend(durationSec) {
    if (!currentUserId) return;
    if (sessionSaveInProgress) return; // FIXED: prevent duplicate saves
    sessionSaveInProgress = true;

    fetch(`${BASE_URL}/api/logs/session`, { // FIXED: was /api/sessions (wrong endpoint)
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            userId: currentUserId,
            plannedSec: studyDuration,
            durationSec: durationSec
        })
    })
    .then(res => res.json())
    .then(data => {
        console.log("Session saved:", data);
        sessionSaveInProgress = false;
    })
    .catch(err => {
        console.error("Session save error:", err);
        sessionSaveInProgress = false;
    });
}

/* ========================================
   END DAY → saves daily total to study_log
======================================== */
function endTheDay() {
    if (!currentUser) {
        alert("Please login first to save your progress!");
        return;
    }

    pauseTimer();

    let progressPercentage = (timeSpentStudying / totalStudyTime) * 100;

    let catGif = "sad_cat.gif";
    let message = "I missed seeing you chase your goals today, but tomorrow is still yours. One slow day doesn't define you. Do your best tomorrow!";

    if (progressPercentage >= 51 && progressPercentage <= 90) {
        catGif = "happy_cat.gif";
        message = "Good progress, but I know you're capable of even more. Quiet progress like this is exactly how big success is built— see you stronger tomorrow!";
    } else if (progressPercentage >= 91) {
        catGif = "excited_cat.gif";
        message = "AMAZING! Today you outworked your excuses. Future you is smiling because of what you did today. Remember this feeling — this is what growth looks like!";
    }

    let hoursStudied = Math.floor(timeSpentStudying / 3600);
    let minutesStudied = Math.floor((timeSpentStudying % 3600) / 60);

    let todos = JSON.parse(localStorage.getItem("todos_" + currentUser)) || [];
    let totalTasks = todos.length;
    let completedTasks = todos.filter(t => t.completed).length;

    // Save any remaining unsaved study time as a session
    if (timeSpentStudying > 0) {
        saveSessionToBackend(timeSpentStudying);
    }

    // Save daily total — reuses /api/logs/session which upserts today's study_log row
    fetch(`${BASE_URL}/api/logs/session`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            userId: currentUserId,
            durationSec: timeSpentStudying,
            plannedSec: totalStudyTime
        })
    })
    .then(res => res.json())
    .then(data => console.log("Study log saved:", data))
    .catch(err => console.error("Study log save error:", err));

    document.getElementById('endDayCat').src = catGif;
    document.getElementById('endDayMessage').innerText = message;
    document.getElementById('hoursStudied').innerText = hoursStudied;
    document.getElementById('minutesStudied').innerText = minutesStudied;
    document.getElementById('tasksCompleted').innerText = completedTasks;
    document.getElementById('totalTasks').innerText = totalTasks;
    document.getElementById('endDayModal').style.display = 'flex';
}

function closeEndDaySummary() {
    document.getElementById('endDayModal').style.display = 'none';
}

function closeEndDayModal() {
    document.getElementById('endDayModal').style.display = 'none';
    timeSpentStudying = 0;
    isBreak = false;
    time = studyDuration;
    initialTime = studyDuration;
    updateTimer();
    document.getElementById('welcomeModal').style.display = 'flex';
}

/* ========================================
   TIMER FUNCTIONALITY
======================================== */
let time = 1500;
let interval = null;
let initialTime = 1500;
let half = false;
let studyDuration = 1500;
let breakDuration = 0;
let isBreak = false;
let isRunning = false;
let totalStudyTime = 7200;
let timeSpentStudying = 0;

const WARNING_SECONDS = 5;

function getBreakDelay() {
    return Math.floor(Math.random() * 2) + 2;
}

function updateTimer() {
    let m = Math.floor(time / 60);
    let s = time % 60;
    document.getElementById('timer').innerText = `${m}:${s < 10 ? "0" + s : s}`;
}

function togglePlayPause() {
    if (isRunning) pauseTimer();
    else startTimer();
}

function startTimer() {
    if (interval) return;
    isRunning = true;
    half = false;
    document.getElementById('playPauseBtn').innerHTML = "⏸";

    let startTimestamp = Date.now();
    let startingTime = time;
    let startingTimeSpent = timeSpentStudying;
    let lastSecond = startingTime;

    interval = setInterval(() => {
        const elapsedSeconds = Math.floor((Date.now() - startTimestamp) / 1000);
        time = startingTime - elapsedSeconds;

        if (!isBreak) {
            timeSpentStudying = startingTimeSpent + elapsedSeconds;
        }

        if (time !== lastSecond) {
            lastSecond = time;
            if (!half && time <= initialTime / 2) half = true;

            if (!isBreak && breakDuration > 0 && time > 0 && time <= WARNING_SECONDS) {
                showCountdownToast(`⏳ Break starts in ${time} second${time === 1 ? '' : 's'}...`, 'warning');
            }
            if (isBreak && time > 0 && time <= WARNING_SECONDS) {
                showCountdownToast(`📖 Study resumes in ${time} second${time === 1 ? '' : 's'}...`, 'break');
            }
        }

        if (time <= 0) {
            time = 0;
            updateTimer();
            clearInterval(interval);
            interval = null;
            isRunning = false;
            document.getElementById('playPauseBtn').innerHTML = "▶";
            removeCountdownToast();

            if (!isBreak && breakDuration > 0) {
                saveSessionToBackend(studyDuration);
                playAlertTone('study');
                setTimeout(() => delayedStartBreak(getBreakDelay()), 5000);
            } else if (!isBreak && breakDuration === 0) {
                saveSessionToBackend(studyDuration);
                playAlertTone('study');
                if (timeSpentStudying >= totalStudyTime) {
                    showToast("🎉 All study time completed! Amazing work!", 5000, 'study');
                    resetTimer();
                } else {
                    showToast("🔥 Session complete! Starting next session...", 3000, 'info');
                    setTimeout(() => {
                        time = studyDuration;
                        initialTime = studyDuration;
                        updateTimer();
                        startTimer();
                    }, 5000);
                }
            } else {
                isBreak = false;
                playAlertTone('break');
                if (timeSpentStudying >= totalStudyTime) {
                    showToast("🎉 All study time completed! Amazing work!", 5000, 'study');
                    resetTimer();
                } else {
                    setTimeout(() => {
                        showToast("📚 Break over! Back to focus mode!", 3000, 'study');
                        time = studyDuration;
                        initialTime = studyDuration;
                        updateTimer();
                        startTimer();
                    }, 5000);
                }
            }
            return;
        }

        updateTimer();
    }, 500);
}

function pauseTimer() {
    clearInterval(interval);
    interval = null;
    isRunning = false;
    document.getElementById('playPauseBtn').innerHTML = "▶";
    removeCountdownToast();
}

function resetTimer() {
    pauseTimer();
    isBreak = false;
    timeSpentStudying = 0;
    time = studyDuration;
    initialTime = studyDuration;
    updateTimer();
    half = false;
    removeCountdownToast();
    removeBreakDelayToast();
}

/* ========================================
   NAVIGATION & ANALYTICS
======================================== */
function handleNavigation(element) {
    const fill = document.getElementById('btnFill');
    let progress = 0;
    element.style.pointerEvents = 'none';

    const navigationInterval = setInterval(() => {
        progress += 5;
        fill.style.width = progress + '%';

        if (progress >= 100) {
            clearInterval(navigationInterval);
            prepareAnalyticsData();
            setTimeout(() => { window.location.href = 'analysis.html'; }, 200);
        }
    }, 20);
}

function prepareAnalyticsData() {
    if (!currentUserId) {
        localStorage.setItem('focusFlow_User', JSON.stringify({
            name: "Guest User", streak: 0,
            lastDate: new Date().toDateString(), totalMinutes: 0
        }));
        return;
    }

    fetch(`${BASE_URL}/api/logs/${currentUserId}`)
    .then(res => res.json())
    .then(analytics => {
        localStorage.setItem('focusFlow_User', JSON.stringify({
            name: currentUsername,
            streak: analytics.streak || 0, // FIXED: was streakDays
            lastDate: new Date().toDateString(),
            totalMinutes: Math.round((analytics.totalHours || 0) * 60),
            totalSessions: analytics.totalSessions || 0,
            avgDailyHours: analytics.avgDailyHours || 0,
            logs: analytics.logs || []
        }));
    })
    .catch(err => {
        console.error("Analytics fetch error:", err);
        localStorage.setItem('focusFlow_User', JSON.stringify({
            name: currentUsername || "User", streak: 0,
            lastDate: new Date().toDateString(),
            totalMinutes: Math.floor(timeSpentStudying / 60)
        }));
    });
}

/* ========================================
   UTILITY FUNCTIONS
======================================== */
function featureComingSoon() {
    alert("Feature coming soon!");
}

/* ========================================
   PAGE VISIBILITY API
======================================== */
document.addEventListener('visibilitychange', function () {
    if (!document.hidden && isRunning) updateTimer();
});

/* ========================================
   VISUALIZER
======================================== */
function initVisualizer() {
    const container = document.getElementById('visualizer');
    if (!container) return;

    const numBars = 25;
    for (let i = 0; i < numBars; i++) {
        const bar = document.createElement('div');
        bar.className = 'bar';
        container.appendChild(bar);
    }

    const bars = document.querySelectorAll('.bar');

    function animate() {
        if (isRunning) {
            bars.forEach((bar, index) => {
                const wave = Math.sin(Date.now() / 200 + index * 0.4) * 20 + 25;
                const random = Math.random() * 10;
                bar.style.height = `${wave + random}px`;
            });
        } else {
            bars.forEach(bar => bar.style.height = '5px');
        }
        requestAnimationFrame(animate);
    }
    animate();
}

window.addEventListener('load', initVisualizer);

/* ========================================
   AUTO RESTORE LOGIN ON PAGE LOAD
======================================== */
window.addEventListener('load', function () {
    const savedUserId = localStorage.getItem("currentUserId");
    const savedUsername = localStorage.getItem("currentUsername");
    const savedEmail = localStorage.getItem("currentUser");

    if (savedUserId && savedUsername) {
        currentUserId = savedUserId;
        currentUsername = savedUsername;
        currentUser = savedEmail;
        document.getElementById('loginBtn').innerText = "👋 Hi, " + savedUsername;
        document.getElementById('loginBtn').style.display = 'none';
        document.getElementById('logoutBtn').style.display = 'block';
        loadTodos();
    }
});