// small helper function for selecting element by id
let id = id => document.getElementById(id);

let username = id("username").innerHTML;
let chatPage = id("chat-page");
let messageForm = id("messageForm");
let messageInput = id("message");
let messageArea = id("messageArea");
let roomIdDisplay = id("room-id-display");

let tableForm = id("table");
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
let endSubscription;
let drawSubscription;
let scoreSubscription;
let timerSubscription;
let resetCanvasSubscription;

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
        stompClient.send(`${path}/chatMessage`, {}, JSON.stringify(chatMessage));
    }
    messageInput.value = '';
    event.preventDefault();
}

messageForm.addEventListener('submit', sendMessage, true);
resetButton.addEventListener("click", clearCanvas);

window.console.log = function(){
    //console.error('Sorry , developers tools are blocked here....');
    window.console.log = function() {
        return false;
    }
};
