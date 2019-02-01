package by.matrosov.crocoproject.validator;

import by.matrosov.crocoproject.model.Room;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class RoomValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return Room.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Room room = (Room) o;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "roomName", "NotEmpty");
        if (room.getRoomName().length() < 3 || room.getRoomName().length() > 32) {
            errors.rejectValue("roomName", "Size.Room.roomName");
        }
    }
}
