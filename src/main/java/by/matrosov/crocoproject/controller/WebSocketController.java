package by.matrosov.crocoproject.controller;

import by.matrosov.crocoproject.model.message.ChatMessage;
import by.matrosov.crocoproject.model.message.DrawMessage;
import by.matrosov.crocoproject.model.message.ScoreMessage;
import by.matrosov.crocoproject.service.game.GameService;
import by.matrosov.crocoproject.service.room.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.*;

@Controller
public class WebSocketController {
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private GameService gameService;

    @Autowired
    private RoomService roomService;

    @MessageMapping("/chat/{roomId}/timer")
    public void updateTimer(@DestinationVariable String roomId) {
        messagingTemplate.convertAndSend(String.format("/topic/%s/timer", roomId), new ChatMessage());
    }

    @MessageMapping("/chat/table")
    public void updateTable(@Payload ChatMessage chatMessage) {
        //close room
        roomService.changeRoomState(Long.parseLong(chatMessage.getContent()));
        messagingTemplate.convertAndSend("/topic/table", chatMessage);
    }

    @MessageMapping("/chat/{roomId}/sendMessage")
    public void sendMessage(@DestinationVariable String roomId, @Payload ChatMessage chatMessage) {
        messagingTemplate.convertAndSend(String.format("/topic/%s/public", roomId), chatMessage);
    }

    @MessageMapping("/chat/{roomId}/changeDrawUser")
    public void changeDrawUser(@DestinationVariable String roomId, @Payload ChatMessage chatMessage, Principal principal){

        //get prev user
        String prevUser = chatMessage.getContent();

        //next user
        String name = "";

        //send message to the chat
        messagingTemplate.convertAndSend(String.format("/topic/%s/public", roomId), chatMessage);
        //remove back sender
        chatMessage.setSender(chatMessage.getSender().split("#")[0]);

        if (prevUser != null){
            //add score
            if (gameService.addScore(prevUser, principal.getName(), roomId)){
                //is end
                chatMessage.setContent(Arrays.toString(gameService.getScore(roomId))); //replace on score message
                messagingTemplate.convertAndSend(String.format("/topic/%s/end", roomId), chatMessage);

                //open room
                //roomService.changeRoomState(Integer.parseInt(roomId));
                //messagingTemplate.convertAndSend("/topic/table", chatMessage);

                //delete room
                roomService.delete(Long.parseLong(roomId));
            }else {
                //set prev user to disable canvas
                messagingTemplate.convertAndSendToUser(prevUser, "/queue/canvas", chatMessage);
                name = gameService.getNextUser(prevUser, roomId);
            }
            gameService.print(); //update score?
        }else {
            name = principal.getName();
        }

        //update score
        ScoreMessage scoreMessage = new ScoreMessage();
        scoreMessage.setUsersScore(gameService.getScore(roomId));
        messagingTemplate.convertAndSend(String.format("/topic/%s/score", roomId), scoreMessage);

        //send modal window
        chatMessage.setContent(gameService.getRandomWords());
        messagingTemplate.convertAndSendToUser(name, "/queue/sendModal", chatMessage);

        // set current user to DRAWING
        chatMessage.setSender(name);
        messagingTemplate.convertAndSend(String.format("/topic/%s/changeDrawUser", roomId), chatMessage);

        //set current user to enable canvas
        messagingTemplate.convertAndSendToUser(name, "/queue/canvas", chatMessage);
    }

    @MessageMapping("/chat/{roomId}/addUser")
    public void addUser(@DestinationVariable String roomId, @Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
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

    @MessageMapping("/chat/{roomId}/changeGuess")
    public void changeGuess(@DestinationVariable String roomId, @Payload ChatMessage chatMessage){

        String word = chatMessage.getContent();

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

    @MessageMapping("/chat/{roomId}/guessDisplay")
    public void clearGuessDisplay(@DestinationVariable String roomId){
        messagingTemplate.convertAndSend(String.format("/topic/%s/guessDisplay", roomId), new ChatMessage());
    }

    @MessageMapping("/chat/{roomId}/resetCanvas")
    public void resetCanvas(@DestinationVariable String roomId){
        messagingTemplate.convertAndSend(String.format("/topic/%s/resetCanvas", roomId), new ChatMessage());
    }
}
