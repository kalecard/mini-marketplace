package com.kale.interview.graphql;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GraphiQlController {

    @GetMapping("/graphiql")
    public String graphiql() {
        return "redirect:/graphiql.html";
    }
}
