package SDK;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BackendWeb {
    @GetMapping("/")
    public String index() {
        return "redirect:/Web/home.html"; // Serve static HTML from /static
    }
}
