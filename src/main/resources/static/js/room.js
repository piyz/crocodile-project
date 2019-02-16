let roomId;
let path = null;

function join2room(event) {
    roomId = event.value;

    tableForm.classList.add('hidden');
    canvasForm.classList.remove('hidden');
    chatPage.classList.remove('hidden');
    userList.classList.remove('hidden');

    roomIdDisplay.textContent = roomId;
    path = `/app/chat/${roomId}`;

    drawSubscription = stompClient.subscribe(`/topic/${roomId}/draw`, onDraw);
    changeGuessSubscription = stompClient.subscribe(`/topic/${roomId}/changeGuess`, onChangeGuess);
    messageReceivedSubscription = stompClient.subscribe(`/topic/${roomId}/public`, onMessageReceived);
    changeDrawUserSubscription = stompClient.subscribe(`/topic/${roomId}/changeDrawUser`, onChangeDrawUser);
    endSubscription = stompClient.subscribe(`/topic/${roomId}/end`, onEnd);
    scoreSubscription = stompClient.subscribe(`/topic/${roomId}/score`, onScore);
    timerSubscription = stompClient.subscribe(`/topic/${roomId}/timer`, onTimer);
    guessIdDisplaySubscription = stompClient.subscribe(`/topic/${roomId}/guessDisplay`, onClearGuessDisplay);
    resetCanvasSubscription = stompClient.subscribe(`/topic/${roomId}/resetCanvas`, onClearCanvas);

    stompClient.send(`${path}/addUser`, {}, JSON.stringify({sender: username, type: 'JOIN'}));
    stompClient.send(`${path}/score`);
}

function onDraw(payload){
    let message = JSON.parse(payload.body);

    context.lineCap = 'round';
    context.lineWidth = 3;
    context.strokeStyle = message.color;

    context.beginPath();
    context.moveTo(message.x1 * width, message.y1 * height);
    context.lineTo(message.x2 * width,message.y2 * height);
    context.stroke();
}

function onChangeGuess(payload) {

    guessOpened.innerHTML = '';

    let content = JSON.parse(payload.body).content;

    guess = content.split("#")[0];

    let randoms = [];
    for (let i = 0; i < content.split("#")[1].length; i++) {
        randoms.push(parseInt(content.split("#")[1].charAt(i)));
    }

    let word = content.split("#")[0];
    for (let i = 0; i < word.length; i++) {
        guessOpened.appendChild(document.createElement('span').appendChild(document.createTextNode(" _ ")));
    }

    jQuery(function ($) {
        let zeroMinutes = 0, display = $('#timer1');
        startGameTimer(zeroMinutes, display);
    });

    let i = 1;
    function startGameTimer(duration, display) {
        let timer = duration, minutes, seconds;
        gameInterval = setInterval(function () {
            minutes = parseInt(timer / 60, 10);
            seconds = parseInt(timer % 60, 10);

            minutes = minutes < 10 ? "0" + minutes : minutes;
            seconds = seconds < 10 ? "0" + seconds : seconds;

            display.text(minutes + ":" + seconds);

            timer++;
            if (timer === i * 30 && (i - 1) < word.length){
                guessOpened.childNodes[randoms[i - 1]].textContent = " " + word.charAt(randoms[i - 1]) + " ";
                i++;
            }

        }, 1000);
    }
}

function onMessageReceived(payload) {
    let message = JSON.parse(payload.body);

    let messageElement = document.createElement('li');

    if (message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' присоединился!';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' вышел!';
    } else if (message.type === "GUESS") {
        if (message.sender.split("#")[1] === "/start") {
            messageElement.classList.add('event-message');
            message.content = message.sender.split("#")[0] + ' запустил игру';
        }else {
            messageElement.classList.add('event-message');
            message.content = message.sender.split("#")[0] + ' написал ' + message.sender.split("#")[1].toUpperCase();
        }
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

function onChangeDrawUser(payload) {
    let message = JSON.parse(payload.body);
    drawUser = message.sender;

    //clear canvas
    context.clearRect(0, 0, canvas.width, canvas.height);
}

function onEnd(payload) {
    let message = JSON.parse(payload.body);

    //TODO remove first and last char
    let score = message.content.split(",");
    for (let i = 0; i < score.length; i++) {
        modalContent.appendChild(document.createElement('h1').appendChild(document.createTextNode(score[i])));
    }

    $('#endModal').modal({backdrop: 'static', keyboard: false});

    guess = "";
    endModal.style.display = "block";

    //unsub from all
    drawSubscription.unsubscribe();
    messageReceivedSubscription.unsubscribe();
    changeGuessSubscription.unsubscribe();
    changeDrawUserSubscription.unsubscribe();
    endSubscription.unsubscribe();
    scoreSubscription.unsubscribe();
    timerSubscription.unsubscribe();
    guessIdDisplaySubscription.unsubscribe();
    resetCanvasSubscription.unsubscribe();

    inGame = false;
}

function onScore(payload) {
    let message = JSON.parse(payload.body);
    userList.innerHTML = "";
    let users = message.usersScore;
    for (let i = 0; i < users.length; i++) {
        let messageElement = document.createElement('li');
        //messageElement.classList.add();
        let usernameElement = document.createElement('span');
        let usernameText = document.createTextNode(users[i]);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
        userList.appendChild(messageElement);
    }
}

function onTimer() {
    clearInterval(gameInterval);
    timer1.innerText = "00:00";
}

function onClearGuessDisplay() {
    guess = "";
}

function onClearCanvas() {
    context.clearRect(0, 0, canvas.width, canvas.height);
}