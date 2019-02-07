package by.matrosov.crocoproject.service.game;

import by.matrosov.crocoproject.model.Dictionary;
import by.matrosov.crocoproject.model.Room;
import by.matrosov.crocoproject.repository.DictionaryRepository;
import by.matrosov.crocoproject.service.room.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameServiceImpl implements GameService{
    private static Map<String, Map<String, Integer>> mapMap = Collections.synchronizedMap(new HashMap<>());
    private static final int FINAL_SCORE = 100;

    //replace on service
    @Autowired
    private DictionaryRepository dictionaryRepository;

    @Override
    public synchronized void addUser(String username, String roomid) {
        Map<String, Integer> innerMap = mapMap.get(roomid);

        if (innerMap == null){
            innerMap = new LinkedHashMap<>();
            innerMap.put(username, 0);
            mapMap.put(roomid, innerMap);
        }else {
            innerMap.put(username, 0);
        }
    }

    @Override
    public synchronized void removeUser(String username, String roomid) throws NullPointerException{
        Map<String, Integer> innerMap = mapMap.get(roomid);

        //if last user -> remove room from mapMap
        if (innerMap.size() == 1){
            mapMap.remove(roomid);
        }else {
            innerMap.remove(username);
        }
    }

    @Override
    public synchronized boolean addScore(String drawer, String guesser, String roomid) {
        Map<String, Integer> innerMap = mapMap.get(roomid);

        innerMap.put(drawer, innerMap.get(drawer) + 5);
        innerMap.put(guesser, innerMap.get(guesser) + 6);

        return innerMap.get(drawer) >= FINAL_SCORE || innerMap.get(guesser) >= FINAL_SCORE;

    }

    @Override
    public synchronized void print() {
        mapMap.entrySet().forEach(System.out::println);
    }

    @Override
    public synchronized String[] getScore(String roomid) throws NullPointerException{
        Map<String, Integer> innerMap = mapMap.get(roomid);

        String score = Arrays.toString(innerMap.entrySet().toArray());

        return score.substring(1, score.length() - 1)
                .replace("=", " ")
                .split(",");
    }

    @Override
    public synchronized String getNextUser(String username, String roomid) {
        Map<String, Integer> innerMap = mapMap.get(roomid);

        int i = 0;
        Object[] users = innerMap.keySet().toArray();
        while (true){
            if (users[i].toString().equals(username)){
                if (i + 1 == innerMap.size()){
                    return users[0].toString();
                }else {
                    return users[i+1].toString();
                }
            }
            i++;
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
