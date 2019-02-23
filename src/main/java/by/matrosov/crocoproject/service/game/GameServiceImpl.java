package by.matrosov.crocoproject.service.game;

import by.matrosov.crocoproject.model.Dictionary;
import by.matrosov.crocoproject.model.Room;
import by.matrosov.crocoproject.model.User;
import by.matrosov.crocoproject.service.dictionary.DictionaryService;
import by.matrosov.crocoproject.service.room.RoomService;
import by.matrosov.crocoproject.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameServiceImpl implements GameService {
    private static final Logger logger = LoggerFactory.getLogger(GameServiceImpl.class);
    private static final int FINAL_SCORE = 100;

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserService userService;

    @Override
    public synchronized void addUser(String username, String roomid) {
        Room room = roomService.getRoomById(Long.parseLong(roomid));
        User user = userService.getUserByUsername(username);

        //init score
        if (user.getScore() != 0){
            user.setScore(0);
            userService.update(user);
            logger.info("init score for " + username);
        }

        //add user to room
        if (room.getUsers() == null){
            Set<User> set = new HashSet<>();
            set.add(user);
            room.setUsers(set);
        }else {
            Set<User> oldSet = room.getUsers();
            oldSet.add(user);
            room.setUsers(oldSet);
        }
        roomService.update(room);
        logger.info(username + " connected to the room " + roomid);
    }

    @Override
    public synchronized void removeUser(String username, String roomid) {
        Room room = roomService.getRoomById(Long.parseLong(roomid));
        User user = userService.getUserByUsername(username);

        Set<User> usersSet = room.getUsers();
        if (usersSet.size() == 1){
            room.setUsers(new HashSet<>());
            roomService.delete(room);
            logger.info("delete room " + roomid);
        }else {
            usersSet.remove(user);
            room.setUsers(usersSet);
            roomService.update(room);
            logger.info(username + " left from room " + roomid);
        }
        //if last user and closed room -> delete room
    }

    @Override
    public synchronized boolean addScore(String drawer, String guesser) {
        User drawerUser = userService.getUserByUsername(drawer);
        User guesserUser = userService.getUserByUsername(guesser);

        drawerUser.setScore(drawerUser.getScore() + 5);
        guesserUser.setScore(guesserUser.getScore() + 6);
        logger.info(drawer + " get 5 point");
        logger.info(guesser + " get 6 point");

        userService.update(drawerUser);
        userService.update(guesserUser);

        return drawerUser.getScore() >= FINAL_SCORE || guesserUser.getScore() >= FINAL_SCORE;

    }

    @Override
    public synchronized String[] getScore(String roomid) throws NullPointerException {

        //TODO rewrite this logic

        Room room = roomService.getRoomById(Long.parseLong(roomid));
        Set<User> userSet = room.getUsers();

        Map<String, Integer> map = new HashMap<>();
        for (User user : userSet) {
            map.put(user.getUsername(), user.getScore());
        }

        String score = Arrays.toString(map.entrySet().toArray());

        return score.substring(1, score.length() - 1)
                .replace("=", " ")
                .split(",");
    }

    @Override
    public synchronized String getNextUser(String username, String roomid) {
        Room room = roomService.getRoomById(Long.parseLong(roomid));

        List<String> users = new ArrayList<>();
        for (User user : room.getUsers()) {
            users.add(user.getUsername());
        }

        int k = 0;
        while (true){
            if (users.get(k).equals(username)){
                if (k + 1 == users.size()){
                    logger.info("next user is " + users.get(0));
                    return users.get(0);
                }else {
                    logger.info("next user is " + users.get(k+1));
                    return users.get(k+1);
                }
            }
            k++;
        }
    }

    @Override
    public synchronized String getRandomWords() {
        Random random = new Random();
        List<Dictionary> list = dictionaryService.getAllWords();
        int r1 = random.nextInt(list.size());
        int r2 = random.nextInt(list.size());
        int r3 = random.nextInt(list.size());

        return list.get(r1).getValue() + "," + list.get(r2).getValue() + "," + list.get(r3).getValue();
    }

    @Override
    public synchronized boolean isRightAnswer(String message, String roomid) {
        Room room = roomService.getRoomById(Long.parseLong(roomid));
        String guess = room.getGuess();
        return message.equals(guess);
    }

    @Override
    public synchronized boolean isGameStart(String message, String roomid) {
        Room room = roomService.getRoomById(Long.parseLong(roomid));
        return message.equals("/start") && room.isOpen();
    }

    @Override
    public synchronized void clearGuess(String roomid) {
        logger.info("clear guess in room '" + roomid + "'");
        roomService.clearGuess(Long.parseLong(roomid));
    }

    @Override
    public synchronized String getDrawerUser(String roomId) {
        Room room = roomService.getRoomById(Long.parseLong(roomId));
        return room.getDrawer();
    }

    @Override
    public synchronized void setDrawerUser(String roomId, String username) {
        logger.info("in room '" + roomId + "'" + " set drawer '" + username + "'");
        Room room = roomService.getRoomById(Long.parseLong(roomId));
        room.setDrawer(username);
        roomService.update(room);
    }

    @Override
    public synchronized void setGuess(String roomId, String word) {
        roomService.setWord(Long.parseLong(roomId), word);
    }
}
