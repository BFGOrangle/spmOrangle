"use client";

import React, { useState, useRef } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { useCurrentUser } from '@/contexts/user-context';
import { getBearerToken } from '@/lib/auth-utils';

export function WebSocketTester() {
  const { currentUser } = useCurrentUser();
  const [isConnected, setIsConnected] = useState(false);
  const [connectionLogs, setConnectionLogs] = useState<string[]>([]);
  const [testUserId, setTestUserId] = useState(currentUser?.backendStaffId?.toString() || '1');
  const clientRef = useRef<Client | null>(null);

  const addLog = (message: string) => {
    const timestamp = new Date().toLocaleTimeString();
    setConnectionLogs(prev => [...prev, `[${timestamp}] ${message}`]);
    console.log(message);
  };

  const connectWebSocket = async () => {
    if (clientRef.current?.connected) {
      addLog('Already connected!');
      return;
    }

    const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';
    const endpoint = '/ws/notifications';
    const fullUrl = `${baseUrl}${endpoint}`;

    addLog(`🔌 Attempting to connect to: ${fullUrl}`);
    addLog(`👤 Current user: ${JSON.stringify({ 
      id: currentUser?.id, 
      backendStaffId: currentUser?.backendStaffId,
      role: currentUser?.role 
    })}`);

    // Get authentication token
    let authToken = '';
    try {
      authToken = await getBearerToken();
      addLog(`🔑 Got auth token: ${authToken ? 'Yes' : 'No'}`);
    } catch (error) {
      addLog(`🚨 Failed to get auth token: ${error}`);
    }

    // Add token as query parameter (alternative approach)
    const tokenParam = authToken ? `?token=${encodeURIComponent(authToken.replace('Bearer ', ''))}` : '';
    const fullUrlWithToken = `${fullUrl}${tokenParam}`;
    
    addLog(`🔗 Final URL: ${fullUrlWithToken.replace(/token=[^&]+/, 'token=***')}`);

    try {
      const socket = new SockJS(fullUrlWithToken);
      
      socket.onopen = () => addLog('✅ SockJS connection opened');
      socket.onclose = (event) => addLog(`❌ SockJS connection closed: ${event.code} - ${event.reason}`);
      socket.onerror = (error) => addLog(`🚨 SockJS error: ${JSON.stringify(error)}`);

      const client = new Client({
        webSocketFactory: () => socket,
        debug: (str) => addLog(`STOMP Debug: ${str}`),
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      client.onConnect = (frame) => {
        addLog('✅ STOMP connected successfully!');
        addLog(`📋 Connection frame: ${JSON.stringify(frame.headers)}`);
        setIsConnected(true);

        // Subscribe to user notifications
        const topic = `/topic/notifications/${testUserId}`;
        addLog(`📡 Subscribing to: ${topic}`);
        
        client.subscribe(topic, (message) => {
          addLog(`📩 Received message: ${message.body}`);
        });

        addLog('✅ Subscription complete');
      };

      client.onDisconnect = () => {
        addLog('❌ STOMP disconnected');
        setIsConnected(false);
      };

      client.onStompError = (frame) => {
        addLog(`🚨 STOMP error: ${frame.headers.message}`);
        addLog(`🚨 Error details: ${frame.body}`);
        setIsConnected(false);
      };

      client.onWebSocketError = (error) => {
        addLog(`🚨 WebSocket error: ${JSON.stringify(error)}`);
      };

      client.onWebSocketClose = (event) => {
        addLog(`🔌 WebSocket closed: Code ${event.code}, Reason: ${event.reason}`);
        setIsConnected(false);
      };

      clientRef.current = client;
      client.activate();
      
      addLog('🚀 Client activation initiated...');
    } catch (error) {
      addLog(`💥 Connection failed: ${error}`);
    }
  };

  const disconnect = () => {
    if (clientRef.current) {
      addLog('🔌 Disconnecting...');
      clientRef.current.deactivate();
      clientRef.current = null;
    }
    setIsConnected(false);
  };

  const clearLogs = () => {
    setConnectionLogs([]);
  };

  const sendTestMessage = () => {
    if (!clientRef.current?.connected) {
      addLog('❌ Not connected - cannot send message');
      return;
    }

    // Note: This would only work if your backend has an endpoint to receive messages
    // This is just for testing purposes
    addLog('📤 Test message functionality not implemented in backend');
  };

  return (
    <Card className="w-full max-w-4xl">
      <CardHeader>
        <CardTitle className="flex items-center justify-between">
          WebSocket Connection Tester
          <Badge variant={isConnected ? "default" : "destructive"}>
            {isConnected ? "Connected" : "Disconnected"}
          </Badge>
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="flex items-center space-x-2">
          <Label htmlFor="userId">Test User ID:</Label>
          <Input
            id="userId"
            value={testUserId}
            onChange={(e) => setTestUserId(e.target.value)}
            className="w-24"
          />
        </div>

        <div className="flex space-x-2">
          <Button onClick={connectWebSocket} disabled={isConnected}>
            Connect
          </Button>
          <Button onClick={disconnect} disabled={!isConnected} variant="destructive">
            Disconnect
          </Button>
          <Button onClick={sendTestMessage} disabled={!isConnected} variant="outline">
            Send Test Message
          </Button>
          <Button onClick={clearLogs} variant="outline">
            Clear Logs
          </Button>
        </div>

        <div className="border rounded-lg p-4 h-96 overflow-y-auto bg-slate-50 dark:bg-slate-900">
          <div className="text-sm font-mono">
            {connectionLogs.length === 0 ? (
              <p className="text-muted-foreground">No logs yet. Click Connect to start testing.</p>
            ) : (
              connectionLogs.map((log, index) => (
                <div key={index} className="mb-1">
                  {log}
                </div>
              ))
            )}
          </div>
        </div>

        <div className="text-sm text-muted-foreground">
          <p><strong>Backend should be running on:</strong> {process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080'}</p>
          <p><strong>WebSocket endpoint:</strong> /ws/notifications</p>
          <p><strong>Expected topic:</strong> /topic/notifications/{testUserId}</p>
        </div>
      </CardContent>
    </Card>
  );
}