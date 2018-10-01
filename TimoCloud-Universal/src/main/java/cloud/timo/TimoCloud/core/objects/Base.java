package cloud.timo.TimoCloud.core.objects;

import cloud.timo.TimoCloud.api.objects.BaseObject;
import cloud.timo.TimoCloud.core.TimoCloudCore;
import cloud.timo.TimoCloud.core.api.BaseObjectCoreImplementation;
import cloud.timo.TimoCloud.core.sockets.Communicatable;
import cloud.timo.TimoCloud.lib.messages.Message;
import cloud.timo.TimoCloud.lib.messages.MessageType;
import io.netty.channel.Channel;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Base implements Communicatable {

    private String name;
    private InetAddress address;
    private InetAddress publicAddress; // Used for connecting to public proxies
    private Channel channel;
    private int availableRam;
    private int maxRam;
    private double cpuLoad;
    private boolean connected;
    private boolean ready;
    private Set<Server> servers;
    private Set<Proxy> proxies;

    public Base(String name, InetAddress address, InetAddress publicAddress, Channel channel) {
        this.name = name;
        this.address = address;
        this.publicAddress = publicAddress;
        this.channel = channel;
        setReady(false);
        servers = new HashSet<>();
        proxies = new HashSet<>();
    }

    @Override
    public void onConnect(Channel channel) {
        setChannel(channel);
        setConnected(true);
        setReady(true);
        TimoCloudCore.getInstance().getCloudFlareManager().onBaseRegisterEvent(this);
        TimoCloudCore.getInstance().info("Base " + getName() + " connected.");
    }

    @Override
    public void onDisconnect() {
        setChannel(null);
        setConnected(false);
        setReady(false);
        setCpuLoad(0);
        setAvailableRam(0);
        TimoCloudCore.getInstance().getCloudFlareManager().onBaseUnregisterEvent(this);
        TimoCloudCore.getInstance().info("Base " + getName() + " disconnected.");
    }

    @Override
    public void onMessage(Message message, Communicatable sender) {
        MessageType type = message.getType();
        Object data = message.getData();
        switch (type) {
            case BASE_RESOURCES:
                Map map = (Map) data;
                setReady((boolean) map.get("ready"));
                int maxRam = ((Number) map.get("maxRam")).intValue();
                int usedRam = servers.stream().mapToInt((server) -> server.getGroup().getRam()).sum() + proxies.stream().mapToInt((proxy) -> proxy.getGroup().getRam()).sum();
                int availableRam = ((Number) map.get("availableRam")).intValue();
                setAvailableRam(Math.max(0, Math.min(availableRam, maxRam-usedRam)));
                setCpuLoad(((Double) map.get("cpuLoad")));
                setMaxRam(maxRam);
                break;
            default:
                sendMessage(message);
        }
    }

    @Override
    public void sendMessage(Message message) {
        if (getChannel() != null) getChannel().writeAndFlush(message.toJson());
    }

    @Override
    public void onHandshakeSuccess() {
        sendMessage(Message.create().setType(MessageType.BASE_HANDSHAKE_SUCCESS));
    }
    public String getName() {
        return name;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public InetAddress getPublicAddress() {
        return publicAddress;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public Channel getChannel() {
        return this.channel;
    }

    public int getAvailableRam() {
        return availableRam;
    }

    public void setAvailableRam(int availableRam) {
        this.availableRam = availableRam;
    }

    public int getMaxRam() {
        return maxRam;
    }

    public void setMaxRam(int maxRam) {
        this.maxRam = maxRam;
    }

    public double getCpuLoad() {
        return cpuLoad;
    }

    public void setCpuLoad(double cpuLoad) {
        this.cpuLoad = cpuLoad;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public Set<Server> getServers() {
        return servers;
    }

    public Set<Proxy> getProxies() {
        return proxies;
    }

    public void addServer(Server server) {
        if (getServers().contains(server)) return;
        getServers().add(server);
    }

    public void removeServer(Server server) {
        if (! getServers().contains(server)) return;
        getServers().remove(server);
    }

    public void addProxy(Proxy proxy) {
        if (getProxies().contains(proxy)) return;
        getProxies().add(proxy);
    }

    public void removeProxy(Proxy proxy) {
        if (! getProxies().contains(proxy)) return;
        getProxies().remove(proxy);
    }

    public BaseObject toBaseObject() {
        return new BaseObjectCoreImplementation(
                getName(),
                getPublicAddress(),
                getCpuLoad(),
                getAvailableRam(),
                getMaxRam(),
                isConnected(),
                isReady(),
                getServers().stream().map(Server::getId).collect(Collectors.toSet()),
                getProxies().stream().map(Proxy::getId).collect(Collectors.toSet())
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Base base = (Base) o;

        return name != null ? name.equals(base.name) : base.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
