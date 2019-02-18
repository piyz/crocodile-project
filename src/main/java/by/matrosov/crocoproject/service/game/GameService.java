package by.matrosov.crocoproject.service.game;

import org.springframework.stereotype.Service;

@Service
public interface GameService {
    void addUser(String username, String roomid);
    void removeUser(String username, String roomid);
    boolean addScore(String drawer, String guesser);
    String[] getScore(String roomid);
    String getNextUser(String username, String roomid);
    String getRandomWords();
}
