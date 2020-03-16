package sol_engine.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sol_engine.module.Module;
import sol_engine.network.communication_layer.NetworkClient;
import sol_engine.network.communication_layer_impls.websockets.NetworkWebsocketsClient;
import sol_engine.network.packet_handling.NetworkPacket;
import sol_engine.network.communication_layer.NetworkCommunicationLayer;
import sol_engine.network.communication_layer.NetworkServer;
import sol_engine.network.communication_layer_impls.websockets.NetworkWebsocketsServer;
import sol_engine.network.server.ServerConnectionData;

import java.util.*;

public class NetworkModule extends Module {
    private final Logger logger = LoggerFactory.getLogger(NetworkModule.class);

    private NetworkModuleConfig config;
    private ServerConnectionData connectionData = null;

    private NetworkServer server = null;
    private NetworkClient client = null;
    private NetworkCommunicationLayer rawPacketLayer;
    private NetworkClassPacketLayer classPacketLayer;

    public NetworkModule(NetworkModuleConfig config) {
        this.config = config;
    }


    @SafeVarargs
    public final void usePacketTypes(Class<? extends NetworkPacket>... packetTypes) {
        usePacketTypes(Arrays.asList(packetTypes));
    }

    public final void usePacketTypes(List<Class<? extends NetworkPacket>> packetTypes) {
        classPacketLayer.usePacketTypes(packetTypes);
    }


    public <T extends NetworkPacket> List<T> peekPackets(Class<T> packetType) {
        return new ArrayList<>(classPacketLayer.peekPackets(packetType));
    }

    public void pushPacket(NetworkPacket packet) {
        classPacketLayer.pushPacket(packet);
    }

    public boolean isConnected() {
        return rawPacketLayer.isConnected();
    }

    public ServerConnectionData getConnectionData() {
        if (connectionData == null) {
            logger.error("Cannot get connection data before internalSetup() is called");
        }
        return connectionData;
    }

    @Override
    public void onSetup() {
        if (config.isServer) {
            if (config.serverConfig == null) {
                String msg = "ServerConfig must be specified in NetworkModuleConfig if isServer is true";
                logger.error(msg);
                throw new IllegalArgumentException(msg);
            }
            server = new NetworkWebsocketsServer();
            rawPacketLayer = server;
            classPacketLayer = new NetworkClassPacketLayer(server);
            this.connectionData = server.start(config.serverConfig);

            // wait for connections
            server.waitForConnections();

        } else {
            if (config.clientConfig == null) {
                String msg = "ClientConfig must be specified in NetworkModuleConfig if isServer is false";
                logger.error(msg);
                throw new IllegalArgumentException(msg);
            }
            client = new NetworkWebsocketsClient();
            rawPacketLayer = client;
            classPacketLayer = new NetworkClassPacketLayer(client);

            client.connect(config.clientConfig);
        }

        classPacketLayer.usePacketTypes(config.packetTypes);
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onEnd() {
        rawPacketLayer.terminate();
    }

    @Override
    public void onUpdate() {
        classPacketLayer.clearAllPackets();
        classPacketLayer.pollAndParseRawPackets();
    }
}
