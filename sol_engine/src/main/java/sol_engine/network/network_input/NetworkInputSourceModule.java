package sol_engine.network.network_input;

import org.joml.Vector2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sol_engine.input_module.InputSourceModule;
import sol_engine.network.network_game.GameHost;
import sol_engine.network.network_sol_module.NetworkServerModule;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NetworkInputSourceModule extends InputSourceModule {
    private final Logger logger = LoggerFactory.getLogger(NetworkInputSourceModule.class);

    private final Class<? extends NetInputPacket> packetType;
    private final Map<String, Field> inputPacketFieldsByName;

    private Map<String, Boolean> triggerActions = new HashMap<>();
    private Map<String, Float> floatActions = new HashMap<>();


    public NetworkInputSourceModule(NetworkInputSourceModuleConfig config) {
        this.packetType = config.inputPacketType;
        inputPacketFieldsByName = Arrays.stream(packetType.getDeclaredFields())
                .peek(field -> field.setAccessible(true))  // in case fields are private, like kotlin classes
                .collect(Collectors.toMap(
                        Field::getName,
                        Function.identity()
                ));
    }


    @Override
    public boolean checkTrigger(String label) {
        return triggerActions.getOrDefault(label, false);
    }

    @Override
    public float floatInput(String label) {
        return floatActions.getOrDefault(label, 0f);
    }

    @Override
    public boolean hasTrigger(String label) {
        return triggerActions.containsKey(label);
    }

    @Override
    public boolean hasFloatInput(String label) {
        return floatActions.containsKey(label);
    }

    @Override
    public void onSetup() {
        usingModules(NetworkServerModule.class);
    }

    @Override
    public void onStart() {
        // setting network module to use the input packet type specified
        getModule(NetworkServerModule.class).usePacketTypes(packetType);
    }

    @Override
    public void onEnd() {

    }

    @Override
    public void onUpdate() {
        NetworkServerModule serverModule = getModule(NetworkServerModule.class);

        Map<GameHost, ? extends Deque<? extends NetInputPacket>> hostPackets
                = serverModule.getCurrentPacketsOfType(packetType);

        hostPackets.forEach((host, packets) -> {
            NetInputPacket packet = packets.peek();
            parseAndPutInputsFromPacket(packet, host.teamIndex, host.playerIndex);
        });
    }

    private void parseAndPutInputsFromPacket(final NetInputPacket packet, int teamIndex, int playerIndex) {
        final String inputGroup = "t" + teamIndex + "p" + playerIndex;
        final String inputGroupPrefix = inputGroup + ":";

        inputPacketFieldsByName.forEach((fieldName, field) -> {
            Class<?> fieldType = field.getType();
            String label = inputGroupPrefix + field.getName();
            try {
                if (fieldType == boolean.class) {
                    triggerActions.put(label, field.getBoolean(packet));
                } else if (fieldType == float.class) {
                    floatActions.put(label, field.getFloat(packet));
                } else {
                    logger.warn("Registered NetInputPacket type has a field with no effect." +
                            "Name: " + fieldName + " type: " + fieldType);
                }
            } catch (IllegalAccessException e) {
                logger.warn("Could not parse field: " + fieldName + ", because: " + e);
            }
        });
    }
}
