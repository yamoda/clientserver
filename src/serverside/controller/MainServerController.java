package serverside.controller;

import serverside.model.datamodel.Student;
import serverside.model.datamodel.StudentPageModel;
import serverside.model.logger.ServerLogger;
import serverside.model.parsers.DOMWriter;
import serverside.model.parsers.SAXReader;
import serverside.view.ServerSideView;

import javax.swing.*;
import java.io.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class MainServerController {
    private String arrayFolder = "arrays";
    private boolean isRunning = true;
    private final int port = 9090;

    Thread serverThread = null;
    Socket clientSocket = null;

    ServerLogger serverLogger;
    ServerSocket serverSocket = null;

    private final StudentPageModel serverModel;
    private final ServerSideView serverView;

    private int currentPage = 1;

    public MainServerController(StudentPageModel model, ServerSideView view) {
        this.serverModel = model;
        this.serverView = view;

        serverView.getLoggingArea().getLogArea().setEditable(false);

        int windowWidth = 600;
        int windowHeight = 600;

        serverView.getWindow().setTitle("Server");
        serverView.getWindow().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        serverView.getWindow().setSize(windowWidth, windowHeight);
        serverView.getWindow().setVisible(true);

        serverLogger = new ServerLogger(view.getLoggingArea().getLogArea());

        serverView.getControlPanel().getStartButton().addActionListener(e -> runServer());
        serverView.getControlPanel().getStopButton().addActionListener(e -> stopServer());
    }

    private void runServer() {
        if (serverThread == null) {
            serverThread = new Thread() {
                @Override
                public void run() {
                    try {
                        serverLogger.addLog("Started the server");

                        isRunning = true;
                        serverSocket = new ServerSocket(9090);

                        serverLogger.addLog("Waiting for client connection");
                        clientSocket = serverSocket.accept();

                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());

                        serverLogger.addLog("Connected to client");
                        String argSeparator = "\\|";
                        while(isRunning) {
                            String command = in.readLine();
                            if (command == null) continue;

                            if (command.contains("load")) {
                                String[] args = command.split(argSeparator);
                                serverLogger.addLog("Requested to load array " + args[1]);
                                load(args[1]);

                                out.writeObject(serverModel.getPage(1));
                                serverLogger.addLog("Sent array to the client");
                            }
                            else if (command.contains("save")) {
                                String[] args = command.split(argSeparator);
                                serverLogger.addLog("Requested to save array " + args[1]);
                                save(args[1]);
                                serverLogger.addLog("Saved array on the server");
                            }
                            else if (command.contains("delete")) {
                                String[] args = command.split(argSeparator);
                                serverLogger.addLog("Requested to delete array " + args[1]);
                                delete(args[1]);
                                serverLogger.addLog("Deleted array");
                            }
                            else if (command.equals("nextPage")) {
                                serverLogger.addLog("Requested next page");
                                var nextPage = serverModel.getPage(currentPage+1);
                                out.writeObject(nextPage);

                                if (nextPage != null) currentPage += 1;
                                serverLogger.addLog("Returned page " + currentPage);
                            }
                            else if (command.equals("prevPage")) {
                                serverLogger.addLog("Requested previous page");
                                var prevPage = serverModel.getPage(currentPage-1);
                                out.writeObject(prevPage);

                                if (prevPage != null) currentPage--;
                                serverLogger.addLog("Returned page " + currentPage);
                            }
                            else if (command.equals("firstPage")) {
                                serverLogger.addLog("Requested the first page");

                                currentPage = 1;
                                out.writeObject(serverModel.getPage(1));

                                serverLogger.addLog("Returned page " + currentPage);
                            }
                            else if (command.equals("lastPage")) {
                                serverLogger.addLog("Requested the last page");

                                currentPage = serverModel.size() / serverModel.getRowsOnPage() + 1;
                                out.writeObject(serverModel.getPage(currentPage));

                                serverLogger.addLog("Returned page " + currentPage);
                            }
                            else if (command.contains("addRow")) {
                                serverLogger.addLog("Requested to add row");
                                String[] args = command.split(argSeparator);

                                String fullName = args[1].trim();
                                int year = Integer.parseInt(args[2].trim());
                                int groupId = Integer.parseInt(args[3].trim());
                                int assignmentsAmount = Integer.parseInt(args[4].trim());
                                int passedAmount = Integer.parseInt(args[5].trim());
                                String progLang = args[6].trim();

                                add(fullName, year, groupId, assignmentsAmount, passedAmount, progLang);
                                serverLogger.addLog("Added row");

                                out.writeObject(serverModel.getPage(currentPage));
                                serverLogger.addLog("Returned page " + currentPage);
                            }
                            else if (command.contains(("removeRow"))) {
                                serverLogger.addLog("Requested to remove row");

                                ServerSocket removeSocket = new ServerSocket(8000);
                                Socket removeRequest = removeSocket.accept();

                                PrintWriter removeOut = new PrintWriter(removeRequest.getOutputStream());
                                serverLogger.addLog("Instantiated a new socket");

                                String[] args = command.split(argSeparator);
                                removeOut.println(remove(args[1].trim(), args[2].trim(), args[3].trim(), args[4].trim()));
                                serverLogger.addLog("Removed row");

                                out.writeObject(serverModel.getPage(currentPage));
                                serverLogger.addLog("Sent result to the client");
                                removeOut.close();
                            }
                            else if (command.contains("searchRow")) {
                                serverLogger.addLog("Requested to search");

                                ServerSocket foundSocket = new ServerSocket(9000);
                                Socket foundRequest = foundSocket.accept();

                                ObjectOutputStream foundOut = new ObjectOutputStream(foundRequest.getOutputStream());
                                serverLogger.addLog("Instantiated a new socket");

                                String[] args = command.split(argSeparator);
                                var foundArr = find(args[1].trim(), args[2].trim(), args[3].trim(), args[4].trim());
                                serverLogger.addLog("Returning result");
                                foundOut.writeObject(foundArr.stream().
                                        map(Student::toObject).
                                        collect(Collectors.toList())
                                );

                                foundOut.close();
                                foundRequest.close();
                                foundSocket.close();
                            }
                        }

                        in.close();
                        out.close();

                        clientSocket.close();
                        serverSocket.close();
                    }
                    catch (SocketException e) { serverLogger.addLog("Stopped the server"); }
                    catch (IOException e) { e.printStackTrace(); }
                }
            };
            serverThread.start();
        }
    }

    private void stopServer() {
        if (serverSocket != null) {
            try {
                isRunning = false;
                serverSocket.close();
                serverThread = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void save(String arrayName) {
        DOMWriter xmlModelWriter = new DOMWriter("Entries", "Entry");
        ArrayList<Student> entriesArray = serverModel.getTableArray();

        xmlModelWriter.parseAndWrite(entriesArray, arrayFolder + File.separator + arrayName);
    }

    private void load(String arrayName) {
        SAXReader xmlDocumentReader = new SAXReader();
        ArrayList<Student> documentEntries = xmlDocumentReader.readAndParse(arrayFolder + File.separator + arrayName);

        serverModel.setTableArray(documentEntries);
    }

    private void delete(String arrayName) {
        File toDelete = new File(arrayFolder + File.separator + arrayName);
        toDelete.delete();
    }

    private void add(String fullName, int year, int groupId, int assignmentAmount, int passedAmount, String progLang) {
        serverModel.getTableArray().add(new Student(fullName, year, groupId, assignmentAmount, passedAmount, progLang));
    }

    private int remove(String fullName, String progLang, String assignmentsAmount, String assignmentsLeft) {
        ArrayList<Student> toRemove = find(fullName, progLang, assignmentsAmount, assignmentsLeft);
        serverModel.getTableArray().removeAll(toRemove);

        return toRemove.size();
    }

    private ArrayList<Student> find(String fullName, String progLang, String assignmentsAmount, String assignmentsLeft) {
        String nameRegex = "(.*)\\s?(" + fullName + ")\\s(.*)";
        ArrayList<Student> matchesArray = (ArrayList<Student>) serverModel.getTableArray().stream()
                .filter(entry -> entry.getFullName().matches(nameRegex) || fullName.equals(Student.ignoreValue))
                .filter(entry -> entry.getProgrammingLanguage().equals(progLang) || progLang.equals(Student.ignoreValue))
                .filter(entry -> String.valueOf(entry.getAssignmentsAmount()).equals(assignmentsAmount) ||
                                 assignmentsAmount.equals(Student.ignoreValue))
                .filter(entry -> String.valueOf(entry.getAssignmentsAmount() - entry.getPassedAmount()).equals(assignmentsLeft) ||
                                 assignmentsLeft.equals(Student.ignoreValue))
                .collect(Collectors.toList());

        return matchesArray;
    }
}
