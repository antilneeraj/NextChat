package com.anonchat.backend.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class IpHandshakeInterceptor implements HandshakeInterceptor {
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // Extract IP address from the underlying Servlet request
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String ipAddress = servletRequest.getServletRequest().getRemoteAddr();

            // Save it into WebSocket session attributes
            attributes.put("IP_ADDRESS", ipAddress);
        }

        return true; // Proceed with the handshake
    }

    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // Nothing to do here as of now
    }
}