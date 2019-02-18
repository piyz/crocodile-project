package by.matrosov.crocoproject.service.game;

import by.matrosov.crocoproject.model.Dictionary;
import by.matrosov.crocoproject.model.Room;
import by.matrosov.crocoproject.model.User;
import by.matrosov.crocoproject.repository.DictionaryRepository;
import by.matrosov.crocoproject.repository.RoomRepository;
import by.matrosov.crocoproject.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.*;

@Service
public class GameServiceImpl implements GameService{

    private static final Logger logger = LoggerFactory.getLogger(HandshakeInterceptor.class);

    private static final int FINAL_SCORE = 100;

    //replace on service
    @Autowired
    private DictionaryRepository dictionaryRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public synchronized void addUser(String username, String roomid) {
        Room room = roomRepository.findRoomByRoomId(Long.parseLong(roomid));
        User user = userRepository.findByUsername(username);

        //init score
        if (user.getScore() != 0){
            user.setScore(0);
            userRepository.save(user);
            logger.info("init score for user: " + username);
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
        roomRepository.save(room);
        logger.info("%s connected to the room %s", username, roomid);
    }

    @Override
    public synchronized void removeUser(String username, String roomid) throws NullPointerException {
        Room room = roomRepository.findRoomByRoomId(Long.parseLong(roomid));
        User user = userRepository.findByUsername(username);

        Set<User> usersSet = room.getUsers();
        usersSet.remove(user);
        room.setUsers(usersSet);

        roomRepository.save(room);
        logger.info("remove user: %s from room: %s", username, roomid);
    }

    @Override
    public synchronized boolean addScore(String drawer, String guesser) {

        User drawerUser = userRepository.findByUsername(drawer);
        User guesserUser = userRepository.findByUsername(guesser);

        drawerUser.setScore(drawerUser.getScore() + 5);
        guesserUser.setScore(guesserUser.getScore() + 6);
        logger.info("user: %s get 5 point", drawer);
        logger.info("user: %s get 6 point", guesser);

        userRepository.save(drawerUser);
        userRepository.save(guesserUser);

        return drawerUser.getScore() >= FINAL_SCORE || guesserUser.getScore() >= FINAL_SCORE;

    }

    @Override
    public synchronized String[] getScore(String roomid) throws NullPointerException{

        //TODO rewrite this logic

        Room room = roomRepository.findRoomByRoomId(Long.parseLong(roomid));
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

        Room room = roomRepository.findRoomByRoomId(Long.parseLong(roomid));

        List<String> users = new ArrayList<>();
        for (User user : room.getUsers()) {
            users.add(user.getUsername());
        }

        int k = 0;
        while (true){
            if (users.get(k).equals(username)){
                if (k + 1 == users.size()){
                    logger.info("next user is %s", users.get(0));
                    return users.get(0);
                }else {
                    logger.info("next user is %s", users.get(k+1));
                    return users.get(k+1);
                }
            }
            k++;
        }
    }

    @Override
    public synchronized String getRandomWords() {
        Random random = new Random();
        List<Dictionary> list = dictionaryRepository.findAll();
        int r1 = random.nextInt(list.size());
        int r2 = random.nextInt(list.size());
        int r3 = random.nextInt(list.size());

        return list.get(r1).getValue() + "," + list.get(r2).getValue() + "," + list.get(r3).getValue();
    }
}
