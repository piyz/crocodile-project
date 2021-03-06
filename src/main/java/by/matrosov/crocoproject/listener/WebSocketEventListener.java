package by.matrosov.crocoproject.listener;

import by.matrosov.crocoproject.model.message.ChatMessage;
import by.matrosov.crocoproject.model.message.ScoreMessage;
import by.matrosov.crocoproject.service.game.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Component
public class WebSocketEventListener {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private GameService gameService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("Received a new web socket connection");
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String destination = headerAccessor.getDestination();
        if (destination.contains("public")){
            String username = headerAccessor.getUser().getName();
            String roomid = destination.split("/")[2];
            gameService.addUser(username, roomid);

            //update score after new user connected
            updateScore(roomid);
        }

        if (destination.contains("table")){
            updateTable();
        }

        //>>> SUBSCRIBE
        //id:sub-0
        //destination:/topic/table

        //System.out.println(headerAccessor.getSessionId());
        //System.out.println(headerAccessor.getSubscriptionId());
        //System.out.println(headerAccessor.getDestination());
        //System.out.println(headerAccessor.getUser().getName());
        // pwtgtupt
        // sub-0
        // /topic/2/public
        // user
    }

    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        if (headerAccessor.getSubscriptionId().contains("sub-4")){
            String username = (String) headerAccessor.getSessionAttributes().get("username");
            String roomId = (String) headerAccessor.getSessionAttributes().get("room_id");

            //update score
            try{
                updateScore(roomId);
            }catch (NullPointerException ignored){ }

            //notify about left from the room
            roomLeft(username, roomId);

            //remove roomId after left the room
            headerAccessor.getSessionAttributes().remove("room_id");

            updateTable();
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String roomId = (String) headerAccessor.getSessionAttributes().get("room_id");
        if (username != null) {
            logger.info("User Disconnected: " + username);
            roomLeft(username, roomId);

            if (roomId != null){
                try{
                    gameService.removeUser(username, roomId);
                    updateScore(roomId);
                }catch (NullPointerException e){
                    logger.info("get error " + e);
                }
            }

            updateTable();
        }
    }

    private void updateTable(){
        logger.info("update table");
        //send empty message
        messagingTemplate.convertAndSend("/topic/table", new ChatMessage());
    }

    private void roomLeft(String username, String roomId){
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(ChatMessage.MessageType.LEAVE);
        chatMessage.setSender(username);
        messagingTemplate.convertAndSend(String.format("/topic/%s/public", roomId), chatMessage);
    }

    private void updateScore(String roomId) throws NullPointerException {
        ScoreMessage scoreMessage = new ScoreMessage();
        scoreMessage.setUsersScore(gameService.getScore(roomId));
        messagingTemplate.convertAndSend(String.format("/topic/%s/score", roomId), scoreMessage);
    }
}
