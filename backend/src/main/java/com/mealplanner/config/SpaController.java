package com.mealplanner.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forwards client-side routes to index.html so React Router can handle them.
 * Only active when the frontend has been built into the static resources.
 */
@Controller
public class SpaController {

    @GetMapping(value = {"/", "/meals", "/people", "/planner", "/shopping"})
    public String forward() {
        return "forward:/index.html";
    }
}
