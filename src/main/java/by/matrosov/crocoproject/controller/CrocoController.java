package by.matrosov.crocoproject.controller;

import by.matrosov.crocoproject.model.Dictionary;
import by.matrosov.crocoproject.model.Room;
import by.matrosov.crocoproject.model.User;
import by.matrosov.crocoproject.service.dictionary.DictionaryService;
import by.matrosov.crocoproject.service.room.RoomService;
import by.matrosov.crocoproject.service.user.UserService;
import by.matrosov.crocoproject.validator.DictionaryValidator;
import by.matrosov.crocoproject.validator.RoomValidator;
import by.matrosov.crocoproject.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;

@Controller
public class CrocoController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private RoomValidator roomValidator;

    @Autowired
    private DictionaryValidator dictionaryValidator;

    @Autowired
    private UserService userService;

    @Autowired
    private DictionaryService dictionaryService;

    @RequestMapping(value = "/logout", method=RequestMethod.GET)
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @RequestMapping(value = {"/login", "/"}, method = RequestMethod.GET)
    public String loginPage(){
        return "login";
    }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index(Model model, Principal principal){
        String username = principal.getName(); //NPE here?
        if (username == null || username.isEmpty()){
            return "redirect:/login";
        }

        List<Room> rooms = roomService.getAll();
        model.addAttribute("listRooms", rooms);
        model.addAttribute("username", username);

        return "index";
    }

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String registrationForm(Model model){
        model.addAttribute("user", new User());
        return "registration";
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String registerUserAccount(@ModelAttribute("user") User user, BindingResult result){
        userValidator.validate(user, result);
        if (result.hasErrors()){
            return "registration";
        }
        userService.save(user);
        return "redirect:/login";
    }

    @RequestMapping(value = "/dictionary/add", method = RequestMethod.GET)
    public String dictionaryForm(Model model){
        long count = dictionaryService.count();
        model.addAttribute("count", count);
        model.addAttribute("dictionary", new Dictionary());
        return "dictionary";
    }

    @RequestMapping(value = "/dictionary/add", method = RequestMethod.POST)
    public String addDictionaryValue(@ModelAttribute("dictionary") Dictionary dictionary, BindingResult result){
        dictionaryValidator.validate(dictionary, result);
        if (result.hasErrors()){
            return "dictionary";
        }
        dictionaryService.save(dictionary);
        return "redirect:/dictionary/add";
    }

    @RequestMapping(value = "/room/add", method = RequestMethod.GET)
    public String roomAddForm(Model model){
        model.addAttribute("room", new Room());
        return "room-add";
    }

    @RequestMapping(value = "/room/add", method = RequestMethod.POST)
    public String addRoom(@ModelAttribute("room") Room room, BindingResult result){
        roomValidator.validate(room, result);
        if (result.hasErrors()){
            return "room-add";
        }
        roomService.save(room);
        return "redirect:/index";
    }

    @RequestMapping(value = "/room/edit", method = RequestMethod.GET)
    public String roomEditForm(@RequestParam(name = "roomId") long roomId, Model model){
        Room room = roomService.getRoomById(roomId);
        model.addAttribute("room", room);
        return "room-edit";
    }

    @RequestMapping(value = "/room/edit", method = RequestMethod.POST)
    public String updateRoom(@RequestParam(name = "roomId") long roomId, Room room, BindingResult result){
        roomValidator.validate(room, result);
        if (result.hasErrors()){
            return "room-edit";
        }
        roomService.update(room, roomId);
        return "redirect:/index";
    }
}
