package catAndDogStudio.geometricfootballserver.mocks;

import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import com.cat_and_dog_studio.geometric_football.protocol.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static com.cat_and_dog_studio.geometric_football.protocol.Model.PlayerSlot.newBuilder;

@Service
@RequiredArgsConstructor
public class MockTeamFactory {

    private final MockPlayerFactory mockPlayerFactory;

    public GeometricFootballRequest.Request hexagoniaFlyers() {
        return GeometricFootballRequest.Request.newBuilder()
                .setType(GeometricFootballRequest.RequestType.SET_TEAM)
                .setTeam(Model.Team.newBuilder()
                        .setName("Hexagonia Flyers")
                        .setLeague("6")
                        .setSelectedTactic("16")
                        .setUniqueId("49")
                        .addAllPlayers(Arrays.asList("50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60",
                                "61", "62", "63", "64", "65", "66", "67", "68", "69"))
                        .build())
                .build();
    }

    public GeometricFootballRequest.Request hexagoniaFlyersTactic() {
        return GeometricFootballRequest.Request.newBuilder()
                .setType(GeometricFootballRequest.RequestType.SET_TACTIC)
                .setTactic(Model.Tactic.newBuilder()
                        .setName("4-4-2")
                        .setUniqueId("16")
                        .setOrder(1)
                        .addAllPlayerSlots(Arrays.asList(
                                newBuilder().setId("17").setName("GK").setPositionX(0f).setPositionY(0.02f).build(),
                                newBuilder().setId("18").setName("CD").setPositionX(0.3f).setPositionY(0.2f).build(),
                                newBuilder().setId("19").setName("CD").setPositionX(-0.3f).setPositionY(0.2f).build(),
                                newBuilder().setId("20").setName("RD").setPositionX(0.8f).setPositionY(0.2f).build(),
                                newBuilder().setId("21").setName("LD").setPositionX(-0.8f).setPositionY(0.2f).build(),
                                newBuilder().setId("22").setName("CM").setPositionX(0.3f).setPositionY(0.5f).build(),
                                newBuilder().setId("23").setName("CM").setPositionX(-0.3f).setPositionY(0.5f).build(),
                                newBuilder().setId("24").setName("RM").setPositionX(0.8f).setPositionY(0.5f).build(),
                                newBuilder().setId("25").setName("LM").setPositionX(-0.8f).setPositionY(0.5f).build(),
                                newBuilder().setId("26").setName("CF").setPositionX(-0.2f).setPositionY(0.8f).build(),
                                newBuilder().setId("27").setName("CF").setPositionX(0.2f).setPositionY(0.8f).build()
                                )
                        )
                        .build())
                .build();
    }

    public GeometricFootballRequest.Request hexagoniaFlyersPlayers() {
        return GeometricFootballRequest.Request.newBuilder()
                .setType(GeometricFootballRequest.RequestType.SET_TEAM_PLAYERS)
                .setPlayers(Model.Players.newBuilder()
                        .addAllPlayers(Arrays.asList(
                                mockPlayerFactory.HelmutMesserschnitt(),
                                mockPlayerFactory.WinstonSpitfire(),
                                mockPlayerFactory.ThierryConcorde(),
                                mockPlayerFactory.SashaTupolew(),
                                mockPlayerFactory.DougF16(),
                                mockPlayerFactory.AnatolyMig29(),
                                mockPlayerFactory.HansJunkers(),
                                mockPlayerFactory.JamesBoeing(),
                                mockPlayerFactory.JonAirbus(),
                                mockPlayerFactory.TomcatF14(),
                                mockPlayerFactory.DimitrijSu27(),
                                mockPlayerFactory.GeorgeAirForceOne(),
                                mockPlayerFactory.PascalMirage(),
                                mockPlayerFactory.ManfredFokker(),
                                mockPlayerFactory.EdwardB17(),
                                mockPlayerFactory.MartinEurofighter(),
                                mockPlayerFactory.ClintNighthawk(),
                                mockPlayerFactory.BrianHurricane(),
                                mockPlayerFactory.WilliamCesna()
                        ))
                        .build())
                .build();
    }

