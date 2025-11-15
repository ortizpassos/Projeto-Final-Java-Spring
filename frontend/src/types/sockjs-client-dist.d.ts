// Declaração de módulo para sockjs-client/dist/sockjs.js
declare module 'sockjs-client/dist/sockjs.js' {
  interface SockJS {
    onopen: ((e?: any) => void) | null;
    onclose: ((e?: any) => void) | null;
    onmessage: ((e: { data: any }) => void) | null;
    send(data: string): void;
    close(): void;
  }
  const SockJS: {
    new (url: string, _reserved?: any, options?: any): SockJS;
  };
  export = SockJS;
}