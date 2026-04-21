package org.unibl.etf.mdp.supplier.gui;

import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.supplier.mq.DirectReceiver;
import org.unibl.etf.mdp.supplier.server.Server;
import org.unibl.etf.mdp.supplier.services.LibraryService;
import org.unibl.etf.mdp.supplier.services.SupplierServerService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Data {
    private static final SupplierServerService serverService = new SupplierServerService();
    private static final LibraryService libraryService = new LibraryService();
    private static Data instance = null;
    private List<BookDto> books;
    private String username;
    private Server serverInstance;
    private Thread serverThread;
    private List<List<BookDto>> requests = new CopyOnWriteArrayList<>();

    private Data(List<BookDto> books, String username) {
        this.books = books;
        this.username = username;
        initServer();
        initMQ();
    }

    public static synchronized Data getInstance(List<BookDto> books, String username) {
        if (instance == null) {
            instance = new Data(books, username);
        }
        return instance;
    }

    public static SupplierServerService getServerservice() {
        return serverService;
    }

    public static LibraryService getLibraryservice() {
        return libraryService;
    }

    private void initMQ() {
        DirectReceiver receiver;

        try {
            receiver = DirectReceiver.getInstance();
            Thread receiverThread = new Thread(() -> {
                try {
                    receiver.startListening(username, msg -> {
                        System.out.println("Received message: " + msg);

                        List<BookDto> bookDtos = (List<BookDto>) msg.getBody();

                        addRequest(bookDtos);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            receiverThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initServer() {
        serverInstance = Server.getInstance(username, books);
        serverThread = new Thread(serverInstance);
        serverThread.start();
    }

    public void shutdownServer() {
        if (serverInstance != null) {
            serverInstance.shutdown();
            try {
                serverThread.join();
                System.out.println("Server thread terminated.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public List<BookDto> getBooks() {
        return books;
    }

    public void setBooks(List<BookDto> books) {
        shutdownServer();
        this.books = books;
        initServer();

    }

    public String getUsername() {
        return username;
    }

    public List<List<BookDto>> getRequests() {
        return requests;
    }

    public void addRequest(List<BookDto> bookDtos) {
        requests.add(bookDtos);
    }

    public void removeRequest(List<BookDto> bookDtos) {
        requests.remove(bookDtos);
    }

}
