package catAndDogStudio.geometricfootballserver;

import catAndDogStudio.geometricfootballserver.infrastructure.Constants;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.InputMessages;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientMessages {
    public static String authenticate(String key) {
        return "MAU;" + key + Constants.END_MESSAGE_MARKER;
    }

    public static String awaitGame(String userName, String preferredColor) {
        return InputMessages.AWAIT_GAME + Constants.MESSAGE_SEPARATOR + userName + Constants.MESSAGE_SEPARATOR
            + preferredColor + Constants.END_MESSAGE_MARKER;
    }
    public static String hostGame(String userName, String preferredColor) {
        return InputMessages.HOST_GAME + Constants.MESSAGE_SEPARATOR + userName + Constants.MESSAGE_SEPARATOR
                + preferredColor + Constants.END_MESSAGE_MARKER;
    }
    public static String joinGameRequest(String hostName) {
        return InputMessages.JOIN_GAME + Constants.MESSAGE_SEPARATOR + hostName + Constants.END_MESSAGE_MARKER;
    }
    public static String invitePlayer(String guestName, String selectedColor) {
        return InputMessages.INVITE_PLAYER + Constants.MESSAGE_SEPARATOR + guestName
                + Constants.MESSAGE_SEPARATOR + selectedColor + Constants.END_MESSAGE_MARKER;
    }
    public static String acceptGuestInvitation(String guestName, String guestColor) {
        return InputMessages.INVITATION_ACCEPTED_BY_HOST + Constants.MESSAGE_SEPARATOR + guestName
                + Constants.MESSAGE_SEPARATOR + guestColor + Constants.END_MESSAGE_MARKER;
    }
    public static String setPlayerMappings(String... playersIdAndUsernames) {
        return InputMessages.SET_TEAM_PLAYERS_USERS_MAPPING + Constants.MESSAGE_SEPARATOR
                + generatePlayersIdsAndUsernames(playersIdAndUsernames)
                + Constants.END_MESSAGE_MARKER;
    }
    public static String generatePlayersIdsAndUsernames(String[] playersIdAndUsernames) {
        StringBuilder playersMapping = new StringBuilder();
        for (int i = 0; i < playersIdAndUsernames.length; i += 2) {
            playersMapping.append(playersIdAndUsernames[i]);
            playersMapping.append(Constants.SUB_MESSAGE_SEPARATOR_FOR_IDS);
            playersMapping.append(playersIdAndUsernames[i + 1]);
            if (i < playersIdAndUsernames.length - 2) {
                playersMapping.append(Constants.MESSAGE_SEPARATOR);
            }
        }
        return playersMapping.toString();
    }
    public static String setTeam() {
        return InputMessages.SET_TEAM + Constants.MESSAGE_SEPARATOR
                + "{\"players\":[\"a8dab18d-572b-4a5a-91bd-6e75abda6234\",\"c78a99da-899a-40d1-bad7-5816b41e8f07\",\"8f3b10ed-2663-4946-8dbe-9553f83736ca\",\"42cda9fb-e418-404b-96e1-455e999215d4\",\"8d9e37fa-2e59-4281-a254-d9e00e1e3ee5\"],\"tacticMappings\":[],\"selectedTactic\":\"49f25a34-350a-4a5c-8a3f-081df519d4bc\",\"league\":\"8cdd75b0-45bb-46b6-b28f-b8fba09e157d\",\"name\":\"FC Drinkers\",\"uniqueId\":\"219fdf91-63bc-4d4e-bf9c-a430a07bce93\",\"icon\":\"\"}"
                + Constants.END_MESSAGE_MARKER;
    }
    public static String setTeamTactic() {
        return InputMessages.SET_TACTIC + Constants.MESSAGE_SEPARATOR
                + "{\"uniqueId\":\"49f25a34-350a-4a5c-8a3f-081df519d4bc\",\"name\":\"4-4-2\",\"order\":1,\"playerSlots\":[{\"id\":\"3d03f8b4-f284-4aff-a98c-6273e5887232\",\"positionX\":0.0,\"positionY\":0.019999999552965165,\"name\":\"GK\"},{\"id\":\"27ed3ce5-f8e8-49a1-9aba-28ed570a2510\",\"positionX\":0.30000001192092898,\"positionY\":0.20000000298023225,\"name\":\"CD\"},{\"id\":\"a6e9dd46-d166-4ff8-9163-d92f1847682d\",\"positionX\":-0.30000001192092898,\"positionY\":0.20000000298023225,\"name\":\"CD\"},{\"id\":\"0cc1851e-5420-46ce-9ca9-8b1563bf4793\",\"positionX\":0.800000011920929,\"positionY\":0.20000000298023225,\"name\":\"RD\"},{\"id\":\"57e92151-b098-445a-b8db-6c4e7b05b678\",\"positionX\":-0.800000011920929,\"positionY\":0.20000000298023225,\"name\":\"LD\"},{\"id\":\"1a5e6897-a543-4f2e-8b71-2b637cdecfd5\",\"positionX\":0.30000001192092898,\"positionY\":0.5,\"name\":\"CM\"},{\"id\":\"a32a5aec-ba61-4a0b-9015-c7f1f9a9a16e\",\"positionX\":-0.30000001192092898,\"positionY\":0.5,\"name\":\"CM\"},{\"id\":\"06913725-1ff4-4756-9a71-86ea04e13fcb\",\"positionX\":0.800000011920929,\"positionY\":0.5,\"name\":\"RM\"},{\"id\":\"ae1b94cf-4d04-4890-9889-8e484163b76c\",\"positionX\":-0.800000011920929,\"positionY\":0.5,\"name\":\"LM\"},{\"id\":\"d3e3ae58-c0bb-4d57-9e09-e61c150d7d48\",\"positionX\":-0.20000000298023225,\"positionY\":0.800000011920929,\"name\":\"CF\"},{\"id\":\"ce11e3ed-c55e-47a0-9905-aacdf5011718\",\"positionX\":0.20000000298023225,\"positionY\":0.800000011920929,\"name\":\"CF\"}]}"
                + Constants.END_MESSAGE_MARKER;
    }
    public static String setTeamPlayers() {
        return InputMessages.SET_TEAM_PLAYERS + Constants.MESSAGE_SEPARATOR
                + "{\"uniqueId\":\"a8dab18d-572b-4a5a-91bd-6e75abda6234\",\"type\":0,\"position\":7,\"team\":\"219fdf91-63bc-4d4e-bf9c-a430a07bce93\",\"firstName\":\"Anatoliy\",\"lastName\":\"Metanol\",\"nickname\":\"\",\"initX\":0.0,\"initZ\":0.0,\"age\":27,\"height\":150,\"width\":75,\"pace\":10,\"passing\":10,\"shotAccuracy\":10,\"shotStrength\":10,\"heading\":10,\"tackling\":10,\"sliding\":10,\"stamina\":10,\"blocking\":10,\"dribbling\":10,\"ballControl\":10,\"marking\":10,\"agility\":10,\"strength\":10,\"jumping\":10,\"handling\":10,\"reflex\":10};{\"uniqueId\":\"c78a99da-899a-40d1-bad7-5816b41e8f07\",\"type\":0,\"position\":7,\"team\":\"219fdf91-63bc-4d4e-bf9c-a430a07bce93\",\"firstName\":\"Alfonso\",\"lastName\":\"Prostopadłościan\",\"nickname\":\"Nieforemny\",\"initX\":0.0,\"initZ\":0.0,\"age\":27,\"height\":150,\"width\":75,\"pace\":10,\"passing\":10,\"shotAccuracy\":10,\"shotStrength\":10,\"heading\":10,\"tackling\":10,\"sliding\":10,\"stamina\":10,\"blocking\":10,\"dribbling\":10,\"ballControl\":10,\"marking\":10,\"agility\":10,\"strength\":10,\"jumping\":10,\"handling\":10,\"reflex\":10};{\"uniqueId\":\"8f3b10ed-2663-4946-8dbe-9553f83736ca\",\"type\":0,\"position\":4,\"team\":\"219fdf91-63bc-4d4e-bf9c-a430a07bce93\",\"firstName\":\"Alfonso\",\"lastName\":\"Prostopadłościan\",\"nickname\":\"Nieforemny\",\"initX\":0.0,\"initZ\":0.0,\"age\":27,\"height\":150,\"width\":75,\"pace\":10,\"passing\":10,\"shotAccuracy\":10,\"shotStrength\":10,\"heading\":10,\"tackling\":10,\"sliding\":10,\"stamina\":10,\"blocking\":10,\"dribbling\":10,\"ballControl\":10,\"marking\":10,\"agility\":10,\"strength\":10,\"jumping\":10,\"handling\":10,\"reflex\":10};{\"uniqueId\":\"42cda9fb-e418-404b-96e1-455e999215d4\",\"type\":0,\"position\":7,\"team\":\"219fdf91-63bc-4d4e-bf9c-a430a07bce93\",\"firstName\":\"Alfonso\",\"lastName\":\"Prostopadłościan\",\"nickname\":\"Nieforemny\",\"initX\":0.0,\"initZ\":0.0,\"age\":27,\"height\":150,\"width\":75,\"pace\":10,\"passing\":10,\"shotAccuracy\":10,\"shotStrength\":10,\"heading\":10,\"tackling\":10,\"sliding\":10,\"stamina\":10,\"blocking\":10,\"dribbling\":10,\"ballControl\":10,\"marking\":10,\"agility\":10,\"strength\":10,\"jumping\":10,\"handling\":10,\"reflex\":10};{\"uniqueId\":\"8d9e37fa-2e59-4281-a254-d9e00e1e3ee5\",\"type\":0,\"position\":4,\"team\":\"219fdf91-63bc-4d4e-bf9c-a430a07bce93\",\"firstName\":\"Alfonso\",\"lastName\":\"Prostopadłościan\",\"nickname\":\"Nieforemny\",\"initX\":0.0,\"initZ\":0.0,\"age\":27,\"height\":150,\"width\":75,\"pace\":10,\"passing\":10,\"shotAccuracy\":10,\"shotStrength\":10,\"heading\":10,\"tackling\":10,\"sliding\":10,\"stamina\":10,\"blocking\":10,\"dribbling\":10,\"ballControl\":10,\"marking\":10,\"agility\":10,\"strength\":10,\"jumping\":10,\"handling\":10,\"reflex\":10}"
                + Constants.END_MESSAGE_MARKER;
    }
    public static String setTacticMapping() {
        return InputMessages.SET_TACTIC_MAPPING + Constants.MESSAGE_SEPARATOR
                + "{\"tacticId\":\"49f25a34-350a-4a5c-8a3f-081df519d4bc\",\"subs\":[\"\",\"\",\"42cda9fb-e418-404b-96e1-455e999215d4\",\"\",\"\",\"\",\"\",\"\"],\"reserves\":[\"c78a99da-899a-40d1-bad7-5816b41e8f07\",\"8f3b10ed-2663-4946-8dbe-9553f83736ca\",\"\",\"\",\"\",\"\",\"\"],\"mainSquad\":[{\"playerSlotId\":\"a32a5aec-ba61-4a0b-9015-c7f1f9a9a16e\",\"playerId\":\"a8dab18d-572b-4a5a-91bd-6e75abda6234\"},{\"playerSlotId\":\"ce11e3ed-c55e-47a0-9905-aacdf5011718\",\"playerId\":\"8d9e37fa-2e59-4281-a254-d9e00e1e3ee5\"},{\"playerSlotId\":\"\",\"playerId\":\"\"},{\"playerSlotId\":\"\",\"playerId\":\"\"},{\"playerSlotId\":\"\",\"playerId\":\"\"},{\"playerSlotId\":\"\",\"playerId\":\"\"},{\"playerSlotId\":\"\",\"playerId\":\"\"},{\"playerSlotId\":\"\",\"playerId\":\"\"},{\"playerSlotId\":\"\",\"playerId\":\"\"},{\"playerSlotId\":\"\",\"playerId\":\"\"},{\"playerSlotId\":\"\",\"playerId\":\"\"}],\"leftCornerTakers\":[],\"rightCornerTakers\":[],\"freekickTakers\":[],\"penaltyTakers\":[]}"
                + Constants.END_MESSAGE_MARKER;
    }
}
