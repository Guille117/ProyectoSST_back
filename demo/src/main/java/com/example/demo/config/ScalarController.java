package com.example.demo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true")
public class ScalarController {

    @GetMapping("/scalar")
    @ResponseBody
    public String scalar() {
        return """
                <!doctype html>
                <html>
                  <head>
                    <title>API Reference - SST</title>
                    <meta charset="utf-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1" />
                  </head>
                  <body>
                    <script id="api-reference" data-url="/v3/api-docs"></script>
                    <script src="https://cdn.jsdelivr.net/npm/@scalar/api-reference"></script>
                  </body>
                </html>
                """;
    }
}
