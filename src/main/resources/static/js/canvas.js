let canvasForm = id("canvas-form");
let canvas  = id("drawing");
let context = canvas.getContext("2d");
let width   = canvas.getAttribute("width");
let height  = canvas.getAttribute("height");

// drawing
document.addEventListener("DOMContentLoaded", function() {
    let mouse = [false, false, [0,0], false];

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

// color buttons
id("red").addEventListener("click", function () {context.strokeStyle = "#F00000"});
id("yellow").addEventListener("click", function () {context.strokeStyle = "#F0DE10"});
id("blue").addEventListener("click", function () {context.strokeStyle = "#001FF0"});
id("black").addEventListener("click", function () {context.strokeStyle = "#090607"});
id("green").addEventListener("click", function () {context.strokeStyle = "#1e8118"});