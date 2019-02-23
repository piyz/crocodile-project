package by.matrosov.crocoproject.service.game;

import org.springframework.stereotype.Service;

@Service
public interface GameService {
    void addUser(String username, String roomid);
    void removeUser(String username, String roomid);
    void setDrawerUser(String roomId, String username);
    void setGuess(String roomId, String word);
    void clearGuess(String roomid);

    boolean addScore(String drawer, String guesser);
    boolean isRightAnswer(String message, String roomid);
    boolean isGameStart(String message, String roomid);

    String[] getScore(String roomid);

    String getNextUser(String username, String roomid);
    String getRandomWords();
    String getDrawerUser(String roomId);
}