package by.matrosov.crocoproject.controller;

import by.matrosov.crocoproject.model.Room;
import by.matrosov.crocoproject.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.security.Principal;
import java.util.List;

@Controller
public class CrocoController {

    @Autowired
    private RoomRepository roomRepository;

    @RequestMapping(value = {"/", "/index"}, method = RequestMethod.GET)
    public String index(Model model, Principal principal){
        String username = principal.getName();
        if (username == null || username.isEmpty()){
            return "redirect:/login";
        }

        List<Room> rooms = roomRepository.findAll();
        model.addAttribute("listRooms", rooms);
        model.addAttribute("username", username);

        return "index";
    }
}
