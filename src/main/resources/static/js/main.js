let roomInput;
let username = document.querySelector('#username').innerHTML;
let chatPage = document.querySelector('#chat-page');
let messageForm = document.querySelector('#messageForm');
let messageInput = document.querySelector('#message');
let messageArea = document.querySelector('#messageArea');
let connectingElement = document.querySelector('.connecting');
let roomIdDisplay = document.querySelector('#room-id-display');
let guessIdDisplay = document.querySelector('#guess-id-display'); guessIdDisplay.textContent = "test"; //test content
let tableForm = document.querySelector('#table');
let unsubButton = document.querySelector('#unsub');
let userList = document.getElementById("userlist");

let guessButton1 = document.querySelector('#guess-button-id-1');
let guessButton2 = document.querySelector('#guess-button-id-2');
let guessButton3 = document.querySelector('#guess-button-id-3');

let stompClient = null;
let currentSubscription1;
let currentSubscription2;
let currentSubscription3;
let currentSubscription4;
let currentDrawSubscription;
let queueSubscription;
let scoreSubscription;
let timerSubscription;
let guessIdDisplaySubscription;
let path = null;

let canvasForm = document.getElementById('canvas-form');
let canvas  = document.getElementById('drawing');
let context = canvas.getContext('2d');
let width   = canvas.getAttribute("width");
let height  = canvas.getAttribute("height");

// Get the modal
let modal = document.querySelector('#myModal');
let endModal = document.querySelector('#endModal');
let modalContent = document.getElementById("modal-cont");

let drawUser = null;
let dru = document.getElementById("draw-user"); //for test

let timer1 = document.getElementById("timer"); //for game
let timer2 = document.getElementById("time"); //for modal

let socket = new SockJS('/ws');
stompClient = Stomp.over(socket);
stompClient.connect({}, onConnected, onError);

let inGame = false;

document.addEventListener("DOMContentLoaded", function() {
    let mouse = [false, false, [0,0], false];

    // set canvas to full browser width/height
    //canvas.width = width;
    //canvas.height = height;

    // register mouse event handlers
    canvas.onmousedown = function(e){ mouse[0] = true; };
    canvas.onmouseup = function(e){ mouse[0] = false; };
    canvas.onmouseleave = function(e){ mouse[0] = false; };

    canvas.onmousemove = function(e) {
        // normalize mouse position to range 0.0 - 1.0
        let rect = canvas.getBoundingClientRect();
        mouse[2][0] = (e.clientX - rect.left) / width;
        mouse[2][1] = (e.clientY - rect.top) / height;
        mouse[1] = true;
    };

    // main loop, running every 25ms
    function mainLoop() {
        // check if the user is drawing
        if (mouse[0] && mouse[1] && mouse[3]) {
            // send line to to the server
            let drawMessage = JSON.stringify({
                sender : username,
                content : mouse[2].toString() + "#" + mouse[3].toString() + context.strokeStyle,
                type : 'DRAW'
            });

            stompClient.send(`${path}/draw`, {}, drawMessage);
            mouse[1] = false;
        }
        mouse[3] = [mouse[2][0], mouse[2][1]];
        //mouse.pos_prev = {x: mouse.pos.x, y: mouse.pos.y};
        setTimeout(mainLoop, 25);
    }
    mainLoop();
});


document.getElementById("red").addEventListener("click", function () {context.strokeStyle = "#F00000"});
document.getElementById("yellow").addEventListener("click", function () {context.strokeStyle = "#F0DE10"});
document.getElementById("blue").addEventListener("click", function () {context.strokeStyle = "#001FF0"});
document.getElementById("black").addEventListener("click", function () {context.strokeStyle = "#090607"});
function onDraw(payload){
    let message = JSON.parse(payload.body);

    context.lineCap = 'round';
    context.lineWidth = 3;
    context.strokeStyle = message.color;
    //context.strokeStyle = color;

    context.beginPath();
    context.moveTo(message.x1 * width, message.y1 * height);
    context.lineTo(message.x2 * width, message.y2 * height);
    context.stroke();
    //0.4449152542372881,0.18916155419222905#0.4449152542372881,0.18507157464212678
}

let colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function unsub() {
    currentSubscription1.unsubscribe();
    currentSubscription2.unsubscribe();
    currentSubscription3.unsubscribe();
    currentSubscription4.unsubscribe();
    currentDrawSubscription.unsubscribe();
    scoreSubscription.unsubscribe();
    timerSubscription.unsubscribe();
    guessIdDisplaySubscription.unsubscribe();

    canvasForm.classList.add('hidden');
    //canvas.classList.add('hidden');
    unsubButton.classList.add('hidden');
    tableForm.classList.remove('hidden');
    chatPage.classList.add('hidden');
    userList.classList.add('hidden');

    //clear chatting before
    messageArea.innerHTML = '';

    //clear score
    userList.innerText = "";

    //clear canvas
    context.clearRect(0, 0, canvas.width, canvas.height);

    //enable input message
    messageInput.disabled = false;

    //clear timer
    timer1.innerText = "02:00";

    //clear open-guess
    guessOpened.innerHTML = '';
    guessIdDisplay.textContent = "test";
    guess.innerHTML = "";

    inGame = false;

    clearInterval(gameInterval);
}

