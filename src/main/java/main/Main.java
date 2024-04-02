package main;

import com.google.common.base.Strings;
import lombok.extern.log4j.Log4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Log4j
public class Main {

    public static final int  MAX_FACTS = 12;
    public static long [] facts = new long [MAX_FACTS];

    public static Map<String, Integer> keeperLimit = new HashMap<>();
    public static Map<String, Integer> batterLimit = new HashMap<>();
    public static Map<String, Integer> rounderLimit = new HashMap<>();
    public static Map<String, Integer> bowlerLimit = new HashMap<>();

    private static void calculateFacts(){
        facts[0] = 1;
        for (int i = 1 ; i < MAX_FACTS ; i++)
            facts[i] = facts[i-1]*i;
    }


    public static void main(String [] args) throws IOException, ParseException {
        System.out.println("On the way to generate the dream team...");

        JSONParser jsonParser = new JSONParser();
        Object obj = jsonParser.parse(new FileReader("/Users/shashibhushanrana/IdeaProjects/dream-11/src/main/resources/input.json"));
        JSONObject jsonObject = (JSONObject)  obj;
        final List<String> keepers = getPlayers(jsonObject, "keepers");
        final List<String> batters = getPlayers(jsonObject, "batters");
        final List<String> rounders = getPlayers(jsonObject, "rounders");
        final List<String> bowlers = getPlayers(jsonObject, "bowlers");

        final int numberOfTeamsRequired = getInt(jsonObject, "teamRequired");
        final int maxCaptainCount = getInt(jsonObject, "maxCaptain");
        final int maxViceCaptainCount = getInt(jsonObject, "maxViceCaptain");
        final int keeperCanAppearInTeamMaxCount = getInt(jsonObject, "keeperCanAppearInTeamMaxCount");
        final int batterCanAppearInTeamMaxCount = getInt(jsonObject, "batterCanAppearInTeamMaxCount");
        final int rounderCanAppearInTeamMaxCount = getInt(jsonObject,"rounderCanAppearInTeamMaxCount");
        final int bowlerCanAppearInTeamMaxCount = getInt(jsonObject, "bowlerCanAppearInTeamMaxCount");
        Map<String, Integer> captaincyCount = new HashMap<>();
        Map<String, Integer> viceCaptaincyCount = new HashMap<>();

        final List<TeamFormat> teamFormatList = getTeamFormats(jsonObject);

        List<List<String>> dreamTeams = new ArrayList<>();
        calculateFacts();

        teamFormatList.forEach(teamFormat -> {

            /*
                Team Count Required = 4
                Combo = 2
                Keepers = 6 {k1, k2, k3, k4, k5, k6}
                {k1, k2}, {k1, k3} ....... {k5, k6} -> nCr (n == number of keepers, r = group)
                nCr = n!/r!(n-r)!
                Maximum Combo size = number of teams / nCr => is the maximum combo size

             */
            int teamCountRequired = teamFormat.requiredCount;

            Map<String, Integer> keeperComboAlreadyPresent = new HashMap<>();
            int maxKeeperCombo = nCr(keepers.size(), teamFormat.keeperCount);
            int maxKeeperCount = maxKeeperCombo > teamCountRequired ? 1 : (int) Math.ceil((float)teamCountRequired/(float)maxKeeperCombo);

            Map<String, Integer> batterComboAlreadyPresent = new HashMap<>();
            int maxBatterCombo = nCr(batters.size(), teamFormat.batterCount);
            int maxBatterCount = maxBatterCombo > teamCountRequired ? 1 : (int) Math.ceil((float)teamCountRequired/(float)maxBatterCombo);

            Map<String, Integer> rounderComboAlreadyPresent = new HashMap<>();
            int maxRounderCombo = nCr(rounders.size(), teamFormat.rounderCount);
            int maxRounderCount = maxRounderCombo > teamCountRequired ? 1 : (int) Math.ceil((float)teamCountRequired/(float)maxRounderCombo);

            Map<String, Integer> bowlerComboAlreadyPresent = new HashMap<>();
            int maxBowlerCombo = nCr(bowlers.size(), teamFormat.bowlerCount);
            int maxBowlerCount = maxBowlerCombo > teamCountRequired ? 1 : (int) Math.ceil((float)teamCountRequired/(float)maxBowlerCombo);

            for (int i = 0 ; i < teamCountRequired ; i++) {
                List<String> requiredKeepers = shuffleArrayAndGetWithoutDuplicateLimits(keepers, teamFormat.keeperCount, keeperComboAlreadyPresent, maxKeeperCount);

                List<String> requiredBatters = shuffleArrayAndGetWithoutDuplicateLimits(batters, teamFormat.batterCount, batterComboAlreadyPresent, maxBatterCount);

                List<String> requiredRounders = shuffleArrayAndGetWithoutDuplicateLimits(rounders, teamFormat.rounderCount, rounderComboAlreadyPresent, maxRounderCount);

                List<String> requiredBowlers = shuffleArrayAndGetWithoutDuplicateLimits(bowlers, teamFormat.bowlerCount, bowlerComboAlreadyPresent, maxBowlerCount);

                List<String> allTeamPlayer = new ArrayList<>();
                AtomicBoolean takeThisTeam = new AtomicBoolean(true);
                allTeamPlayer.addAll(requiredKeepers);

                if(takeThisTeam.get()){
                    requiredKeepers.forEach(keeper -> {
                        if (keeperLimit.getOrDefault(keeper, 0) + 1 > keeperCanAppearInTeamMaxCount){
                            takeThisTeam.set(false);
                        }
                    });
                }

                allTeamPlayer.addAll(requiredBatters);
                if(takeThisTeam.get()){
                    requiredBatters.forEach(batter -> {
                        if (batterLimit.getOrDefault(batter, 0) + 1 > batterCanAppearInTeamMaxCount){
                            takeThisTeam.set(false);
                        }
                    });
                }

                allTeamPlayer.addAll(requiredRounders);
                if(takeThisTeam.get()){
                    requiredRounders.forEach(rounder -> {
                        if (rounderLimit.getOrDefault(rounder, 0) + 1 > rounderCanAppearInTeamMaxCount){
                            takeThisTeam.set(false);
                        }
                    });
                }

                allTeamPlayer.addAll(requiredBowlers);
                if(takeThisTeam.get()){
                    requiredBowlers.forEach(bowler -> {
                        if (bowlerLimit.getOrDefault(bowler, 0) + 1 > bowlerCanAppearInTeamMaxCount){
                            takeThisTeam.set(false);
                        }
                    });
                }

                if(!takeThisTeam.get()){
                    System.out.println("Team generated is discarded due to max count validation..., Trying to regenerate");
                    i--;
                    continue;
                }

                assignCaptainAndViceCaptain(allTeamPlayer, captaincyCount, viceCaptaincyCount, maxCaptainCount, maxViceCaptainCount);

                System.out.println("Successfully added a team in the Dream 11........");

                requiredKeepers.forEach(keeper -> {keeperLimit.put(keeper, keeperLimit.getOrDefault(keeper, 0) + 1);});
                requiredBatters.forEach(batter -> {batterLimit.put(batter, batterLimit.getOrDefault(batter, 0) + 1);});
                requiredRounders.forEach(rounder -> {rounderLimit.put(rounder, rounderLimit.getOrDefault(rounder, 0) + 1);});
                requiredBowlers.forEach(bowler -> {bowlerLimit.put(bowler, bowlerLimit.getOrDefault(bowler, 0) + 1);});

                keeperComboAlreadyPresent.put(getKey(requiredKeepers), keeperComboAlreadyPresent.getOrDefault(requiredKeepers, 0) + 1);
                batterComboAlreadyPresent.put(getKey(requiredBatters), batterComboAlreadyPresent.getOrDefault(requiredBatters, 0)+ 1);
                rounderComboAlreadyPresent.put(getKey(requiredRounders), rounderComboAlreadyPresent.getOrDefault(requiredRounders, 0) + 1);
                bowlerComboAlreadyPresent.put(getKey(requiredBowlers), bowlerComboAlreadyPresent.getOrDefault(requiredBowlers, 0) + 1);

                dreamTeams.add(allTeamPlayer);
            }
        });
        showDreamTeam(dreamTeams);
    }

