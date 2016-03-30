/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander;

import java.util.concurrent.Semaphore;

import com.foilen.smalltools.net.commander.command.CommandImplementation;
import com.foilen.smalltools.net.commander.command.CommandImplementationConnectionAware;
import com.foilen.smalltools.net.commander.command.CommandRequest;
import com.foilen.smalltools.net.commander.connectionpool.CommanderConnection;

public class GrabRemoteConnectionCommand implements CommandRequest, CommandImplementation, CommandImplementationConnectionAware {

    private static CommanderConnection commanderConnection;
    private static Semaphore semaphore = new Semaphore(0);

    public static void reset() {
        semaphore = new Semaphore(0);
        commanderConnection = null;
    }


    @Override
    public String commandImplementationClass() {
        return GrabRemoteConnectionCommand.class.getName();
    }

    public static CommanderConnection getCommanderConnection() {
        return commanderConnection;
    }

    @Override
    public void run() {
        semaphore.release();
    }

    @Override
    public void setCommanderConnection(CommanderConnection commConn) {
        commanderConnection = commConn;
    }

    public static void waitForRun() throws InterruptedException {
        semaphore.acquire();
    }

}