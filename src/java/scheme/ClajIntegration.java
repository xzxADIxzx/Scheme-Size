package scheme;

import arc.func.Cons;
import arc.net.Client;
import arc.net.Connection;
import arc.net.DcReason;
import arc.net.NetListener;
import arc.struct.Seq;
import arc.util.Reflect;
import arc.util.Threads;
import mindustry.net.ArcNetProvider.*;

import static mindustry.Vars.*;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.github.bsideup.jabel.Desugar;

public class ClajIntegration {

    public static Seq<Client> clients = new Seq<>();
    public static NetListener serverListener;

    public static void load() {
        var provider = Reflect.get(net, "provider");
        var server = Reflect.get(provider, "server");
        serverListener = Reflect.get(server, "dispatchListener");
    }

    // region room management

    public static Client createRoom(String ip, int port, Cons<String> link, Runnable disconnected) throws IOException {
        Client client = new Client(8192, 8192, new Serializer());
        Threads.daemon("CLaJ Client", client::run);

        client.addListener(serverListener);
        client.addListener(new NetListener() {
            @Override
            public void connected(Connection connection) {
                client.sendTCP("new");
            }

            @Override
            public void disconnected(Connection connection, DcReason reason) {
                disconnected.run();
            }

            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof String key) link.get(key + "#" + ip + ":" + port);
            }
        });

        client.connect(5000, ip, port, port);
        clients.add(client);

        return client;
    }

    public static void joinRoom(String ip, int port, String key, Runnable success) {
        logic.reset();
        net.reset();

        netClient.beginConnecting();
        net.connect(ip, port, () -> {
            if (!net.client()) return;
            success.run();

            ByteBuffer buffer = ByteBuffer.allocate(8192);
            buffer.put(Serializer.linkID);
            Serializer.writeString(buffer, key);

            buffer.limit(buffer.position()).position(0);
            net.send(buffer, true);
        });
    }

    public static void clear() {
        clients.each(Client::close);
        clients.clear();
    }

    // endregion

    public static Link parseLink(String link) throws IOException {
        link = link.trim();
        if (!link.startsWith("CLaJ")) throw new IOException("@join.missing-prefix");

        int hash = link.indexOf('#');
        if (hash != 42 + 4) throw new IOException("@join.wrong-key-length");

        int semicolon = link.indexOf(':');
        if (semicolon == -1) throw new IOException("@join.semicolon-not-found");

        int port;
        try {
            port = Integer.parseInt(link.substring(semicolon + 1));
        } catch (Throwable ignored) {
            throw new IOException("@join.failed-to-parse");
        }

        return new Link(link.substring(0, hash), link.substring(hash + 1, semicolon), port);
    }

    @Desugar
    public record Link(String key, String ip, int port) {}

    public static class Serializer extends PacketSerializer {

        public static final byte linkID = -3;

        @Override
        public void write(ByteBuffer buffer, Object object) {
            if (object instanceof String link) {
                buffer.put(linkID);
                writeString(buffer, link);
            } else
                super.write(buffer, object);
        }

        @Override
        public Object read(ByteBuffer buffer) {
            if (buffer.get() == linkID) return readString(buffer);

            buffer.position(buffer.position() - 1);
            return super.read(buffer);
        }

        public static void writeString(ByteBuffer buffer, String message) {
            buffer.putInt(message.length());
            for (char chara : message.toCharArray())
                buffer.putChar(chara);
        }

        public static String readString(ByteBuffer buffer) {
            int length = buffer.getInt();

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; i++)
                builder.append(buffer.getChar());

            return builder.toString();
        }
    }
}
