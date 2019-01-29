package by.matrosov.crocoproject.model;

import javax.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "room_id")
    private long roomId;

    @Column(name = "room_name")
    private String roomName;

    @Column(name = "open")
    private boolean isOpen;

    public long getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public boolean isOpen() {
        return isOpen;
    }
}
