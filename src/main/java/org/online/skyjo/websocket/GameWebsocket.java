package org.online.skyjo.websocket;

import org.online.skyjo.object.Game;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used to broadcast the game to all players when there is a change to communicate
 * before being sent the game is encoded to json via the MessageEncoder encode method.
 * Each time a player join a game it subscribes to the websocket with id : gameId + playerName
 * The game is broadcasted to each websocket session whose id contains the gameId.
 */
@ServerEndpoint(value = "/websocket/games/{id}", encoders = MessageEncoder.class)
@ApplicationScoped
public class GameWebsocket {

	Map<String, Session> sessions = new ConcurrentHashMap<>();

	@OnOpen
	public void onOpen(Session session, @PathParam("id") String id) {
		sessions.put(id, session);
	}

	@OnClose
	public void onClose(Session session, @PathParam("id") String id) {
		sessions.remove(id);
		broadcastMessage(id);
	}

	@OnError
	public void onError(Session session, @PathParam("id") String id, Throwable throwable) {
		sessions.remove(id);
		broadcastMessage(id);
	}

	@OnMessage
	public void onMessage(String message, @PathParam("id") String id) {
		if (message.equalsIgnoreCase("_ready_")) {
			broadcastMessage(id);
		} else {
			broadcastMessage(id);
		}
	}

	public void broadcastMessage(String gameId) {
		sessions.entrySet().stream().filter(entry -> entry.getKey().contains(gameId)).forEach(entry -> {
			entry.getValue().getAsyncRemote().sendObject(gameId, result -> {
				if (result.getException() != null) {
					System.out.println("Unable to send message: " + result.getException());
				}
			});
		});
	}

	/**
	 * Send a json encoded game to all
	 * @param game
	 */
	public void broadcastGame(Game game) {
		sessions.entrySet().stream().filter(entry -> entry.getKey().contains(game.getId().toString())).forEach(entry -> {
			entry.getValue().getAsyncRemote().sendObject(game, result -> {
				if (result.getException() != null) {
					System.out.println("Unable to send message: " + result.getException());
				}
			});
		});
	}

}
