package sol_engine.network.network_sol_module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sol_engine.module.Module;
import sol_engine.network.network_game.GameHost;
import sol_engine.network.network_game.PacketsQueueByHost;
import sol_engine.network.network_game.PacketsQueueByType;
import sol_engine.network.network_game.game_server.NetworkGameServer;
import sol_engine.network.packet_handling.NetworkPacket;
import sol_engine.network.network_game.game_server.ServerConnectionData;

import java.util.*;

public class NetworkServerModule extends Module {
    private final Logger logger = LoggerFactory.getLogger(NetworkServerModule.class);

    private NetworkServerModuleConfig config;
    private ServerConnectionData connectionData = null;

    private NetworkGameServer server;

    public NetworkServerModule(NetworkServerModuleConfig config) {
        this.config = config;
    }


    @SafeVarargs
    public final void usePacketTypes(Class<? extends NetworkPacket>... packetTypes) {
        usePacketTypes(Arrays.asList(packetTypes));
    }

    public final void usePacketTypes(List<Class<? extends NetworkPacket>> packetTypes) {
        if (server != null) {
            server.usePacketTypes(packetTypes);
        } else {
            logger.warn("calling usePacketTypes() before game server is setup");
        }
    }

    public NetworkGameServer getGameServer() {
        return server;
    }

    public PacketsQueueByType peekPacketsForHost(GameHost host) {
        return server.peekPacketsForHost(host);
    }

    public <T extends NetworkPacket> PacketsQueueByHost<T> peekPacketsOfType(Class<T> type) {
        return server.peekPacketsOfType(type);
    }

    public void sendPacketAll(NetworkPacket packet) {
        server.sendPacketAll(packet);
    }

    public boolean isRunning() {
        return server.isRunning();
    }

    public ServerConnectionData getConnectionData() {
        if (connectionData == null) {
            logger.error("Cannot get connection data before internalSetup() is called");
        }
        return connectionData;
    }

    @Override
    public void onSetup() {
        server = new NetworkGameServer();
        connectionData = server.setup(config.gameServerConfig);
        server.usePacketTypes(config.packetTypes);
    }

    @Override
    public void onStart() {
        if (server != null) {
            server.start();
            if (config.waitForAllPlayerConnections) {
                logger.info("Server started, waiting for all connections");
                server.waitForAllPlayerConnections();
            }
        } else {
            logger.error("server not setup when onStart() is called");
        }
    }

    @Override
    public void onEnd() {
        server.stop();
    }

    @Override
    public void onUpdate() {
        server.clearAllPackets();
    }
}