function connecting(event) {
    roomInput = event.value;

    canvasForm.classList.remove('hidden');
    //canvas.classList.remove('hidden');
    unsubButton.classList.remove('hidden');
    tableForm.classList.add('hidden');
    chatPage.classList.remove('hidden');
    userList.classList.remove('hidden');
    //event.preventDefault();

    enterRoom(roomInput);
}

function onConnected() {
    connectingElement.classList.add('hidden');
    stompClient.subscribe(`/topic/table`, onChangeTable);
}

function onChangeTable() {
    $('#table').load(document.URL + ' #table')
}

function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}

// Leave the current room and enter a new one.
function enterRoom(roomId) {
    roomIdDisplay.textContent = roomId;
    path = `/app/chat/${roomId}`;

    stompClient.subscribe('/user/queue/canvas', onCanvas);
    queueSubscription = stompClient.subscribe('/user/queue/sendModal', getModalWindow);
    currentDrawSubscription = stompClient.subscribe(`/topic/${roomId}/draw`, onDraw);
    currentSubscription2 = stompClient.subscribe(`/topic/${roomId}/changeGuess`, changeGuess);
    currentSubscription1 = stompClient.subscribe(`/topic/${roomId}/public`, onMessageReceived);
    currentSubscription3 = stompClient.subscribe(`/topic/${roomId}/changeDrawUser`, onChangeDrawUser);
    currentSubscription4 = stompClient.subscribe(`/topic/${roomId}/end`, onEnd);
    scoreSubscription = stompClient.subscribe(`/topic/${roomId}/score`, onScore);
    timerSubscription = stompClient.subscribe(`/topic/${roomId}/timer`, onTimer);
    guessIdDisplaySubscription = stompClient.subscribe(`/topic/${roomId}/guessDisplay`, clearGuessDisplay);

    stompClient.send(`${path}/addUser`, {}, JSON.stringify({sender: username, type: 'JOIN'}));
    stompClient.send(`${path}/score`);
}

function onScore(payload) {
    let message = JSON.parse(payload.body);
    userList.innerText = "";
    userList.appendChild(document.createElement('td').appendChild(document.createTextNode(message.content)));
}

function onEnd(payload) {
    let message = JSON.parse(payload.body);

    //result text
    modalContent.appendChild(document.createElement('td').appendChild(document.createTextNode(message.content)));

    guessIdDisplay.textContent = '';
    endModal.style.display = "block";

    //unsub from modal window unsub from all
    queueSubscription.unsubscribe();
    currentDrawSubscription.unsubscribe();
    currentSubscription1.unsubscribe();
    currentSubscription2.unsubscribe();
    currentSubscription3.unsubscribe();
    currentSubscription4.unsubscribe();
    scoreSubscription.unsubscribe();
    timerSubscription.unsubscribe();
    guessIdDisplaySubscription.unsubscribe();

    inGame = false;
}

function onCanvas() {
    if (canvas.style['pointer-events'] === 'none'){
        canvas.style['pointer-events'] = "auto";
    } else {
        canvas.style['pointer-events'] = 'none';
    }

    messageInput.disabled = messageInput.disabled === false;
}

function onChangeDrawUser(payload) {
    let message = JSON.parse(payload.body);
    drawUser = message.sender;

    //dru.innerText = message.sender;

    //impl clear canvas
    context.clearRect(0, 0, canvas.width, canvas.height);
}

function clearCanvas() {
    context.clearRect(0, 0, canvas.width, canvas.height);
}

function getModalWindow(payload) {
    let message = JSON.parse(payload.body);

    guessButton1.textContent = message.content.split(",")[0];
    guessButton2.textContent = message.content.split(",")[1];
    guessButton3.textContent = message.content.split(",")[2];

    //modal.style.display = "block";
    $('#myModal').modal({backdrop: 'static', keyboard: false});

    guessButton1.onclick = function () {
        clearInterval(interval);
        document.getElementById("time").innerText = "00:05";

        stompClient.send(`${path}/changeGuess`, {}, JSON.stringify({content : guessButton1.textContent}));
        $('#myModal').modal('hide');
    };

    guessButton2.onclick = function () {
        clearInterval(interval);
        document.getElementById("time").innerText = "00:05";

        stompClient.send(`${path}/changeGuess`, {}, JSON.stringify({content : guessButton2.textContent}));
        $('#myModal').modal('hide');
    };

    guessButton3.onclick = function () {
        clearInterval(interval);
        document.getElementById("time").innerText = "00:05";

        stompClient.send(`${path}/changeGuess`, {}, JSON.stringify({content : guessButton3.textContent}));
        $('#myModal').modal('hide');
    };

    jQuery(function ($) {
        let fiveSeconds = 5, display = $('#time');
        startTimer(fiveSeconds, display);
    });

    let interval;
    function startTimer(duration, display) {
        let timer = duration, minutes, seconds;
        interval = setInterval(function () {
            minutes = parseInt(timer / 60, 10);
            seconds = parseInt(timer % 60, 10);

            minutes = minutes < 10 ? "0" + minutes : minutes;
            seconds = seconds < 10 ? "0" + seconds : seconds;

            display.text(minutes + ":" + seconds);

            if (--timer < 0) {
                clearInterval(interval);
                timer2.innerText = "00:05";

                let random = Math.floor(Math.random() * 4);
                stompClient.send(`${path}/changeGuess`, {}, JSON.stringify({content : message.content.split(",")[random]}));

                $('#myModal').modal('hide');
            }
        }, 1000);
    }
}

