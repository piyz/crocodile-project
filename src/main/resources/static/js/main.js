// small helper function for selecting element by id
let id = id => document.getElementById(id);

let drawUser = null;
let inGame = false;

let username = id("username").innerHTML;
let chatPage = id("chat-page");
let messageForm = id("messageForm");
let messageInput = id("message");
let messageArea = id("messageArea");
let roomIdDisplay = id("room-id-display");
let guessIdDisplay = id("guess-id-display"); guessIdDisplay.textContent = "/start"; //for test
let tableForm = id("table");
let unsubButton = id("unsubscribe");
let userList = id("userlist");
let resetButton = id("resetButton");

// guess buttons
let guessButton1 = id("guess-button-id-1");
let guessButton2 = id("guess-button-id-2");
let guessButton3 = id("guess-button-id-3");

// modal window
let modal = id("myModal");
let endModal = id("endModal");
let modalContent = id("modal-cont");

// timers
let timer1 = id("timer1"); // game
let timer2 = id("timer2"); // modal

// guess
let guessOpened = id("guess-window-open-id");
let gameInterval;

// subscriptions
let messageReceivedSubscription;
let changeGuessSubscription;
let changeDrawUserSubscription;
let endSubscription;
let drawSubscription;
let scoreSubscription;
let timerSubscription;
let guessIdDisplaySubscription;
let resetCanvasSubscription;

function unsubscribe() {
    messageReceivedSubscription.unsubscribe();
    changeGuessSubscription.unsubscribe();
    changeDrawUserSubscription.unsubscribe();
    endSubscription.unsubscribe();
    drawSubscription.unsubscribe();
    scoreSubscription.unsubscribe();
    timerSubscription.unsubscribe();
    guessIdDisplaySubscription.unsubscribe();
    resetCanvasSubscription.unsubscribe();

    canvasForm.classList.add('hidden');
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

    //enable input message and reset button
    messageInput.disabled = false;
    resetButton.disabled = true;

    //clear timer
    timer1.innerText = "02:00";

    //clear open-guess
    guessOpened.innerHTML = '';
    guessIdDisplay.textContent = "/start";

    inGame = false;
    drawUser = null;

    clearInterval(gameInterval);

    //TODO disable canvas
    canvas.style['pointer-events'] = 'none';
}

function clearCanvas() {
    stompClient.send(`${path}/resetCanvas`);
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
            stompClient.send(`${path}/guessDisplay`);

            //reset color to def
            context.strokeStyle = "#000000";

            //start the game
            if (chatMessage.content === '/start' && inGame === false){
                inGame = true;
                stompClient.send(`/app/chat/table`, {}, JSON.stringify({content : roomId}));
            }

            //reset timer
            stompClient.send(`${path}/timer`);

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

messageForm.addEventListener('submit', sendMessage, true);
unsubButton.addEventListener("click", unsubscribe);
resetButton.addEventListener("click", clearCanvas);

//disable back button in browser
window.onbeforeunload = function() { return "Your work will be lost."; };
window.console.log = function(){
    //console.error('Sorry , developers tools are blocked here....');
    window.console.log = function() {
        return false;
    }
};