package stirling.software.SPDF.controller.api;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import stirling.software.SPDF.config.security.UserService;
import stirling.software.SPDF.model.User;

@Controller
public class UserController {
    
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password, Model model) {
        if(userService.usernameExists(username)) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }

        userService.saveUser(username, password);
        return "redirect:/login?registered=true";
    }
    
    
    @PostMapping("/updateUserSettings")
	public String updateUserSettings(HttpServletRequest request, Principal principal) {
	    Map<String, String[]> paramMap = request.getParameterMap();
	    Map<String, String> updates = new HashMap<>();

	    System.out.println("Received parameter map: " + paramMap);

	    for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
	        updates.put(entry.getKey(), entry.getValue()[0]);
	    }

	    System.out.println("Processed updates: " + updates);

	    // Assuming you have a method in userService to update the settings for a user
	    userService.updateUserSettings(principal.getName(), updates);

	    return "redirect:/account";  // Redirect to a page of your choice after updating
	}

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/admin/saveUser")
    public String saveUser(@RequestParam String username, @RequestParam String password, @RequestParam String role) {
        userService.saveUser(username, password, role);
        return "redirect:/addUsers";  // Redirect to account page after adding the user
    }

    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/deleteUser/{username}")
    public String deleteUser(@PathVariable String username) {
    	userService.deleteUser(username); 
        return "redirect:/addUsers";
    }
    
    @PostMapping("/get-api-key")
    public ResponseEntity<String> getApiKey(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authenticated.");
        }
        String username = principal.getName();
        String apiKey = userService.getApiKeyForUser(username);
        if (apiKey == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("API key not found for user.");
        }
        return ResponseEntity.ok(apiKey);
    }

    @PostMapping("/update-api-key")
    public ResponseEntity<String> updateApiKey(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authenticated.");
        }
        String username = principal.getName();
        User user = userService.refreshApiKeyForUser(username);
        String apiKey = user.getApiKey();
        if (apiKey == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("API key not found for user.");
        }
        return ResponseEntity.ok(apiKey);
    }
    
    
}
