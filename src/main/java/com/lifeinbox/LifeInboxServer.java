package com.lifeinbox;

import com.lifeinbox.api.InboxController;
import com.lifeinbox.application.CandidateConfirmationService;
import com.lifeinbox.application.InboxApplicationService;
import com.lifeinbox.infrastructure.repository.InMemoryLifeInboxStore;
import com.lifeinbox.llm.HeuristicLifeItemExtractor;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.time.Clock;

public class LifeInboxServer {
    public static void main(String[] args) throws Exception {
        int port = resolvePort(args);

        InMemoryLifeInboxStore store = new InMemoryLifeInboxStore();
        HeuristicLifeItemExtractor extractor = new HeuristicLifeItemExtractor(Clock.systemDefaultZone());
        InboxApplicationService inboxService = new InboxApplicationService(store, store, extractor);
        CandidateConfirmationService confirmationService = new CandidateConfirmationService(store, store);
        InboxController controller = new InboxController(inboxService, confirmationService);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/inbox", controller::handle);
        server.setExecutor(null);
        server.start();

        System.out.println("Life Inbox listening on http://localhost:" + port);
    }

    private static int resolvePort(String[] args) {
        if (args.length > 0) {
            return Integer.parseInt(args[0]);
        }
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            return Integer.parseInt(envPort.trim());
        }
        return 8080;
    }
}
