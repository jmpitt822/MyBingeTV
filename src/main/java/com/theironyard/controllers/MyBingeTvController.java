package com.theironyard.controllers;

import com.google.gson.Gson;
import com.theironyard.entities.SavedShow;
import com.theironyard.entities.User;
import com.theironyard.entities.ViewResult;
import com.theironyard.jsonInputEntities.Show;
import com.theironyard.jsonInputEntities.ShowDetail;
import com.theironyard.services.SavedShowRepo;
import com.theironyard.services.UserRepo;
import com.theironyard.utilities.PasswordStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Zach on 10/31/16.
 */
@Controller
public class MyBingeTvController {

    @Autowired
    UserRepo users;

    @Autowired
    SavedShowRepo savedShows;

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String index(Model model, HttpSession session) {
        User user = users.findFirstByName((String) session.getAttribute("username"));
        List<SavedShow> showList = savedShows.findAllByUser(user);

        model.addAttribute("showList", showList);
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("jsonResults", session.getAttribute("jsonResults"));
        return "index";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String login() {
        return "login";
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public User login(HttpSession session, String username, String password, HttpServletResponse response) throws Exception {
        User user = users.findFirstByName(username);
        if (user == null) {
            throw new Exception("Username not found, please create an account");
        } else if (!PasswordStorage.verifyPassword(password, user.getPassword())) {
            throw new Exception("wrong password");
        }
        session.setAttribute("username", username);
        response.sendRedirect("/");
        return user;
    }

    @RequestMapping(path = "/create-account", method = RequestMethod.POST)
    public String createAccount(HttpSession session, String newusername, String newpassword, String validatepassword,
                                HttpServletResponse response) throws Exception {
        User user = users.findFirstByName(newusername);
        if (user != null) {
            throw new Exception("Username already in use, please choose another");
        } else if (!newpassword.equals(validatepassword)) {
            throw new Exception("Error: passwords do not match");
        }
        user = new User(newusername, PasswordStorage.createHash(newpassword));
        users.save(user);
        session.setAttribute("username", newusername);
        return "redirect:/";
    }

    @RequestMapping(path = "/logout", method = RequestMethod.POST)
    public void logout(HttpSession session, HttpServletResponse response) throws Exception {
        session.invalidate();
        response.sendRedirect("/");
    }



    @RequestMapping(path = "/addToUserList", method = RequestMethod.POST)
    public String addToUserList(Model model, HttpSession session, String getId) {

        User user = users.findFirstByName((String) session.getAttribute("username"));
        ArrayList<ViewResult> resultList = (ArrayList) session.getAttribute("resultList");
        for (ViewResult r : resultList) {
            if (r.getId().equals(getId)) {
                SavedShow addToList = new SavedShow(r.getTitle(), r.getArtwork_208x117(), r.getId(), user);
                savedShows.save(addToList);
            }
        }
        model.addAttribute("resultList", session.getAttribute("resultList"));
        return "searchResults";
    }




}