let guess = document.getElementById("guess-window-id");
let guessOpened = document.getElementById("guess-window-open-id");
let gameInterval;
function changeGuess(payload) {
    guess.innerHTML = '';
    guessOpened.innerHTML = '';

    let content = JSON.parse(payload.body).content;

    guessIdDisplay.textContent = content.split("#")[0];

    let randoms = [];
    for (let i = 0; i < content.split("#")[1].length; i++) {
        randoms.push(parseInt(content.split("#")[1].charAt(i)));
    }

    let word = content.split("#")[0];
    for (let i = 0; i < word.length; i++) {
        guess.appendChild(document.createElement('span').appendChild(document.createTextNode(word.charAt(i))));
        guessOpened.appendChild(document.createElement('span').appendChild(document.createTextNode(" _ ")));
    }

    jQuery(function ($) {
        let twoMinutes = 60 * 2, display = $('#timer');
        startGameTimer(twoMinutes, display);
    });

    let count = 0;
    function startGameTimer(duration, display) {
        let timer = duration, minutes, seconds;
        gameInterval = setInterval(function () {
            minutes = parseInt(timer / 60, 10);
            seconds = parseInt(timer % 60, 10);

            minutes = minutes < 10 ? "0" + minutes : minutes;
            seconds = seconds < 10 ? "0" + seconds : seconds;

            display.text(minutes + ":" + seconds);

            timer--;
            if (timer < 0) {
                clearInterval(gameInterval);
                timer1.innerText = "02:00";

                //stompClient.send(`${path}/timeOver`, {}, JSON.stringify({sender: username, content : drawUser, type: 'OVER'}));
            } else if (timer < 90 && count === 0){
                //open first letter
                guessOpened.childNodes[randoms[count]].textContent = " " + word.charAt(randoms[count]) + " ";
                count++;
            } else if (timer < 60 && count === 1){
                //open second letter
                guessOpened.childNodes[randoms[count]].textContent = " " + word.charAt(randoms[count]) + " ";
                count++;
            } else if (timer < 30 && count === 2){
                //open third letter
                guessOpened.childNodes[randoms[count]].textContent = " " + word.charAt(randoms[count]) + " ";
                count++;
            }
        }, 1000);
    }
    //guessOpened.childNodes[3].textContent = " A ";
}

function onTimer() {
    clearInterval(gameInterval);
    timer1.innerText = "02:00";
}

function clearGuessDisplay() {
    guessIdDisplay.textContent = "";
}

function sendMessage(event) {
    let messageContent = messageInput.value.trim();
    if (messageContent && stompClient) {
        let chatMessage = {
            sender: username,
            content: messageInput.value,
            type: 'CHAT'
        };

        if (chatMessage.content === guessIdDisplay.textContent) {

            let answer = chatMessage.content;

            //clear guessdidsplay
            stompClient.send(`${path}/guessDisplay`, {});

            //reset color to def
            context.strokeStyle = "#000000";

            //start the game
            if (chatMessage.content === 'test' && inGame === false){
                inGame = true;
                stompClient.send(`/app/chat/table`, {}, JSON.stringify({content : roomInput}));
            }

            //reset timer
            stompClient.send(`${path}/timer`, {});

            stompClient.send(`${path}/changeDrawUser`, {}, JSON.stringify({
                sender: username + "#" + answer,
                content : drawUser,
                type: 'GUESS'
            }));

            //update score
            stompClient.send(`${path}/score`);

        } else {
            stompClient.send(`${path}/sendMessage`, {}, JSON.stringify(chatMessage));
        }
    }
    messageInput.value = '';
    event.preventDefault();
}

function onMessageReceived(payload) {
    let message = JSON.parse(payload.body);

    let messageElement = document.createElement('li');

    if (message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' joined!';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' left!';
    } else if (message.type === "GUESS") {
        messageElement.classList.add('event-message');
        message.content = message.sender.split("#")[0] + ' отгадал ' + message.sender.split("#")[1].toUpperCase();
    } else {
        messageElement.classList.add('chat-message');

        let avatarElement = document.createElement('i');
        let avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);

        messageElement.appendChild(avatarElement);

        let usernameElement = document.createElement('span');
        let usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
    }

    let textElement = document.createElement('p');
    let messageText = document.createTextNode(message.content);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

function getAvatarColor(messageSender) {
    let hash = 0;
    for (let i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    let index = Math.abs(hash % colors.length);
    return colors[index];
}

messageForm.addEventListener('submit', sendMessage, true);

//disable back button in browser
window.onbeforeunload = function() { return "Your work will be lost."; };