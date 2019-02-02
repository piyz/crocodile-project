let stompClient = null;
let connectingElement = document.querySelector(".connecting");

let socket = new SockJS('/ws');
stompClient = Stomp.over(socket);
stompClient.connect({}, onConnected, onError);

function onConnected() {
    connectingElement.classList.add('hidden');
    stompClient.subscribe(`/topic/table`, onChangeTable);
}

function onChangeTable() {
    $('#table').load(document.URL + ' #table')
}

function onError() {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}