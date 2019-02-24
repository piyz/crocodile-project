package by.matrosov.crocoproject.controller;

import by.matrosov.crocoproject.model.Room;
import by.matrosov.crocoproject.model.message.*;
import by.matrosov.crocoproject.service.game.GameService;
import by.matrosov.crocoproject.service.room.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Controller
public class WebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private GameService gameService;

    @Autowired
    private RoomService roomService;

    @MessageMapping("/chat/{roomId}/chatMessage")
    public void getChatMessage(@DestinationVariable String roomId, @Payload ChatMessage chatMessage){
        String message = chatMessage.getContent();
        String sender = chatMessage.getSender();
        logger.info("received chat message '" + message + "' from '" + sender + "' in room '" + roomId + "'");

        if (gameService.isRightAnswer(message, roomId)){
            logger.info("message '" + message + "' is the right answer");

            gameService.clearGuess(roomId);

            //reset pointer color to def

            if (gameService.isGameStart(message, roomId)){
                logger.info("game started in room " + roomId);
                updateTable(roomId);
            }

            updateTimer(roomId);
            changeDrawUser(roomId, message, sender);

        } else {
            sendMessage(roomId, chatMessage);
        }
    }

    private void changeDrawUser(String roomId, String message, String sender){
        logger.info("change drawer in room " + roomId);
        sendEventMessage(roomId, sender, message);

        String prevUser = gameService.getDrawerUser(roomId);
        String nextUser = "";
        boolean isEnd = false;
        if (prevUser != null){
            logger.info("previous user is " + prevUser);

            if (gameService.addScore(prevUser, sender)){
                logger.info("end game in room " + roomId);
                sendEndModalWindow(roomId);
                roomService.roomOnEnd(Long.parseLong(roomId));
                isEnd = true;
            }else {
                logger.info("continue game in room " + roomId);
                disableCanvas(prevUser);

                nextUser = gameService.getNextUser(prevUser, roomId);
            }
        } else {
            logger.info("previous user is null");
            nextUser = sender;
        }

        if (!isEnd){
            updateScore(roomId);
            sendModalWindow(nextUser);
            setDrawerUser(roomId, nextUser);
            enableCanvas(nextUser);
        }
    }

    private void sendEndModalWindow(String roomId){
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(Arrays.toString(gameService.getScore(roomId))); //replace on score message
        messagingTemplate.convertAndSend(String.format("/topic/%s/end", roomId), chatMessage);
    }

    private void disableCanvas(String prevUser){
        messagingTemplate.convertAndSendToUser(prevUser, "/queue/canvas", new ChatMessage());
    }

    private void enableCanvas(String nextUser){
        logger.info("enable canvas for " + nextUser);
        //empty message
        messagingTemplate.convertAndSendToUser(nextUser, "/queue/canvas", new ChatMessage());
    }

    private void setDrawerUser(String roomId, String nextUser){
        logger.info("change draw user in room " + roomId);
        gameService.setDrawerUser(roomId, nextUser);
    }

    private void sendModalWindow(String nextUser){
        logger.info("send modal window to " + nextUser);
        //message with 3 words
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(gameService.getRandomWords());
        messagingTemplate.convertAndSendToUser(nextUser, "/queue/sendModal", chatMessage);
    }

    private void updateScore(String roomId){
        logger.info("update score in room " + roomId);
        ScoreMessage scoreMessage = new ScoreMessage();
        scoreMessage.setUsersScore(gameService.getScore(roomId));
        messagingTemplate.convertAndSend(String.format("/topic/%s/score", roomId), scoreMessage);
    }

    private void updateTable(String roomId){
        logger.info("close room " + roomId);
        roomService.changeRoomState(Long.parseLong(roomId));
        //send empty message
        messagingTemplate.convertAndSend("/topic/table", new ChatMessage());
    }

    private void updateTimer(String roomId){
        logger.info("update timer in room " + roomId);
        //send empty message
        messagingTemplate.convertAndSend(String.format("/topic/%s/timer", roomId), new ChatMessage());
    }

    private void sendMessage(String roomId, ChatMessage chatMessage){
        String message = chatMessage.getContent();
        logger.info("send message '" + message + "' to the room '" + roomId + "'");
        messagingTemplate.convertAndSend(String.format("/topic/%s/public", roomId), chatMessage);
    }

    private void sendEventMessage(String roomId, String sender, String message){
        GuessMessage guessMessage = new GuessMessage();
        guessMessage.setSender(sender);
        guessMessage.setAnswer(message);
        guessMessage.setType(ChatMessage.MessageType.GUESS);
        logger.info("send event message to the room " + roomId);
        messagingTemplate.convertAndSend(String.format("/topic/%s/public", roomId), guessMessage);
    }

    @MessageMapping("/chat/{roomId}/addUser")
    public void addUser(@DestinationVariable String roomId, @Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {

        if (headerAccessor.getSessionAttributes().get("room_id") == null){

            String currentRoomId = (String) headerAccessor.getSessionAttributes().put("room_id", roomId);

            if (currentRoomId != null) {
                ChatMessage leaveMessage = new ChatMessage();
                leaveMessage.setType(ChatMessage.MessageType.LEAVE);
                leaveMessage.setSender(chatMessage.getSender());
                messagingTemplate.convertAndSend(String.format("/topic/%s/public", currentRoomId), leaveMessage);
            }

            headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
            messagingTemplate.convertAndSend(String.format("/topic/%s/public", roomId), chatMessage);
        }
    }

    @MessageMapping("/chat/{roomId}/changeGuess")
    public void changeGuess(@DestinationVariable String roomId, @Payload WordMessage wordMessage){

        String word = wordMessage.getWord();

        //get indexes for letters which will be open
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < word.length(); i++) {
            list.add(i);
        }
        Collections.shuffle(list);

        String result = word + "#";
        StringBuilder sb = new StringBuilder(result);
        for (Integer integer : list) {
            sb.append(integer);
        }

        gameService.setGuess(roomId, word);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(sb.toString());

        messagingTemplate.convertAndSend(String.format("/topic/%s/changeGuess", roomId), chatMessage);
    }

    @MessageMapping("/chat/{roomId}/draw")
    public void draw(@DestinationVariable String roomId, @Payload ChatMessage chatMessage){
        DrawMessage drawMessage = new DrawMessage();
        drawMessage.setSender(chatMessage.getSender());
        drawMessage.setX1(Float.parseFloat(chatMessage.getContent().split("#")[0].split(",")[0]));
        drawMessage.setY1(Float.parseFloat(chatMessage.getContent().split("#")[0].split(",")[1]));
        drawMessage.setX2(Float.parseFloat(chatMessage.getContent().split("#")[1].split(",")[0]));
        drawMessage.setY2(Float.parseFloat(chatMessage.getContent().split("#")[1].split(",")[1]));
        drawMessage.setColor("#" + chatMessage.getContent().split("#")[2]);

        messagingTemplate.convertAndSend(String.format("/topic/%s/draw", roomId), drawMessage);
    }

    @MessageMapping("/chat/{roomId}/resetCanvas")
    public void resetCanvas(@DestinationVariable String roomId, SimpMessageHeaderAccessor headerAccessor){
        Room room = roomService.getRoomById(Long.parseLong(roomId));
        String drawer = room.getDrawer();

        if (headerAccessor.getUser().getName().equals(drawer)){
            messagingTemplate.convertAndSend(String.format("/topic/%s/resetCanvas", roomId), new ChatMessage());
        }
    }
}
