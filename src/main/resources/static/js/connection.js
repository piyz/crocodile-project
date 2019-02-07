let stompClient = null;
let connectingElement = document.querySelector(".connecting");

let socket = new SockJS('/ws');
stompClient = Stomp.over(socket);
stompClient.connect({}, onConnected, onError);

function onConnected() {
    connectingElement.classList.add('hidden');
    stompClient.subscribe(`/topic/table`, onChangeTable);
    stompClient.subscribe('/user/queue/canvas', onCanvas);
    stompClient.subscribe('/user/queue/sendModal', onModalWindow);
}

function onCanvas() {
    if (canvas.style['pointer-events'] === 'none'){
        canvas.style['pointer-events'] = "auto";
    } else {
        canvas.style['pointer-events'] = 'none';
    }

    messageInput.disabled = messageInput.disabled === false;
    resetButton.disabled = resetButton.disabled === false;
}

function onModalWindow(payload) {
    let message = JSON.parse(payload.body);

    guessButton1.textContent = message.content.split(",")[0];
    guessButton2.textContent = message.content.split(",")[1];
    guessButton3.textContent = message.content.split(",")[2];

    $('#myModal').modal({backdrop: 'static', keyboard: false});

    guessButton1.onclick = function () {
        clearInterval(interval);
        id("timer2").innerText = "00:05";

        stompClient.send(`${path}/changeGuess`, {}, JSON.stringify({content : guessButton1.textContent}));
        $('#myModal').modal('hide');
    };

    guessButton2.onclick = function () {
        clearInterval(interval);
        id("timer2").innerText = "00:05";

        stompClient.send(`${path}/changeGuess`, {}, JSON.stringify({content : guessButton2.textContent}));
        $('#myModal').modal('hide');
    };

    guessButton3.onclick = function () {
        clearInterval(interval);
        id("timer2").innerText = "00:05";

        stompClient.send(`${path}/changeGuess`, {}, JSON.stringify({content : guessButton3.textContent}));
        $('#myModal').modal('hide');
    };

    jQuery(function ($) {
        let fiveSeconds = 5, display = $('#timer2');
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

                let random = Math.floor(Math.random() * 3);
                stompClient.send(`${path}/changeGuess`, {}, JSON.stringify({content : message.content.split(",")[random]}));

                $('#myModal').modal('hide');
            }
        }, 1000);
    }
}

function onChangeTable() {
    $('#table').load(document.URL + ' #table')
}

function onError() {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}