package main.shuffler;

import com.google.common.base.Strings;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TeamGenerator {

    public static Random random = new Random();

    public static Set<String> keeperSet = new HashSet<String>();
    public static Set<String> batterSet = new HashSet<String>();
    public static Set<String> rounderSet = new HashSet<String>();
    public static Set<String> bowlerSet = new HashSet<String>();
    public static Map<String, Integer> captainCount = new HashMap<>();
    public static Map<String, Integer> viceCaptainCount = new HashMap<>();
    public static Map<String, Integer> totalCount = new HashMap<>();

    public static int MAX_CAPTAIN_COUNT = 2;
    public static int MAX_VICE_CAPTAIN_COUNT = 2;
    public static int MAX_TOTAL_COUNT_FOR_CAPTAIN_AND_VICE_CAPTAIN = 3;

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
        Collections.sort(team);
        for(String st : team){
            sb.append(st);
        }
        return sb.toString();
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

        return players;
    }


    public static void main(String[] args) throws Exception {
        List<String> players = readPlayers();

        // Maintain at least 13 players for 20 teams

        int teamCount = 20;
        List<List<String>> teams = new ArrayList<>();
        Set<String> alreadyTakenTeam = new HashSet<>();

        for(int i = 0 ; i < teamCount; i++){
            for(int j = 0 ; j < random.nextInt()%99 + 1 ; j++) {
                Collections.shuffle(players);
            }

            int captain = (random.nextInt()%11 + 11)%11;
            int viceCaptain = captain;
            while(viceCaptain == captain) {
                viceCaptain = (random.nextInt()%11 + 11)%11;
            }

            List<String> currentTeam = new ArrayList<>();
            int oneTeamSize = 11;

            List<String> checker = new ArrayList<>();
            boolean atLeastOneKeeper = false;
            boolean atLeastOneBatter = false;
            boolean atLeastOneRounder = false;
            boolean atLeastOneBowler = false;

            for(int j = 0 ; j < oneTeamSize ; j++){
                String player = players.get(j);
                if( keeperSet.contains(player) ){
                    atLeastOneKeeper = true;
                }
                if ( batterSet.contains(player) ){
                    atLeastOneBatter = true;
                }
                if( rounderSet.contains(player)){
                    atLeastOneRounder = true;
                }

                if( bowlerSet.contains(player) ){
                    atLeastOneBowler = true;
                }
                checker.add(player);
            }

            if( alreadyTakenTeam.contains(encodeTeam(checker)) ||
                    !atLeastOneKeeper ||
                    !atLeastOneBatter ||
                    !atLeastOneRounder ||
                    !atLeastOneBowler ||
                    (captainCount.getOrDefault(checker.get(captain), 0) + 1 > MAX_CAPTAIN_COUNT) ||
                    (viceCaptainCount.getOrDefault(checker.get(viceCaptain), 0) + 1 > MAX_VICE_CAPTAIN_COUNT) ||
                    (totalCount.getOrDefault(checker.get(viceCaptain), 0) + 1 > MAX_TOTAL_COUNT_FOR_CAPTAIN_AND_VICE_CAPTAIN) ||
                    (totalCount.getOrDefault(checker.get(captain), 0) + 1 > MAX_TOTAL_COUNT_FOR_CAPTAIN_AND_VICE_CAPTAIN)
            ){
                System.out.println("Already taken team........");
                i--;
                continue;
            }


            alreadyTakenTeam.add(encodeTeam(checker));
            captainCount.put(checker.get(captain), captainCount.getOrDefault(checker.get(captain), 0) + 1);
            totalCount.put(checker.get(captain), totalCount.getOrDefault(checker.get(captain), 0) + 1);

            viceCaptainCount.put(checker.get(viceCaptain),viceCaptainCount.getOrDefault(checker.get(viceCaptain), 0) + 1);
            totalCount.put(checker.get(viceCaptain), totalCount.getOrDefault(checker.get(viceCaptain), 0) + 1);


            for(int j = 0 ; j < oneTeamSize ; j++){
                String player = checker.get(j);
                if ( j == captain){
                    player = "C-" + player;
                }
                else if ( j == viceCaptain){
                    player = "VC-" + player;
                }
                currentTeam.add(player);
            }

            teams.add(currentTeam);
        }
        showDreamTeam(teams);
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