    public GeometricFootballRequest.Request hexagoniaFlyersTacticMapping() {
        return GeometricFootballRequest.Request.newBuilder()
                .setType(GeometricFootballRequest.RequestType.SET_TACTIC_MAPPING)
                .setTacticMapping(Model.TacticMapping.newBuilder()
                        .setTacticId("16")
                        .addAllSubs(Arrays.asList("64", "62", "69", "61", "65", "67", "68", "63"))
                        .addAllReserves(Arrays.asList("", "", "", "", "", "", ""))
                        .addAllMainSquad(Arrays.asList(mainSquadPosition("26", "50"),
                                mainSquadPosition("27", "51"),
                                mainSquadPosition("23", "52"),
                                mainSquadPosition("25", "53"),
                                mainSquadPosition("22", "54"),
                                mainSquadPosition("24", "55"),
                                mainSquadPosition("20", "56"),
                                mainSquadPosition("21", "57"),
                                mainSquadPosition("19", "58"),
                                mainSquadPosition("18", "59"),
                                mainSquadPosition("17", "60")))
                        .build())
                .build();
    }

    public GeometricFootballRequest.Request hexagoniaFlyersPlayerFootballerMapping(final String username) {
        return GeometricFootballRequest.Request.newBuilder()
                .setType(GeometricFootballRequest.RequestType.SET_TEAM_PLAYERS_USERS_MAPPING)
                .setPlayerFootballerMappings(Model.PlayerFootballerMappings.newBuilder()
                        .addAllPlayerOwners(Arrays.asList(
                                playerOwner(username, "60", "RED"),
                                playerOwner(username, "59", "RED"),
                                playerOwner(username, "58", "RED"),
                                playerOwner(username, "56", "RED"),
                                playerOwner(username, "57", "RED"),
                                playerOwner(username, "54", "RED"),
                                playerOwner(username, "52", "RED"),
                                playerOwner(username, "55", "RED"),
                                playerOwner(username, "53", "RED"),
                                playerOwner(username, "50", "RED"),
                                playerOwner(username, "51", "RED"),
                                playerOwner(username, "64", "RED"),
                                playerOwner(username, "62", "RED"),
                                playerOwner(username, "69", "RED"),
                                playerOwner(username, "61", "RED"),
                                playerOwner(username, "65", "RED"),
                                playerOwner(username, "67", "RED"),
                                playerOwner(username, "68", "RED"),
                                playerOwner(username, "63", "RED")
                        ))
                        .build())
                .build();
    }

    public GeometricFootballRequest.Request hexagoniaFlyersPlayerFootballerMappingSubset(final String username) {
        return GeometricFootballRequest.Request.newBuilder()
                .setType(GeometricFootballRequest.RequestType.SET_TEAM_PLAYERS_USERS_MAPPING)
                .setPlayerFootballerMappings(Model.PlayerFootballerMappings.newBuilder()
                        .addAllPlayerOwners(Arrays.asList(
                                playerOwner(username, "60", "GREEN"),
                                playerOwner(username, "59", "GREEN"),
                                playerOwner(username, "58", "GREEN")
                        ))
                        .build())
                .build();
    }

    private Model.MainSquadPosition mainSquadPosition(final String slotId, final String playerId)
    {
        return Model.MainSquadPosition.newBuilder()
                .setPlayerSlotId(slotId)
                .setPlayerId(playerId)
                .build();
    }

    private Model.PlayerOwner playerOwner(final String username, final String playerId, final String color)
    {
        return Model.PlayerOwner.newBuilder()
                .setUserName(username)
                .setPlayerId(playerId)
                .setColor(color)
                .build();
    }
}
