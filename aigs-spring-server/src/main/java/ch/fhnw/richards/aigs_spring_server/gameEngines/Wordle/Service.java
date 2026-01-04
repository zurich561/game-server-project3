package ch.fhnw.richards.aigs_spring_server.gameEngines.Wordle;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.fhnw.richards.aigs_spring_server.game.Game;

import java.util.List;


public class Service {
        
        // Loris: checks if the word exists
        public boolean CheckWordExistence(String guess)
        throws Exception {
            String uri = "https://api.dictionaryapi.dev/api/v2/entries/en/" + guess;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            return response.statusCode() != 404;
        }

        // Loris: generates a random word for the game
        public String GenerateWord (int length)
        throws Exception {
            String uri = "https://random-word-api.vercel.app/api?words=1&length=" + length;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

                ObjectMapper mapper = new ObjectMapper();
                String[] words = mapper.readValue(response.body(), String[].class);

            return words[0];
        }

        // Loris: saves a guess to game options and evaluates each character
        @SuppressWarnings("unchecked")
        public Game addGuess(Game game, String guess) {
                // converts String into json
                ObjectMapper mapper = new ObjectMapper();
                HashMap<String, Object> opts = null;

                try {
                        opts = mapper.readValue(game.getOptions(), HashMap.class);
                } catch (JsonMappingException e) {
                        e.printStackTrace();
                } catch (JsonProcessingException e) {
                        e.printStackTrace();
                } 

                String solution = ((String) opts.get("solutionWord")).toLowerCase();
                int wordLen = ((Number) opts.get("wordLength")).intValue();
                
                // initial checks
                if (guess == null || guess.length() != wordLen) {
                        throw new IllegalArgumentException("invalid guess length");
                }

                // check the status for each character
                List<String> status = evaluateGuess(solution, guess.toLowerCase());

                // get current guesses
                List<List<Map<String, String>>> guesses =
                        (List<List<Map<String, String>>>) opts.get("guesses");
                if (guesses == null) {
                        guesses = new ArrayList<>();
                        opts.put("guesses", guesses);
                }
                
                // add the guess character by character and add status
                List<Map<String, String>> letters = new ArrayList<>(wordLen);
                for (int i = 0; i < wordLen; i++) {
                        Map<String, String> letter = new HashMap<>();
                        letter.put("char", String.valueOf(guess.charAt(i)));
                        letter.put("status", status.get(i)); // correct|present|absent
                        letters.add(letter);
                }

                // add the letters to the other guesses
                guesses.add(letters);

                int round = ((Number) opts.get("round")).intValue();
                opts.put("round", round + 1);

                // check if result is correct
                evaluateResult(game, solution, guess, round + 1);

                // save new game options and return
                try {
                        game.setOptions(mapper.writeValueAsString(opts));
                } catch (JsonProcessingException e) {
                        e.printStackTrace();
                }

                return game;
        }

        // Loris: evaluates if char is correct
        public List<String> evaluateGuess(String solution, String guess) {
                int len = solution.length();
                String[] status = new String[len];
                int[] counts = new int[26];

                char[] sol = solution.toCharArray();
                char[] g = guess.toCharArray();
                
                // Pass 1: char matches exactly
                for (int i = 0; i < len; i++) {
                        if (g[i] == sol[i]) {
                        status[i] = "correct";
                        } else {
                        int idx = sol[i] - 'a';
                        if (idx >= 0 && idx < 26) counts[idx]++;
                        }
                }

                // Pass 2: char is in word or not correct at all
                for (int i = 0; i < len; i++) {
                        // skip to next character if char matched previously
                        if (status[i] != null) continue;
                        int idx = g[i] - 'a';
                        if (idx >= 0 && idx < 26 && counts[idx] > 0) {
                        status[i] = "present";
                        counts[idx]--;
                        } else {
                        status[i] = "absent";
                        }
                }

                List<String> out = new ArrayList<>(len);
                for (int i = 0; i < len; i++) out.add(status[i]);
                return out;
        }

        private void evaluateResult(Game game, String solution, String guess, int round) {
                boolean correct = solution.equalsIgnoreCase(guess);

                if (correct) {
                        game.setResult(Boolean.TRUE);
                        return;
                }

                if (round >= 6) {
                        game.setResult(Boolean.FALSE);
                }

        }
}