    private static int nCr(int n, int r) {
        return (int) (facts[n]/(facts[r]*facts[n-r]));
    }

    private static void assignCaptainAndViceCaptain(List<String> allTeamPlayer, Map<String, Integer> captaincyCount, Map<String, Integer> viceCaptaincyCount, int maxCaptaincyCount, int maxViceCaptaincyCount) {
        Set<Integer> indicesForCaptainAndViceCaptain;
        Iterator<Integer> iterator;
        int captainIndex;
        int viceCaptainIndex;
        while(true) {
            indicesForCaptainAndViceCaptain = generateRandomIndices(2, allTeamPlayer.size());
            iterator = indicesForCaptainAndViceCaptain.iterator();
            captainIndex = iterator.next().intValue();
            viceCaptainIndex = iterator.next().intValue();
            System.out.println("Generated captain and vice captain.... " + captainIndex + " " + viceCaptainIndex);
            int currentCaptaincyCount = captaincyCount.getOrDefault(allTeamPlayer.get(captainIndex), 0);
            int currentViceCaptaincyCount = viceCaptaincyCount.getOrDefault(allTeamPlayer.get(viceCaptainIndex), 0);
            System.out.println("CurrentCaptaincyCount: " + currentCaptaincyCount + ",Max: " + maxCaptaincyCount + " \ncurrentViceCaptaincyCount : " + currentViceCaptaincyCount + ",Max: " + maxCaptaincyCount);
            if( currentCaptaincyCount < maxCaptaincyCount && currentViceCaptaincyCount < maxViceCaptaincyCount){
                break;
            }
        }
        captaincyCount.put(allTeamPlayer.get(captainIndex), captaincyCount.getOrDefault(allTeamPlayer.get(captainIndex), 0) + 1);
        allTeamPlayer.set(captainIndex,  "Cap-" + allTeamPlayer.get(captainIndex));

        viceCaptaincyCount.put(allTeamPlayer.get(viceCaptainIndex), viceCaptaincyCount.getOrDefault(allTeamPlayer.get(viceCaptainIndex), 0) + 1);
        allTeamPlayer.set(viceCaptainIndex, "VCap-" + allTeamPlayer.get(viceCaptainIndex));
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

    private static String getKey(List<String> stringList){
        List<String> copy = List.copyOf(stringList);
        copy = copy.stream().sorted().collect(Collectors.toList());

        StringBuilder sb = new StringBuilder("|");
        copy.stream().forEach(x -> sb.append(x + "|"));
        return sb.toString();
    }

    private static String getDisplayForDreamTeam(List<String> dt) {
        StringBuilder sb = new StringBuilder();
        dt.forEach(x -> sb.append(x + "  |  "));
        return sb.toString();
    }

    private static List<String> shuffleArrayAndGet(List<String> players, int playerCount) {
        List<String> outputPlayers = new ArrayList<>();
        Set<Integer> indicesToPick = generateRandomIndices(playerCount, players.size());

        for ( int index : indicesToPick){
            outputPlayers.add(players.get(index));
        }
        return outputPlayers;
    }

    private static List<String> shuffleArrayAndGetWithoutDuplicateLimits(List<String> players, int playerCount, Map<String, Integer> checkListCombo, int maxCountAllowed){
        List<String> result;
        while(true) {
            System.out.println("Generating combo...");
            result = shuffleArrayAndGet(players, playerCount);
            int currentCombo = checkListCombo.getOrDefault(getKey(result), 0);
            if ( currentCombo < maxCountAllowed){
                break;
            }
        }
        return result;
    }

    private static Set<Integer> generateRandomIndices(int count, int modSize) {
        final Random random = new Random();
        Set<Integer> indices = new HashSet<>();

        while(indices.size() < count){
            int generatedInt = (modSize + random.nextInt()%modSize)%modSize;
            indices.add(generatedInt);
        }
        return indices;
    }

    private static List<TeamFormat> getTeamFormats(JSONObject jsonObject) {
        JSONArray jsonArray = (JSONArray) jsonObject.get("formats");
        List<TeamFormat> teamFormatList = new ArrayList<>();

        jsonArray.forEach(formatObject -> {
            JSONObject jsonObjectLocal = (JSONObject) formatObject;
            int requiredCount = getInt(jsonObjectLocal, "requiredCount");
            JSONObject teamFormatObject = (JSONObject) jsonObjectLocal.get("format");
            int keeperCount = getInt(teamFormatObject, "keeper");
            int batterCount = getInt(teamFormatObject, "batter");
            int rounderCount = getInt(teamFormatObject, "rounder");
            int bowlerCount = getInt(teamFormatObject, "bowler");
            teamFormatList.add(TeamFormat.builder()
                            .keeperCount(keeperCount)
                            .batterCount(batterCount)
                            .rounderCount(rounderCount)
                            .bowlerCount(bowlerCount)
                            .requiredCount(requiredCount)
                    .build());
        });

        return teamFormatList;
    }


    private static int getInt(JSONObject jsonObject, String key){
        return ((Long)jsonObject.get(key)).intValue();
    }


    private static List<String> getPlayers(JSONObject jsonObject, String key){
        JSONArray player = (JSONArray) jsonObject.get(key);
        List<String> playerList = new ArrayList<>();
        player.forEach(x -> {playerList.add(x.toString());});
        return playerList;
     }
}

