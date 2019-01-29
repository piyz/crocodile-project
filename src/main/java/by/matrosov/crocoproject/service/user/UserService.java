package by.matrosov.crocoproject.service.user;

import by.matrosov.crocoproject.model.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    User findByUsername(String username);
    void save(User user);
}
