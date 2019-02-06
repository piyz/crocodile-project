package by.matrosov.crocoproject.service.room;

import by.matrosov.crocoproject.model.Room;
import by.matrosov.crocoproject.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {

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
    public void delete(long roomid) {
        roomRepository.deleteById(roomid);
    }

    @Override
    public Room getRoomById(long roomid) {
        return roomRepository.findRoomByRoomId(roomid);
    }
}
