package by.matrosov.crocoproject.service.room;

import by.matrosov.crocoproject.model.Room;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RoomService {
    List<Room> getAll();
    void changeRoomState(long roomid);
    void save(Room room);
    void update(Room room, long roomid);
    void roomOnEnd(long roomid);
    Room getRoomById(long roomid);
    Room getRoomByRoomName(String roomName);
}
