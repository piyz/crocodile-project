package by.matrosov.crocoproject.service.room;

import by.matrosov.crocoproject.model.Room;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RoomService {
    List<Room> getAll();
}
