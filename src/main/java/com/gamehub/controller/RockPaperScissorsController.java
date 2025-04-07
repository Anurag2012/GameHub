    package com.gamehub.controller;

    import jakarta.servlet.http.HttpSession;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.servlet.mvc.support.RedirectAttributes;

    import java.util.Random;
    import java.util.Map;
    import java.util.HashMap; // Import HashMap

    @Controller // Ensure this annotation is present
    public class RockPaperScissorsController {

        private static final String RPS_SCORES_KEY = "rpsScores";
        private static final String RPS_LAST_RESULT_KEY = "rpsLastResult";
        private final Random random = new Random();

        @GetMapping("/rps") // Ensure this annotation is present
        public String showRpsPage(Model model, HttpSession session) {
            // Get scores from session or initialize
            @SuppressWarnings("unchecked")
            Map<String, Integer> scores = (Map<String, Integer>) session.getAttribute(RPS_SCORES_KEY);
            if (scores == null) {
                // Use HashMap for mutable map
                scores = new HashMap<>(Map.of("player", 0, "cpu", 0, "draws", 0));
                session.setAttribute(RPS_SCORES_KEY, scores);
            }

            // Get last result from flash attributes (if redirected) or session
            if (!model.containsAttribute("lastResult")) { // Check if not already added by redirect
                 model.addAttribute("lastResult", session.getAttribute(RPS_LAST_RESULT_KEY));
            }

            model.addAttribute("scores", scores);
            return "rps"; // Name of the Thymeleaf template
        }

        @PostMapping("/rps/play") // Ensure this annotation is present
        public String playGame(@RequestParam String playerChoice, HttpSession session, RedirectAttributes redirectAttributes) {
            // 1. Get CPU choice
            String[] choices = {"ROCK", "PAPER", "SCISSORS"};
            String cpuChoice = choices[random.nextInt(choices.length)];

            // 2. Determine winner
            String result;
            if (playerChoice.equals(cpuChoice)) {
                result = "DRAW";
            } else if (
                (playerChoice.equals("ROCK") && cpuChoice.equals("SCISSORS")) ||
                (playerChoice.equals("PAPER") && cpuChoice.equals("ROCK")) ||
                (playerChoice.equals("SCISSORS") && cpuChoice.equals("PAPER"))
            ) {
                result = "PLAYER_WINS";
            } else {
                result = "CPU_WINS";
            }

            // 3. Update scores in session
            @SuppressWarnings("unchecked")
            Map<String, Integer> scores = (Map<String, Integer>) session.getAttribute(RPS_SCORES_KEY);
            if (scores == null) { // Should not happen after GET, but good practice
                 scores = new HashMap<>(Map.of("player", 0, "cpu", 0, "draws", 0));
            }

            switch (result) {
                case "PLAYER_WINS":
                    scores.put("player", scores.getOrDefault("player", 0) + 1);
                    break;
                case "CPU_WINS":
                    scores.put("cpu", scores.getOrDefault("cpu", 0) + 1);
                    break;
                case "DRAW":
                    scores.put("draws", scores.getOrDefault("draws", 0) + 1);
                    break;
            }
            session.setAttribute(RPS_SCORES_KEY, scores);

            // 4. Store results for display on next page load
            Map<String, String> lastResult = Map.of(
                "playerChoice", playerChoice,
                "cpuChoice", cpuChoice,
                "result", result
            );
            // Use flash attributes to survive the redirect
            redirectAttributes.addFlashAttribute("lastResult", lastResult);
            session.setAttribute(RPS_LAST_RESULT_KEY, lastResult); // Also store in session for direct refresh

            return "redirect:/rps";
        }
    }