package cloud.timo.TimoCloud.core.objects;

import cloud.timo.TimoCloud.api.objects.ServerGroupObject;
import cloud.timo.TimoCloud.api.objects.properties.ServerGroupProperties;
import cloud.timo.TimoCloud.core.TimoCloudCore;
import cloud.timo.TimoCloud.core.api.ServerGroupObjectCoreImplementation;

import java.util.*;
import java.util.stream.Collectors;

public class ServerGroup implements Group {

    private String name;

    private int onlineAmount;
    private int maxAmount;
    private int ram;
    private boolean isStatic;
    private int priority;
    private String baseName;
    private Set<String> sortOutStates;

    private Map<String, Server> servers = new HashMap<>();

    public ServerGroup() {}

    public ServerGroup(Map<String, Object> properties) {
        construct(properties);
    }

    public ServerGroup(String name, int onlineAmount, int maxAmount, int ram, boolean isStatic, int priority, String baseName, Collection<String> sortOutStates) {
        construct(name, onlineAmount, maxAmount, ram, isStatic, priority, baseName, sortOutStates);
    }

    public void construct(Map<String, Object> properties) {
        try {
            String name = (String) properties.get("name");
            ServerGroupProperties defaultProperties = new ServerGroupProperties(name);
            construct(
                    name,
                    ((Number) properties.getOrDefault("online-amount", defaultProperties.getOnlineAmount())).intValue(),
                    ((Number) properties.getOrDefault("max-amount", defaultProperties.getMaxAmount())).intValue(),
                    ((Number) properties.getOrDefault("ram", defaultProperties.getRam())).intValue(),
                    (Boolean) properties.getOrDefault("static", defaultProperties.isStatic()),
                    ((Number) properties.getOrDefault("priority", defaultProperties.getPriority())).intValue(),
                    (String) properties.getOrDefault("base", defaultProperties.getBaseName()),
                    (Collection<String>) properties.getOrDefault("sort-out-states", defaultProperties.getSortOutStates()));
        } catch (Exception e) {
            TimoCloudCore.getInstance().severe("Error while loading server group '" + properties.get("name") + "':");
            e.printStackTrace();
        }
    }

    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("name", getName());
        properties.put("online-amount", getOnlineAmount());
        properties.put("max-amount", getMaxAmount());
        properties.put("ram", getRam());
        properties.put("static", isStatic());
        properties.put("priority", getPriority());
        if (getBaseName() != null) properties.put("base", getBaseName());
        properties.put("sort-out-states", getSortOutStates());
        return properties;
    }

    public void construct(String name, int startupAmount, int maxAmount, int ram, boolean isStatic, int priority, String baseName, Collection<String> sortOutStates) {
        if (isStatic() && startupAmount > 1) {
            TimoCloudCore.getInstance().severe("Static groups (" + name + ") can only have 1 server. Please set 'onlineAmount' to 1");
            startupAmount = 1;
        }
        this.name = name;
        setOnlineAmount(startupAmount);
        setMaxAmount(maxAmount);
        setRam(ram);
        setStatic(isStatic);
        setPriority(priority);
        setBaseName(baseName);
        setSortOutStates(sortOutStates);
        if (isStatic() && getBaseName() == null) {
            TimoCloudCore.getInstance().severe("Static server group " + getName() + " has no base specified. Please specify a base name in order to get a server started.");
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public GroupType getType() {
        return GroupType.SERVER;
    }

    public int getOnlineAmount() {
        return onlineAmount;
    }

    public void stopAllServers() {
        for (Server server : getServers()) {
            server.stop();
            removeServer(server);
        }
    }

    public void onServerConnect(Server server) {

    }

    public void addServer(Server server) {
        if (server == null) {
            TimoCloudCore.getInstance().severe("Fatal error: Tried to add server which is null. Please report this.");
            return;
        }
        servers.put(server.getId(), server);
        TimoCloudCore.getInstance().getInstanceManager().addServer(server);
    }

    public void removeServer(Server server) {
        servers.remove(server.getId());
        TimoCloudCore.getInstance().getInstanceManager().removeServer(server);
    }

    public Collection<Server> getServers() {
        return new HashSet<>(servers.values());
    }

    public Server getServerById(String id) {
        return servers.get(id);
    }

    public void setOnlineAmount(int onlineAmount) {
        this.onlineAmount = onlineAmount;
    }

    public void setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public int getRam() {
        if (ram < 128) {
            ram *= 1024;
        }
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public String getBaseName() {
        return baseName;
    }

    public Set<String> getSortOutStates() {
        return sortOutStates;
    }

    public void setSortOutStates(Collection<String> sortOutStates) {
        this.sortOutStates = new HashSet<>(sortOutStates);
    }

    public ServerGroupObject toGroupObject() {
        return new ServerGroupObjectCoreImplementation(
                getName(),
                getServers().stream().map(Server::toServerObject).collect(Collectors.toSet()),
                getOnlineAmount(),
                getMaxAmount(),
                getRam(),
                isStatic(),
                getBaseName(),
                getSortOutStates()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerGroup that = (ServerGroup) o;
        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }

}
