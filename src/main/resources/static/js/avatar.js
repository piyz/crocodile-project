let colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function getAvatarColor(messageSender) {
    let hash = 0;
    for (let i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    let index = Math.abs(hash % colors.length);
    return colors[index];
}

function getAvatarEmotion(fontAwesome, i, users) {
    if (i === 0){
        fontAwesome.className = "far fa-smile";
    }else if (i === users.length - 1){
        fontAwesome.className = "far fa-frown";
    }else {
        fontAwesome.className = "far fa-meh";
    }
}