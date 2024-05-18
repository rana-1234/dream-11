package main.shuffler;

import com.google.common.base.Strings;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TeamGenerator {

    public static class PlayerConfig{
        Long maxCaptainCount;
        Long maxViceCaptainCount;
        Long maxOccurrenceCount;
    }

    public static Random random = new Random();

    public static Set<String> keeperSet = new HashSet<String>();
    public static Set<String> batterSet = new HashSet<String>();
    public static Set<String> rounderSet = new HashSet<String>();
    public static Set<String> bowlerSet = new HashSet<String>();

    public static Set<String> captainsCantBeFrom = new HashSet<String>();
    public static Set<String> viceCaptainCantBeFrom = new HashSet<String>();
    public static Map<String, Integer> captainCount = new HashMap<>();
    public static Map<String, Integer> viceCaptainCount = new HashMap<>();
    public static Map<String, Integer> totalCount = new HashMap<>();

    public static Map<String, PlayerConfig> playerConfig = new HashMap<>();

    public static HashMap<String, Integer> captainVsViceCaptainComboAlreadyTaken = new HashMap<>();

    public static Long MAX_CAPTAIN_COUNT = 2L;
    public static Long MAX_VICE_CAPTAIN_COUNT = 2L;
    public static Long MAX_TOTAL_COUNT_FOR_CAPTAIN_AND_VICE_CAPTAIN = 3L;
    public static Long MAX_OCCURRENCE_COUNT = 5L;
    public static Long TOTAL_TEAM_REQUIRED = 20L;

    public static Long MAX_SAME_CAPTAIN_VICE_CAPTAIN_OCCURRENCE_ALLOWED = 1L;

    static void printAllContainers(){
        System.out.println("Printing captaincy containers...");
        for(Map.Entry<String, Integer> entry : captainCount.entrySet()){
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
        System.out.println("Printing vice captaincy containers...");
        for(Map.Entry<String, Integer> entry : viceCaptainCount.entrySet()){
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
        System.out.println("Printing total captaincy containers...");
        for(Map.Entry<String, Integer> entry : totalCount.entrySet()){
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
    }

    public static String encodeTeam(List<String> team){
        StringBuilder sb = new StringBuilder();
        List<String> sortedTeam = team.stream().sorted().collect(Collectors.toList());
        for(String st : sortedTeam){
            sb.append(st);
        }
        return sb.toString();
    }

    public static int readComments() throws IOException, ParseException{
        JSONParser jsonParser = new JSONParser();
        Object obj = jsonParser.parse(new FileReader("/Users/shashibhushanrana/IdeaProjects/dream-11/src/main/resources/team_input.json"));
        JSONObject jsonObject = (JSONObject)  obj;
        String commentString =  jsonObject.get("Comments").toString();

        int repeatTill = 0;
        for (int i = 0 ; i < commentString.length(); i++){
            repeatTill += commentString.charAt(i);
        }

        MAX_CAPTAIN_COUNT = (Long) jsonObject.get("max_captain_count");
        MAX_VICE_CAPTAIN_COUNT = (Long) jsonObject.get("max_vice_captain_count");
        MAX_TOTAL_COUNT_FOR_CAPTAIN_AND_VICE_CAPTAIN = (Long) jsonObject.get("max_total_count");
        TOTAL_TEAM_REQUIRED = (Long) jsonObject.get("total_teams_required");
        MAX_OCCURRENCE_COUNT = (Long) jsonObject.get("max_occurrence_count");
        MAX_SAME_CAPTAIN_VICE_CAPTAIN_OCCURRENCE_ALLOWED = (Long) jsonObject.get("max_same_captain_vice_captain_occurrence_allowed");

        JSONArray capNotFrom = (JSONArray)jsonObject.get("captains_not_from");
        capNotFrom.forEach(player -> captainsCantBeFrom.add(player.toString()));

        JSONArray viceCapNotFrom = (JSONArray)jsonObject.get("vice_captains_not_from");
        viceCapNotFrom.forEach(player -> viceCaptainCantBeFrom.add(player.toString()));

        return repeatTill;
    }

    public static List<String> readPlayers() throws IOException, ParseException {

        JSONParser jsonParser = new JSONParser();
        Object obj = jsonParser.parse(new FileReader("/Users/shashibhushanrana/IdeaProjects/dream-11/src/main/resources/team_input.json"));
        JSONObject jsonObject = (JSONObject)  obj;
        List<String> players = new ArrayList<>();
        JSONArray keepers  = (JSONArray) jsonObject.get("keepers");

        keepers.forEach(keeper -> players.add(keeper.toString()));
        keepers.forEach(keeper -> keeperSet.add(keeper.toString()));

        JSONArray batters = (JSONArray) jsonObject.get("batters");
        batters.forEach(batter -> players.add(batter.toString()));
        batters.forEach(batter -> batterSet.add(batter.toString()));

        JSONArray rounders = (JSONArray) jsonObject.get("rounders");
        rounders.forEach(rounder -> players.add(rounder.toString()));
        rounders.forEach(rounder -> rounderSet.add(rounder.toString()));

        JSONArray bowlers = (JSONArray) jsonObject.get("bowlers");
        bowlers.forEach(bowler -> players.add(bowler.toString()));
        bowlers.forEach(bowler -> bowlerSet.add(bowler.toString()));

        JSONObject individualsPlayerConfig = (JSONObject) jsonObject.get("individual_player_config");

        individualsPlayerConfig.forEach((playerName, config ) -> {
            PlayerConfig config1 = new PlayerConfig();
            JSONObject jsonObjectConfig = (JSONObject) config;

            config1.maxCaptainCount = (Long) (jsonObjectConfig.get("max_captain_count") != null ? jsonObjectConfig.get("max_captain_count"):MAX_CAPTAIN_COUNT );
            config1.maxViceCaptainCount = (Long) (jsonObjectConfig.get("max_vice_captain_count") != null ? jsonObjectConfig.get("max_vice_captain_count") : MAX_VICE_CAPTAIN_COUNT);
            config1.maxOccurrenceCount = (Long) (jsonObjectConfig.get("max_occurrence_count") != null ? jsonObjectConfig.get("max_occurrence_count") : MAX_OCCURRENCE_COUNT);
            playerConfig.put((String)playerName, config1);
        });

        for(String p: players){
            if (!playerConfig.containsKey(p)){
                PlayerConfig config = new PlayerConfig();
                config.maxCaptainCount = MAX_CAPTAIN_COUNT;
                config.maxViceCaptainCount = MAX_VICE_CAPTAIN_COUNT;
                config.maxOccurrenceCount = MAX_OCCURRENCE_COUNT;
                playerConfig.put(p, config);
            }
        }
        return players;
    }

    public static boolean violatingIndividualPlayerConfig(List<String> players, int captainIndex, int viceCaptainIndex, Map<String, Long> occurrence, Map<String, Long> captainOccurrence, Map<String, Long> viceCaptainOccurrence){
        for(int i = 0 ; i < players.size() ; i++){
            String player = players.get(i);
            PlayerConfig config = playerConfig.get(player);
            if (config.maxOccurrenceCount <= occurrence.getOrDefault(player, 0L)){
                return true;
            }
            if ( i == captainIndex && config.maxCaptainCount <= captainOccurrence.getOrDefault(player, 0L)){
                return true;
            }
            else if( i == viceCaptainIndex && config.maxViceCaptainCount <= viceCaptainOccurrence.getOrDefault(player, 0L)){
                return true;
            }
        }
        return false;
    }


    public static void main(String[] args) throws Exception {

        // Must be read first.
        int prabhuKaNamLekarTeamBanRahaH = readComments();

        List<List<String>> teams = new ArrayList<>();

        // Will read the players and its config
        List<String> players = readPlayers();

        // TOTAL_TEAM_COUNT would be recalculated based on readComments.
        long teamCount = TOTAL_TEAM_REQUIRED;

        int maxTryCount = 30000010;
        List<List<String>> previousTeams = new ArrayList<>();

        int teamCreated = 0;

        while(prabhuKaNamLekarTeamBanRahaH-- >= 1 && maxTryCount > 0 ) {
            teams = new ArrayList<>();
            captainCount = new HashMap<>();
            viceCaptainCount = new HashMap<>();
            captainVsViceCaptainComboAlreadyTaken = new HashMap<>();
            totalCount = new HashMap<>();

            // Maintain at least 13 players for 20 teams
            Set<String> alreadyTakenTeam = new HashSet<>();
            // Total occurrence of player in a team
            Map<String, Long> occurrence = new HashMap<>();
            Map<String, Long> captainOccurrence = new HashMap<>();
            Map<String, Long> viceCaptainOccurrence = new HashMap<>();

            for (int i = 0; i < teamCount && maxTryCount > 0; i++) {
                Collections.shuffle(players);
                List<String> currentTeam = new ArrayList<>();
                int oneTeamSize = 11;

                List<String> checker = new ArrayList<>();
                boolean atLeastOneKeeper = false;
                boolean atLeastOneBatter = false;
                boolean atLeastOneRounder = false;
                boolean atLeastOneBowler = false;

                for (int j = 0; j < players.size(); j++) {
                    String player = players.get(j);

                    // if player is already completed the maximum occurrence, remove those from getting selected
                    if (occurrence.getOrDefault(player, 0L) >= playerConfig.get(player).maxOccurrenceCount){
                        continue;
                    }

                    if (keeperSet.contains(player)) {
                        atLeastOneKeeper = true;
                    }
                    if (batterSet.contains(player)) {
                        atLeastOneBatter = true;
                    }
                    if (rounderSet.contains(player)) {
                        atLeastOneRounder = true;
                    }

                    if (bowlerSet.contains(player)) {
                        atLeastOneBowler = true;
                    }
                    checker.add(player);

                    if( checker.size() == oneTeamSize){
                        break;
                    }
                }

                if( checker.size() != oneTeamSize){
                    // if not able to select players such a way that occurrence is the problem, then there is no
                    // way to proceed and try
                    maxTryCount--;
                    break;
                }

                int captain = (random.nextInt() % 11 + 11) % 11;
                while(captainsCantBeFrom.contains(checker.get(captain))) {
                    captain = (random.nextInt() % 11 + 11) % 11;
                }

                int viceCaptain = captain;
                while (viceCaptain == captain || viceCaptainCantBeFrom.contains(checker.get(viceCaptain))) {
                    viceCaptain = (random.nextInt() % 11 + 11) % 11;
                }

                String encodedCaptainViceCaptain = encodeTeam(List.of(checker.get(captain), checker.get(viceCaptain)));

                if (alreadyTakenTeam.contains(encodeTeam(checker)) ||
                        !atLeastOneKeeper ||
                        !atLeastOneBatter ||
                        !atLeastOneRounder ||
                        !atLeastOneBowler ||
                        (captainCount.getOrDefault(checker.get(captain), 0) + 1 > MAX_CAPTAIN_COUNT) ||
                        (viceCaptainCount.getOrDefault(checker.get(viceCaptain), 0) + 1 > MAX_VICE_CAPTAIN_COUNT) ||
                        (totalCount.getOrDefault(checker.get(viceCaptain), 0) + 1 > MAX_TOTAL_COUNT_FOR_CAPTAIN_AND_VICE_CAPTAIN) ||
                        (totalCount.getOrDefault(checker.get(captain), 0) + 1 > MAX_TOTAL_COUNT_FOR_CAPTAIN_AND_VICE_CAPTAIN) ||
                        (captainVsViceCaptainComboAlreadyTaken.getOrDefault(encodedCaptainViceCaptain, 0)+1 > MAX_SAME_CAPTAIN_VICE_CAPTAIN_OCCURRENCE_ALLOWED) ||
                        violatingIndividualPlayerConfig(checker, captain, viceCaptain, occurrence, captainOccurrence, viceCaptainOccurrence)
                ) {
                    maxTryCount--;
                    i--;
                    continue;
                }


                alreadyTakenTeam.add(encodeTeam(checker));
                captainCount.put(checker.get(captain), captainCount.getOrDefault(checker.get(captain), 0) + 1);
                totalCount.put(checker.get(captain), totalCount.getOrDefault(checker.get(captain), 0) + 1);

                viceCaptainCount.put(checker.get(viceCaptain), viceCaptainCount.getOrDefault(checker.get(viceCaptain), 0) + 1);
                totalCount.put(checker.get(viceCaptain), totalCount.getOrDefault(checker.get(viceCaptain), 0) + 1);
                captainVsViceCaptainComboAlreadyTaken.put(encodedCaptainViceCaptain, captainVsViceCaptainComboAlreadyTaken.getOrDefault(encodedCaptainViceCaptain, 0) + 1);
                captainOccurrence.put(checker.get(captain), captainOccurrence.getOrDefault(checker.get(captain), 0L)+1);
                viceCaptainOccurrence.put(checker.get(viceCaptain), viceCaptainOccurrence.getOrDefault(checker.get(viceCaptain), 0L)+1);

                for (int j = 0; j < oneTeamSize; j++) {
                    String player = checker.get(j);
                    occurrence.put(player, occurrence.getOrDefault(player, 0L) + 1);
                    if (j == captain) {
                        player = "C-" + player;
                    } else if (j == viceCaptain) {
                        player = "VC-" + player;
                    }
                    currentTeam.add(player);
                }
                teams.add(currentTeam);
            }

            if( teams.size() == teamCount) {
                previousTeams = teams.stream().collect(Collectors.toList());
            }
            else{
                teamCreated = teams.size();
            }
        }
        if (previousTeams.isEmpty()) {
            System.out.println("Couldn't create teams from given constraints...");
            System.out.println("Last known team created was : " + teamCreated);
        }
        else {
            showDreamTeam(previousTeams);
        }
    }

    private static void showDreamTeam(List<List<String>> dtList) {

        List<List<String>> columnWiseTeam = new ArrayList<>();
        for ( int i = 1 ; i <= dtList.get(0).size() ; i++){
            columnWiseTeam.add(new ArrayList<>());
        }

        dtList.forEach(x -> {
            for(int i = 0 ; i < x.size(); i++){
                columnWiseTeam.get(i).add(x.get(i));
            }
        });

        printTeams(columnWiseTeam, "Team");
    }

    private static void printTeams(List<List<String>> dtList, String tokenPrinter){
        System.out.printf("%s", Strings.repeat("-", dtList.get(0).size()*20));
        System.out.println();
        for(int j = 1 ; j<= dtList.get(0).size() ; j++){
            System.out.printf("%18s", tokenPrinter + j);
        }
        System.out.println();
        System.out.printf("%s", Strings.repeat("-", dtList.get(0).size()*20));
        System.out.println();
        dtList.forEach(dt -> {
            dt.forEach(x -> System.out.printf("%18s", x));
            System.out.println();
        });
        System.out.printf("%s", Strings.repeat("-", dtList.get(0).size()*20));
        System.out.println();
    }
}
