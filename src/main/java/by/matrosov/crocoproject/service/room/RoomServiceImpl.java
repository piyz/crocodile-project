package by.matrosov.crocoproject.service.room;

import by.matrosov.crocoproject.model.Room;
import by.matrosov.crocoproject.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {
    private static final Logger logger = LoggerFactory.getLogger(RoomServiceImpl.class);

    @Autowired
    private RoomRepository roomRepository;

    @Override
    public List<Room> getAll() {
        return roomRepository.findAll();
    }

    @Override
    public void changeRoomState(long roomid) {
        Room room2update = roomRepository.findRoomByRoomId(roomid);

        if (room2update.isOpen()){
            room2update.setOpen(false);
        }else {
            room2update.setOpen(true);
        }

        roomRepository.save(room2update);
    }

    @Override
    public void save(Room room) {
        room.setOpen(true);
        room.setGuess("/start");
        roomRepository.save(room);
    }

    @Override
    public void update(Room room, long roomid) {
        Room roomExist = roomRepository.findRoomByRoomId(roomid);
        roomExist.setRoomName(room.getRoomName());
        roomRepository.save(roomExist);
    }

    @Override
    public void roomOnEnd(long roomid) {
        logger.info("game ended in room ", roomid);
        Room room = roomRepository.findRoomByRoomId(roomid);
        room.setUsers(new HashSet<>());
        roomRepository.delete(room);
        logger.info("delete room " + roomid);
    }

    @Override
    public Room getRoomById(long roomid) {
        return roomRepository.findRoomByRoomId(roomid);
    }

    @Override
    public Room getRoomByRoomName(String roomName) {
        return roomRepository.findRoomByRoomName(roomName);
    }

    @Override
    public void clearGuess(long roomid) {
        logger.info("clear guess field in room '" + roomid + "' in database");
        Room room = roomRepository.findRoomByRoomId(roomid);
        room.setGuess(" ");
        roomRepository.save(room);
    }

    @Override
    public void update(Room room) {
        roomRepository.save(room);
    }

    @Override
    public void setWord(long roomId, String word) {
        logger.info("set guess '" + word + "' in room '" + roomId + "'");
        Room room = roomRepository.findRoomByRoomId(roomId);
        room.setGuess(word);
        roomRepository.save(room);
    }

    @Override
    public void delete(Room room) {
        roomRepository.delete(room);
    }
}
