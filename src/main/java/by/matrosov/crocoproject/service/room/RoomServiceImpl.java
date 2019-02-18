package by.matrosov.crocoproject.service.room;

import by.matrosov.crocoproject.model.Room;
import by.matrosov.crocoproject.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.HashSet;
import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {

    private static final Logger logger = LoggerFactory.getLogger(HandshakeInterceptor.class);

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
        Room room = roomRepository.findRoomByRoomId(roomid);
        room.setUsers(new HashSet<>());
        room.setOpen(true);
        roomRepository.save(room);
        logger.info("game ended in room %s", roomid);
    }

    @Override
    public Room getRoomById(long roomid) {
        return roomRepository.findRoomByRoomId(roomid);
    }

    @Override
    public Room getRoomByRoomName(String roomName) {
        return roomRepository.findRoomByRoomName(roomName);
    }
}
