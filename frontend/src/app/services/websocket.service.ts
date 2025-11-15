import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs.js';
import { Observable, Subject, BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private client: Client;
  private connected$ = new BehaviorSubject<boolean>(false);
  
  // Subjects para os diferentes tópicos
  private deviceStatusUpdateSubject = new Subject<any>();
  private productionUpdateSubject = new Subject<any>();
  private deviceRegisteredSubject = new Subject<any>();
  private loginSuccessSubject = new Subject<any>();
  private loginFailedSubject = new Subject<any>();
  private operacaoSelecionadaSubject = new Subject<any>();
  private producaoSuccessSubject = new Subject<any>();

  constructor() {
    const socketUrl = window.location.hostname === 'localhost'
      ? 'http://localhost:3001/ws'
      : 'https://monitor-ellas-backend.onrender.com/ws';

    this.client = new Client({
      webSocketFactory: () => new SockJS(socketUrl),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (str) => {
        console.log('STOMP Debug:', str);
      },
      onConnect: () => {
        console.log('WebSocket conectado via STOMP');
        this.connected$.next(true);
        this.subscribeToTopics();
      },
      onDisconnect: () => {
        console.log('WebSocket desconectado');
        this.connected$.next(false);
      },
      onStompError: (frame) => {
        console.error('STOMP Error:', frame);
      }
    });

    this.client.activate();
  }

  private subscribeToTopics() {
    // Subscrever aos tópicos do servidor
    this.client.subscribe('/topic/deviceStatusUpdate', (message: IMessage) => {
      this.deviceStatusUpdateSubject.next(JSON.parse(message.body));
    });

    // Assinar produção por usuário logado
    const userStr = localStorage.getItem('user');
    let usuarioId = null;
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        usuarioId = user.id;
      } catch {}
    }
    if (usuarioId) {
      this.client.subscribe(`/topic/productionUpdate.${usuarioId}`, (message: IMessage) => {
        this.productionUpdateSubject.next(JSON.parse(message.body));
      });
    } else {
      // fallback: não assina produção se não houver usuário
      console.warn('Usuário não logado, não assinando produção');
    }

    this.client.subscribe('/topic/deviceRegistered', (message: IMessage) => {
      this.deviceRegisteredSubject.next(JSON.parse(message.body));
    });

    this.client.subscribe('/topic/loginSuccess', (message: IMessage) => {
      this.loginSuccessSubject.next(JSON.parse(message.body));
    });

    this.client.subscribe('/topic/loginFailed', (message: IMessage) => {
      this.loginFailedSubject.next(JSON.parse(message.body));
    });

    this.client.subscribe('/topic/operacaoSelecionada', (message: IMessage) => {
      this.operacaoSelecionadaSubject.next(JSON.parse(message.body));
    });

    this.client.subscribe('/topic/producaoSuccess', (message: IMessage) => {
      this.producaoSuccessSubject.next(JSON.parse(message.body));
    });
  }

  // Métodos para enviar mensagens ao servidor
  registerDevice(data: any) {
    if (this.client.connected) {
      this.client.publish({
        destination: '/app/registerDevice',
        body: JSON.stringify(data)
      });
    } else {
      console.error('WebSocket não está conectado');
    }
  }

  loginFuncionario(data: any) {
    if (this.client.connected) {
      this.client.publish({
        destination: '/app/loginFuncionario',
        body: JSON.stringify(data)
      });
    } else {
      console.error('WebSocket não está conectado');
    }
  }

  selecionarOperacao(data: any) {
    if (this.client.connected) {
      this.client.publish({
        destination: '/app/selecionarOperacao',
        body: JSON.stringify(data)
      });
    } else {
      console.error('WebSocket não está conectado');
    }
  }

  enviarProducao(data: any) {
    if (this.client.connected) {
      this.client.publish({
        destination: '/app/producao',
        body: JSON.stringify(data)
      });
    } else {
      console.error('WebSocket não está conectado');
    }
  }

  // Observables para os componentes se inscreverem
  onDeviceStatusUpdate(): Observable<any> {
    return this.deviceStatusUpdateSubject.asObservable();
  }

  onProductionUpdate(): Observable<any> {
    return this.productionUpdateSubject.asObservable();
  }

  onDeviceRegistered(): Observable<any> {
    return this.deviceRegisteredSubject.asObservable();
  }

  onLoginSuccess(): Observable<any> {
    return this.loginSuccessSubject.asObservable();
  }

  onLoginFailed(): Observable<any> {
    return this.loginFailedSubject.asObservable();
  }

  onOperacaoSelecionada(): Observable<any> {
    return this.operacaoSelecionadaSubject.asObservable();
  }

  onProducaoSuccess(): Observable<any> {
    return this.producaoSuccessSubject.asObservable();
  }

  isConnected(): Observable<boolean> {
    return this.connected$.asObservable();
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
    }
  }
}
